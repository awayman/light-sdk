package com.thelightphone.homeassistant

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray

internal class HomeAssistantApi {
    private val json = Json { ignoreUnknownKeys = true }

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json)
        }
    }

    suspend fun listStates(baseUrl: String, token: String): Result<JsonArray> = runCatching {
        val response = client.get("$baseUrl/api/states") {
            if (token.isNotEmpty()) header("Authorization", "Bearer $token")
        }

        if (!response.status.isSuccess()) {
            val body = response.bodyAsText().take(500)
            throw IllegalStateException("States HTTP ${response.status.value}: $body")
        }

        response.body()
    }

    fun close() {
        client.close()
    }
}
