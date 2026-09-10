package com.gymquest.app.feature.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymquest.app.core.result.AppResult
import com.gymquest.app.core.time.ClockProvider
import com.gymquest.app.domain.model.ExerciseVariant
import com.gymquest.app.domain.model.WorkoutExercise
import com.gymquest.app.domain.model.WorkoutSession
import com.gymquest.app.domain.model.WorkoutSessionDetail
import com.gymquest.app.domain.model.WorkoutSet
import com.gymquest.app.domain.model.enums.SetType
import com.gymquest.app.domain.usecase.catalog.ObserveActiveExerciseVariantsUseCase
import com.gymquest.app.domain.usecase.session.AddExerciseToSessionUseCase
import com.gymquest.app.domain.usecase.session.CancelWorkoutSessionUseCase
import com.gymquest.app.domain.usecase.session.CompleteWorkoutSessionUseCase
import com.gymquest.app.domain.usecase.session.DeleteWorkoutSetUseCase
import com.gymquest.app.domain.usecase.session.ObserveActiveSessionUseCase
import com.gymquest.app.domain.usecase.session.ResolveRestBeforeNextSetUseCase
import com.gymquest.app.domain.usecase.session.SaveWorkoutSetUseCase
import com.gymquest.app.domain.usecase.session.StartRestAfterSetUseCase
import com.gymquest.app.domain.usecase.session.StartWorkoutSessionUseCase
import com.gymquest.app.domain.usecase.session.UpdateWorkoutSetUseCase
import com.gymquest.app.domain.usecase.progress.ApplyWorkoutProgressUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.catch

enum class SessionOperation { None, Completing, Cancelling }

enum class SessionOutcome { None, Completed, Cancelled }

sealed interface SetSaveState {
    data object Idle : SetSaveState
    data object Saving : SetSaveState
    data object Saved : SetSaveState
    data class Failed(val message: String) : SetSaveState
}

data class SessionUiState(
    val activeSession: WorkoutSessionDetail? = null,
    val variants: List<ExerciseVariant> = emptyList(),
    val selectedVariantId: Long? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = true,
    val operation: SessionOperation = SessionOperation.None,
    val outcome: SessionOutcome = SessionOutcome.None,
    val manualRestTimers: Map<Long, ManualRestTimerState> = emptyMap(),
    val manualRestErrors: Map<Long, String> = emptyMap(),
    val setSaveStates: Map<Long, SetSaveState> = emptyMap(),
)

sealed interface SessionAction {
    data object StartSession : SessionAction
    data object AddSelectedVariant : SessionAction
    data object CompleteSession : SessionAction
    data object CancelSession : SessionAction
    data object DismissOutcome : SessionAction
    data class SelectVariant(val variantId: Long) : SessionAction
    data class SaveSet(
        val workoutExerciseId: Long,
        val weightInput: String,
        val repsInput: String,
        val setType: SetType,
    ) : SessionAction
    data class UpdateSet(
        val set: WorkoutSet,
        val weightInput: String,
        val repsInput: String,
        val setType: SetType,
    ) : SessionAction
    data class DeleteSet(val setId: Long) : SessionAction
    data class StartRest(val workoutExerciseId: Long) : SessionAction
    data class PauseRest(val workoutExerciseId: Long) : SessionAction
    data class ResumeRest(val workoutExerciseId: Long) : SessionAction
    data class AdvanceRest(val workoutExerciseId: Long) : SessionAction
    data class ChangeRestTarget(val workoutExerciseId: Long, val targetInput: String) : SessionAction
}

sealed interface SessionEffect {
    data class ShowMessage(val message: String) : SessionEffect
}

