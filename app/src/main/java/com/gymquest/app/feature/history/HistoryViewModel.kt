package com.gymquest.app.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymquest.app.domain.model.WorkoutSession
import com.gymquest.app.domain.model.WorkoutSessionDetail
import com.gymquest.app.domain.usecase.session.ObserveSessionDetailUseCase
import com.gymquest.app.domain.usecase.session.ObserveSessionHistoryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn

data class HistoryUiState(
    val sessions: List<WorkoutSession> = emptyList(),
    val selectedSessionId: Long? = null,
    val selectedSessionDetail: WorkoutSessionDetail? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = true,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    observeSessionHistory: ObserveSessionHistoryUseCase,
    observeSessionDetail: ObserveSessionDetailUseCase,
) : ViewModel() {
    private val selectedSessionId = MutableStateFlow<Long?>(null)
    private val refreshRequests = MutableStateFlow(0)

    val uiState: StateFlow<HistoryUiState> = refreshRequests.flatMapLatest {
        val selectedSessionDetail = selectedSessionId.flatMapLatest { sessionId ->
            if (sessionId == null) flowOf(null) else observeSessionDetail(sessionId)
        }
        combine(
            observeSessionHistory(),
            selectedSessionId,
            selectedSessionDetail,
        ) { sessions, selectedId, detail ->
            HistoryUiState(
                sessions = sessions,
                selectedSessionId = selectedId,
                selectedSessionDetail = detail,
                isLoading = false,
            )
        }.onStart { emit(HistoryUiState()) }
            .catch { emit(HistoryUiState(isLoading = false, errorMessage = "No se pudo cargar el historial.")) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
            initialValue = HistoryUiState(),
    )

    fun selectSession(sessionId: Long) {
        selectedSessionId.value = sessionId
    }

    fun retry() {
        refreshRequests.value += 1
    }
}
