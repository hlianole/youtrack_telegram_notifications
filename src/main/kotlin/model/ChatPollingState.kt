package com.hlianole.jetbrains.internship.youtrack_telegram.model

import com.github.kotlintelegrambot.entities.ChatId

data class ChatPollingState(
    val chatId: ChatId,
    var pollIntervalSeconds: Long = 60L,
    var lastPolledAt: Long = 0L,
    var isActive: Boolean = false,
)
