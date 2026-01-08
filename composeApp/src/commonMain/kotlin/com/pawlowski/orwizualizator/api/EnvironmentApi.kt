package com.pawlowski.orwizualizator.api

import com.pawlowski.orwizualizator.models.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.flow.*

expect fun createHttpClient(): HttpClient

class EnvironmentApi(
    private val baseUrl: String = "http://0.0.0.0:8080",
) {
    private val client = createHttpClient()

    suspend fun getEnvironmentState(): EnvironmentState = client.get("$baseUrl/api/environment/state").body()
    
    suspend fun getAlerts(): List<Alert> = try {
        client.get("$baseUrl/api/environment/alerts").body()
    } catch (e: Exception) {
        emptyList<Alert>()
    }
    
    suspend fun getMessages(): List<AgentMessage> = try {
        client.get("$baseUrl/api/environment/agents/messages").body()
    } catch (e: Exception) {
        emptyList<AgentMessage>()
    }

    fun observeEnvironmentState(intervalSeconds: Long = 1): Flow<EnvironmentState> =
        flow {
            while (true) {
                try {
                    emit(getEnvironmentState())
                } catch (e: Exception) {
                    // W przypadku błędu, emitujemy ostatni stan lub pusty
                    println("Error fetching state: ${e.message}")
                }
                kotlinx.coroutines.delay(intervalSeconds * 1000)
            }
        }
    
    fun observeAlerts(intervalSeconds: Long = 2): Flow<List<Alert>> =
        flow {
            while (true) {
                try {
                    emit(getAlerts())
                } catch (e: Exception) {
                    emit(emptyList<Alert>())
                }
                kotlinx.coroutines.delay(intervalSeconds * 1000)
            }
        }
    
    fun observeMessages(intervalSeconds: Long = 2): Flow<List<AgentMessage>> =
        flow {
            while (true) {
                try {
                    emit(getMessages())
                } catch (e: Exception) {
                    emit(emptyList<AgentMessage>())
                }
                kotlinx.coroutines.delay(intervalSeconds * 1000)
            }
        }

    fun close() {
        client.close()
    }
}
