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
        ACTIVE_SESSION_TRIGGERS.forEach(database::execSQL)
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
