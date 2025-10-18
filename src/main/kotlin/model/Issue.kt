package com.hlianole.jetbrains.internship.youtrack_telegram.model

import kotlinx.serialization.Serializable

@Serializable
data class Issue(
    val id: String,
    val idReadable: String,
    val summary: String,
    val description: String? = null,
    val project: ProjectReference? = null,
) {
    fun toMessage(): String {
        val sb = StringBuilder()
        sb.append("Issue *$idReadable*\n")
        if (project != null) {
            sb.append("Project *${project.shortName}*\n")
        }
        sb.append("*Summary:*\n$summary\n")
        if (description != null) {
            sb.append("*Description:*\n$description\n")
        }
        return sb.toString()
    }
}

@Serializable
data class IssueCreationRequest(
    val summary: String,
    val project: ProjectIdReference
)

@Serializable
data class ProjectIdReference(
    val id: String,
)

@Serializable
data class ProjectReference(
    val id: String,
    val shortName: String,
)
