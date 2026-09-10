package com.gymquest.app.feature.catalog

import androidx.lifecycle.ViewModelStore
import com.gymquest.app.core.result.AppResult
import com.gymquest.app.core.time.ClockProvider
import com.gymquest.app.domain.model.ExerciseBase
import com.gymquest.app.domain.model.ExerciseCatalogEntry
import com.gymquest.app.domain.model.ExerciseVariant
import com.gymquest.app.domain.model.MuscleGroup
import com.gymquest.app.domain.model.MediaFile
import com.gymquest.app.domain.model.enums.MediaOwnerType
import com.gymquest.app.domain.model.enums.EquipmentType
import com.gymquest.app.domain.model.enums.WeightComparisonType
import com.gymquest.app.domain.repository.ExerciseRepository
import com.gymquest.app.domain.repository.MediaRepository
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
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {
    private val now = Instant.parse("2026-09-07T12:00:00Z")

    @Test
    fun `catalog writes muscle groups with the next sort order and selects the created group`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeExerciseRepository(
                muscleGroups = MutableStateFlow(
                    listOf(
                        MuscleGroup(id = 1, name = "Pecho", sortOrder = 0),
                        MuscleGroup(id = 2, name = "Espalda", sortOrder = 1),
                    ),
                ),
            ).apply { createMuscleGroupResult = AppResult.Success(9L) }
            val viewModel = viewModel(repository)

            advanceUntilIdle()
            viewModel.addMuscleGroup("Pierna")
            advanceUntilIdle()

            assertEquals(MuscleGroup(name = "Pierna", sortOrder = 2), repository.createdMuscleGroup)
            assertEquals(9L, viewModel.uiState.value.selectedMuscleGroupId)
            assertEquals("Grupo muscular creado.", viewModel.uiState.value.feedback)
            assertEquals(CatalogCreation.MuscleGroup, viewModel.uiState.value.lastCreation)
            ViewModelStore().apply { put("catalog", viewModel); clear() }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `catalog writes exercise base and variant with selected group and clock timestamps`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeExerciseRepository(
                muscleGroups = MutableStateFlow(listOf(MuscleGroup(id = 4, name = "Pierna", sortOrder = 0))),
            )
            val viewModel = viewModel(repository)

            advanceUntilIdle()
            viewModel.addExerciseBase("Sentadilla", "  ")
            advanceUntilIdle()
            viewModel.addExerciseVariant(
                exerciseBaseId = 8,
                name = "Barra alta",
                equipmentType = EquipmentType.BARBELL,
                weightComparisonType = WeightComparisonType.TOTAL_WEIGHT,
                notes = "",
            )
            advanceUntilIdle()

            assertEquals(
                ExerciseBase(
                    name = "Sentadilla",
                    primaryMuscleGroupId = 4,
                    description = null,
                    createdAt = now,
                    updatedAt = now,
                ),
                repository.createdExerciseBase,
            )
            assertEquals(
                ExerciseVariant(
                    exerciseBaseId = 8,
                    name = "Barra alta",
                    equipmentType = EquipmentType.BARBELL,
                    weightComparisonType = WeightComparisonType.TOTAL_WEIGHT,
                    notes = null,
                    createdAt = now,
                    updatedAt = now,
                ),
                repository.createdExerciseVariant,
            )
            assertEquals("Variante creada.", viewModel.uiState.value.feedback)
            assertEquals(CatalogCreation.Variant(8), viewModel.uiState.value.lastCreation)
            ViewModelStore().apply { put("catalog", viewModel); clear() }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `catalog refuses to create an exercise before a muscle group exists`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeExerciseRepository()
            val viewModel = viewModel(repository)

            advanceUntilIdle()
            viewModel.addExerciseBase("Press banca", null)
            advanceUntilIdle()

            assertNull(repository.createdExerciseBase)
            assertEquals("Crea primero un grupo muscular.", viewModel.uiState.value.feedback)
            ViewModelStore().apply { put("catalog", viewModel); clear() }
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun viewModel(repository: FakeExerciseRepository) = CatalogViewModel(
        createMuscleGroup = CreateMuscleGroupUseCase(repository),
        observeMuscleGroups = ObserveMuscleGroupsUseCase(repository),
        createExerciseBase = CreateExerciseBaseUseCase(repository),
        createExerciseVariant = CreateExerciseVariantUseCase(repository),
        observeExerciseCatalog = ObserveExerciseCatalogUseCase(repository),
        updateExerciseBaseUseCase = UpdateExerciseBaseUseCase(repository),
        updateExerciseVariantUseCase = UpdateExerciseVariantUseCase(repository),
        archiveExerciseBaseUseCase = ArchiveExerciseBaseUseCase(repository),
        archiveExerciseVariantUseCase = ArchiveExerciseVariantUseCase(repository),
        replaceExerciseBaseMedia = ReplaceExerciseBaseMediaUseCase(FakeMediaRepository()),
        removeExerciseBaseMedia = RemoveExerciseBaseMediaUseCase(FakeMediaRepository()),
        observeExerciseBaseMedia = ObserveExerciseBaseMediaUseCase(FakeMediaRepository()),
        clock = ClockProvider { now },
    )

    private class FakeExerciseRepository(
        private val muscleGroups: Flow<List<MuscleGroup>> = flowOf(emptyList()),
        private val catalog: Flow<List<ExerciseCatalogEntry>> = flowOf(emptyList()),
    ) : ExerciseRepository {
        var createMuscleGroupResult: AppResult<Long> = AppResult.Success(1L)
        var createExerciseBaseResult: AppResult<Long> = AppResult.Success(2L)
        var createExerciseVariantResult: AppResult<Long> = AppResult.Success(3L)
        var createdMuscleGroup: MuscleGroup? = null
        var createdExerciseBase: ExerciseBase? = null
        var createdExerciseVariant: ExerciseVariant? = null

        override fun observeMuscleGroups(): Flow<List<MuscleGroup>> = muscleGroups
        override fun observeActiveExerciseVariants(): Flow<List<ExerciseVariant>> = flowOf(emptyList())
        override fun observeActiveExerciseCatalog(): Flow<List<ExerciseCatalogEntry>> = catalog
        override fun observeVariantsForExerciseBase(exerciseBaseId: Long): Flow<List<ExerciseVariant>> = flowOf(emptyList())
        override suspend fun findExerciseVariantById(variantId: Long): AppResult<ExerciseVariant?> = AppResult.Success(null)

        override suspend fun createMuscleGroup(muscleGroup: MuscleGroup): AppResult<Long> {
            createdMuscleGroup = muscleGroup
            return createMuscleGroupResult
        }

        override suspend fun createExerciseBase(exerciseBase: ExerciseBase): AppResult<Long> {
            createdExerciseBase = exerciseBase
            return createExerciseBaseResult
        }

        override suspend fun updateExerciseBase(exerciseBase: ExerciseBase): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun archiveExerciseBase(baseId: Long, updatedAt: Instant): AppResult<Unit> = AppResult.Success(Unit)

        override suspend fun createExerciseVariant(exerciseVariant: ExerciseVariant): AppResult<Long> {
            createdExerciseVariant = exerciseVariant
            return createExerciseVariantResult
        }

        override suspend fun updateExerciseVariant(exerciseVariant: ExerciseVariant): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun archiveExerciseVariant(variantId: Long, updatedAt: Instant): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class FakeMediaRepository : MediaRepository {
        override fun observeActiveForOwnerType(ownerType: MediaOwnerType): Flow<List<MediaFile>> = flowOf(emptyList())
        override suspend fun replaceForOwner(mediaFile: MediaFile): AppResult<Long> = AppResult.Success(1L)
        override suspend fun removeForOwner(ownerType: MediaOwnerType, ownerId: Long): AppResult<Unit> = AppResult.Success(Unit)
    }
}
