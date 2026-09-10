package com.gymquest.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymquest.app.domain.model.ProgressSummary
import com.gymquest.app.domain.model.WorkoutSessionDetail
import com.gymquest.app.domain.usecase.progress.ObserveProgressSummaryUseCase
import com.gymquest.app.domain.usecase.session.ObserveActiveSessionUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow

data class HomeUiState(
    val summary: ProgressSummary? = null,
    val activeSession: WorkoutSessionDetail? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val hasActiveSession: Boolean
        get() = activeSession != null
}

sealed interface HomeAction {
    data object OpenSession : HomeAction
}

class HomeViewModel(
    observeProgressSummary: ObserveProgressSummaryUseCase,
    observeActiveSession: ObserveActiveSessionUseCase,
) : ViewModel() {
    private val refreshRequests = MutableStateFlow(0)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = refreshRequests.flatMapLatest {
        combine(
            observeProgressSummary(),
            observeActiveSession(),
        ) { summary, activeSession ->
            HomeUiState(summary = summary, activeSession = activeSession, isLoading = false)
        }.onStart { emit(HomeUiState()) }
            .catch { emit(HomeUiState(isLoading = false, errorMessage = "No se pudo actualizar el inicio.")) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HomeUiState(),
    )

    fun retry() {
        refreshRequests.value += 1
    }
}
