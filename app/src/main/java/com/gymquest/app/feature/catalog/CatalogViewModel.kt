package com.gymquest.app.feature.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymquest.app.core.result.AppResult
import com.gymquest.app.core.time.ClockProvider
import com.gymquest.app.domain.model.ExerciseBase
import com.gymquest.app.domain.model.ExerciseCatalogEntry
import com.gymquest.app.domain.model.ExerciseVariant
import com.gymquest.app.domain.model.MuscleGroup
import com.gymquest.app.domain.model.MediaFile
import com.gymquest.app.domain.model.enums.EquipmentType
import com.gymquest.app.domain.model.enums.MediaOwnerType
import com.gymquest.app.domain.model.enums.MediaType
import com.gymquest.app.domain.model.enums.WeightComparisonType
import com.gymquest.app.domain.usecase.catalog.CreateExerciseBaseUseCase
import com.gymquest.app.domain.usecase.catalog.CreateExerciseVariantUseCase
import com.gymquest.app.domain.usecase.catalog.CreateMuscleGroupUseCase
import com.gymquest.app.domain.usecase.catalog.ObserveExerciseCatalogUseCase
import com.gymquest.app.domain.usecase.catalog.ObserveMuscleGroupsUseCase
import com.gymquest.app.domain.usecase.catalog.UpdateExerciseBaseUseCase
import com.gymquest.app.domain.usecase.catalog.UpdateExerciseVariantUseCase
import com.gymquest.app.domain.usecase.catalog.ArchiveExerciseBaseUseCase
import com.gymquest.app.domain.usecase.catalog.ArchiveExerciseVariantUseCase
import com.gymquest.app.domain.usecase.catalog.ObserveExerciseBaseMediaUseCase
import com.gymquest.app.domain.usecase.catalog.RemoveExerciseBaseMediaUseCase
import com.gymquest.app.domain.usecase.catalog.ReplaceExerciseBaseMediaUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

sealed interface CatalogCreation {
    data object MuscleGroup : CatalogCreation
    data object ExerciseBase : CatalogCreation
    data class Variant(val exerciseBaseId: Long) : CatalogCreation
}

