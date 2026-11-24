package com.panalbit.panalsuite.data.remote

import com.panalbit.panalsuite.domain.*
import com.google.gson.annotations.SerializedName
import retrofit2.http.*

interface AuthApi {
    @POST("api/auth/login") suspend fun login(@Body request: LoginRequest): LoginResponse
    @GET("api/users") suspend fun users(): List<User>

    @POST("api/users")
    suspend fun createUser(
        @Header("Authorization") authorization: String,
        @Body request: CreateUserRequest
    ): User
}

interface TicketsApi {
    @GET("api/tickets") suspend fun list(@Query("requesterId") requesterId: Long?): List<Ticket>
    @GET("api/tickets/{id}") suspend fun get(@Path("id") id: Long): Ticket
    @POST("api/tickets") suspend fun create(@Body request: CreateTicketRequest): Ticket
    @PUT("api/tickets/{id}") suspend fun update(@Path("id") id: Long, @Body request: UpdateTicketRequest): Ticket
    @DELETE("api/tickets/{id}") suspend fun delete(@Path("id") id: Long)
    @PATCH("api/tickets/{id}/status") suspend fun changeStatus(@Path("id") id: Long, @Body request: StatusChangeRequest): Ticket
    @GET("api/tickets/stats") suspend fun stats(@Query("assignedToId") assignedToId: Long?): Stats
    @GET("api/tickets/{id}/comments") suspend fun comments(@Path("id") id: Long): List<Comment>
    @POST("api/tickets/{id}/comments") suspend fun addComment(@Path("id") id: Long, @Body request: CommentRequest): Comment
    @GET("api/notifications") suspend fun notifications(@Query("userId") userId: Long): List<AppNotification>
    @PATCH("api/notifications/{id}/read") suspend fun markRead(@Path("id") id: Long): AppNotification
}

interface IndicatorsApi {
    @GET("api") suspend fun latest(): Map<String, com.google.gson.JsonElement>
}

data class IndicatorDto(
    @SerializedName("codigo") val code: String,
    @SerializedName("nombre") val name: String,
    @SerializedName("unidad_medida") val unit: String,
    @SerializedName("fecha") val date: String,
    @SerializedName("valor") val value: Double
)
