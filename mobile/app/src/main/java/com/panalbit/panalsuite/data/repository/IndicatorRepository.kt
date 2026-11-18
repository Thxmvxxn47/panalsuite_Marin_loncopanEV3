package com.panalbit.panalsuite.data.repository

import com.google.gson.Gson
import com.panalbit.panalsuite.data.remote.ApiException
import com.panalbit.panalsuite.data.remote.IndicatorDto
import com.panalbit.panalsuite.data.remote.IndicatorsApi
import com.panalbit.panalsuite.data.remote.apiResult
import com.panalbit.panalsuite.domain.Indicator

class IndicatorRepository(private val api: IndicatorsApi) {
    private val gson = Gson()

    suspend fun getIndicators() = apiResult {
        val values = api.latest()
        listOf("uf", "dolar", "euro", "utm", "ipc", "bitcoin").mapNotNull { code ->
            values[code]?.takeUnless { it.isJsonNull }?.let {
                val item = gson.fromJson(it, IndicatorDto::class.java)
                Indicator(item.code, item.name, item.unit, item.date, item.value)
            }
        }.ifEmpty { throw ApiException("El servicio no devolvió indicadores") }
    }
}
