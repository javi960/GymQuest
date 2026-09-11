package com.gymquest.app.data.local

import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            validateVersionOneData(db)
            createUniqueIndexes(db)
            createIntegrityTriggers(db)
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            validateWeightComparisonTypes(db)
            migrateWeightComparisonTypes(db, "exercise_variants")
            migrateWeightComparisonTypes(db, "gym_machines")
        }
    }

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE media_files ADD COLUMN thumbnailUri TEXT")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS martial_stances (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    martialStyleId INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    translation TEXT,
                    description TEXT,
                    notes TEXT,
                    isArchived INTEGER NOT NULL,
                    createdAt TEXT NOT NULL,
                    updatedAt TEXT NOT NULL,
                    FOREIGN KEY(martialStyleId) REFERENCES martial_styles(id)
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS index_martial_stances_martialStyleId ON martial_stances(martialStyleId)")
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS martial_content_steps (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    technicalContentId INTEGER NOT NULL,
                    orderIndex INTEGER NOT NULL,
                    stanceId INTEGER,
                    techniqueId INTEGER,
                    direction TEXT NOT NULL,
                    side TEXT,
                    movementType TEXT,
                    displacement TEXT,
                    turnDegrees INTEGER,
                    angleDegrees INTEGER,
                    description TEXT,
                    hasKiai INTEGER NOT NULL,
                    hasPause INTEGER NOT NULL,
                    mediaFileId INTEGER,
                    createdAt TEXT NOT NULL,
                    updatedAt TEXT NOT NULL,
                    FOREIGN KEY(technicalContentId) REFERENCES martial_technical_contents(id) ON DELETE CASCADE,
                    FOREIGN KEY(stanceId) REFERENCES martial_stances(id),
                    FOREIGN KEY(techniqueId) REFERENCES martial_techniques(id)
                )
                """.trimIndent(),
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_martial_content_steps_technicalContentId_orderIndex ON martial_content_steps(technicalContentId, orderIndex)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_martial_content_steps_stanceId ON martial_content_steps(stanceId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_martial_content_steps_techniqueId ON martial_content_steps(techniqueId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_martial_content_steps_mediaFileId ON martial_content_steps(mediaFileId)")
        }
    }

    val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS martial_question_categories (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, createdAt TEXT NOT NULL, updatedAt TEXT NOT NULL)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_martial_question_categories_name ON martial_question_categories(name)")
            db.execSQL("CREATE TABLE IF NOT EXISTS martial_questions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, categoryId INTEGER NOT NULL, prompt TEXT NOT NULL, explanation TEXT NOT NULL, difficulty TEXT NOT NULL, isArchived INTEGER NOT NULL, createdAt TEXT NOT NULL, updatedAt TEXT NOT NULL, FOREIGN KEY(categoryId) REFERENCES martial_question_categories(id))")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_martial_questions_categoryId ON martial_questions(categoryId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_martial_questions_isArchived ON martial_questions(isArchived)")
            db.execSQL("CREATE TABLE IF NOT EXISTS martial_answer_options (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, questionId INTEGER NOT NULL, text TEXT NOT NULL, isCorrect INTEGER NOT NULL, sortOrder INTEGER NOT NULL, createdAt TEXT NOT NULL, updatedAt TEXT NOT NULL, FOREIGN KEY(questionId) REFERENCES martial_questions(id) ON DELETE CASCADE)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_martial_answer_options_questionId ON martial_answer_options(questionId)")
        }
    }

    val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS martial_belt_settings (id INTEGER NOT NULL PRIMARY KEY, belt TEXT NOT NULL, updatedAt TEXT NOT NULL)",
            )
        }
    }

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS workout_routines (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, notes TEXT, isArchived INTEGER NOT NULL, createdAt TEXT NOT NULL, updatedAt TEXT NOT NULL)")
            db.execSQL("CREATE TABLE IF NOT EXISTS routine_days (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, routineId INTEGER NOT NULL, weekday TEXT NOT NULL, label TEXT, notes TEXT, createdAt TEXT NOT NULL, updatedAt TEXT NOT NULL, FOREIGN KEY(routineId) REFERENCES workout_routines(id) ON DELETE CASCADE)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_routine_days_routineId_weekday ON routine_days(routineId, weekday)")
            db.execSQL("CREATE TABLE IF NOT EXISTS routine_exercises (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, routineDayId INTEGER NOT NULL, exerciseVariantId INTEGER NOT NULL, gymMachineId INTEGER, orderIndex INTEGER NOT NULL, notes TEXT, createdAt TEXT NOT NULL, updatedAt TEXT NOT NULL, FOREIGN KEY(routineDayId) REFERENCES routine_days(id) ON DELETE CASCADE, FOREIGN KEY(exerciseVariantId) REFERENCES exercise_variants(id), FOREIGN KEY(gymMachineId) REFERENCES gym_machines(id))")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_routine_exercises_routineDayId_orderIndex ON routine_exercises(routineDayId, orderIndex)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_routine_exercises_exerciseVariantId ON routine_exercises(exerciseVariantId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_routine_exercises_gymMachineId ON routine_exercises(gymMachineId)")
            db.execSQL("CREATE TABLE IF NOT EXISTS routine_planned_sets (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, routineExerciseId INTEGER NOT NULL, setNumber INTEGER NOT NULL, targetWeight REAL, targetReps INTEGER, createdAt TEXT NOT NULL, updatedAt TEXT NOT NULL, FOREIGN KEY(routineExerciseId) REFERENCES routine_exercises(id) ON DELETE CASCADE)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_routine_planned_sets_routineExerciseId_setNumber ON routine_planned_sets(routineExerciseId, setNumber)")
        }
    }

    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "ALTER TABLE user_profiles ADD COLUMN defaultRestSeconds INTEGER NOT NULL DEFAULT 90",
            )
        }
    }

    /** Removes the retired weekly-routine feature and all of its local data. */
    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("DROP TABLE IF EXISTS routine_planned_sets")
            db.execSQL("DROP TABLE IF EXISTS routine_exercises")
            db.execSQL("DROP TABLE IF EXISTS routine_days")
            db.execSQL("DROP TABLE IF EXISTS workout_routines")
            db.execSQL("DROP TABLE IF EXISTS workout_sets")
            db.execSQL("DROP TABLE IF EXISTS workout_exercises")
            db.execSQL("DROP TABLE IF EXISTS workout_sessions")
            db.execSQL("DROP TABLE IF EXISTS exercise_mastery")
            db.execSQL("DROP TABLE IF EXISTS character_stats")
            db.execSQL("DROP TABLE IF EXISTS user_achievements")
            db.execSQL("DROP TABLE IF EXISTS achievements")
            db.execSQL("DROP TABLE IF EXISTS gym_machines")
            db.execSQL("DROP TABLE IF EXISTS gyms")
            db.execSQL("DROP TABLE IF EXISTS user_profiles")
        }
    }

    /** Adds structured educational content to the offline exercise catalogue. */
    val MIGRATION_9_10 = object : Migration(9, 10) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE exercise_bases ADD COLUMN secondaryMuscles TEXT")
            db.execSQL("ALTER TABLE exercise_bases ADD COLUMN instructions TEXT")
            db.execSQL("ALTER TABLE exercise_bases ADD COLUMN techniqueTips TEXT")
            db.execSQL("ALTER TABLE exercise_bases ADD COLUMN commonMistakes TEXT")
        }
    }

    /** Restores weekly planning without tying planned days to calendar dates. */
    val MIGRATION_10_11 = object : Migration(10, 11) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("CREATE TABLE IF NOT EXISTS weekly_training_plans (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, notes TEXT, isArchived INTEGER NOT NULL, createdAt TEXT NOT NULL, updatedAt TEXT NOT NULL)")
            db.execSQL("CREATE TABLE IF NOT EXISTS weekly_training_days (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, planId INTEGER NOT NULL, dayKey TEXT NOT NULL, label TEXT, sortOrder INTEGER NOT NULL, createdAt TEXT NOT NULL, updatedAt TEXT NOT NULL, FOREIGN KEY(planId) REFERENCES weekly_training_plans(id) ON DELETE CASCADE)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_weekly_training_days_planId_dayKey ON weekly_training_days(planId, dayKey)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_weekly_training_days_planId_sortOrder ON weekly_training_days(planId, sortOrder)")
            db.execSQL("CREATE TABLE IF NOT EXISTS training_sessions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, planId INTEGER NOT NULL, plannedDayId INTEGER NOT NULL, planNameSnapshot TEXT NOT NULL, plannedDaySnapshot TEXT NOT NULL, startedAt TEXT NOT NULL, endedAt TEXT, createdAt TEXT NOT NULL, updatedAt TEXT NOT NULL, FOREIGN KEY(planId) REFERENCES weekly_training_plans(id), FOREIGN KEY(plannedDayId) REFERENCES weekly_training_days(id))")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_training_sessions_startedAt ON training_sessions(startedAt)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_training_sessions_planId ON training_sessions(planId)")
            db.execSQL("CREATE INDEX IF NOT EXISTS index_training_sessions_plannedDayId ON training_sessions(plannedDayId)")
        }
    }

    /** Stores a verified catalogue GIF URL separately from media chosen by the user. */
    val MIGRATION_11_12 = object : Migration(11, 12) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE exercise_bases ADD COLUMN builtInGifUrl TEXT")
        }
    }
    val MIGRATION_12_13 = object : Migration(12, 13) { override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS weekly_training_exercises (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, dayId INTEGER NOT NULL, exerciseVariantId INTEGER NOT NULL, sortOrder INTEGER NOT NULL, targetSets INTEGER NOT NULL, targetReps INTEGER, targetWeight REAL, restSeconds INTEGER, createdAt TEXT NOT NULL, updatedAt TEXT NOT NULL, FOREIGN KEY(dayId) REFERENCES weekly_training_days(id) ON DELETE CASCADE, FOREIGN KEY(exerciseVariantId) REFERENCES exercise_variants(id))")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_weekly_training_exercises_dayId_sortOrder ON weekly_training_exercises(dayId, sortOrder)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_weekly_training_exercises_exerciseVariantId ON weekly_training_exercises(exerciseVariantId)")
    } }
    val MIGRATION_13_14 = object : Migration(13, 14) { override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS training_session_exercises (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, sessionId INTEGER NOT NULL, exerciseVariantId INTEGER NOT NULL, sortOrder INTEGER NOT NULL, plannedSets INTEGER NOT NULL, plannedReps INTEGER, plannedWeight REAL, plannedRestSeconds INTEGER, createdAt TEXT NOT NULL, updatedAt TEXT NOT NULL, FOREIGN KEY(sessionId) REFERENCES training_sessions(id) ON DELETE CASCADE, FOREIGN KEY(exerciseVariantId) REFERENCES exercise_variants(id))")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_training_session_exercises_sessionId_sortOrder ON training_session_exercises(sessionId, sortOrder)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_training_session_exercises_exerciseVariantId ON training_session_exercises(exerciseVariantId)")
    } }
    val MIGRATION_14_15 = object : Migration(14, 15) { override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS training_session_sets (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, sessionExerciseId INTEGER NOT NULL, setNumber INTEGER NOT NULL, weight REAL NOT NULL, reps INTEGER NOT NULL, setType TEXT NOT NULL, completedAt TEXT NOT NULL, createdAt TEXT NOT NULL, updatedAt TEXT NOT NULL, FOREIGN KEY(sessionExerciseId) REFERENCES training_session_exercises(id) ON DELETE CASCADE)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_training_session_sets_sessionExerciseId_setNumber ON training_session_sets(sessionExerciseId, setNumber)")
    } }
    /** Makes routine provenance optional for a true free session. */
    val MIGRATION_15_16 = object : Migration(15, 16) { override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE training_sessions_new (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, planId INTEGER, plannedDayId INTEGER, planNameSnapshot TEXT NOT NULL, plannedDaySnapshot TEXT NOT NULL, startedAt TEXT NOT NULL, endedAt TEXT, createdAt TEXT NOT NULL, updatedAt TEXT NOT NULL, FOREIGN KEY(planId) REFERENCES weekly_training_plans(id), FOREIGN KEY(plannedDayId) REFERENCES weekly_training_days(id))")
        db.execSQL("INSERT INTO training_sessions_new (id, planId, plannedDayId, planNameSnapshot, plannedDaySnapshot, startedAt, endedAt, createdAt, updatedAt) SELECT id, planId, plannedDayId, planNameSnapshot, plannedDaySnapshot, startedAt, endedAt, createdAt, updatedAt FROM training_sessions")
        db.execSQL("DROP TABLE training_sessions")
        db.execSQL("ALTER TABLE training_sessions_new RENAME TO training_sessions")
        db.execSQL("CREATE INDEX index_training_sessions_startedAt ON training_sessions(startedAt)")
        db.execSQL("CREATE INDEX index_training_sessions_planId ON training_sessions(planId)")
        db.execSQL("CREATE INDEX index_training_sessions_plannedDayId ON training_sessions(plannedDayId)")
    } }

    val INTEGRITY_CALLBACK = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            createIntegrityTriggers(db)
        }
    }

    private fun createUniqueIndexes(database: SupportSQLiteDatabase) {
        database.execSQL("DROP INDEX IF EXISTS `index_workout_exercises_workoutSessionId_orderIndex`")
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_workout_exercises_workoutSessionId_orderIndex` " +
                "ON `workout_exercises` (`workoutSessionId`, `orderIndex`)",
        )
        database.execSQL("DROP INDEX IF EXISTS `index_user_achievements_achievementId`")
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_user_achievements_achievementId` " +
                "ON `user_achievements` (`achievementId`)",
        )
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_martial_content_techniques_contentId_orderIndex` " +
                "ON `martial_content_techniques` (`contentId`, `orderIndex`)",
        )
        database.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_martial_reminder_windows_settingsId_sortOrder` " +
                "ON `martial_reminder_windows` (`settingsId`, `sortOrder`)",
        )
    }

    private fun validateVersionOneData(database: SupportSQLiteDatabase) {
        requireNoRows(
            database,
            """
            SELECT 1 FROM workout_exercises
            GROUP BY workoutSessionId, orderIndex HAVING COUNT(*) > 1 LIMIT 1
            """.trimIndent(),
            "Cannot migrate: duplicate workout exercise order exists.",
        )
        requireNoRows(
            database,
            """
            SELECT 1 FROM martial_content_techniques
            GROUP BY contentId, orderIndex HAVING COUNT(*) > 1 LIMIT 1
            """.trimIndent(),
            "Cannot migrate: duplicate martial technique order exists.",
        )
        requireNoRows(
            database,
            """
            SELECT 1 FROM martial_reminder_windows
            GROUP BY settingsId, sortOrder HAVING COUNT(*) > 1 LIMIT 1
            """.trimIndent(),
            "Cannot migrate: duplicate martial reminder window order exists.",
        )
        requireNoRows(
            database,
            "SELECT 1 FROM user_achievements GROUP BY achievementId HAVING COUNT(*) > 1 LIMIT 1",
            "Cannot migrate: an achievement is unlocked more than once.",
        )
        requireNoRows(
            database,
            """
            SELECT COUNT(*) FROM workout_sessions
            WHERE status = 'ACTIVE' AND endedAt IS NULL
            HAVING COUNT(*) > 1
            """.trimIndent(),
            "Cannot migrate: more than one workout session is active.",
        )
        requireNoRows(
            database,
            """
            SELECT 1 FROM martial_ranks WHERE isCurrent = 1
            GROUP BY martialStyleId HAVING COUNT(*) > 1 LIMIT 1
            """.trimIndent(),
            "Cannot migrate: more than one current martial rank exists for a style.",
        )
        requireNoRows(
            database,
            """
            SELECT 1 FROM martial_practice_items
            WHERE (contentId IS NULL AND techniqueId IS NULL)
               OR (contentId IS NOT NULL AND techniqueId IS NOT NULL)
            LIMIT 1
            """.trimIndent(),
            "Cannot migrate: a martial practice item has an invalid technical target.",
        )
        requireNoRows(
            database,
            """
            SELECT 1 FROM martial_secondary_missions
            WHERE (technicalContentId IS NULL AND techniqueId IS NULL)
               OR (technicalContentId IS NOT NULL AND techniqueId IS NOT NULL)
            LIMIT 1
            """.trimIndent(),
            "Cannot migrate: a martial secondary mission has an invalid technical target.",
        )
    }

    private fun validateWeightComparisonTypes(database: SupportSQLiteDatabase) {
        listOf("exercise_variants", "gym_machines").forEach { table ->
            requireNoRows(
                database,
                """
                SELECT 1 FROM $table
                WHERE weightComparisonType NOT IN (
                    'DIRECT_WEIGHT',
                    'MACHINE_SPECIFIC',
                    'BODYWEIGHT',
                    'ASSISTED_BODYWEIGHT',
                    'NOT_COMPARABLE'
                )
                LIMIT 1
                """.trimIndent(),
                "Cannot migrate: unknown weight comparison type exists in $table.",
            )
        }
    }

    private fun migrateWeightComparisonTypes(database: SupportSQLiteDatabase, table: String) {
        database.execSQL(
            """
            UPDATE $table
            SET weightComparisonType = CASE weightComparisonType
                WHEN 'DIRECT_WEIGHT' THEN 'TOTAL_WEIGHT'
                WHEN 'ASSISTED_BODYWEIGHT' THEN 'ASSISTED_WEIGHT'
                WHEN 'MACHINE_SPECIFIC' THEN 'NOT_COMPARABLE_BETWEEN_MACHINES'
                WHEN 'NOT_COMPARABLE' THEN 'NOT_COMPARABLE_BETWEEN_MACHINES'
                ELSE weightComparisonType
            END
            """.trimIndent(),
        )
    }

    private fun requireNoRows(database: SupportSQLiteDatabase, query: String, message: String) {
        database.query(query).use { cursor ->
            check(!cursor.moveToFirst()) { message }
        }
    }

    private fun createIntegrityTriggers(database: SupportSQLiteDatabase) {
        CURRENT_RANK_TRIGGERS.forEach(database::execSQL)
        PRACTICE_ITEM_TARGET_TRIGGERS.forEach(database::execSQL)
        SECONDARY_MISSION_TARGET_TRIGGERS.forEach(database::execSQL)
    }

    private val ACTIVE_SESSION_TRIGGERS = listOf(
        """
        CREATE TRIGGER IF NOT EXISTS prevent_duplicate_active_workout_session_insert
        BEFORE INSERT ON workout_sessions
        WHEN NEW.status = 'ACTIVE' AND NEW.endedAt IS NULL
          AND EXISTS (SELECT 1 FROM workout_sessions WHERE status = 'ACTIVE' AND endedAt IS NULL)
        BEGIN SELECT RAISE(ABORT, 'Only one workout session can be active'); END
        """.trimIndent(),
        """
        CREATE TRIGGER IF NOT EXISTS prevent_duplicate_active_workout_session_update
        BEFORE UPDATE OF status, endedAt ON workout_sessions
        WHEN NEW.status = 'ACTIVE' AND NEW.endedAt IS NULL
          AND EXISTS (SELECT 1 FROM workout_sessions WHERE id != NEW.id AND status = 'ACTIVE' AND endedAt IS NULL)
        BEGIN SELECT RAISE(ABORT, 'Only one workout session can be active'); END
        """.trimIndent(),
    )

    private val CURRENT_RANK_TRIGGERS = listOf(
        """
        CREATE TRIGGER IF NOT EXISTS prevent_duplicate_current_martial_rank_insert
        BEFORE INSERT ON martial_ranks
        WHEN NEW.isCurrent = 1
          AND EXISTS (SELECT 1 FROM martial_ranks WHERE martialStyleId = NEW.martialStyleId AND isCurrent = 1)
        BEGIN SELECT RAISE(ABORT, 'Only one martial rank can be current per style'); END
        """.trimIndent(),
        """
        CREATE TRIGGER IF NOT EXISTS prevent_duplicate_current_martial_rank_update
        BEFORE UPDATE OF isCurrent, martialStyleId ON martial_ranks
        WHEN NEW.isCurrent = 1
          AND EXISTS (SELECT 1 FROM martial_ranks WHERE id != NEW.id AND martialStyleId = NEW.martialStyleId AND isCurrent = 1)
        BEGIN SELECT RAISE(ABORT, 'Only one martial rank can be current per style'); END
        """.trimIndent(),
    )

    private val PRACTICE_ITEM_TARGET_TRIGGERS = listOf(
        targetTrigger("martial_practice_items", "contentId", "techniqueId", "practice_item", "insert", "INSERT"),
        targetTrigger("martial_practice_items", "contentId", "techniqueId", "practice_item", "update", "UPDATE OF contentId, techniqueId"),
    )

    private val SECONDARY_MISSION_TARGET_TRIGGERS = listOf(
        targetTrigger("martial_secondary_missions", "technicalContentId", "techniqueId", "secondary_mission", "insert", "INSERT"),
        targetTrigger("martial_secondary_missions", "technicalContentId", "techniqueId", "secondary_mission", "update", "UPDATE OF technicalContentId, techniqueId"),
    )

    private fun targetTrigger(
        table: String,
        firstColumn: String,
        secondColumn: String,
        namePrefix: String,
        operationName: String,
        operation: String,
    ) = """
        CREATE TRIGGER IF NOT EXISTS validate_${namePrefix}_target_$operationName
        BEFORE $operation ON $table
        WHEN (NEW.$firstColumn IS NULL AND NEW.$secondColumn IS NULL)
          OR (NEW.$firstColumn IS NOT NULL AND NEW.$secondColumn IS NOT NULL)
        BEGIN SELECT RAISE(ABORT, 'Exactly one technical target is required'); END
    """.trimIndent()
}
