package com.panalbit.panalsuite.util

import android.content.Context
import android.graphics.Bitmap
import java.io.File
import java.io.FileOutputStream

/** Guarda las fotos de evidencia en el almacenamiento interno de la app (no requiere permisos). */
object EvidenceFiles {

    fun save(context: Context, ticketId: Long, bitmap: Bitmap): String {
        val dir = File(context.filesDir, "evidence").apply { mkdirs() }
        val file = File(dir, "ticket_${ticketId}_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
        return file.absolutePath
    }
}
