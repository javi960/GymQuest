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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

data class HistoryUiState(
    val sessions: List<WorkoutSession> = emptyList(),
    val selectedSessionId: Long? = null,
    val selectedSessionDetail: WorkoutSessionDetail? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    observeSessionHistory: ObserveSessionHistoryUseCase,
    observeSessionDetail: ObserveSessionDetailUseCase,
) : ViewModel() {
    private val selectedSessionId = MutableStateFlow<Long?>(null)
    private val selectedSessionDetail = selectedSessionId.flatMapLatest { sessionId ->
        if (sessionId == null) flowOf(null) else observeSessionDetail(sessionId)
    }

    val uiState: StateFlow<HistoryUiState> = combine(
        observeSessionHistory(),
        selectedSessionId,
        selectedSessionDetail,
    ) { sessions, selectedId, detail ->
        HistoryUiState(
            sessions = sessions,
            selectedSessionId = selectedId,
            selectedSessionDetail = detail,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HistoryUiState(),
    )

    fun selectSession(sessionId: Long) {
        selectedSessionId.value = sessionId
    }
}
