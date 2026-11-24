package com.panalbit.panalsuite.ui.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panalbit.panalsuite.data.repository.TicketRepository
import com.panalbit.panalsuite.domain.Ticket
import com.panalbit.panalsuite.domain.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

const val ALL_STATUSES = "TODOS"
const val ALL_PRIORITIES = "TODAS"

data class TicketsUiState(
    val isLoading: Boolean = false,
    val allTickets: List<Ticket> = emptyList(),
    val query: String = "",
    val statusFilter: String = ALL_STATUSES,
    val priorityFilter: String = ALL_PRIORITIES,
    val offline: Boolean = false,
    val lastSyncedAt: Long? = null,
    val error: String? = null
) {
    val hasFilters: Boolean
        get() = query.isNotBlank() || statusFilter != ALL_STATUSES || priorityFilter != ALL_PRIORITIES

    /** Aplica los mismos filtros a los tickets en línea y a la copia local. */
    val visibleTickets: List<Ticket>
        get() = allTickets
            .filter { statusFilter == ALL_STATUSES || it.status == statusFilter }
            .filter { priorityFilter == ALL_PRIORITIES || it.priority == priorityFilter }
            .filter {
                query.isBlank() ||
                    it.title.contains(query, ignoreCase = true) ||
                    (it.ticketNumber ?: "").contains(query, ignoreCase = true) ||
                    it.category.contains(query, ignoreCase = true)
            }
}

class TicketsViewModel(
    private val ticketRepository: TicketRepository,
    private val currentTime: () -> Long = System::currentTimeMillis
) : ViewModel() {

    private val _state = MutableStateFlow(TicketsUiState())
    val state: StateFlow<TicketsUiState> = _state.asStateFlow()

    fun load(user: User) {
        if (_state.value.isLoading) return
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            ticketRepository.getTickets(user).fold(
                onSuccess = { loaded ->
                    val syncedAt = if (loaded.offline) null else currentTime()
                    _state.update {
                        it.copy(
                            isLoading = false,
                            allTickets = loaded.data,
                            offline = loaded.offline,
                            lastSyncedAt = syncedAt ?: it.lastSyncedAt
                        )
                    }
                },
                onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    fun onQueryChange(value: String) {
        _state.update { it.copy(query = value) }
    }

    fun onStatusFilter(value: String) {
        _state.update { it.copy(statusFilter = value) }
    }

    fun onPriorityFilter(value: String) {
        _state.update { it.copy(priorityFilter = value) }
    }

    fun clearFilters() {
        _state.update { it.copy(query = "", statusFilter = ALL_STATUSES, priorityFilter = ALL_PRIORITIES) }
    }
}
