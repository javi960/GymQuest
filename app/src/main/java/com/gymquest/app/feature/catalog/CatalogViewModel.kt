package com.gymquest.app.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymquest.app.core.result.AppResult
import com.gymquest.app.core.time.ClockProvider
import com.gymquest.app.domain.model.ExerciseBase
import com.gymquest.app.domain.model.ExerciseCatalogEntry
import com.gymquest.app.domain.model.ExerciseVariant
import com.gymquest.app.domain.model.MuscleGroup
import com.gymquest.app.domain.model.enums.EquipmentType
import com.gymquest.app.domain.model.enums.WeightComparisonType
import com.gymquest.app.domain.usecase.catalog.CreateExerciseBaseUseCase
import com.gymquest.app.domain.usecase.catalog.CreateExerciseVariantUseCase
import com.gymquest.app.domain.usecase.catalog.CreateMuscleGroupUseCase
import com.gymquest.app.domain.usecase.catalog.ObserveExerciseCatalogUseCase
import com.gymquest.app.domain.usecase.catalog.ObserveMuscleGroupsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CatalogUiState(
    val muscleGroups: List<MuscleGroup> = emptyList(),
    val catalog: List<ExerciseCatalogEntry> = emptyList(),
    val selectedMuscleGroupId: Long? = null,
    val feedback: String? = null,
)

class CatalogViewModel(
    private val createMuscleGroup: CreateMuscleGroupUseCase,
    private val observeMuscleGroups: ObserveMuscleGroupsUseCase,
    private val createExerciseBase: CreateExerciseBaseUseCase,
    private val createExerciseVariant: CreateExerciseVariantUseCase,
    private val observeExerciseCatalog: ObserveExerciseCatalogUseCase,
    private val clock: ClockProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeMuscleGroups().collect { groups ->
                _uiState.update { state ->
                    state.copy(
                        muscleGroups = groups,
                        selectedMuscleGroupId = state.selectedMuscleGroupId?.takeIf { selectedId ->
                            groups.any { it.id == selectedId }
                        } ?: groups.firstOrNull()?.id,
                    )
                }
            }
        }
        viewModelScope.launch {
            observeExerciseCatalog().collect { catalog ->
                _uiState.update { it.copy(catalog = catalog) }
            }
        }
    }

    fun selectMuscleGroup(id: Long) {
        _uiState.update { it.copy(selectedMuscleGroupId = id, feedback = null) }
    }

    fun addMuscleGroup(name: String) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val result = createMuscleGroup(
                MuscleGroup(name = name, sortOrder = currentState.muscleGroups.size),
            )
            _uiState.update { state ->
                when (result) {
                    is AppResult.Success -> state.copy(selectedMuscleGroupId = result.value, feedback = "Grupo muscular creado.")
                    is AppResult.Failure -> state.copy(feedback = result.error.message)
                }
            }
        }
    }

    fun addExerciseBase(name: String, description: String?) {
        val muscleGroupId = _uiState.value.selectedMuscleGroupId
        if (muscleGroupId == null) {
            _uiState.update { it.copy(feedback = "Crea primero un grupo muscular.") }
            return
        }
        viewModelScope.launch {
            val now = clock.now()
            val result = createExerciseBase(
                ExerciseBase(
                    name = name,
                    primaryMuscleGroupId = muscleGroupId,
                    description = description?.takeIf(String::isNotBlank),
                    createdAt = now,
                    updatedAt = now,
                ),
            )
            _uiState.update { state ->
                when (result) {
                    is AppResult.Success -> state.copy(feedback = "Ejercicio creado.")
                    is AppResult.Failure -> state.copy(feedback = result.error.message)
                }
            }
        }
    }

    fun addExerciseVariant(
        exerciseBaseId: Long,
        name: String,
        equipmentType: EquipmentType,
        weightComparisonType: WeightComparisonType,
        notes: String?,
    ) {
        viewModelScope.launch {
            val now = clock.now()
            val result = createExerciseVariant(
                ExerciseVariant(
                    exerciseBaseId = exerciseBaseId,
                    name = name,
                    equipmentType = equipmentType,
                    weightComparisonType = weightComparisonType,
                    notes = notes?.takeIf(String::isNotBlank),
                    createdAt = now,
                    updatedAt = now,
                ),
            )
            _uiState.update { state ->
                when (result) {
                    is AppResult.Success -> state.copy(feedback = "Variante creada.")
                    is AppResult.Failure -> state.copy(feedback = result.error.message)
                }
            }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedback = null) }
    }
}
