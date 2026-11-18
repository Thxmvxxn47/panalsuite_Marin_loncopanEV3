package com.panalbit.panalsuite.data.repository

import com.google.gson.Gson
import com.panalbit.panalsuite.data.local.CachedTicket
import com.panalbit.panalsuite.data.local.EvidenceDao
import com.panalbit.panalsuite.data.local.EvidenceRow
import com.panalbit.panalsuite.data.local.TicketDao
import com.panalbit.panalsuite.data.remote.TicketsApi
import com.panalbit.panalsuite.data.remote.apiResult
import com.panalbit.panalsuite.domain.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException

class TicketRepository(
    private val api: TicketsApi,
    private val tickets: TicketDao,
    private val evidence: EvidenceDao
) {
    private val gson = Gson()
    private val cacheLock = Mutex()

    suspend fun getTickets(user: User) = apiResult {
        val requesterId = user.id.takeIf { user.role == Roles.REQUESTER }
        cacheLock.withLock {
            load(
                remote = {
                    api.list(requesterId).also { list ->
                        tickets.replace(requesterId, list.map(::encode))
                    }
                },
                cached = { tickets.list(requesterId).takeIf { it.isNotEmpty() }?.map(::decode) }
            )
        }
    }

    suspend fun getTicket(id: Long) = apiResult {
        cacheLock.withLock {
            try {
                load(
                    remote = { api.get(id).also { tickets.save(encode(it)) } },
                    cached = { tickets.get(id)?.let(::decode) }
                )
            } catch (error: HttpException) {
                if (error.code() == 404) tickets.delete(id)
                throw error
            }
        }
    }

    suspend fun createTicket(request: CreateTicketRequest) = updateCache { api.create(request) }
    suspend fun updateTicket(id: Long, request: UpdateTicketRequest) = updateCache { api.update(id, request) }
    suspend fun changeStatus(id: Long, request: StatusChangeRequest) = updateCache { api.changeStatus(id, request) }

    suspend fun deleteTicket(id: Long) = apiResult {
        cacheLock.withLock {
            api.delete(id)
            tickets.delete(id)
        }
    }

    suspend fun getStats(assignedToId: Long?) = apiResult { api.stats(assignedToId) }
    suspend fun getComments(id: Long) = apiResult { api.comments(id) }
    suspend fun addComment(id: Long, request: CommentRequest) = apiResult { api.addComment(id, request) }
    suspend fun getNotifications(userId: Long) = apiResult { api.notifications(userId) }
    suspend fun markNotificationRead(id: Long) = apiResult { api.markRead(id) }

    suspend fun getEvidence(ticketId: Long) = evidence.list(ticketId).map {
        Evidence(it.id, it.ticketId, it.filePath, it.createdAt)
    }

    suspend fun addEvidence(ticketId: Long, filePath: String): Evidence {
        val createdAt = System.currentTimeMillis()
        val id = evidence.add(EvidenceRow(ticketId = ticketId, filePath = filePath, createdAt = createdAt))
        return Evidence(id, ticketId, filePath, createdAt)
    }

    private suspend fun updateCache(request: suspend () -> Ticket) = apiResult {
        cacheLock.withLock { request().also { tickets.save(encode(it)) } }
    }

    private suspend fun <T> load(remote: suspend () -> T, cached: suspend () -> T?): Loaded<T> =
        try {
            Loaded(remote())
        } catch (error: IOException) {
            Loaded(cached() ?: throw error, offline = true)
        } catch (error: HttpException) {
            if (error.code() < 500) throw error
            Loaded(cached() ?: throw error, offline = true)
        }

    private fun encode(ticket: Ticket) = CachedTicket(ticket.id, ticket.requesterId, gson.toJson(ticket))
    private fun decode(ticket: CachedTicket) = gson.fromJson(ticket.json, Ticket::class.java)
}
