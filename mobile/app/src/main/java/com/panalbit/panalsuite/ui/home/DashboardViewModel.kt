package com.panalbit.panalsuite.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panalbit.panalsuite.data.repository.TicketRepository
import com.panalbit.panalsuite.domain.Roles
import com.panalbit.panalsuite.domain.Stats
import com.panalbit.panalsuite.domain.Ticket
import com.panalbit.panalsuite.domain.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = false,
    val stats: Stats? = null,
    val recentTickets: List<Ticket> = emptyList(),
    val offline: Boolean = false,
    val error: String? = null
)

class DashboardViewModel(private val ticketRepository: TicketRepository) : ViewModel() {

    private val _state = MutableStateFlow(DashboardUiState())
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    fun load(user: User) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val assignee = if (user.role == Roles.RESOLVER) user.id else null
            val stats = ticketRepository.getStats(assignee)
            val tickets = ticketRepository.getTickets(user)

            _state.update { current ->
                current.copy(
                    isLoading = false,
                    stats = stats.getOrNull() ?: current.stats,
                    recentTickets = tickets.getOrNull()?.data?.take(RECENT_LIMIT) ?: current.recentTickets,
                    offline = tickets.getOrNull()?.offline ?: false,
                    error = stats.exceptionOrNull()?.message ?: tickets.exceptionOrNull()?.message
                )
            }
        }
    }

    private companion object {
        const val RECENT_LIMIT = 5
    }
}
