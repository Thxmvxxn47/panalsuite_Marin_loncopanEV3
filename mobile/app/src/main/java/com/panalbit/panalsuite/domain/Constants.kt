package com.panalbit.panalsuite.domain

object Roles {
    const val REQUESTER = "SOLICITANTE"
    const val RESOLVER = "RESOLUTOR"
    const val SUPERVISOR = "SUPERVISOR"
    const val MANAGEMENT = "GERENCIA"
    const val ADMIN = "ADMINISTRADOR"
}

object Status {
    const val NEW = "NUEVO"
    const val IN_PROGRESS = "EN_PROGRESO"
    const val PENDING = "PENDIENTE"
    const val RESOLVED = "RESUELTO"
    const val CLOSED = "CERRADO"
    val all = listOf(NEW, IN_PROGRESS, PENDING, RESOLVED, CLOSED)
}

object Priority {
    const val CRITICAL = "CRITICA"
    const val HIGH = "ALTA"
    const val MEDIUM = "MEDIA"
    const val LOW = "BAJA"
    val all = listOf(LOW, MEDIUM, HIGH, CRITICAL)
}

object TicketTypes {
    val all = listOf("INCIDENTE", "REQUERIMIENTO", "CONSULTA")
}

object Categories {
    val all = listOf("TI", "INFRAESTRUCTURA", "COBRANZA", "GASTOS")
}

object Branches {
    val all = listOf(
        "Casa Matriz", "Tienda Alameda", "Farmacia Sur", "Tienda Plaza Norte",
        "Sucursal Providencia", "Departamento Legal", "Proveedor ABC"
    )
}
