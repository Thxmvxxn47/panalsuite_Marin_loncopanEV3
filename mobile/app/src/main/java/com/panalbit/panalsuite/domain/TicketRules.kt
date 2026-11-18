package com.panalbit.panalsuite.domain

/** Reglas de negocio de los tickets segun rol y estado (mismas reglas de la Experiencia 2). */
object TicketRules {

    const val TAKE = "TOMAR"
    const val ASSIGN = "ASIGNAR"
    const val REASSIGN = "REASIGNAR"

    private val actionToStatus = mapOf(
        "TOMAR" to Status.IN_PROGRESS,
        "RESOLVER" to Status.RESOLVED,
        "PAUSAR" to Status.PENDING,
        "REANUDAR" to Status.IN_PROGRESS,
        "CERRAR" to Status.CLOSED,
        "REABRIR" to Status.IN_PROGRESS,
        "ESCALAR" to Status.IN_PROGRESS,
        "RECHAZAR" to Status.CLOSED,
        "ASIGNAR" to Status.IN_PROGRESS,
        "REASIGNAR" to Status.IN_PROGRESS
    )

    fun availableActions(status: String, role: String): List<String> = when (role) {
        Roles.RESOLVER -> when (status) {
            Status.NEW -> listOf("TOMAR", "RECHAZAR")
            Status.IN_PROGRESS -> listOf("PAUSAR", "RESOLVER", "ESCALAR")
            Status.PENDING -> listOf("REANUDAR", "RESOLVER")
            else -> emptyList()
        }
        Roles.SUPERVISOR -> when (status) {
            Status.NEW -> listOf("ASIGNAR", "REASIGNAR", "RECHAZAR")
            Status.IN_PROGRESS -> listOf("REASIGNAR", "ESCALAR", "PAUSAR")
            Status.PENDING -> listOf("REANUDAR", "REASIGNAR")
            Status.RESOLVED -> listOf("CERRAR", "REABRIR")
            else -> emptyList()
        }
        Roles.ADMIN -> listOf("ASIGNAR", "REASIGNAR", "ESCALAR", "CERRAR", "REABRIR")
        else -> emptyList()
    }

    fun statusFor(action: String): String? = actionToStatus[action]

    fun requiresAssignee(action: String): Boolean = action == ASSIGN || action == REASSIGN

    fun canCreate(role: String): Boolean = role != Roles.MANAGEMENT

    fun canEdit(role: String): Boolean = role == Roles.SUPERVISOR || role == Roles.ADMIN

    fun canDelete(role: String): Boolean = role == Roles.ADMIN

    fun canComment(role: String): Boolean = role != Roles.MANAGEMENT

    fun canWriteInternalComments(role: String): Boolean = role != Roles.REQUESTER && role != Roles.MANAGEMENT

    fun canAddEvidence(role: String): Boolean = role != Roles.MANAGEMENT
}
