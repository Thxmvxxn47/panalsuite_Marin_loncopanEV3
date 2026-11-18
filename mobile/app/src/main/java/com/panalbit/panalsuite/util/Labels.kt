package com.panalbit.panalsuite.util

/** Textos visibles para el usuario (español) a partir de los codigos internos. */
object Labels {

    fun status(value: String): String = when (value) {
        "NUEVO" -> "Nuevo"
        "EN_PROGRESO" -> "En progreso"
        "PENDIENTE" -> "Pendiente"
        "RESUELTO" -> "Resuelto"
        "CERRADO" -> "Cerrado"
        else -> value
    }

    fun priority(value: String): String = when (value) {
        "CRITICA" -> "Crítica"
        "ALTA" -> "Alta"
        "MEDIA" -> "Media"
        "BAJA" -> "Baja"
        else -> value
    }

    fun type(value: String): String = when (value) {
        "INCIDENTE" -> "Incidente"
        "REQUERIMIENTO" -> "Requerimiento"
        "CONSULTA" -> "Consulta"
        else -> value
    }

    fun category(value: String): String = when (value) {
        "TI" -> "TI"
        "INFRAESTRUCTURA" -> "Infraestructura"
        "COBRANZA" -> "Cobranza"
        "GASTOS" -> "Gastos"
        else -> value
    }

    fun role(value: String): String = when (value) {
        "SOLICITANTE" -> "Solicitante"
        "RESOLUTOR" -> "Resolutor"
        "SUPERVISOR" -> "Supervisor"
        "GERENCIA" -> "Gerencia"
        "ADMINISTRADOR" -> "Administrador"
        else -> value
    }

    fun action(value: String): String = when (value) {
        "TOMAR" -> "Tomar"
        "RESOLVER" -> "Resolver"
        "PAUSAR" -> "Pausar"
        "REANUDAR" -> "Reanudar"
        "CERRAR" -> "Cerrar"
        "REABRIR" -> "Reabrir"
        "ESCALAR" -> "Escalar"
        "RECHAZAR" -> "Rechazar"
        "ASIGNAR" -> "Asignar"
        "REASIGNAR" -> "Reasignar"
        else -> value
    }

    /** Convierte "2026-11-17T12:30:45.123" en "17-11-2026 12:30". */
    fun dateTime(iso: String?): String {
        if (iso.isNullOrBlank()) return "—"
        val parts = iso.split('T')
        if (parts.size != 2) return iso
        val date = parts[0].split('-')
        if (date.size != 3) return iso
        return "${date[2]}-${date[1]}-${date[0]} ${parts[1].take(5)}"
    }

    fun ticketNumber(number: String?, id: Long): String = number ?: "TKT-$id"
}
