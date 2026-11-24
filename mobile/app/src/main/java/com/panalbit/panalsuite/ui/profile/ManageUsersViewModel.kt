package com.panalbit.panalsuite.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panalbit.panalsuite.data.repository.AuthRepository
import com.panalbit.panalsuite.domain.CreateUserRequest
import com.panalbit.panalsuite.domain.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ManageUsersState(
    val users: List<User> = emptyList(),
    val loading: Boolean = false,
    val saving: Boolean = false,
    val showForm: Boolean = false,
    val listError: String? = null,
    val formError: String? = null,
    val createdUsername: String? = null
)

class ManageUsersViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(ManageUsersState())
    val state = _state.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (_state.value.loading || _state.value.saving) return
        _state.update { it.copy(loading = true, listError = null) }
        viewModelScope.launch {
            repository.getUsers().fold(
                onSuccess = { users -> _state.update { it.copy(loading = false, users = users) } },
                onFailure = { error -> _state.update { it.copy(loading = false, listError = error.message) } }
            )
        }
    }

    fun setFormVisible(visible: Boolean) {
        if (_state.value.saving) return
        _state.update { it.copy(showForm = visible, formError = null, createdUsername = null) }
    }

    fun create(request: CreateUserRequest) {
        if (_state.value.saving || _state.value.loading) return
        _state.update { it.copy(saving = true, formError = null) }
        viewModelScope.launch {
            repository.createUser(request).fold(
                onSuccess = { user ->
                    _state.update {
                        it.copy(
                            saving = false,
                            showForm = false,
                            users = (it.users + user).distinctBy(User::id),
                            createdUsername = user.username
                        )
                    }
                },
                onFailure = { error -> _state.update { it.copy(saving = false, formError = error.message) } }
            )
        }
    }
}
