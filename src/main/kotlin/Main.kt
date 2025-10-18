package com.hlianole.jetbrains.internship.youtrack_telegram

import com.hlianole.jetbrains.internship.youtrack_telegram.api.YouTrackApiClient
import com.hlianole.jetbrains.internship.youtrack_telegram.bot.TelegramBot
import com.typesafe.config.ConfigFactory
import kotlinx.coroutines.*

fun main() {

    val config = ConfigFactory.load()
    val youTrackUrl = config.getString("youtrack.url")
    val youTrackToken = config.getString("youtrack.token")

    val telegramBotToken = config.getString("telegram.botToken")
    val telegramAllowedChatIds: Set<Long>
    try {
        telegramAllowedChatIds = config.getString("telegram.allowedChatIds")
            .split(",")
            .map {
                it.trim().toLong()
            }
            .toSet()
    } catch (_: Exception) {
        throw RuntimeException("Wrong Telegram IDs format")
    }

    println("--- Main: Starting ---")

    val youTrackApiClient = YouTrackApiClient(
        baseUrl = youTrackUrl,
        token = youTrackToken
    )

    println("--- Main: Created Api Client ---")

    val telegramBot = TelegramBot(
        token = telegramBotToken,
        chatIds = telegramAllowedChatIds,
        apiClient = youTrackApiClient
    )

    println("--- Main: Created Bot ---")

    telegramBot.start()

    val pollingJob = CoroutineScope(Dispatchers.Default).launch {
        println("--- Main: Starting polling job ---")
        while (isActive) {
            val timeNow = System.currentTimeMillis()

            val activeChatStates = telegramBot.getActiveChatStates()

            activeChatStates.forEach { chatState ->
                val lastPoll = chatState.lastPolledAt
                if (timeNow - lastPoll < chatState.pollIntervalSeconds * 1000L) {
                    return@forEach
                }

                try {
                    chatState.lastPolledAt = timeNow
                    telegramBot.sendNotifications(chatId = chatState.chatId)
                } catch (_: Exception) {
                    // TODO
                }
            }
            delay(5000)
        }
    }

    Runtime.getRuntime().addShutdownHook(Thread {
        println("--- Main: Closing ---")
        runBlocking {
            pollingJob.cancelAndJoin()
            youTrackApiClient.close()
            telegramBot.stop()
        }
    })

    runBlocking {
        println("--- Main: Ending polling job ---")
        pollingJob.join()
    }
}