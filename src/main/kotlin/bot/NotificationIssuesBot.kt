package com.hlianole.jetbrains.internship.youtrack_telegram.bot

import com.github.kotlintelegrambot.entities.ChatId
import com.hlianole.jetbrains.internship.youtrack_telegram.api.YouTrackApiClient
import com.hlianole.jetbrains.internship.youtrack_telegram.model.ChatPollingState
import io.ktor.util.collections.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Abstract class to separate some logic from the [TelegramBot]
 * */
abstract class NotificationIssuesBot(
    chatIds: Set<Long>,
    private val apiClient: YouTrackApiClient
) {
    protected val chatStates = ConcurrentHashMap<ChatId, ChatPollingState>()
    private val seenNotifications = ConcurrentSet<String>()

    init {
        chatIds.forEach { chatIdLong ->
            val chatId = ChatId.fromId(chatIdLong)
            chatStates[chatId] = ChatPollingState(
                chatId = chatId,
            )
        }
    }


    protected fun commandInfo(): String {
        return """
           |*Usage:*
           |Use `/start-poll` to start polling notifications. Notifications are checked every 60 seconds
           |
           |To change polling interval use `/interval <seconds>`
           |
           |To stop use `/stop-poll` to stop polling notifications. You will be able to yse other commands
           |
           |Use `/notifications` to check for the new notifications without waiting
           |
           |Use `/create <project-id-(short-name)> <summary>` to create a new issue
       """.trimMargin()
    }

    protected fun commandStartNotifying(chatId: ChatId) : String {
        return if (chatStates[chatId]?.isActive == false) {
            chatStates[chatId]?.isActive = true
            """
                |Started polling
            """.trimMargin()
        } else {
            """
                |Reject. Polling already started
            """.trimMargin()
        }
    }

    protected fun commandStopNotifying(chatId: ChatId): String {
        return if (chatStates[chatId]?.isActive == false) {
            """
                |Reject. Polling not yet started or already stopped
            """.trimMargin()
        } else {
            chatStates[chatId]?.isActive = false
            """
                |Stopped polling
            """.trimMargin()
        }
    }

    protected fun commandInterval(chatId: ChatId, intervalStr: String): String {
        return try {
            val interval = intervalStr.toLong()
            chatStates[chatId]?.pollIntervalSeconds = interval
            """
                |Changed polling interval to $interval
            """.trimMargin()
        } catch (_: NumberFormatException) {
            """
                |Error. Please provide valid number
            """.trimMargin()
        }
    }

    protected suspend fun commandNotifications(): List<String> {
        val response = apiClient.getNotifications()
        val notSeen = response.filter {
            !seenNotifications.contains(it.id)
        }

        return if (notSeen.isEmpty()) {
            listOf("""
                    |No recent notifications
                """.trimMargin())
        } else {
            notSeen.map {
                seenNotifications.add(it.id)
                it.toMessage()
            }
        }
    }

    protected suspend fun commandCreate(projectShortName: String, summary: String): String {
        val projectId = apiClient.getProjectIdByName(projectShortName)?.id
            ?: return """
                |Can not find project with short name (id): $projectShortName
            """.trimMargin()

        if (summary.isBlank()) {
            return """
                |Can not create issue with blank summary
            """.trimMargin()
        }

        val createdIssue = apiClient.createIssue(
            projectId = projectId,
            summary = summary
        )
        return if (createdIssue != null) {
            """
                |Issue created successfully
            """.trimMargin()
        } else {
            """
                |Error occurred while creating issue
            """.trimMargin()
        }
    }
}