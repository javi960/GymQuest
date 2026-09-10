package com.gymquest.app.feature.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymquest.app.domain.model.ProgressSummary
import com.gymquest.app.domain.usecase.progress.ObserveProgressSummaryUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

data class ProgressUiState(
    val summary: ProgressSummary? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

class ProgressViewModel(private val observeProgressSummary: ObserveProgressSummaryUseCase) : ViewModel() {
    private val refreshRequests = MutableStateFlow(0)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ProgressUiState> = refreshRequests.flatMapLatest {
        observeProgressSummary()
            .map { ProgressUiState(summary = it, isLoading = false) }
            .onStart { emit(ProgressUiState()) }
            .catch { emit(ProgressUiState(isLoading = false, errorMessage = "No se pudo cargar el progreso.")) }
    }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ProgressUiState())

    fun retry() {
        refreshRequests.value += 1
    }
}
