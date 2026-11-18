package com.panalbit.panalsuite.di

import android.content.Context
import com.panalbit.panalsuite.BuildConfig
import com.panalbit.panalsuite.data.local.AppDatabase
import com.panalbit.panalsuite.data.local.DataStoreSessionStorage
import com.panalbit.panalsuite.data.remote.AuthApi
import com.panalbit.panalsuite.data.remote.IndicatorsApi
import com.panalbit.panalsuite.data.remote.TicketsApi
import com.panalbit.panalsuite.data.repository.AuthRepository
import com.panalbit.panalsuite.data.repository.IndicatorRepository
import com.panalbit.panalsuite.data.repository.TicketRepository
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/** Contenedor manual de dependencias (sin Hilt para mantener el proyecto simple). */
class AppContainer(context: Context) {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
        .build()

    private fun retrofit(baseUrl: String): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(httpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Microservicios propios
    private val authApi: AuthApi = retrofit(BuildConfig.AUTH_BASE_URL).create(AuthApi::class.java)
    private val ticketsApi: TicketsApi = retrofit(BuildConfig.TICKETS_BASE_URL).create(TicketsApi::class.java)

    // API externa (otra instancia de Retrofit con otra URL base)
    private val indicatorsApi: IndicatorsApi = retrofit(BuildConfig.INDICATORS_BASE_URL).create(IndicatorsApi::class.java)

    private val database = AppDatabase.create(context)

    val authRepository = AuthRepository(authApi, DataStoreSessionStorage(context))
    val ticketRepository = TicketRepository(ticketsApi, database.ticketDao(), database.evidenceDao())
    val indicatorRepository = IndicatorRepository(indicatorsApi)
}
