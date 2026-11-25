package com.panalbit.panalsuite.ui.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panalbit.panalsuite.data.repository.AuthRepository
import com.panalbit.panalsuite.data.repository.TicketRepository
import com.panalbit.panalsuite.domain.Comment
import com.panalbit.panalsuite.domain.CommentRequest
import com.panalbit.panalsuite.domain.Evidence
import com.panalbit.panalsuite.domain.StatusChangeRequest
import com.panalbit.panalsuite.domain.Ticket
import com.panalbit.panalsuite.domain.TicketRules
import com.panalbit.panalsuite.domain.Roles
import com.panalbit.panalsuite.domain.User
import com.panalbit.panalsuite.util.Validators
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TicketDetailUiState(
    val isLoading: Boolean = false,
    val isWorking: Boolean = false,
    val ticket: Ticket? = null,
    val comments: List<Comment> = emptyList(),
    val evidence: List<Evidence> = emptyList(),
    val actions: List<String> = emptyList(),
    val resolvers: List<User> = emptyList(),
    val offline: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val commentError: String? = null,
    val commentSentCount: Int = 0,
    val deleted: Boolean = false
)

class TicketDetailViewModel(
    private val ticketRepository: TicketRepository,
    private val authRepository: AuthRepository,
    private val ticketId: Long,
    private val user: User
) : ViewModel() {

    private val _state = MutableStateFlow(TicketDetailUiState())
    val state: StateFlow<TicketDetailUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            ticketRepository.getTicket(ticketId).fold(
                onSuccess = { loaded ->
                    _state.update {
                        it.copy(
                            isLoading = false,
                            ticket = loaded.data,
                            offline = loaded.offline,
                            actions = TicketRules.availableActions(loaded.data.status, user.role)
                        )
                    }
                    loadComments()
                    loadEvidence()
                },
                onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    private suspend fun loadComments() {
        ticketRepository.getComments(ticketId).onSuccess { list ->
            val visible = if (user.role == Roles.REQUESTER) list.filter { !it.internal } else list
            _state.update { it.copy(comments = visible) }
        }
    }

    private suspend fun loadEvidence(): Boolean {
        return try {
            val items = ticketRepository.getEvidence(ticketId)
            _state.update { it.copy(evidence = items) }
            true
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            _state.update { it.copy(message = "No se pudo cargar la evidencia local") }
            false
        }
    }

    fun loadResolvers() {
        if (_state.value.resolvers.isNotEmpty()) return
        viewModelScope.launch {
            authRepository.getResolvers().fold(
                onSuccess = { list -> _state.update { it.copy(resolvers = list) } },
                onFailure = { e -> _state.update { it.copy(message = e.message) } }
            )
        }
    }

    fun addComment(text: String, internal: Boolean) {
        val error = Validators.validateComment(text)
        if (error != null) {
            _state.update { it.copy(commentError = error) }
            return
        }
        if (!TicketRules.canComment(user.role)) return
        val isInternal = internal && TicketRules.canWriteInternalComments(user.role)
        viewModelScope.launch {
            _state.update { it.copy(isWorking = true, commentError = null) }
            ticketRepository.addComment(ticketId, CommentRequest(user.id, user.name, text.trim(), isInternal)).fold(
                onSuccess = {
                    loadComments()
                    _state.update { s ->
                        s.copy(isWorking = false, message = "Comentario agregado", commentSentCount = s.commentSentCount + 1)
                    }
                },
                onFailure = { e -> _state.update { it.copy(isWorking = false, message = e.message) } }
            )
        }
    }

    /** Ejecuta una accion del flujo (tomar, resolver, asignar...). Para asignar se debe indicar el resolutor. */
    fun executeAction(action: String, assignee: User? = null) {
        val newStatus = TicketRules.statusFor(action) ?: return
        val target = when {
            action == TicketRules.TAKE -> user
            TicketRules.requiresAssignee(action) -> assignee
            else -> null
        }
        if (TicketRules.requiresAssignee(action) && target == null) {
            _state.update { it.copy(message = "Selecciona un resolutor") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isWorking = true) }
            val request = StatusChangeRequest(newStatus, target?.id, target?.name, user.id, user.name)
            ticketRepository.changeStatus(ticketId, request).fold(
                onSuccess = { updated ->
                    _state.update {
                        it.copy(
                            isWorking = false,
                            ticket = updated,
                            actions = TicketRules.availableActions(updated.status, user.role),
                            message = "Acción ejecutada correctamente"
                        )
                    }
                },
                onFailure = { e -> _state.update { it.copy(isWorking = false, message = e.message) } }
            )
        }
    }

    fun deleteTicket() {
        if (!TicketRules.canDelete(user.role)) return
        viewModelScope.launch {
            _state.update { it.copy(isWorking = true) }
            ticketRepository.deleteTicket(ticketId).fold(
                onSuccess = { _state.update { it.copy(isWorking = false, deleted = true) } },
                onFailure = { e -> _state.update { it.copy(isWorking = false, message = e.message) } }
            )
        }
    }

    fun addEvidence(filePath: String) {
        if (filePath.isBlank()) {
            _state.update { it.copy(message = "Selecciona una foto para adjuntar") }
            return
        }
        viewModelScope.launch {
            try {
                ticketRepository.addEvidence(ticketId, filePath)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                _state.update { it.copy(message = "No se pudo guardar la evidencia") }
                return@launch
            }
            if (loadEvidence()) {
                _state.update { it.copy(message = "Evidencia guardada en el dispositivo") }
            } else {
                _state.update { it.copy(message = "La evidencia se guardó, pero no se pudo actualizar la lista") }
            }
        }
    }

    fun messageShown() {
        _state.update { it.copy(message = null) }
    }
}
