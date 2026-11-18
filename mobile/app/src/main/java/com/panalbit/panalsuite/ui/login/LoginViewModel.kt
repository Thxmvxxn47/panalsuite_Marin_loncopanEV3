package com.panalbit.panalsuite.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panalbit.panalsuite.data.repository.AuthRepository
import com.panalbit.panalsuite.util.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val username: String = "",
    val password: String = "",
    val usernameError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val loggedIn: Boolean = false
)

class LoginViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun onUsernameChange(value: String) {
        _state.update { it.copy(username = value, usernameError = null, error = null) }
    }

    fun onPasswordChange(value: String) {
        _state.update { it.copy(password = value, passwordError = null, error = null) }
    }

    fun submit() {
        val current = _state.value
        if (current.isLoading) return
        val usernameError = Validators.validateUsername(current.username)
        val passwordError = Validators.validatePassword(current.password)
        if (usernameError != null || passwordError != null) {
            _state.update { it.copy(usernameError = usernameError, passwordError = passwordError) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            authRepository.login(current.username, current.password).fold(
                onSuccess = { _state.update { s -> s.copy(isLoading = false, loggedIn = true) } },
                onFailure = { e -> _state.update { s -> s.copy(isLoading = false, error = e.message) } }
            )
        }
    }
}
