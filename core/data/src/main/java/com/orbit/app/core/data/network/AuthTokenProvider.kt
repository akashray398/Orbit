package com.orbit.app.core.data.network

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authDataStore by preferencesDataStore(name = "orbit_auth_prefs")

@Singleton
class AuthTokenProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val httpClient: OkHttpClient
) {
    private val mutex = Mutex()

    private object Keys {
        val INSTALL_ID = stringPreferencesKey("install_id")
        val AUTH_TOKEN = stringPreferencesKey("auth_token")
    }

    suspend fun getOrRegisterToken(baseUrl: String, forceRefresh: Boolean = false): String {
        return mutex.withLock {
            val prefs = context.authDataStore.data.first()
            var installId = prefs[Keys.INSTALL_ID]
            if (installId == null) {
                installId = UUID.randomUUID().toString()
                context.authDataStore.edit { it[Keys.INSTALL_ID] = installId }
            }

            val existingToken = prefs[Keys.AUTH_TOKEN]
            if (!forceRefresh && !existingToken.isNullOrEmpty()) {
                return@withLock existingToken
            }

            val registerUrl = "$baseUrl/v1/auth/register"
            val bodyJson = JSONObject().apply {
                put("installId", installId)
            }.toString()

            val request = Request.Builder()
                .url(registerUrl)
                .post(bodyJson.toRequestBody("application/json".toMediaType()))
                .build()

            try {
                httpClient.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val respBody = response.body?.string() ?: ""
                        val jsonObj = JSONObject(respBody)
                        val newToken = jsonObj.getString("token")
                        context.authDataStore.edit { it[Keys.AUTH_TOKEN] = newToken }
                        return@withLock newToken
                    }
                }
            } catch (_: Exception) {}

            return@withLock existingToken ?: "dev-fallback-token"
        }
    }
}
