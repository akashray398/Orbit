package com.orbit.app.core.domain.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

class ToolArgumentException(message: String) : Exception(message)

class ToolArguments(val jsonObject: JsonObject) {

    fun getString(key: String): String {
        return jsonObject[key]?.jsonPrimitive?.content
            ?: throw ToolArgumentException("Missing required string parameter: '$key'")
    }

    fun getOptionalString(key: String): String? {
        return jsonObject[key]?.jsonPrimitive?.content
    }

    fun getInt(key: String): Int {
        val prim = jsonObject[key]?.jsonPrimitive
        return prim?.intOrNull ?: prim?.content?.toIntOrNull()
            ?: throw ToolArgumentException("Missing or invalid int parameter: '$key'")
    }

    fun getInt(key: String, default: Int): Int {
        val prim = jsonObject[key]?.jsonPrimitive
        return prim?.intOrNull ?: prim?.content?.toIntOrNull() ?: default
    }

    fun getLong(key: String): Long {
        val prim = jsonObject[key]?.jsonPrimitive
        return prim?.longOrNull ?: prim?.content?.toLongOrNull()
            ?: throw ToolArgumentException("Missing or invalid long parameter: '$key'")
    }

    fun getLong(key: String, default: Long): Long {
        val prim = jsonObject[key]?.jsonPrimitive
        return prim?.longOrNull ?: prim?.content?.toLongOrNull() ?: default
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        val prim = jsonObject[key]?.jsonPrimitive
        return prim?.booleanOrNull ?: prim?.content?.toBooleanStrictOrNull() ?: default
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromJsonString(jsonStr: String): ToolArguments {
            return try {
                val element = json.parseToJsonElement(jsonStr)
                ToolArguments(element as? JsonObject ?: JsonObject(emptyMap()))
            } catch (e: Exception) {
                ToolArguments(JsonObject(emptyMap()))
            }
        }
    }
}
