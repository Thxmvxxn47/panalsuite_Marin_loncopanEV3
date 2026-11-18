package com.panalbit.panalsuite.ui

import com.panalbit.panalsuite.data.remote.ApiException
import com.panalbit.panalsuite.data.repository.IndicatorRepository
import com.panalbit.panalsuite.data.repository.TicketRepository
import com.panalbit.panalsuite.domain.AppNotification
import com.panalbit.panalsuite.domain.Indicator
import com.panalbit.panalsuite.testutil.MainDispatcherRule
import com.panalbit.panalsuite.ui.indicators.IndicatorsViewModel
import com.panalbit.panalsuite.ui.notifications.NotificationsViewModel
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class NotificationsIndicatorsViewModelTest {
    @get:Rule val mainRule = MainDispatcherRule()

    private fun n(id: Long, read: Boolean) =
        AppNotification(id, 1, "Título", "Mensaje", "ASIGNADO", 1, "TKT-2026-001", read, "2026-11-17T10:00:00")

    @Test fun notifications_load_unread_and_mark_read() {
        val repo = mockk<TicketRepository>()
        coEvery { repo.getNotifications(1) } returns Result.success(listOf(n(1, false), n(2, true)))
        val vm = NotificationsViewModel(repo)
        vm.load(1)
        assertEquals(1, vm.state.value.unreadCount)

        coEvery { repo.markNotificationRead(1) } returns Result.success(n(1, true))
        vm.markRead(1)
        assertEquals(0, vm.state.value.unreadCount)
    }

    @Test fun notifications_mark_read_failure_keeps_state() {
        val repo = mockk<TicketRepository>()
        coEvery { repo.getNotifications(1) } returns Result.success(listOf(n(1, false)))
        coEvery { repo.markNotificationRead(1) } returns Result.failure(ApiException("x"))
        val vm = NotificationsViewModel(repo)
        vm.load(1)
        vm.markRead(1)
        assertEquals(1, vm.state.value.unreadCount)
    }

    @Test fun notifications_error() {
        val repo = mockk<TicketRepository>()
        coEvery { repo.getNotifications(1) } returns Result.failure(ApiException("sin avisos"))
        val vm = NotificationsViewModel(repo)
        vm.load(1)
        assertEquals("sin avisos", vm.state.value.error)
    }

    @Test fun indicators_success_and_error() {
        val repo = mockk<IndicatorRepository>()
        val item = Indicator("uf", "UF", "Pesos", "2026-11-17", 39000.0)
        coEvery { repo.getIndicators() } returns Result.success(listOf(item))
        val vm = IndicatorsViewModel(repo)
        vm.load()
        assertEquals(listOf(item), vm.state.value.items)

        coEvery { repo.getIndicators() } returns Result.failure(ApiException("api caída"))
        vm.load()
        assertEquals("api caída", vm.state.value.error)
        assertTrue(!vm.state.value.isLoading)
    }
}
