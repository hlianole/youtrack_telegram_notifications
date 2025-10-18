package com.hlianole.jetbrains.internship.youtrack_telegram.bot

import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.hlianole.jetbrains.internship.youtrack_telegram.api.YouTrackApiClient
import com.hlianole.jetbrains.internship.youtrack_telegram.model.ChatPollingState

/**
 * Telegram Bot
 * */
class TelegramBot(
    private val token: String,
    private val chatIds: Set<Long>,
    apiClient: YouTrackApiClient
): NotificationIssuesBot(
    chatIds = chatIds,
    apiClient = apiClient,
) {
    private val bot = bot {
        token = this@TelegramBot.token
        dispatch {
            command("start") {
                val chatId = ChatId.fromId(message.chat.id)
                if (!chatIds.contains(chatId.id)) {
                    sendNotAllowedMessage(chatId)
                    return@command
                }
                val message = commandInfo()
                bot.sendMessage(
                    chatId = chatId,
                    text = message,
                    parseMode = ParseMode.MARKDOWN
                )
            }

            command("start-poll") {
                val chatId = ChatId.fromId(message.chat.id)
                if (!chatIds.contains(chatId.id)) {
                    sendNotAllowedMessage(chatId)
                    return@command
                }
                val message = commandStartNotifying(chatId)
                bot.sendMessage(
                    chatId = chatId,
                    text = message,
                    parseMode = ParseMode.MARKDOWN
                )
            }

            command("stop-poll") {
                val chatId = ChatId.fromId(message.chat.id)
                if (!chatIds.contains(chatId.id)) {
                    sendNotAllowedMessage(chatId)
                    return@command
                }
                val message = commandStopNotifying(chatId)
                bot.sendMessage(
                    chatId = chatId,
                    text = message,
                    parseMode = ParseMode.MARKDOWN
                )
            }

            command("interval") {
                val chatId = ChatId.fromId(message.chat.id)
                if (!chatIds.contains(chatId.id)) {
                    sendNotAllowedMessage(chatId)
                    return@command
                }

                val errorMessage = """
                    |Please provide the interval in seconds as an integer
                """.trimMargin()

                if (args.size != 1) {
                    bot.sendMessage(
                        chatId = chatId,
                        text = errorMessage,
                        parseMode = ParseMode.MARKDOWN
                    )
                    return@command
                }

                val message = commandInterval(chatId, args[0])
                bot.sendMessage(
                    chatId = chatId,
                    text = message,
                    parseMode = ParseMode.MARKDOWN
                )
            }

            command("notifications") {
                val chatId = ChatId.fromId(message.chat.id)
                if (!chatIds.contains(chatId.id)) {
                    sendNotAllowedMessage(chatId)
                    return@command
                }
                val messages = commandNotifications()
                if (messages.isNotEmpty()) {
                    messages.forEach { message ->
                        bot.sendMessage(
                            chatId = chatId,
                            text = message,
                            parseMode = ParseMode.MARKDOWN
                        )
                    }
                } else {
                    bot.sendMessage(
                        chatId = chatId,
                        text = """
                            |No recent notifications found
                        """.trimMargin(),
                        parseMode = ParseMode.MARKDOWN
                    )
                }
            }

            command("create") {
                val chatId = ChatId.fromId(message.chat.id)
                if (!chatIds.contains(chatId.id)) {
                    sendNotAllowedMessage(chatId)
                    return@command
                }

                if (args.size < 2) {
                    val message = """
                        |Please provide the project short name (id) and the issue summary
                    """.trimMargin()

                    bot.sendMessage(
                        chatId = chatId,
                        text = message,
                        parseMode = ParseMode.MARKDOWN
                    )
                    return@command
                }

                val projectShortName = args[0]
                val summary = args.drop(1).joinToString(" ")

                val message = commandCreate(
                    projectShortName = projectShortName,
                    summary = summary
                )
                bot.sendMessage(
                    chatId = chatId,
                    text = message,
                    parseMode = ParseMode.MARKDOWN
                )
            }
        }
    }

    private fun sendNotAllowedMessage(chatId: ChatId) {
        val message = """
            |You are not allowed to use this bot
        """.trimMargin()

        bot.sendMessage(
            chatId = chatId,
            text = message,
            parseMode = ParseMode.MARKDOWN
        )
    }

    fun start() {
        bot.startPolling()
    }

    fun stop() {
        bot.stopPolling()
    }

    suspend fun sendNotifications(chatId: ChatId) {
        val notifications = commandNotifications()
        if (notifications.isNotEmpty()) {
            bot.sendMessage(
                chatId = chatId,
                text = """
                    |All recent notifications:
                """.trimMargin(),
                parseMode = ParseMode.MARKDOWN
            )
            notifications.forEach { message ->
                bot.sendMessage(
                    chatId = chatId,
                    text = message,
                    parseMode = ParseMode.MARKDOWN
                )
            }
        }
    }

    /**
    * Takes the list of [ChatPollingState] for the Main job. Filters only active
    * */
    fun getActiveChatStates(): List<ChatPollingState> {
        return chatStates.values
            .asSequence()
            .filter {
                it.isActive
            }
            .toList()
    }
}
