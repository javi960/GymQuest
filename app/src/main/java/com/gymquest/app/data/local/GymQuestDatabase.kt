package com.gymquest.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gymquest.app.data.local.entity.ExerciseBaseEntity
import com.gymquest.app.data.local.entity.ExerciseMasteryEntity
import com.gymquest.app.data.local.entity.ExerciseVariantEntity
import com.gymquest.app.data.local.entity.GymEntity
import com.gymquest.app.data.local.entity.GymMachineEntity
import com.gymquest.app.data.local.entity.AchievementEntity
import com.gymquest.app.data.local.entity.CharacterStatsEntity
import com.gymquest.app.data.local.entity.MartialArtEntity
import com.gymquest.app.data.local.entity.MartialContentTechniqueCrossRef
import com.gymquest.app.data.local.entity.MartialPracticeItemEntity
import com.gymquest.app.data.local.entity.MartialPracticeSessionEntity
import com.gymquest.app.data.local.entity.MartialRankEntity
import com.gymquest.app.data.local.entity.MartialReminderSettingsEntity
import com.gymquest.app.data.local.entity.MartialReminderWindowEntity
import com.gymquest.app.data.local.entity.MartialSecondaryMissionEntity
import com.gymquest.app.data.local.entity.MartialStyleEntity
import com.gymquest.app.data.local.entity.MartialTechnicalContentEntity
import com.gymquest.app.data.local.entity.MartialTechniqueEntity
import com.gymquest.app.data.local.entity.MediaFileEntity
import com.gymquest.app.data.local.entity.MuscleGroupEntity
import com.gymquest.app.data.local.entity.UserAchievementEntity
import com.gymquest.app.data.local.entity.UserProfileEntity
import com.gymquest.app.data.local.entity.WorkoutExerciseEntity
import com.gymquest.app.data.local.entity.WorkoutSessionEntity
import com.gymquest.app.data.local.entity.WorkoutSetEntity
import com.gymquest.app.data.local.dao.ExerciseDao
import com.gymquest.app.data.local.dao.MachineDao
import com.gymquest.app.data.local.dao.MartialArtsDao
import com.gymquest.app.data.local.dao.MartialReminderDao
import com.gymquest.app.data.local.dao.ProgressDao
import com.gymquest.app.data.local.dao.WorkoutDao

@Database(
    entities = [
        MuscleGroupEntity::class,
        ExerciseBaseEntity::class,
        ExerciseVariantEntity::class,
        GymEntity::class,
        GymMachineEntity::class,
        WorkoutSessionEntity::class,
        WorkoutExerciseEntity::class,
        WorkoutSetEntity::class,
        UserProfileEntity::class,
        CharacterStatsEntity::class,
        ExerciseMasteryEntity::class,
        AchievementEntity::class,
        UserAchievementEntity::class,
        MediaFileEntity::class,
        MartialArtEntity::class,
        MartialStyleEntity::class,
        MartialRankEntity::class,
        MartialTechnicalContentEntity::class,
        MartialTechniqueEntity::class,
        MartialContentTechniqueCrossRef::class,
        MartialPracticeSessionEntity::class,
        MartialPracticeItemEntity::class,
        MartialReminderSettingsEntity::class,
        MartialReminderWindowEntity::class,
        MartialSecondaryMissionEntity::class,
    ],
    version = DatabaseConstants.SCHEMA_VERSION,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class GymQuestDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun machineDao(): MachineDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun progressDao(): ProgressDao
    abstract fun martialArtsDao(): MartialArtsDao
    abstract fun martialReminderDao(): MartialReminderDao

    companion object {
        fun create(context: Context): GymQuestDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                GymQuestDatabase::class.java,
                DatabaseConstants.DATABASE_NAME,
            )
                .addMigrations(DatabaseMigrations.MIGRATION_1_2, DatabaseMigrations.MIGRATION_2_3)
                .addCallback(DatabaseMigrations.INTEGRITY_CALLBACK)
                .build()
    }
}