data class CatalogUiState(
    val muscleGroups: List<MuscleGroup> = emptyList(),
    val catalog: List<ExerciseCatalogEntry> = emptyList(),
    val mediaByExerciseBaseId: Map<Long, MediaFile> = emptyMap(),
    val selectedMuscleGroupId: Long? = null,
    val feedback: String? = null,
    val lastCreation: CatalogCreation? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

data class ExerciseMediaDraft(
    val uri: String,
    val mimeType: String,
    val title: String,
    val mediaType: MediaType,
)

class CatalogViewModel(
    private val createMuscleGroup: CreateMuscleGroupUseCase,
    private val observeMuscleGroups: ObserveMuscleGroupsUseCase,
    private val createExerciseBase: CreateExerciseBaseUseCase,
    private val createExerciseVariant: CreateExerciseVariantUseCase,
    private val observeExerciseCatalog: ObserveExerciseCatalogUseCase,
    private val updateExerciseBaseUseCase: UpdateExerciseBaseUseCase,
    private val updateExerciseVariantUseCase: UpdateExerciseVariantUseCase,
    private val archiveExerciseBaseUseCase: ArchiveExerciseBaseUseCase,
    private val archiveExerciseVariantUseCase: ArchiveExerciseVariantUseCase,
    private val replaceExerciseBaseMedia: ReplaceExerciseBaseMediaUseCase,
    private val removeExerciseBaseMedia: RemoveExerciseBaseMediaUseCase,
    private val observeExerciseBaseMedia: ObserveExerciseBaseMediaUseCase,
    private val clock: ClockProvider,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()
    private var muscleGroupsLoaded = false
    private var catalogLoaded = false
    private var observersJob: Job? = null

    init {
        observeData()
    }

    fun retry() {
        observeData()
    }

    private fun observeData() {
        observersJob?.cancel()
        muscleGroupsLoaded = false
        catalogLoaded = false
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        observersJob = viewModelScope.launch {
            launch {
                observeMuscleGroups()
                    .catch {
                        muscleGroupsLoaded = true
                        _uiState.update { it.copy(isLoading = !(muscleGroupsLoaded && catalogLoaded), errorMessage = "No se pudieron cargar los grupos musculares.") }
                    }
                    .collect { groups ->
                muscleGroupsLoaded = true
                _uiState.update { state ->
                    state.copy(
                        muscleGroups = groups,
                        selectedMuscleGroupId = state.selectedMuscleGroupId?.takeIf { selectedId ->
                            groups.any { it.id == selectedId }
                        } ?: groups.firstOrNull()?.id,
                        isLoading = !(muscleGroupsLoaded && catalogLoaded),
                    )
                }
            }
            }
            launch {
                observeExerciseCatalog()
                    .catch {
                        catalogLoaded = true
                        _uiState.update { it.copy(isLoading = !(muscleGroupsLoaded && catalogLoaded), errorMessage = "No se pudo cargar el catalogo de ejercicios.") }
                    }
                    .collect { catalog ->
                catalogLoaded = true
                _uiState.update { it.copy(catalog = catalog, isLoading = !(muscleGroupsLoaded && catalogLoaded)) }
            }
            }
            launch {
                observeExerciseBaseMedia().collect { media ->
                    _uiState.update { state ->
                        state.copy(mediaByExerciseBaseId = media
                            .groupBy { it.ownerId }
                            .mapValues { (_, files) -> files.maxBy { it.createdAt } })
                    }
                }
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
                    is AppResult.Success -> state.copy(selectedMuscleGroupId = result.value, feedback = "Grupo muscular creado.", lastCreation = CatalogCreation.MuscleGroup)
                    is AppResult.Failure -> state.copy(feedback = result.error.message, lastCreation = null)
                }
            }
        }
    }

    fun addExerciseBase(name: String, description: String?, mediaDraft: ExerciseMediaDraft? = null) {
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
            if (result is AppResult.Success && mediaDraft != null) {
                val mediaResult = saveExerciseBaseMedia(result.value, mediaDraft, now)
                _uiState.update { state ->
                    when (mediaResult) {
                        is AppResult.Success -> state.copy(feedback = "Ejercicio y guía visual guardados.", lastCreation = CatalogCreation.ExerciseBase)
                        is AppResult.Failure -> state.copy(
                            feedback = "Ejercicio creado, pero la guía no se pudo guardar: ${mediaResult.error.message}",
                            lastCreation = null,
                        )
                    }
                }
            } else {
                _uiState.update { state ->
                    when (result) {
                        is AppResult.Success -> state.copy(feedback = "Ejercicio creado.", lastCreation = CatalogCreation.ExerciseBase)
                        is AppResult.Failure -> state.copy(feedback = result.error.message, lastCreation = null)
                    }
                }
            }
        }
    }

    fun replaceExerciseBaseMedia(exerciseBaseId: Long, draft: ExerciseMediaDraft) = viewModelScope.launch {
        val result = saveExerciseBaseMedia(exerciseBaseId, draft, clock.now())
        _uiState.update { it.copy(feedback = result.message("Guía visual sustituida.")) }
    }

    fun removeExerciseBaseMedia(exerciseBaseId: Long) = viewModelScope.launch {
        val result = removeExerciseBaseMedia.invoke(exerciseBaseId)
        _uiState.update { it.copy(feedback = result.message("Guía visual eliminada. El progreso no ha cambiado.")) }
    }

    private suspend fun saveExerciseBaseMedia(
        exerciseBaseId: Long,
        draft: ExerciseMediaDraft,
        createdAt: java.time.Instant,
    ): AppResult<Long> = replaceExerciseBaseMedia(
        MediaFile(
            ownerType = MediaOwnerType.EXERCISE_BASE,
            ownerId = exerciseBaseId,
            mediaType = draft.mediaType,
            localUri = draft.uri,
            title = draft.title,
            notes = "Guía visual local (${draft.mimeType})",
            createdAt = createdAt,
        ),
    )

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
                    is AppResult.Success -> state.copy(feedback = "Variante creada.", lastCreation = CatalogCreation.Variant(exerciseBaseId))
                    is AppResult.Failure -> state.copy(feedback = result.error.message, lastCreation = null)
                }
            }
        }
    }

    fun clearFeedback() {
        _uiState.update { it.copy(feedback = null, lastCreation = null) }
    }

    fun updateExerciseBase(exercise: ExerciseBase) = viewModelScope.launch {
        val result = updateExerciseBaseUseCase(exercise.copy(updatedAt = clock.now()))
        _uiState.update { it.copy(feedback = result.message("Ejercicio actualizado.")) }
    }

    fun updateExerciseVariant(variant: ExerciseVariant) = viewModelScope.launch {
        val result = updateExerciseVariantUseCase(variant.copy(updatedAt = clock.now()))
        _uiState.update { it.copy(feedback = result.message("Variante actualizada.")) }
    }

    fun archiveExerciseBase(id: Long) = viewModelScope.launch {
        val result = archiveExerciseBaseUseCase(id, clock.now())
        _uiState.update { it.copy(feedback = result.message("Ejercicio archivado.")) }
    }

    fun archiveExerciseVariant(id: Long) = viewModelScope.launch {
        val result = archiveExerciseVariantUseCase(id, clock.now())
        _uiState.update { it.copy(feedback = result.message("Variante archivada.")) }
    }

    private fun AppResult<*>.message(success: String): String = when (this) {
        is AppResult.Success -> success
        is AppResult.Failure -> error.message
    }
}
