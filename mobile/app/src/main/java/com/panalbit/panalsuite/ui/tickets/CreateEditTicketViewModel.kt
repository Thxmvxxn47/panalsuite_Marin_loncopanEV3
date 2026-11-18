package com.panalbit.panalsuite.ui.tickets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panalbit.panalsuite.data.repository.TicketRepository
import com.panalbit.panalsuite.domain.CreateTicketRequest
import com.panalbit.panalsuite.domain.UpdateTicketRequest
import com.panalbit.panalsuite.domain.User
import com.panalbit.panalsuite.util.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TicketFormState(
    val title: String = "",
    val description: String = "",
    val type: String = "INCIDENTE",
    val category: String = "TI",
    val priority: String = "MEDIA",
    val branch: String = "",
    val titleError: String? = null,
    val descriptionError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

/** Formulario de crear (ticketId == null) o editar un ticket. */
class CreateEditTicketViewModel(
    private val ticketRepository: TicketRepository,
    private val user: User,
    private val ticketId: Long?
) : ViewModel() {

    private val _state = MutableStateFlow(TicketFormState())
    val state: StateFlow<TicketFormState> = _state.asStateFlow()

    val isEditing: Boolean get() = ticketId != null

    init {
        if (ticketId != null) loadExisting(ticketId)
    }

    private fun loadExisting(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            ticketRepository.getTicket(id).fold(
                onSuccess = { loaded ->
                    val t = loaded.data
                    _state.update {
                        it.copy(
                            isLoading = false, title = t.title, description = t.description, type = t.type,
                            category = t.category, priority = t.priority, branch = t.branch ?: ""
                        )
                    }
                },
                onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    fun onTitleChange(value: String) = _state.update { it.copy(title = value, titleError = null) }
    fun onDescriptionChange(value: String) = _state.update { it.copy(description = value, descriptionError = null) }
    fun onTypeChange(value: String) = _state.update { it.copy(type = value) }
    fun onCategoryChange(value: String) = _state.update { it.copy(category = value) }
    fun onPriorityChange(value: String) = _state.update { it.copy(priority = value) }
    fun onBranchChange(value: String) = _state.update { it.copy(branch = value) }

    fun submit() {
        val form = _state.value
        if (form.isLoading) return
        val titleError = Validators.validateTitle(form.title)
        val descriptionError = Validators.validateDescription(form.description)
        if (titleError != null || descriptionError != null) {
            _state.update { it.copy(titleError = titleError, descriptionError = descriptionError) }
            return
        }
        val branch = form.branch.ifBlank { null }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            val result = if (ticketId == null) {
                ticketRepository.createTicket(
                    CreateTicketRequest(
                        form.title.trim(), form.description.trim(), form.type, form.category,
                        form.priority, branch, user.id, user.name
                    )
                )
            } else {
                ticketRepository.updateTicket(
                    ticketId,
                    UpdateTicketRequest(
                        form.title.trim(), form.description.trim(), form.type, form.category, form.priority, branch
                    )
                )
            }
            result.fold(
                onSuccess = { _state.update { it.copy(isLoading = false, saved = true) } },
                onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }
}
