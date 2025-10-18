package com.hlianole.jetbrains.internship.youtrack_telegram.model

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys

data class Notification(
    val id: String,
    val recipient: Recipient? = null,
    val metadata: NotificationMetadata? = null,
    val issue: Issue? = null,
) {
    fun toMessage(): String {
        val sb = StringBuilder()
        sb.append(
            if (recipient != null) {
                "*Notification for user* ${recipient.login}"
            } else {
                "*Anonymous notification*"
            }
        )
        sb.append(" | *id*:$id\n")
        if (metadata != null) {
            if (metadata.type != null) {
                sb.append("Type: \n${metadata.type}\n")
            }
            if (metadata.change?.humanReadableTimeStamp != null) {
                sb.append("Change: \n${metadata.change.humanReadableTimeStamp}\n")
            }
        }
        if (issue != null) {
            sb.append("\n*Issue*:\n")
            sb.append(issue.toMessage())
        }
        return sb.toString()
    }
}

@Serializable
data class NotificationApi(
    val id: String,
    val recipient: Recipient? = null,
    val metadata: String? = null,
    val issue: Issue? = null,
    val read: Boolean? = null,
)

@Serializable
data class Recipient(
    val login: String,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class NotificationMetadata(
    val type: String? = null,
    val change: Change? = null,
)

@OptIn(ExperimentalSerializationApi::class)
@Serializable
@JsonIgnoreUnknownKeys
data class Change(
    val humanReadableTimeStamp: String? = null,
)
