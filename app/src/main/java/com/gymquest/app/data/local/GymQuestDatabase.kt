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
import com.gymquest.app.data.local.entity.MartialBeltSettingsEntity
import com.gymquest.app.data.local.entity.MartialContentTechniqueCrossRef
import com.gymquest.app.data.local.entity.MartialPracticeItemEntity
import com.gymquest.app.data.local.entity.MartialPracticeSessionEntity
import com.gymquest.app.data.local.entity.MartialRankEntity
import com.gymquest.app.data.local.entity.MartialReminderSettingsEntity
import com.gymquest.app.data.local.entity.MartialReminderWindowEntity
import com.gymquest.app.data.local.entity.MartialSecondaryMissionEntity
import com.gymquest.app.data.local.entity.MartialStyleEntity
import com.gymquest.app.data.local.entity.MartialStanceEntity
import com.gymquest.app.data.local.entity.MartialContentStepEntity
import com.gymquest.app.data.local.entity.MartialTechnicalContentEntity
import com.gymquest.app.data.local.entity.MartialTechniqueEntity
import com.gymquest.app.data.local.entity.MartialQuestionCategoryEntity
import com.gymquest.app.data.local.entity.MartialQuestionEntity
import com.gymquest.app.data.local.entity.MartialAnswerOptionEntity
import com.gymquest.app.data.local.entity.MediaFileEntity
import com.gymquest.app.data.local.entity.MuscleGroupEntity
import com.gymquest.app.data.local.entity.UserAchievementEntity
import com.gymquest.app.data.local.entity.UserProfileEntity
import com.gymquest.app.data.local.entity.WorkoutExerciseEntity
import com.gymquest.app.data.local.entity.WorkoutSessionEntity
import com.gymquest.app.data.local.entity.WorkoutSetEntity
import com.gymquest.app.data.local.entity.WorkoutRoutineEntity
import com.gymquest.app.data.local.entity.RoutineDayEntity
import com.gymquest.app.data.local.entity.RoutineExerciseEntity
import com.gymquest.app.data.local.entity.RoutinePlannedSetEntity
import com.gymquest.app.data.local.dao.ExerciseDao
import com.gymquest.app.data.local.dao.MediaFileDao
import com.gymquest.app.data.local.dao.MachineDao
import com.gymquest.app.data.local.dao.MartialArtsDao
import com.gymquest.app.data.local.dao.MartialReminderDao
import com.gymquest.app.data.local.dao.MartialMiniGameDao
import com.gymquest.app.data.local.dao.ProgressDao
import com.gymquest.app.data.local.dao.WorkoutDao
import com.gymquest.app.data.local.dao.RoutineDao

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
        MartialBeltSettingsEntity::class,
        MartialStyleEntity::class,
        MartialRankEntity::class,
        MartialTechnicalContentEntity::class,
        MartialTechniqueEntity::class,
        MartialStanceEntity::class,
        MartialContentStepEntity::class,
        MartialContentTechniqueCrossRef::class,
        MartialPracticeSessionEntity::class,
        MartialPracticeItemEntity::class,
        MartialReminderSettingsEntity::class,
        MartialReminderWindowEntity::class,
        MartialSecondaryMissionEntity::class,
        MartialQuestionCategoryEntity::class,
        MartialQuestionEntity::class,
        MartialAnswerOptionEntity::class,
        WorkoutRoutineEntity::class,
        RoutineDayEntity::class,
        RoutineExerciseEntity::class,
        RoutinePlannedSetEntity::class,
    ],
    version = DatabaseConstants.SCHEMA_VERSION,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class GymQuestDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun mediaFileDao(): MediaFileDao
    abstract fun machineDao(): MachineDao
    abstract fun workoutDao(): WorkoutDao
    abstract fun progressDao(): ProgressDao
    abstract fun martialArtsDao(): MartialArtsDao
    abstract fun martialReminderDao(): MartialReminderDao
    abstract fun martialMiniGameDao(): MartialMiniGameDao
    abstract fun routineDao(): RoutineDao

    companion object {
        fun create(context: Context): GymQuestDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                GymQuestDatabase::class.java,
                DatabaseConstants.DATABASE_NAME,
            )
                .addMigrations(
                    DatabaseMigrations.MIGRATION_1_2,
                    DatabaseMigrations.MIGRATION_2_3,
                    DatabaseMigrations.MIGRATION_3_4,
                    DatabaseMigrations.MIGRATION_4_5,
                    DatabaseMigrations.MIGRATION_5_6,
                    DatabaseMigrations.MIGRATION_6_7,
                )
                .addCallback(DatabaseMigrations.INTEGRITY_CALLBACK)
                .build()
    }
}
