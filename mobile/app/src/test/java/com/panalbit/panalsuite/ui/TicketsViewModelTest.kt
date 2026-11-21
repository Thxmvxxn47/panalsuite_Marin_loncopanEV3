package com.panalbit.panalsuite.ui

import com.panalbit.panalsuite.data.remote.ApiException
import com.panalbit.panalsuite.data.repository.TicketRepository
import com.panalbit.panalsuite.domain.Loaded
import com.panalbit.panalsuite.domain.Stats
import com.panalbit.panalsuite.testutil.MainDispatcherRule
import com.panalbit.panalsuite.testutil.Samples
import com.panalbit.panalsuite.ui.home.DashboardViewModel
import com.panalbit.panalsuite.ui.tickets.ALL_STATUSES
import com.panalbit.panalsuite.ui.tickets.TicketsViewModel
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TicketsViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()
    private val repo = mockk<TicketRepository>()

    private val tickets = listOf(
        Samples.ticket(1, "Impresora sin tinta", "NUEVO"),
        Samples.ticket(2, "VPN caída", "EN_PROGRESO", category = "INFRAESTRUCTURA"),
        Samples.ticket(3, "Cobro duplicado", "NUEVO", category = "COBRANZA")
    )

    @Test fun load_and_filter() {
        coEvery { repo.getTickets(Samples.supervisor) } returns Result.success(Loaded(tickets))
        val vm = TicketsViewModel(repo)
        vm.load(Samples.supervisor)
        assertEquals(3, vm.state.value.visibleTickets.size)

        vm.onStatusFilter("NUEVO")
        assertEquals(2, vm.state.value.visibleTickets.size)

        vm.onQueryChange("cobro")
        assertEquals(listOf(3L), vm.state.value.visibleTickets.map { it.id })

        vm.onStatusFilter(ALL_STATUSES)
        vm.onQueryChange("infraestructura")
        assertEquals(listOf(2L), vm.state.value.visibleTickets.map { it.id })

        vm.onQueryChange("tkt-2026-001")
        assertEquals(listOf(1L), vm.state.value.visibleTickets.map { it.id })
    }

    @Test fun offline_flag_and_error() {
        coEvery { repo.getTickets(any()) } returns Result.success(Loaded(tickets, offline = true))
        val vm = TicketsViewModel(repo)
        vm.load(Samples.admin)
        assertTrue(vm.state.value.offline)

        coEvery { repo.getTickets(any()) } returns Result.failure(ApiException("fallo"))
        vm.load(Samples.admin)
        assertEquals("fallo", vm.state.value.error)
        assertFalse(vm.state.value.isLoading)
    }

    @Test fun keeps_last_sync_while_offline_and_updates_it_on_retry() {
        var now = 1000L
        val vm = TicketsViewModel(repo) { now }
        coEvery { repo.getTickets(Samples.admin) } returns Result.success(Loaded(tickets))
        vm.load(Samples.admin)
        assertEquals(1000L, vm.state.value.lastSyncedAt)

        now = 2000L
        coEvery { repo.getTickets(Samples.admin) } returns Result.success(Loaded(tickets, offline = true))
        vm.load(Samples.admin)
        assertTrue(vm.state.value.offline)
        assertEquals(tickets, vm.state.value.allTickets)
        assertEquals(1000L, vm.state.value.lastSyncedAt)

        now = 3000L
        coEvery { repo.getTickets(Samples.admin) } returns Result.success(Loaded(tickets))
        vm.load(Samples.admin)
        assertFalse(vm.state.value.offline)
        assertEquals(3000L, vm.state.value.lastSyncedAt)
    }

    @Test fun dashboard_loads_stats_and_recent_limit() {
        val many = (1L..8L).map { Samples.ticket(it) }
        val stats = Stats(8, 3, 2, 1, 1, 1, 1, 2, 0)
        coEvery { repo.getStats(2L) } returns Result.success(stats)
        coEvery { repo.getTickets(Samples.resolver) } returns Result.success(Loaded(many))
        val vm = DashboardViewModel(repo)
        vm.load(Samples.resolver)
        assertEquals(stats, vm.state.value.stats)
        assertEquals(5, vm.state.value.recentTickets.size)
    }

    @Test fun dashboard_error_and_keeps_previous_data() {
        coEvery { repo.getStats(null) } returns Result.failure(ApiException("sin stats"))
        coEvery { repo.getTickets(Samples.supervisor) } returns Result.success(Loaded(tickets, offline = true))
        val vm = DashboardViewModel(repo)
        vm.load(Samples.supervisor)
        assertEquals("sin stats", vm.state.value.error)
        assertTrue(vm.state.value.offline)
        assertNotNull(vm.state.value.recentTickets)
    }
}
