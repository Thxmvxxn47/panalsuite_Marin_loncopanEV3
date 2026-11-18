package com.panalbit.panalsuite.ui

import com.panalbit.panalsuite.data.remote.ApiException
import com.panalbit.panalsuite.data.repository.TicketRepository
import com.panalbit.panalsuite.domain.CreateTicketRequest
import com.panalbit.panalsuite.domain.Loaded
import com.panalbit.panalsuite.domain.UpdateTicketRequest
import com.panalbit.panalsuite.testutil.MainDispatcherRule
import com.panalbit.panalsuite.testutil.Samples
import com.panalbit.panalsuite.ui.tickets.CreateEditTicketViewModel
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

class CreateEditTicketViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()
    private val repo = mockk<TicketRepository>()

    private fun fill(vm: CreateEditTicketViewModel) {
        vm.onTitleChange("Impresora sin tinta")
        vm.onDescriptionChange("La impresora del segundo piso no imprime")
        vm.onTypeChange("REQUERIMIENTO")
        vm.onCategoryChange("INFRAESTRUCTURA")
        vm.onPriorityChange("ALTA")
        vm.onBranchChange("Casa Matriz")
    }

    @Test fun invalid_form_shows_errors() {
        val vm = CreateEditTicketViewModel(repo, Samples.requester, null)
        vm.submit()
        assertNotNull(vm.state.value.titleError)
        assertNotNull(vm.state.value.descriptionError)
        assertFalse(vm.isEditing)
        coVerify(exactly = 0) { repo.createTicket(any()) }
    }

    @Test fun create_success() {
        val slot = slot<CreateTicketRequest>()
        coEvery { repo.createTicket(capture(slot)) } returns Result.success(Samples.ticket())
        val vm = CreateEditTicketViewModel(repo, Samples.requester, null)
        fill(vm)
        vm.submit()
        assertTrue(vm.state.value.saved)
        assertEquals("Casa Matriz", slot.captured.branch)
        assertEquals(Samples.requester.id, slot.captured.requesterId)
        assertEquals("ALTA", slot.captured.priority)
    }

    @Test fun blank_branch_is_sent_as_null() {
        val slot = slot<CreateTicketRequest>()
        coEvery { repo.createTicket(capture(slot)) } returns Result.success(Samples.ticket())
        val vm = CreateEditTicketViewModel(repo, Samples.requester, null)
        fill(vm)
        vm.onBranchChange("")
        vm.submit()
        assertNull(slot.captured.branch)
    }

    @Test fun create_failure_shows_error() {
        coEvery { repo.createTicket(any()) } returns Result.failure(ApiException("Sin conexión"))
        val vm = CreateEditTicketViewModel(repo, Samples.requester, null)
        fill(vm)
        vm.submit()
        assertEquals("Sin conexión", vm.state.value.error)
        assertFalse(vm.state.value.saved)
    }

    @Test fun edit_loads_existing_and_updates() {
        coEvery { repo.getTicket(1) } returns Result.success(Loaded(Samples.ticket(1)))
        val slot = slot<UpdateTicketRequest>()
        coEvery { repo.updateTicket(1, capture(slot)) } returns Result.success(Samples.ticket(1))
        val vm = CreateEditTicketViewModel(repo, Samples.supervisor, 1)
        assertTrue(vm.isEditing)
        assertEquals("Impresora sin tinta", vm.state.value.title)
        assertEquals("Casa Matriz", vm.state.value.branch)
        vm.onTitleChange("Impresora sin tinta (editado)")
        vm.submit()
        assertTrue(vm.state.value.saved)
        assertEquals("Impresora sin tinta (editado)", slot.captured.title)
    }

    @Test fun edit_load_failure_sets_error() {
        coEvery { repo.getTicket(9) } returns Result.failure(ApiException("No se encontró el recurso solicitado", 404))
        val vm = CreateEditTicketViewModel(repo, Samples.supervisor, 9)
        assertEquals("No se encontró el recurso solicitado", vm.state.value.error)
    }
}
