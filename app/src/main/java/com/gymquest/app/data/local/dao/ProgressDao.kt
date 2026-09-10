package com.gymquest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.gymquest.app.data.local.entity.AchievementEntity
import com.gymquest.app.data.local.entity.CharacterStatsEntity
import com.gymquest.app.data.local.entity.ExerciseMasteryEntity
import com.gymquest.app.data.local.entity.UserAchievementEntity
import com.gymquest.app.data.local.entity.UserProfileEntity
import com.gymquest.app.data.local.projection.ExerciseMasteryWithVariantName
import kotlinx.coroutines.flow.Flow

@Dao
interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserProfile(entity: UserProfileEntity)

    @Query("SELECT * FROM user_profiles WHERE id = :profileId LIMIT 1")
    suspend fun getUserProfile(profileId: Long): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCharacterStats(entity: CharacterStatsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertExerciseMastery(entity: ExerciseMasteryEntity)

    @Insert
    suspend fun insertAchievement(entity: AchievementEntity): Long

    @Insert
    suspend fun insertUserAchievement(entity: UserAchievementEntity): Long

    @Query("SELECT * FROM character_stats WHERE userProfileId = 1 LIMIT 1")
    suspend fun getCharacterStats(): CharacterStatsEntity?

    @Query("SELECT * FROM character_stats WHERE userProfileId = 1 LIMIT 1")
    fun observeCharacterStats(): Flow<CharacterStatsEntity?>

    @Query("SELECT * FROM exercise_mastery WHERE exerciseVariantId = :variantId LIMIT 1")
    suspend fun getExerciseMastery(variantId: Long): ExerciseMasteryEntity?

    @Query("SELECT * FROM exercise_mastery WHERE exerciseVariantId = :variantId LIMIT 1")
    fun observeExerciseMastery(variantId: Long): Flow<ExerciseMasteryEntity?>

    @Query("SELECT * FROM exercise_mastery ORDER BY accumulatedVolume DESC, exerciseVariantId ASC")
    fun observeExerciseMasteries(): Flow<List<ExerciseMasteryEntity>>

    @Query(
        """
        SELECT exercise_mastery.*, exercise_variants.name AS variantName
        FROM exercise_mastery
        INNER JOIN exercise_variants ON exercise_variants.id = exercise_mastery.exerciseVariantId
        ORDER BY exercise_mastery.accumulatedVolume DESC, exercise_variants.name ASC
        """,
    )
    fun observeExerciseMasteriesWithVariantNames(): Flow<List<ExerciseMasteryWithVariantName>>

    @Query("DELETE FROM exercise_mastery")
    suspend fun deleteAllExerciseMasteries()
}
