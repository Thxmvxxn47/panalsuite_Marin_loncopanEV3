package com.panalbit.panalsuite.util

/** Validadores de formularios. Devuelven el mensaje de error (en español) o null si es valido. */
object Validators {

    const val MIN_USERNAME = 3
    const val MIN_PASSWORD = 4
    const val MIN_TITLE = 5
    const val MAX_TITLE = 150
    const val MIN_DESCRIPTION = 10
    const val MAX_DESCRIPTION = 2000
    const val MAX_COMMENT = 1000

    fun validateUsername(value: String): String? = when {
        value.isBlank() -> "Ingresa tu usuario"
        value.trim().length < MIN_USERNAME -> "Debe tener al menos $MIN_USERNAME caracteres"
        else -> null
    }

    fun validatePassword(value: String): String? = when {
        value.isBlank() -> "Ingresa tu contraseña"
        value.length < MIN_PASSWORD -> "Debe tener al menos $MIN_PASSWORD caracteres"
        else -> null
    }

    fun validateTitle(value: String): String? = when {
        value.isBlank() -> "El título es obligatorio"
        value.trim().length < MIN_TITLE -> "Debe tener al menos $MIN_TITLE caracteres"
        value.trim().length > MAX_TITLE -> "Máximo $MAX_TITLE caracteres"
        else -> null
    }

    fun validateDescription(value: String): String? = when {
        value.isBlank() -> "La descripción es obligatoria"
        value.trim().length < MIN_DESCRIPTION -> "Describe el problema con al menos $MIN_DESCRIPTION caracteres"
        value.trim().length > MAX_DESCRIPTION -> "Máximo $MAX_DESCRIPTION caracteres"
        else -> null
    }

    fun validateComment(value: String): String? = when {
        value.isBlank() -> "Escribe un comentario"
        value.trim().length > MAX_COMMENT -> "Máximo $MAX_COMMENT caracteres"
        else -> null
    }

    fun validateRequired(value: String?, fieldName: String): String? =
        if (value.isNullOrBlank()) "Selecciona $fieldName" else null
}
