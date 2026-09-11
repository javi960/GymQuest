package com.gymquest.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.gymquest.app.data.local.entity.ExerciseBaseEntity
import com.gymquest.app.data.local.entity.ExerciseVariantEntity
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
import com.gymquest.app.data.local.entity.WeeklyTrainingPlanEntity
import com.gymquest.app.data.local.entity.WeeklyTrainingDayEntity
import com.gymquest.app.data.local.entity.TrainingSessionEntity
import com.gymquest.app.data.local.entity.WeeklyTrainingExerciseEntity
import com.gymquest.app.data.local.entity.TrainingSessionExerciseEntity
import com.gymquest.app.data.local.entity.TrainingSessionSetEntity
import com.gymquest.app.data.local.dao.ExerciseDao
import com.gymquest.app.data.local.dao.MediaFileDao
import com.gymquest.app.data.local.dao.MartialArtsDao
import com.gymquest.app.data.local.dao.MartialReminderDao
import com.gymquest.app.data.local.dao.MartialMiniGameDao
import com.gymquest.app.data.local.dao.WeeklyTrainingDao

@Database(
    entities = [
        MuscleGroupEntity::class,
        WeeklyTrainingPlanEntity::class,
        WeeklyTrainingDayEntity::class,
        TrainingSessionEntity::class,
        WeeklyTrainingExerciseEntity::class,
        TrainingSessionExerciseEntity::class,
        TrainingSessionSetEntity::class,
        ExerciseBaseEntity::class,
        ExerciseVariantEntity::class,
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
    ],
    version = DatabaseConstants.SCHEMA_VERSION,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class GymQuestDatabase : RoomDatabase() {
    abstract fun exerciseDao(): ExerciseDao
    abstract fun mediaFileDao(): MediaFileDao
    abstract fun martialArtsDao(): MartialArtsDao
    abstract fun martialReminderDao(): MartialReminderDao
    abstract fun martialMiniGameDao(): MartialMiniGameDao
    abstract fun weeklyTrainingDao(): WeeklyTrainingDao

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
                    DatabaseMigrations.MIGRATION_7_8,
                    DatabaseMigrations.MIGRATION_8_9,
                    DatabaseMigrations.MIGRATION_9_10,
                    DatabaseMigrations.MIGRATION_10_11,
                    DatabaseMigrations.MIGRATION_11_12,
                    DatabaseMigrations.MIGRATION_12_13,
                    DatabaseMigrations.MIGRATION_13_14,
                    DatabaseMigrations.MIGRATION_14_15,
                    DatabaseMigrations.MIGRATION_15_16,
                )
                .addCallback(DatabaseMigrations.INTEGRITY_CALLBACK)
                .build()
    }
}
