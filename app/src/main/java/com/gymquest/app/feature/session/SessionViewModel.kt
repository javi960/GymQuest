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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SessionUiState(
    val activeSession: WorkoutSessionDetail? = null,
    val variants: List<ExerciseVariant> = emptyList(),
    val selectedVariantId: Long? = null,
)

sealed interface SessionAction {
    data object StartSession : SessionAction
    data object AddSelectedVariant : SessionAction
    data object CompleteSession : SessionAction
    data object CancelSession : SessionAction
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
    private val clock: ClockProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<SessionEffect>()
    val effects: SharedFlow<SessionEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            observeActiveSession().collect { session ->
                _uiState.update { it.copy(activeSession = session) }
            }
        }
        viewModelScope.launch {
            observeActiveExerciseVariants().collect { variants ->
                _uiState.update { state ->
                    state.copy(
                        variants = variants,
                        selectedVariantId = state.selectedVariantId?.takeIf { selectedId ->
                            variants.any { it.id == selectedId }
                        } ?: variants.firstOrNull()?.id,
                    )
                }
            }
        }
    }

    fun onAction(action: SessionAction) {
        when (action) {
            SessionAction.AddSelectedVariant -> addSelectedVariantToSession()
            SessionAction.CancelSession -> cancelSession()
            SessionAction.CompleteSession -> completeSession()
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
        }
    }

    fun startSession() {
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
            sendMessage("El ejercicio de sesion no existe.")
            return
        }
        viewModelScope.launch {
            val now = clock.now()
            val restResolution = resolveRestBeforeNextSet(exercise.sets.lastOrNull(), now)
            if (restResolution != null) {
                val updateResult = updateWorkoutSet(restResolution.previousSet)
                if (updateResult is AppResult.Failure) {
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
            setFeedback(result, "Serie guardada.")
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

    fun completeSession() {
        val session = _uiState.value.activeSession?.session ?: return
        viewModelScope.launch {
            setFeedback(completeWorkoutSession(session, clock.now()), "Sesion completada.")
        }
    }

    fun cancelSession() {
        val session = _uiState.value.activeSession?.session ?: return
        viewModelScope.launch {
            setFeedback(cancelWorkoutSession(session, clock.now()), "Sesion cancelada.")
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
