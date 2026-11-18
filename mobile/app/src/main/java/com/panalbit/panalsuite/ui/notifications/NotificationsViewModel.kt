package com.panalbit.panalsuite.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panalbit.panalsuite.data.repository.TicketRepository
import com.panalbit.panalsuite.domain.AppNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NotificationsUiState(
    val isLoading: Boolean = false,
    val items: List<AppNotification> = emptyList(),
    val error: String? = null
) {
    val unreadCount: Int get() = items.count { !it.read }
}

class NotificationsViewModel(private val ticketRepository: TicketRepository) : ViewModel() {

    private val _state = MutableStateFlow(NotificationsUiState())
    val state: StateFlow<NotificationsUiState> = _state.asStateFlow()

    fun load(userId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            ticketRepository.getNotifications(userId).fold(
                onSuccess = { list -> _state.update { it.copy(isLoading = false, items = list) } },
                onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    fun markRead(id: Long) {
        viewModelScope.launch {
            ticketRepository.markNotificationRead(id).onSuccess {
                _state.update { state ->
                    state.copy(items = state.items.map { n -> if (n.id == id) n.copy(read = true) else n })
                }
            }
        }
    }
}
