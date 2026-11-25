package com.panalbit.panalsuite.ui

import com.panalbit.panalsuite.data.remote.ApiException
import com.panalbit.panalsuite.data.repository.AuthRepository
import com.panalbit.panalsuite.data.repository.TicketRepository
import com.panalbit.panalsuite.domain.Comment
import com.panalbit.panalsuite.domain.Evidence
import com.panalbit.panalsuite.domain.Loaded
import com.panalbit.panalsuite.domain.StatusChangeRequest
import com.panalbit.panalsuite.domain.User
import com.panalbit.panalsuite.testutil.MainDispatcherRule
import com.panalbit.panalsuite.testutil.Samples
import com.panalbit.panalsuite.ui.tickets.TicketDetailViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class DetailViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()
    private val tickets = mockk<TicketRepository>()
    private val auth = mockk<AuthRepository>()

    private val publicComment = Comment(1, 1, 1, "Ana", "Hola", false, "2026-11-17T10:00:00")
    private val internalComment = Comment(2, 1, 2, "Raúl", "Nota interna", true, "2026-11-17T10:05:00")

    private fun vm(user: User): TicketDetailViewModel {
        coEvery { tickets.getTicket(1) } returns Result.success(Loaded(Samples.ticket(1, status = "NUEVO")))
        coEvery { tickets.getComments(1) } returns Result.success(listOf(publicComment, internalComment))
        coEvery { tickets.getEvidence(1) } returns listOf(Evidence(1, 1, "/x.jpg", 1L))
        return TicketDetailViewModel(tickets, auth, 1, user)
    }

    @Test fun load_resolver_sees_internal_comments_and_actions() {
        val vm = vm(Samples.resolver)
        vm.load()
        val s = vm.state.value
        assertNotNull(s.ticket)
        assertEquals(2, s.comments.size)
        assertEquals(1, s.evidence.size)
        assertEquals(listOf("TOMAR", "RECHAZAR"), s.actions)
    }

    @Test fun requester_does_not_see_internal_comments() {
        val vm = vm(Samples.requester)
        vm.load()
        assertEquals(listOf(publicComment), vm.state.value.comments)
        assertTrue(vm.state.value.actions.isEmpty())
    }

    @Test fun load_failure() {
        coEvery { tickets.getTicket(1) } returns Result.failure(ApiException("caído"))
        val vm = TicketDetailViewModel(tickets, auth, 1, Samples.admin)
        vm.load()
        assertEquals("caído", vm.state.value.error)
        assertNull(vm.state.value.ticket)
    }

    @Test fun invalid_comment_is_rejected() {
        val vm = vm(Samples.resolver)
        vm.addComment("  ", false)
        assertNotNull(vm.state.value.commentError)
        coVerify(exactly = 0) { tickets.addComment(any(), any()) }
    }

    @Test fun management_cannot_comment() {
        val mgmt = User(4, "gerencia", "Gerente", "g@panal.cl", "GERENCIA")
        val vm = vm(mgmt)
        vm.addComment("hola", false)
        coVerify(exactly = 0) { tickets.addComment(any(), any()) }
    }

    @Test fun requester_comment_is_never_internal_and_success_increments_counter() {
        val vm = vm(Samples.requester)
        vm.load()
        coEvery { tickets.addComment(1, any()) } returns Result.success(publicComment)
        vm.addComment("  Gracias  ", true)
        val s = vm.state.value
        assertEquals(1, s.commentSentCount)
        assertEquals("Comentario agregado", s.message)
        coVerify { tickets.addComment(1, match { !it.internal && it.text == "Gracias" }) }
    }

    @Test fun comment_failure_shows_message() {
        val vm = vm(Samples.resolver)
        coEvery { tickets.addComment(any(), any()) } returns Result.failure(ApiException("falló"))
        vm.addComment("hola", true)
        assertEquals("falló", vm.state.value.message)
        assertFalse(vm.state.value.isWorking)
    }

    @Test fun take_action_assigns_to_self() {
        val vm = vm(Samples.resolver)
        vm.load()
        val req = slot<StatusChangeRequest>()
        coEvery { tickets.changeStatus(1, capture(req)) } returns Result.success(Samples.ticket(1, status = "EN_PROGRESO"))
        vm.executeAction("TOMAR")
        assertEquals("EN_PROGRESO", req.captured.status)
        assertEquals(Samples.resolver.id, req.captured.assignedToId)
        assertEquals("Acción ejecutada correctamente", vm.state.value.message)
        assertEquals(listOf("PAUSAR", "RESOLVER", "ESCALAR"), vm.state.value.actions)
    }

    @Test fun assign_requires_resolver_selection() {
        val vm = vm(Samples.supervisor)
        vm.executeAction("ASIGNAR")
        assertEquals("Selecciona un resolutor", vm.state.value.message)
        coVerify(exactly = 0) { tickets.changeStatus(any(), any()) }

        val req = slot<StatusChangeRequest>()
        coEvery { tickets.changeStatus(1, capture(req)) } returns Result.success(Samples.ticket(1, status = "EN_PROGRESO"))
        vm.executeAction("ASIGNAR", Samples.resolver)
        assertEquals(Samples.resolver.id, req.captured.assignedToId)
        assertEquals(Samples.supervisor.id, req.captured.actorId)
    }

    @Test fun unknown_action_is_ignored() {
        val vm = vm(Samples.resolver)
        vm.executeAction("VOLAR")
        coVerify(exactly = 0) { tickets.changeStatus(any(), any()) }
    }

    @Test fun action_failure() {
        val vm = vm(Samples.resolver)
        coEvery { tickets.changeStatus(any(), any()) } returns Result.failure(ApiException("no se pudo"))
        vm.executeAction("RECHAZAR")
        assertEquals("no se pudo", vm.state.value.message)
    }

    @Test fun resolvers_loaded_once() {
        val vm = vm(Samples.supervisor)
        coEvery { auth.getResolvers() } returns Result.success(listOf(Samples.resolver))
        vm.loadResolvers()
        vm.loadResolvers()
        assertEquals(1, vm.state.value.resolvers.size)
        coVerify(exactly = 1) { auth.getResolvers() }
    }

    @Test fun resolvers_failure_sets_message() {
        val vm = vm(Samples.supervisor)
        coEvery { auth.getResolvers() } returns Result.failure(ApiException("sin lista"))
        vm.loadResolvers()
        assertEquals("sin lista", vm.state.value.message)
    }

    @Test fun delete_only_admin() {
        val notAdmin = vm(Samples.supervisor)
        notAdmin.deleteTicket()
        coVerify(exactly = 0) { tickets.deleteTicket(any()) }

        val vm = vm(Samples.admin)
        coEvery { tickets.deleteTicket(1) } returns Result.success(Unit)
        vm.deleteTicket()
        assertTrue(vm.state.value.deleted)
    }

    @Test fun delete_failure() {
        val vm = vm(Samples.admin)
        coEvery { tickets.deleteTicket(1) } returns Result.failure(ApiException("no"))
        vm.deleteTicket()
        assertFalse(vm.state.value.deleted)
        assertEquals("no", vm.state.value.message)
    }

    @Test fun evidence_read_failure_keeps_ticket_open() {
        val vm = vm(Samples.resolver)
        coEvery { tickets.getEvidence(1) } throws IllegalStateException("lectura fallida")
        vm.load()
        assertNotNull(vm.state.value.ticket)
        assertEquals(2, vm.state.value.comments.size)
        assertEquals("No se pudo cargar la evidencia local", vm.state.value.message)
    }

    @Test fun blank_evidence_path_is_rejected() {
        val vm = vm(Samples.resolver)
        vm.addEvidence("  ")
        coVerify(exactly = 0) { tickets.addEvidence(any(), any()) }
        assertEquals("Selecciona una foto para adjuntar", vm.state.value.message)
    }

    @Test fun evidence_write_failure_reports_error() {
        val vm = vm(Samples.resolver)
        coEvery { tickets.addEvidence(1, "/y.jpg") } throws IllegalStateException("escritura fallida")
        vm.addEvidence("/y.jpg")
        assertEquals("No se pudo guardar la evidencia", vm.state.value.message)
        coVerify(exactly = 0) { tickets.getEvidence(any()) }
    }

    @Test fun evidence_write_success_with_refresh_failure_reports_both() {
        val vm = vm(Samples.resolver)
        coEvery { tickets.addEvidence(1, "/y.jpg") } returns Evidence(2, 1, "/y.jpg", 2L)
        coEvery { tickets.getEvidence(1) } throws IllegalStateException("lectura fallida")
        vm.addEvidence("/y.jpg")
        assertEquals("La evidencia se guardó, pero no se pudo actualizar la lista", vm.state.value.message)
    }

    @Test fun evidence_and_message_cleared() {
        val vm = vm(Samples.resolver)
        coEvery { tickets.addEvidence(1, "/y.jpg") } returns Evidence(2, 1, "/y.jpg", 2L)
        vm.addEvidence("/y.jpg")
        assertEquals("Evidencia guardada en el dispositivo", vm.state.value.message)
        vm.messageShown()
        assertNull(vm.state.value.message)
    }
}
