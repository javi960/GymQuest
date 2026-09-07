package com.gymquest.app.domain.repository

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.ExerciseBase
import com.gymquest.app.domain.model.ExerciseCatalogEntry
import com.gymquest.app.domain.model.ExerciseVariant
import com.gymquest.app.domain.model.MuscleGroup
import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface ExerciseRepository {
    fun observeMuscleGroups(): Flow<List<MuscleGroup>>

    fun observeActiveExerciseVariants(): Flow<List<ExerciseVariant>>

    fun observeActiveExerciseCatalog(): Flow<List<ExerciseCatalogEntry>>

    fun observeVariantsForExerciseBase(exerciseBaseId: Long): Flow<List<ExerciseVariant>>

    suspend fun findExerciseVariantById(variantId: Long): AppResult<ExerciseVariant?>

    suspend fun createMuscleGroup(muscleGroup: MuscleGroup): AppResult<Long>

    suspend fun createExerciseBase(exerciseBase: ExerciseBase): AppResult<Long>

    suspend fun updateExerciseBase(exerciseBase: ExerciseBase): AppResult<Unit>

    suspend fun archiveExerciseBase(baseId: Long, updatedAt: Instant): AppResult<Unit>

    suspend fun createExerciseVariant(exerciseVariant: ExerciseVariant): AppResult<Long>

    suspend fun updateExerciseVariant(exerciseVariant: ExerciseVariant): AppResult<Unit>

    suspend fun archiveExerciseVariant(variantId: Long, updatedAt: Instant): AppResult<Unit>
}
