package com.panalbit.panalsuite.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panalbit.panalsuite.data.repository.AuthRepository
import com.panalbit.panalsuite.domain.User
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SessionState {
    data object Loading : SessionState
    data object LoggedOut : SessionState
    data class LoggedIn(val user: User) : SessionState
}

/** Mantiene la sesion (DataStore) para decidir entre login y pantalla principal. */
class AppViewModel(private val authRepository: AuthRepository) : ViewModel() {

    val sessionState: StateFlow<SessionState> = authRepository.session
        .map { session -> if (session == null) SessionState.LoggedOut else SessionState.LoggedIn(session.user) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, SessionState.Loading)

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}
