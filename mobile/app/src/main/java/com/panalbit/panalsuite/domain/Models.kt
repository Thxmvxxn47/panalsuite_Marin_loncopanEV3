package com.panalbit.panalsuite.domain

/** Modelos de dominio. Los nombres de campos coinciden con el JSON de los microservicios. */

data class User(
    val id: Long,
    val username: String,
    val name: String,
    val email: String,
    val role: String
)

data class CreateUserRequest(
    val username: String,
    val name: String,
    val email: String,
    val role: String,
    val password: String
)

data class LoginRequest(val username: String, val password: String)

data class LoginResponse(val token: String, val user: User)

data class Session(val token: String, val user: User)

data class Ticket(
    val id: Long,
    val ticketNumber: String?,
    val title: String,
    val description: String,
    val type: String,
    val category: String,
    val priority: String,
    val status: String,
    val branch: String?,
    val requesterId: Long,
    val requesterName: String,
    val assignedToId: Long?,
    val assignedToName: String?,
    val createdAt: String,
    val updatedAt: String
)

data class CreateTicketRequest(
    val title: String,
    val description: String,
    val type: String,
    val category: String,
    val priority: String,
    val branch: String?,
    val requesterId: Long,
    val requesterName: String
)

data class UpdateTicketRequest(
    val title: String,
    val description: String,
    val type: String,
    val category: String,
    val priority: String,
    val branch: String?
)

data class StatusChangeRequest(
    val status: String,
    val assignedToId: Long?,
    val assignedToName: String?,
    val actorId: Long,
    val actorName: String
)

data class Comment(
    val id: Long,
    val ticketId: Long,
    val authorId: Long,
    val authorName: String,
    val text: String,
    val internal: Boolean,
    val createdAt: String
)

data class CommentRequest(
    val authorId: Long,
    val authorName: String,
    val text: String,
    val internal: Boolean
)

data class AppNotification(
    val id: Long,
    val recipientId: Long,
    val title: String,
    val message: String,
    val type: String,
    val ticketId: Long?,
    val ticketNumber: String?,
    val read: Boolean,
    val createdAt: String
)

data class Stats(
    val total: Long,
    val open: Long,
    val inProgress: Long,
    val pending: Long,
    val resolved: Long,
    val closed: Long,
    val critical: Long,
    val high: Long,
    val myAssigned: Long
)

/** Indicador economico de la API externa (mindicador.cl). */
data class Indicator(
    val code: String,
    val name: String,
    val unit: String,
    val date: String,
    val value: Double
)

/** Evidencia fotografica guardada solo en el dispositivo. */
data class Evidence(val id: Long, val ticketId: Long, val filePath: String, val createdAt: Long)

/** Resultado de una carga: `offline` = true cuando se muestran datos guardados en Room. */
data class Loaded<T>(val data: T, val offline: Boolean = false)
