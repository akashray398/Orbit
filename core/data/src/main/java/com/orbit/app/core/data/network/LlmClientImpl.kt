package com.orbit.app.core.data.network

import android.os.Build
import com.orbit.app.core.common.network.NetworkMonitor
import com.orbit.app.core.domain.model.AppError
import com.orbit.app.core.domain.model.ContentPart
import com.orbit.app.core.domain.model.LlmEvent
import com.orbit.app.core.domain.model.LlmRequest
import com.orbit.app.core.domain.repository.LlmClient
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LlmClientImpl @Inject constructor(
    private val httpClient: OkHttpClient,
    private val authTokenProvider: AuthTokenProvider,
    private val networkMonitor: NetworkMonitor
) : LlmClient {

    private val json = Json { ignoreUnknownKeys = true }

    fun resolveBaseUrl(): String {
        val isEmulator = Build.FINGERPRINT.startsWith("generic") ||
                Build.MODEL.contains("google_sdk") ||
                Build.HARDWARE.contains("goldfish") ||
                Build.HARDWARE.contains("ranchu")
        return if (isEmulator) "http://10.0.2.2:5000" else "http://192.168.1.100:5000"
    }

    private fun checkHealthFast(baseUrl: String): Boolean {
        val healthClient = httpClient.newBuilder()
            .connectTimeout(1, TimeUnit.SECONDS)
            .readTimeout(1, TimeUnit.SECONDS)
            .build()

        val req = Request.Builder().url("$baseUrl/health").get().build()
        return try {
            healthClient.newCall(req).execute().use { it.isSuccessful }
        } catch (_: Exception) {
            false
        }
    }

    override fun stream(request: LlmRequest): Flow<LlmEvent> = callbackFlow {
        val baseUrl = resolveBaseUrl()
        Timber.d("[LlmClient] Starting request to $baseUrl")

        if (!networkMonitor.isOnline.value) {
            Timber.w("[LlmClient] Device offline")
            trySend(LlmEvent.Failed(AppError.Network("Device is offline")))
            close()
            return@callbackFlow
        }

        val isHealthy = checkHealthFast(baseUrl)
        if (!isHealthy) {
            Timber.w("[LlmClient] Fast health check failed for $baseUrl")
            trySend(LlmEvent.Failed(AppError.ServerUnavailable("Server at $baseUrl is unreachable")))
            close()
            return@callbackFlow
        }

        val streamingClient = httpClient.newBuilder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        val token = authTokenProvider.getOrRegisterToken(baseUrl)

        val reqJson = buildJsonObject {
            put("promptProfile", JsonPrimitive(request.promptProfile))
            put("maxOutputTokens", JsonPrimitive(request.maxOutputTokens))
            put("temperature", JsonPrimitive(request.temperature))

            put("messages", buildJsonArray {
                request.messages.forEach { msg ->
                    add(buildJsonObject {
                        put("role", JsonPrimitive(msg.role))
                        put("content", buildJsonArray {
                            msg.content.forEach { part ->
                                when (part) {
                                    is ContentPart.Text -> {
                                        add(buildJsonObject {
                                            put("type", JsonPrimitive("text"))
                                            put("text", JsonPrimitive(part.text))
                                        })
                                    }
                                    is ContentPart.ToolCall -> {
                                        add(buildJsonObject {
                                            put("type", JsonPrimitive("tool_call"))
                                            put("id", JsonPrimitive(part.id))
                                            put("name", JsonPrimitive(part.name))
                                            put("arguments", json.parseToJsonElement(part.argumentsJson))
                                        })
                                    }
                                    is ContentPart.ToolResult -> {
                                        add(buildJsonObject {
                                            put("type", JsonPrimitive("tool_result"))
                                            put("toolCallId", JsonPrimitive(part.toolCallId))
                                            put("content", JsonPrimitive(part.content))
                                            put("isError", JsonPrimitive(part.isError))
                                        })
                                    }
                                }
                            }
                        })
                    })
                }
            })

            if (request.tools.isNotEmpty()) {
                put("tools", buildJsonArray {
                    request.tools.forEach { tool ->
                        add(buildJsonObject {
                            put("name", JsonPrimitive(tool.name))
                            put("description", JsonPrimitive(tool.description))
                            put("parameters", json.parseToJsonElement(tool.parametersJsonSchema))
                        })
                    }
                })
            }
        }.toString()

        val httpRequest = Request.Builder()
            .url("$baseUrl/v1/chat/stream")
            .post(reqJson.toRequestBody("application/json".toMediaType()))
            .header("Authorization", "Bearer $token")
            .header("X-Orbit-Client-Version", "1.0.0")
            .build()

        val call = streamingClient.newCall(httpRequest)

        try {
            val response = call.execute()

            if (response.code == 429) {
                val retryAfter = response.header("Retry-After")?.toIntOrNull()
                trySend(LlmEvent.Failed(AppError.RateLimited(retryAfter)))
                close()
                return@callbackFlow
            }

            if (response.code == 401) {
                val refreshedToken = authTokenProvider.getOrRegisterToken(baseUrl, forceRefresh = true)
                val retryRequest = httpRequest.newBuilder()
                    .header("Authorization", "Bearer $refreshedToken")
                    .build()
                val retryCall = streamingClient.newCall(retryRequest)
                val retryResponse = retryCall.execute()
                if (!retryResponse.isSuccessful) {
                    trySend(LlmEvent.Failed(AppError.Unauthorized("Auth failed after token refresh")))
                    close()
                    return@callbackFlow
                }
            }

            if (!response.isSuccessful) {
                trySend(LlmEvent.Failed(AppError.ServerUnavailable("Server responded with HTTP ${response.code}")))
                close()
                return@callbackFlow
            }

            val body = response.body
            if (body == null) {
                trySend(LlmEvent.Failed(AppError.InvalidResponse("Empty response body")))
                close()
                return@callbackFlow
            }

            val reader = BufferedReader(InputStreamReader(body.byteStream()))
            var currentEvent = ""

            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val l = line!!
                if (l.isEmpty()) continue

                if (l.startsWith("event: ")) {
                    currentEvent = l.substring(7).trim()
                } else if (l.startsWith("data: ")) {
                    val data = l.substring(6).trim()
                    try {
                        val jsonElem = json.parseToJsonElement(data).jsonObject
                        when (currentEvent) {
                            "text_delta" -> {
                                val text = jsonElem["text"]?.jsonPrimitive?.content ?: ""
                                trySend(LlmEvent.TextDelta(text))
                            }
                            "tool_call" -> {
                                val id = jsonElem["id"]?.jsonPrimitive?.content ?: ""
                                val name = jsonElem["name"]?.jsonPrimitive?.content ?: ""
                                val args = jsonElem["arguments"].toString()
                                trySend(LlmEvent.ToolCallRequested(id, name, args))
                            }
                            "usage" -> {
                                val input = jsonElem["inputTokens"]?.jsonPrimitive?.int ?: 0
                                val output = jsonElem["outputTokens"]?.jsonPrimitive?.int ?: 0
                                trySend(LlmEvent.Usage(input, output))
                            }
                            "done" -> {
                                val reason = jsonElem["stopReason"]?.jsonPrimitive?.content ?: "end_turn"
                                trySend(LlmEvent.Done(reason))
                            }
                            "error" -> {
                                val msg = jsonElem["message"]?.jsonPrimitive?.content ?: "Stream error"
                                trySend(LlmEvent.Failed(AppError.Unknown(msg)))
                            }
                        }
                    } catch (_: Exception) {}
                }
            }

            close()
        } catch (e: Exception) {
            Timber.e(e, "[LlmClient] Stream failure")
            trySend(LlmEvent.Failed(AppError.Network("Network stream error: ${e.message}", e)))
            close()
        }

        awaitClose {
            call.cancel()
        }
    }
}
