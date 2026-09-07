package com.gymquest.app.domain.usecase.catalog

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.ExerciseBase
import com.gymquest.app.domain.model.ExerciseCatalogEntry
import com.gymquest.app.domain.model.ExerciseVariant
import com.gymquest.app.domain.model.MuscleGroup
import com.gymquest.app.domain.model.enums.EquipmentType
import com.gymquest.app.domain.model.enums.WeightComparisonType
import com.gymquest.app.domain.repository.ExerciseRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class ExerciseCatalogUseCasesTest {
    private val timestamp = Instant.parse("2026-09-04T10:15:30Z")

    @Test
    fun writeUseCases_delegateTheExactModelsAndResults() = runBlocking {
        val repository = RecordingExerciseRepository()
        val base = exerciseBase()
        val variant = exerciseVariant()
        val createBaseResult = AppResult.Success(11L)
        val updateBaseResult = AppResult.Success(Unit)
        val archiveBaseResult = AppResult.Success(Unit)
        val createVariantResult = AppResult.Success(12L)
        val updateVariantResult = AppResult.Success(Unit)
        val archiveVariantResult = AppResult.Success(Unit)
        repository.createBaseResult = createBaseResult
        repository.updateBaseResult = updateBaseResult
        repository.archiveBaseResult = archiveBaseResult
        repository.createVariantResult = createVariantResult
        repository.updateVariantResult = updateVariantResult
        repository.archiveVariantResult = archiveVariantResult

        assertSame(createBaseResult, CreateExerciseBaseUseCase(repository)(base))
        assertSame(updateBaseResult, UpdateExerciseBaseUseCase(repository)(base))
        assertSame(archiveBaseResult, ArchiveExerciseBaseUseCase(repository)(base.id, timestamp))
        assertSame(createVariantResult, CreateExerciseVariantUseCase(repository)(variant))
        assertSame(updateVariantResult, UpdateExerciseVariantUseCase(repository)(variant))
        assertSame(archiveVariantResult, ArchiveExerciseVariantUseCase(repository)(variant.id, timestamp))
        assertEquals(base, repository.createdBase)
        assertEquals(base, repository.updatedBase)
        assertEquals(base.id to timestamp, repository.archivedBase)
        assertEquals(variant, repository.createdVariant)
        assertEquals(variant, repository.updatedVariant)
        assertEquals(variant.id to timestamp, repository.archivedVariant)
    }

    @Test
    fun observeCatalogUseCase_exposesTheRepositoryFlow() = runBlocking {
        val entry = ExerciseCatalogEntry(exerciseBase(), listOf(exerciseVariant()))
        val repository = RecordingExerciseRepository().apply {
            catalog = flowOf(listOf(entry))
        }

        assertEquals(listOf(entry), ObserveExerciseCatalogUseCase(repository)().first())
    }

    private fun exerciseBase() = ExerciseBase(
        id = 7,
        name = "Bench press",
        primaryMuscleGroupId = 2,
        createdAt = timestamp,
        updatedAt = timestamp,
    )

    private fun exerciseVariant() = ExerciseVariant(
        id = 8,
        exerciseBaseId = 7,
        name = "Barbell",
        equipmentType = EquipmentType.BARBELL,
        weightComparisonType = WeightComparisonType.TOTAL_WEIGHT,
        createdAt = timestamp,
        updatedAt = timestamp,
    )

    private class RecordingExerciseRepository : ExerciseRepository {
        var catalog: Flow<List<ExerciseCatalogEntry>> = flowOf(emptyList())
        lateinit var createBaseResult: AppResult<Long>
        lateinit var updateBaseResult: AppResult<Unit>
        lateinit var archiveBaseResult: AppResult<Unit>
        lateinit var createVariantResult: AppResult<Long>
        lateinit var updateVariantResult: AppResult<Unit>
        lateinit var archiveVariantResult: AppResult<Unit>
        var createdBase: ExerciseBase? = null
        var updatedBase: ExerciseBase? = null
        var archivedBase: Pair<Long, Instant>? = null
        var createdVariant: ExerciseVariant? = null
        var updatedVariant: ExerciseVariant? = null
        var archivedVariant: Pair<Long, Instant>? = null

        override fun observeMuscleGroups(): Flow<List<MuscleGroup>> = flowOf(emptyList())
        override fun observeActiveExerciseVariants(): Flow<List<ExerciseVariant>> = flowOf(emptyList())
        override fun observeActiveExerciseCatalog(): Flow<List<ExerciseCatalogEntry>> = catalog
        override fun observeVariantsForExerciseBase(exerciseBaseId: Long): Flow<List<ExerciseVariant>> = flowOf(emptyList())
        override suspend fun findExerciseVariantById(variantId: Long): AppResult<ExerciseVariant?> = error("Unused")
        override suspend fun createMuscleGroup(muscleGroup: MuscleGroup): AppResult<Long> = error("Unused")

        override suspend fun createExerciseBase(exerciseBase: ExerciseBase): AppResult<Long> {
            createdBase = exerciseBase
            return createBaseResult
        }

        override suspend fun updateExerciseBase(exerciseBase: ExerciseBase): AppResult<Unit> {
            updatedBase = exerciseBase
            return updateBaseResult
        }

        override suspend fun archiveExerciseBase(baseId: Long, updatedAt: Instant): AppResult<Unit> {
            archivedBase = baseId to updatedAt
            return archiveBaseResult
        }

        override suspend fun createExerciseVariant(exerciseVariant: ExerciseVariant): AppResult<Long> {
            createdVariant = exerciseVariant
            return createVariantResult
        }

        override suspend fun updateExerciseVariant(exerciseVariant: ExerciseVariant): AppResult<Unit> {
            updatedVariant = exerciseVariant
            return updateVariantResult
        }

        override suspend fun archiveExerciseVariant(variantId: Long, updatedAt: Instant): AppResult<Unit> {
            archivedVariant = variantId to updatedAt
            return archiveVariantResult
        }
    }
}
