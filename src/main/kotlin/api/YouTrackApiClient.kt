package com.hlianole.jetbrains.internship.youtrack_telegram.api

import com.hlianole.jetbrains.internship.youtrack_telegram.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import java.util.*
import java.util.zip.GZIPInputStream

class YouTrackApiClient(
    private val baseUrl: String,
    private val token: String
) {
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
        }
    }

    suspend fun getNotifications(): List<Notification> {
        return try {
            println("\n--- ApiClient: getNotifications() ---")
            val response = client.get("$baseUrl/api/users/notifications") {
                accept(ContentType.Application.Json)
                bearerAuth(token)
                parameter(
                    "fields",
                    "id,recipient(login),metadata(type,change),issue(id,idReadable,summary,description,project(id,shortName)),read"
                )
                parameter(
                    "filter",
                    "read: false"
                )
            }

            println("   response.status: ${response.status.value}")

            response.body<List<NotificationApi>>().map {
                println("READ: " + it.read)
                it.toDomain()
            }
        } catch (e: Exception) {
            println("   error: ${e.message}")
            emptyList()
        }
    }

    suspend fun createIssue(projectId: String, summary: String): Issue? {
        return try {
            println("\n--- ApiClient: createIssue() ---")
            val request = IssueCreationRequest(
                summary = summary,
                project = ProjectIdReference(
                    id = projectId
                ),
            )

            val response = client.post("$baseUrl/api/issues") {
                accept(ContentType.Application.Json)
                bearerAuth(token)
                contentType(ContentType.Application.Json)
                parameter(
                    "fields",
                    "id,idReadable,summary,description,project(id,shortName)"
                )
                setBody(request)
            }

            println("   response.status: ${response.status.value}")

            response.body()
        } catch (e: Exception) {
            println("   error: ${e.message}")
            null
        }
    }

    suspend fun getProjectIdByName(shortName: String): ProjectIdReference? {
        return try {
            println("\n--- ApiClient: getProjectIdByName() ---")
            val response = client.get("$baseUrl/api/admin/projects") {
                accept(ContentType.Application.Json)
                bearerAuth(token)
                parameter(
                    "fields",
                    "id,shortName"
                )
            }

            println("   response.status: ${response.status.value}")

            val found = response.body<List<ProjectReference>>().find {
                it.shortName == shortName
            }
            return if (found != null) {
                ProjectIdReference(found.id)
            } else {
                null
            }
        } catch (e: Exception) {
            println("   error: ${e.message}")
            null
        }
    }

    fun close() {
        client.close()
    }

    private fun decodeMetadata(encoded: String?): NotificationMetadata? {
        if (encoded == null) {
            return null
        }

        return try {
            val decoded = Base64.getDecoder().decode(encoded)
            val gzipStream = GZIPInputStream(decoded.inputStream())
            val jsonString = gzipStream.bufferedReader().readText()
            Json.decodeFromString<NotificationMetadata>(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    private fun NotificationApi.toDomain(): Notification {
        return Notification(
            id = id,
            recipient = recipient,
            metadata = decodeMetadata(metadata),
            issue = issue,
        )
    }
}