class SessionViewModel(
    private val observeActiveSession: ObserveActiveSessionUseCase,
    private val observeActiveExerciseVariants: ObserveActiveExerciseVariantsUseCase,
    private val startWorkoutSession: StartWorkoutSessionUseCase,
    private val cancelWorkoutSession: CancelWorkoutSessionUseCase,
    private val completeWorkoutSession: CompleteWorkoutSessionUseCase,
    private val addExerciseToSession: AddExerciseToSessionUseCase,
    private val saveWorkoutSet: SaveWorkoutSetUseCase,
    private val updateWorkoutSet: UpdateWorkoutSetUseCase,
    private val deleteWorkoutSet: DeleteWorkoutSetUseCase,
    private val startRestAfterSet: StartRestAfterSetUseCase,
    private val resolveRestBeforeNextSet: ResolveRestBeforeNextSetUseCase,
    private val applyWorkoutProgress: ApplyWorkoutProgressUseCase,
    private val clock: ClockProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<SessionEffect>()
    val effects: SharedFlow<SessionEffect> = _effects.asSharedFlow()
    private var observationJob: Job? = null
    private var activeSessionLoadFailed = false
    private var variantsLoadFailed = false
    private var activeSessionLoaded = false
    private var variantsLoaded = false

    init {
        observeData()
    }

    fun retry() {
        observeData()
    }

    private fun observeData() {
        observationJob?.cancel()
        activeSessionLoaded = false
        variantsLoaded = false
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        observationJob = viewModelScope.launch {
            launch {
                observeActiveSession()
                    .catch {
                        activeSessionLoadFailed = true
                        activeSessionLoaded = true
                        _uiState.update { it.copy(isLoading = false, errorMessage = "No se pudo cargar la sesion activa.") }
                    }
                    .collect { session ->
                        activeSessionLoadFailed = false
                        activeSessionLoaded = true
                        _uiState.update {
                            it.copy(
                                activeSession = session,
                                errorMessage = loadErrorMessage(),
                                isLoading = !(activeSessionLoaded && variantsLoaded),
                            )
                        }
                    }
            }
            launch {
                observeActiveExerciseVariants()
                    .catch {
                        variantsLoadFailed = true
                        variantsLoaded = true
                        _uiState.update { it.copy(isLoading = false, errorMessage = "No se pudo cargar el catalogo de ejercicios.") }
                    }
                    .collect { variants ->
                        variantsLoadFailed = false
                        variantsLoaded = true
                        _uiState.update { state ->
                            state.copy(
                                variants = variants,
                                selectedVariantId = state.selectedVariantId?.takeIf { selectedId ->
                                    variants.any { it.id == selectedId }
                                } ?: variants.firstOrNull()?.id,
                                errorMessage = loadErrorMessage(),
                                isLoading = !(activeSessionLoaded && variantsLoaded),
                            )
                        }
                    }
            }
        }
    }

    private fun loadErrorMessage(): String? = when {
        activeSessionLoadFailed -> "No se pudo cargar la sesion activa."
        variantsLoadFailed -> "No se pudo cargar el catalogo de ejercicios."
        else -> null
    }

    fun onAction(action: SessionAction) {
        when (action) {
            SessionAction.AddSelectedVariant -> addSelectedVariantToSession()
            SessionAction.CancelSession -> cancelSession()
            SessionAction.CompleteSession -> completeSession()
            SessionAction.DismissOutcome -> _uiState.update { it.copy(outcome = SessionOutcome.None) }
            is SessionAction.DeleteSet -> deleteSet(action.setId)
            is SessionAction.SaveSet -> saveSet(
                action.workoutExerciseId,
                action.weightInput,
                action.repsInput,
                action.setType,
            )
            is SessionAction.SelectVariant -> selectVariant(action.variantId)
            SessionAction.StartSession -> startSession()
            is SessionAction.UpdateSet -> updateSet(action.set, action.weightInput, action.repsInput, action.setType)
            is SessionAction.StartRest -> restartRest(action.workoutExerciseId)
            is SessionAction.PauseRest -> updateRestTimer(action.workoutExerciseId) { it.pause() }
            is SessionAction.ResumeRest -> updateRestTimer(action.workoutExerciseId) { it.resume() }
            is SessionAction.AdvanceRest -> updateRestTimer(action.workoutExerciseId) { it.advanceBy(1) }
            is SessionAction.ChangeRestTarget -> changeRestTarget(action.workoutExerciseId, action.targetInput)
        }
    }

    fun startSession() {
        _uiState.update { it.copy(outcome = SessionOutcome.None) }
        viewModelScope.launch {
            val now = clock.now()
            val result = startWorkoutSession(
                WorkoutSession(startedAt = now, createdAt = now, updatedAt = now),
            )
            setFeedback(result, "Sesion iniciada.")
        }
    }

    fun selectVariant(variantId: Long) {
        _uiState.update { it.copy(selectedVariantId = variantId) }
    }

    fun addSelectedVariantToSession() {
        val state = _uiState.value
        val session = state.activeSession?.session
        val variantId = state.selectedVariantId
        if (session == null) {
            sendMessage("Inicia una sesion antes de anadir ejercicios.")
            return
        }
        if (variantId == null) {
            sendMessage("Crea una variante en el catalogo antes de anadir ejercicios.")
            return
        }
        viewModelScope.launch {
            val now = clock.now()
            val result = addExerciseToSession(
                WorkoutExercise(
                    workoutSessionId = session.id,
                    exerciseVariantId = variantId,
                    orderIndex = state.activeSession.exercises.size,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
            setFeedback(result, "Ejercicio anadido a la sesion.")
        }
    }

    fun saveSet(workoutExerciseId: Long, weightInput: String, repsInput: String, setType: SetType) {
        if (_uiState.value.setSaveStates[workoutExerciseId] is SetSaveState.Saving) return
        val weightValue = weightInput.replace(',', '.').toDoubleOrNull()
        val reps = repsInput.toIntOrNull()
        if (weightValue == null || reps == null) {
            sendMessage("Introduce un peso y repeticiones validos.")
            return
        }
        val exercise = _uiState.value.activeSession
            ?.exercises
            ?.firstOrNull { it.workoutExercise.id == workoutExerciseId }
        if (exercise == null) {
            updateSetSaveState(workoutExerciseId, SetSaveState.Failed("El ejercicio de sesion no existe."))
            return
        }
        updateSetSaveState(workoutExerciseId, SetSaveState.Saving)
        viewModelScope.launch {
            val now = clock.now()
            val restResolution = resolveRestBeforeNextSet(exercise.sets.lastOrNull(), now)
            if (restResolution != null) {
                val updateResult = updateWorkoutSet(restResolution.previousSet)
                if (updateResult is AppResult.Failure) {
                    updateSetSaveState(workoutExerciseId, SetSaveState.Failed(updateResult.error.message))
                    setFeedback(updateResult, "Descanso actualizado.")
                    return@launch
                }
            }
            val result = saveWorkoutSet(
                startRestAfterSet(
                    WorkoutSet(
                        workoutExerciseId = workoutExerciseId,
                        setNumber = (exercise.sets.maxOfOrNull { it.setNumber } ?: 0) + 1,
                        weightValue = weightValue,
                        reps = reps,
                        setType = setType,
                        startedAt = now,
                        restBeforeSeconds = restResolution?.restBeforeSeconds,
                        createdAt = now,
                        updatedAt = now,
                    ),
                    savedAt = now,
                ),
            )
            if (result is AppResult.Success) {
                restartRest(workoutExerciseId)
                updateSetSaveState(workoutExerciseId, SetSaveState.Saved)
            } else {
                updateSetSaveState(workoutExerciseId, SetSaveState.Failed((result as AppResult.Failure).error.message))
            }
            setFeedback(result, "Serie guardada.")
        }
    }

    private fun updateSetSaveState(workoutExerciseId: Long, saveState: SetSaveState) {
        _uiState.update { state ->
            state.copy(setSaveStates = state.setSaveStates + (workoutExerciseId to saveState))
        }
    }

    fun updateSet(set: WorkoutSet, weightInput: String, repsInput: String, setType: SetType) {
        val weightValue = weightInput.replace(',', '.').toDoubleOrNull()
        val reps = repsInput.toIntOrNull()
        if (weightValue == null || reps == null) {
            sendMessage("Introduce un peso y repeticiones validos.")
            return
        }
        viewModelScope.launch {
            val now = clock.now()
            val result = updateWorkoutSet(
                set.copy(
                    weightValue = weightValue,
                    reps = reps,
                    setType = setType,
                    updatedAt = now,
                ),
            )
            setFeedback(result, "Serie actualizada.")
        }
    }

    fun deleteSet(setId: Long) {
        viewModelScope.launch {
            setFeedback(deleteWorkoutSet(setId), "Serie eliminada.")
        }
    }

    private fun restartRest(workoutExerciseId: Long) =
        updateRestTimer(workoutExerciseId) { it.restart() }

    private fun changeRestTarget(workoutExerciseId: Long, targetInput: String) {
        val targetSeconds = targetInput.toIntOrNull()
        if (targetSeconds == null || targetSeconds <= 0) {
            _uiState.update { state ->
                state.copy(manualRestErrors = state.manualRestErrors + (workoutExerciseId to "Introduce al menos 1 segundo de descanso."))
            }
            return
        }
        _uiState.update { state ->
            val currentTimer = state.manualRestTimers[workoutExerciseId] ?: ManualRestTimerState()
            state.copy(
                manualRestTimers = state.manualRestTimers + (workoutExerciseId to currentTimer.withTarget(targetSeconds)),
                manualRestErrors = state.manualRestErrors - workoutExerciseId,
            )
        }
    }

    private fun updateRestTimer(
        workoutExerciseId: Long,
        transform: (ManualRestTimerState) -> ManualRestTimerState,
    ) {
        _uiState.update { state ->
            val currentTimer = state.manualRestTimers[workoutExerciseId] ?: ManualRestTimerState()
            state.copy(manualRestTimers = state.manualRestTimers + (workoutExerciseId to transform(currentTimer)))
        }
    }

    fun completeSession() {
        val session = _uiState.value.activeSession?.session ?: return
        _uiState.update { it.copy(operation = SessionOperation.Completing, outcome = SessionOutcome.None) }
        viewModelScope.launch {
            val completedAt = clock.now()
            when (val completion = completeWorkoutSession(session, completedAt)) {
                is AppResult.Failure -> {
                    _uiState.update { it.copy(operation = SessionOperation.None) }
                    setFeedback(completion, "Sesion completada.")
                }
                is AppResult.Success -> {
                    val progress = applyWorkoutProgress(completedAt)
                    _uiState.update {
                        it.copy(
                            operation = SessionOperation.None,
                            outcome = if (progress is AppResult.Success) SessionOutcome.Completed else SessionOutcome.None,
                        )
                    }
                    setFeedback(progress, "Sesion completada y progreso actualizado.")
                }
            }
        }
    }

    fun cancelSession() {
        val session = _uiState.value.activeSession?.session ?: return
        _uiState.update { it.copy(operation = SessionOperation.Cancelling, outcome = SessionOutcome.None) }
        viewModelScope.launch {
            val result = cancelWorkoutSession(session, clock.now())
            _uiState.update {
                it.copy(
                    operation = SessionOperation.None,
                    outcome = if (result is AppResult.Success) SessionOutcome.Cancelled else SessionOutcome.None,
                )
            }
            setFeedback(result, "Sesion cancelada.")
        }
    }

    private fun setFeedback(result: AppResult<*>, successMessage: String) {
        val message = when (result) {
            is AppResult.Success -> successMessage
            is AppResult.Failure -> result.error.message
        }
        sendMessage(message)
    }

    private fun sendMessage(message: String) {
        viewModelScope.launch {
            _effects.emit(SessionEffect.ShowMessage(message))
        }
    }
}
