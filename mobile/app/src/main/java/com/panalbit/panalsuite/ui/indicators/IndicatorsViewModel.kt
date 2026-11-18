package com.panalbit.panalsuite.ui.indicators

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.panalbit.panalsuite.data.repository.IndicatorRepository
import com.panalbit.panalsuite.domain.Indicator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IndicatorsUiState(
    val isLoading: Boolean = false,
    val items: List<Indicator> = emptyList(),
    val error: String? = null
)

class IndicatorsViewModel(private val repository: IndicatorRepository) : ViewModel() {

    private val _state = MutableStateFlow(IndicatorsUiState())
    val state: StateFlow<IndicatorsUiState> = _state.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getIndicators().fold(
                onSuccess = { list -> _state.update { it.copy(isLoading = false, items = list) } },
                onFailure = { e -> _state.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }
}
