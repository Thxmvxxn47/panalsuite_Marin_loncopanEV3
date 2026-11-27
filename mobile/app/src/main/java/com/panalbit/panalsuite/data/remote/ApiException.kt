package com.panalbit.panalsuite.data.remote

import kotlinx.coroutines.CancellationException
import com.google.gson.JsonParser
import retrofit2.HttpException
import java.io.IOException

class ApiException(message: String, val status: Int? = null) : Exception(message)

internal suspend fun <T> apiResult(block: suspend () -> T): Result<T> = try {
    Result.success(block())
} catch (error: CancellationException) {
    throw error
} catch (error: ApiException) {
    Result.failure(error)
} catch (error: HttpException) {
    val message = runCatching {
        val body = error.response()?.errorBody()?.use { it.string() }
        body?.let { JsonParser.parseString(it).asJsonObject.get("message")?.asString?.ifBlank { null } }
    }.getOrNull()
    Result.failure(ApiException(message ?: "El servicio respondió con error (${error.code()})", error.code()))
} catch (error: IOException) {
    Result.failure(ApiException("No se pudo conectar con el servicio"))
} catch (error: Exception) {
    android.util.Log.e("PanalSuiteApi", "Error procesando respuesta", error)
    Result.failure(ApiException("No se pudieron procesar los datos (${error.javaClass.simpleName})"))
}
