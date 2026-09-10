package com.gymquest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gymquest.app.data.local.entity.ExerciseBaseEntity
import com.gymquest.app.data.local.entity.ExerciseVariantEntity
import com.gymquest.app.data.local.entity.MuscleGroupEntity
import com.gymquest.app.data.local.relation.ExerciseBaseWithVariants
import com.gymquest.app.data.seed.CatalogSeedEntry
import com.gymquest.app.domain.model.enums.WeightComparisonType
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface ExerciseDao {
    @Insert
    suspend fun insertMuscleGroup(entity: MuscleGroupEntity): Long

    @Insert
    suspend fun insertExerciseBase(entity: ExerciseBaseEntity): Long

    @Insert
    suspend fun insertExerciseVariant(entity: ExerciseVariantEntity): Long

    @Update
    suspend fun updateExerciseBase(entity: ExerciseBaseEntity): Int

    @Update
    suspend fun updateExerciseVariant(entity: ExerciseVariantEntity): Int

    @Query("SELECT EXISTS(SELECT 1 FROM muscle_groups WHERE id = :muscleGroupId AND isArchived = 0)")
    suspend fun isActiveMuscleGroup(muscleGroupId: Long): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM exercise_bases WHERE id = :exerciseBaseId AND isArchived = 0)")
    suspend fun isActiveExerciseBase(exerciseBaseId: Long): Boolean

    @Query(
        "SELECT * FROM muscle_groups WHERE isArchived = 0 ORDER BY sortOrder, name",
    )
    fun observeActiveMuscleGroups(): Flow<List<MuscleGroupEntity>>

    @Query(
        """
        SELECT exercise_variants.*
        FROM exercise_variants
        INNER JOIN exercise_bases ON exercise_bases.id = exercise_variants.exerciseBaseId
        WHERE exercise_variants.isArchived = 0 AND exercise_bases.isArchived = 0
        ORDER BY exercise_variants.exerciseBaseId, exercise_variants.name
        """,
    )
    fun observeActiveExerciseVariants(): Flow<List<ExerciseVariantEntity>>

    @Query("SELECT * FROM exercise_bases WHERE isArchived = 0 ORDER BY name")
    fun observeActiveExerciseBases(): Flow<List<ExerciseBaseEntity>>

    @Query(
        """
        SELECT exercise_variants.*
        FROM exercise_variants
        INNER JOIN exercise_bases ON exercise_bases.id = exercise_variants.exerciseBaseId
        WHERE exercise_variants.exerciseBaseId = :baseId
            AND exercise_variants.isArchived = 0
            AND exercise_bases.isArchived = 0
        ORDER BY exercise_variants.name
        """,
    )
    fun observeVariantsForActiveExerciseBase(baseId: Long): Flow<List<ExerciseVariantEntity>>

    @Query(
        """
        SELECT exercise_variants.*
        FROM exercise_variants
        INNER JOIN exercise_bases ON exercise_bases.id = exercise_variants.exerciseBaseId
        WHERE exercise_variants.isArchived = 0 AND exercise_bases.isArchived = 0
        ORDER BY exercise_variants.exerciseBaseId, exercise_variants.name
        """,
    )
    suspend fun getActiveExerciseVariants(): List<ExerciseVariantEntity>

    @Query("SELECT * FROM exercise_variants WHERE id = :variantId LIMIT 1")
    suspend fun getExerciseVariant(variantId: Long): ExerciseVariantEntity?

    @Query("SELECT * FROM exercise_bases WHERE id = :baseId LIMIT 1")
    suspend fun getExerciseBase(baseId: Long): ExerciseBaseEntity?

    @Transaction
    @Query("SELECT * FROM exercise_bases WHERE id = :baseId LIMIT 1")
    suspend fun getExerciseBaseWithVariants(baseId: Long): ExerciseBaseWithVariants?

    @Query("SELECT * FROM exercise_variants WHERE exerciseBaseId = :baseId ORDER BY name")
    suspend fun getVariantsForExerciseBase(baseId: Long): List<ExerciseVariantEntity>

    @Query("UPDATE exercise_bases SET isArchived = 1, updatedAt = :updatedAt WHERE id = :baseId")
    suspend fun archiveExerciseBase(baseId: Long, updatedAt: Instant): Int

    @Query("UPDATE exercise_variants SET isArchived = 1, updatedAt = :updatedAt WHERE id = :variantId")
    suspend fun archiveExerciseVariant(variantId: Long, updatedAt: Instant): Int

    @Query("SELECT id FROM muscle_groups WHERE name = :name LIMIT 1")
    suspend fun findMuscleGroupIdByName(name: String): Long?

    @Query("SELECT id FROM exercise_bases WHERE primaryMuscleGroupId = :muscleGroupId AND name = :name LIMIT 1")
    suspend fun findExerciseBaseId(muscleGroupId: Long, name: String): Long?

    @Query("SELECT id FROM exercise_bases WHERE primaryMuscleGroupId = :muscleGroupId AND name = :name AND isBuiltIn = 1 LIMIT 1")
    suspend fun findBuiltInExerciseBaseId(muscleGroupId: Long, name: String): Long?

    @Query("SELECT id FROM exercise_bases WHERE name = :name AND isBuiltIn = 1 LIMIT 1")
    suspend fun findBuiltInExerciseBaseIdByName(name: String): Long?

    @Query("SELECT id FROM exercise_variants WHERE exerciseBaseId = :exerciseBaseId AND name = :name LIMIT 1")
    suspend fun findExerciseVariantId(exerciseBaseId: Long, name: String): Long?

    @Transaction
    suspend fun seedBuiltInCatalog(entries: List<CatalogSeedEntry>, now: Instant) {
        val groups = mutableMapOf<String, Long>()
        entries.forEach { entry ->
            val groupId = groups[entry.muscleGroupName] ?: run {
                findMuscleGroupIdByName(entry.muscleGroupName)
                    ?: insertMuscleGroup(MuscleGroupEntity(name = entry.muscleGroupName, sortOrder = groups.size))
            }.also { groups[entry.muscleGroupName] = it }
            val baseId = findBuiltInExerciseBaseId(groupId, entry.exerciseName)
                ?: findBuiltInExerciseBaseIdByName(entry.exerciseName)?.also { existingId ->
                    updateExerciseBase(
                        ExerciseBaseEntity(
                            id = existingId,
                            name = entry.exerciseName,
                            primaryMuscleGroupId = groupId,
                            description = entry.description,
                            isBuiltIn = true,
                            createdAt = now,
                            updatedAt = now,
                        ),
                    )
                }
                ?: findBuiltInExerciseBaseIdByName(entry.sourceExerciseName)?.also { existingId ->
                    updateExerciseBase(
                        ExerciseBaseEntity(
                            id = existingId,
                            name = entry.exerciseName,
                            primaryMuscleGroupId = groupId,
                            description = entry.description,
                            isBuiltIn = true,
                            createdAt = now,
                            updatedAt = now,
                        ),
                    )
                }
                ?: insertExerciseBase(
                    ExerciseBaseEntity(
                        name = entry.exerciseName,
                        primaryMuscleGroupId = groupId,
                        description = entry.description,
                        isBuiltIn = true,
                        createdAt = now,
                        updatedAt = now,
                    ),
                )
            if (findExerciseVariantId(baseId, entry.variantName) == null) {
                insertExerciseVariant(
                    ExerciseVariantEntity(
                        exerciseBaseId = baseId,
                        name = entry.variantName,
                        equipmentType = entry.equipmentType,
                        weightComparisonType = WeightComparisonType.TOTAL_WEIGHT,
                        notes = "Catálogo local MIT: hasaneyldrm/exercises-dataset. Sin media de terceros.",
                        isBuiltIn = true,
                        createdAt = now,
                        updatedAt = now,
                    ),
                )
            }
        }
    }
}
