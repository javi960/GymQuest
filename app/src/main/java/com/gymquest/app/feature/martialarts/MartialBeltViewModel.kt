package com.gymquest.app.feature.martialarts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.MartialBelt
import com.gymquest.app.domain.usecase.martial.GetCurrentMartialBeltUseCase
import com.gymquest.app.domain.usecase.martial.SetCurrentMartialBeltUseCase
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MartialBeltUiState(
    val belt: MartialBelt = MartialBelt.WHITE,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

class MartialBeltViewModel(
    private val getCurrentBelt: GetCurrentMartialBeltUseCase,
    private val setCurrentBelt: SetCurrentMartialBeltUseCase,
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(MartialBeltUiState())
    val uiState: StateFlow<MartialBeltUiState> = mutableUiState.asStateFlow()

    init { load() }

    private fun load() = viewModelScope.launch {
        when (val result = getCurrentBelt()) {
            is AppResult.Success -> mutableUiState.value = MartialBeltUiState(belt = result.value, isLoading = false)
            is AppResult.Failure -> mutableUiState.value = MartialBeltUiState(isLoading = false, errorMessage = "No se pudo cargar el cinturón actual.")
        }
    }

    fun confirmBelt(belt: MartialBelt) = viewModelScope.launch {
        mutableUiState.value = mutableUiState.value.copy(isSaving = true, errorMessage = null)
        when (setCurrentBelt(belt, Instant.now())) {
            is AppResult.Success -> mutableUiState.value = MartialBeltUiState(belt = belt, isLoading = false)
            is AppResult.Failure -> mutableUiState.value = mutableUiState.value.copy(isSaving = false, errorMessage = "No se pudo guardar el cinturón.")
        }
    }
}
