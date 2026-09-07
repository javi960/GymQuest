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

@Dao
interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUserProfile(entity: UserProfileEntity)

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

    @Query("SELECT * FROM exercise_mastery WHERE exerciseVariantId = :variantId LIMIT 1")
    suspend fun getExerciseMastery(variantId: Long): ExerciseMasteryEntity?
}
