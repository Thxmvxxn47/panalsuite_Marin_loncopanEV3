package com.panalbit.panalsuite.data.repository

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.panalbit.panalsuite.data.local.CachedTicket
import com.panalbit.panalsuite.data.local.EvidenceDao
import com.panalbit.panalsuite.data.local.EvidenceRow
import com.panalbit.panalsuite.data.local.TicketDao
import com.panalbit.panalsuite.data.remote.AuthApi
import com.panalbit.panalsuite.data.remote.ApiException
import com.panalbit.panalsuite.data.remote.IndicatorsApi
import com.panalbit.panalsuite.data.remote.TicketsApi
import com.panalbit.panalsuite.domain.CommentRequest
import com.panalbit.panalsuite.domain.CreateTicketRequest
import com.panalbit.panalsuite.domain.LoginRequest
import com.panalbit.panalsuite.domain.LoginResponse
import com.panalbit.panalsuite.domain.StatusChangeRequest
import com.panalbit.panalsuite.domain.UpdateTicketRequest
import com.panalbit.panalsuite.testutil.FakeSessionStorage
import com.panalbit.panalsuite.testutil.Samples
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class RepositoriesTest {
    private val ticket = Samples.ticket()
    private val cached = CachedTicket(ticket.id, ticket.requesterId, Gson().toJson(ticket))
    private val api = mockk<TicketsApi>()
    private val dao = mockk<TicketDao>(relaxUnitFun = true)
    private val evidence = mockk<EvidenceDao>(relaxUnitFun = true)
    private val repository = TicketRepository(api, dao, evidence)

    @Test fun loginPersistsSessionAndResolverListIsFiltered() = runTest {
        val authApi = mockk<AuthApi>()
        val storage = FakeSessionStorage()
        val auth = AuthRepository(authApi, storage)
        val response = LoginResponse("token", Samples.admin)
        coEvery { authApi.login(LoginRequest("admin", "clave")) } returns response
        coEvery { authApi.users() } returns listOf(Samples.admin, Samples.resolver)
        assertEquals(response, auth.login(" admin ", "clave").getOrThrow())
        assertEquals("token", auth.session.first()?.token)
        assertEquals(listOf(Samples.resolver), auth.getResolvers().getOrThrow())
        auth.logout()
        assertNull(auth.session.first())
    }

    @Test fun listsRequesterTicketsAndFallsBackToItsOwnCache() = runTest {
        coEvery { api.list(1L) } returns listOf(ticket)
        assertFalse(repository.getTickets(Samples.requester).getOrThrow().offline)
        coVerify { dao.replace(1L, listOf(cached)) }
        coEvery { api.list(1L) } throws IOException()
        coEvery { dao.list(1L) } returns listOf(cached)
        val result = repository.getTickets(Samples.requester).getOrThrow()
        assertEquals(listOf(ticket), result.data)
        assertTrue(result.offline)
    }

    @Test fun detailSavesRemoteDataAndUsesCacheOnlyForServiceFailure() = runTest {
        coEvery { api.get(1L) } returns ticket
        assertEquals(ticket, repository.getTicket(1L).getOrThrow().data)
        coVerify { dao.save(cached) }
        coEvery { dao.get(1L) } returns cached
        coEvery { api.get(1L) } throws httpError(503, "Temporal")
        assertTrue(repository.getTicket(1L).getOrThrow().offline)
        coEvery { api.get(1L) } throws httpError(404, "Ticket eliminado")
        val error = repository.getTicket(1L).exceptionOrNull() as ApiException
        assertEquals(404, error.status)
        assertEquals("Ticket eliminado", error.message)
        coVerify { dao.delete(1L) }
    }

    @Test fun networkFailureWithoutCacheAndEmptyExternalResponseAreReported() = runTest {
        coEvery { api.get(1L) } throws IOException()
        coEvery { dao.get(1L) } returns null
        assertTrue(repository.getTicket(1L).isFailure)
        val indicators = mockk<IndicatorsApi>()
        coEvery { indicators.latest() } returns emptyMap()
        assertEquals("El servicio no devolvió indicadores", IndicatorRepository(indicators).getIndicators().exceptionOrNull()?.message)
    }

    @Test fun requesterListWithNoCachedTicketsReportsNetworkFailure() = runTest {
        coEvery { api.list(1L) } throws IOException()
        coEvery { dao.list(1L) } returns emptyList()
        assertEquals("No se pudo conectar con el servicio", repository.getTickets(Samples.requester).exceptionOrNull()?.message)
    }

    @Test fun cancellationPropagatesAndUnstructuredErrorsHaveAReadableMessage() = runTest {
        coEvery { api.stats(null) } throws CancellationException("cancelled")
        val cancelled = runCatching { repository.getStats(null) }.exceptionOrNull()
        assertTrue(cancelled is CancellationException)
        coEvery { api.stats(null) } throws HttpException(Response.error<Unit>(502, "Bad gateway".toResponseBody()))
        assertEquals("El servicio respondió con error (502)", repository.getStats(null).exceptionOrNull()?.message)
    }

    @Test fun ticketWritesUpdateCacheAndAncillaryEndpointsUseTheirIds() = runTest {
        val create = CreateTicketRequest(ticket.title, ticket.description, ticket.type, ticket.category,
            ticket.priority, ticket.branch, ticket.requesterId, ticket.requesterName)
        val update = UpdateTicketRequest(ticket.title, ticket.description, ticket.type, ticket.category, ticket.priority, ticket.branch)
        val status = StatusChangeRequest("EN_PROGRESO", 2L, "Resolutor", 2L, "Resolutor")
        val comment = CommentRequest(1, "Ana", "Revisado", false)
        coEvery { api.create(create) } returns ticket
        coEvery { api.update(1L, update) } returns ticket
        coEvery { api.changeStatus(1L, status) } returns ticket
        coEvery { api.delete(1L) } returns Unit
        coEvery { api.stats(2L) } returns mockk()
        coEvery { api.comments(1L) } returns emptyList()
        coEvery { api.addComment(1L, comment) } returns mockk()
        coEvery { api.notifications(1L) } returns emptyList()
        coEvery { api.markRead(3L) } returns mockk()

        assertEquals(ticket, repository.createTicket(create).getOrThrow())
        assertEquals(ticket, repository.updateTicket(1L, update).getOrThrow())
        assertEquals(ticket, repository.changeStatus(1L, status).getOrThrow())
        repository.deleteTicket(1L).getOrThrow()
        repository.getStats(2L).getOrThrow()
        repository.getComments(1L).getOrThrow()
        repository.addComment(1L, comment).getOrThrow()
        repository.getNotifications(1L).getOrThrow()
        repository.markNotificationRead(3L).getOrThrow()
        coVerify(exactly = 3) { dao.save(cached) }
        coVerify { dao.delete(1L) }
    }

    @Test fun createdTicketIsReturnedEvenIfLocalCacheFails() = runTest {
        val request = CreateTicketRequest(ticket.title, ticket.description, ticket.type, ticket.category,
            ticket.priority, ticket.branch, ticket.requesterId, ticket.requesterName)
        coEvery { api.create(request) } returns ticket
        coEvery { dao.save(cached) } throws IllegalStateException("caché no disponible")

        assertEquals(ticket, repository.createTicket(request).getOrThrow())
        coVerify(exactly = 1) { api.create(request) }
    }

    @Test fun evidenceStaysLocalAndIndicatorsMapSpanishFields() = runTest {
        coEvery { evidence.list(1L) } returns listOf(EvidenceRow(2, 1, "/photo.jpg", 123))
        coEvery { evidence.add(any()) } returns 2L
        assertEquals(2L, repository.addEvidence(1L, "/photo.jpg").id)
        coVerify { evidence.add(match { it.ticketId == 1L && it.filePath == "/photo.jpg" }) }
        assertEquals(123L, repository.getEvidence(1L).single().createdAt)

        val indicators = mockk<IndicatorsApi>()
        val usd = JsonParser.parseString("""{"codigo":"dolar","nombre":"Dólar","unidad_medida":"Pesos","fecha":"2026-11-17","valor":950.5}""")
        coEvery { indicators.latest() } returns mapOf("dolar" to usd)
        val item = IndicatorRepository(indicators).getIndicators().getOrThrow().single()
        assertEquals("Dólar", item.name)
        assertEquals(950.5, item.value, 0.0)
    }

    private fun httpError(code: Int, message: String) =
        HttpException(Response.error<Unit>(code, """{"message":"$message"}""".toResponseBody()))
}
