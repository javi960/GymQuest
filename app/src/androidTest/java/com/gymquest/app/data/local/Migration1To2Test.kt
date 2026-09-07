package com.gymquest.app.data.local

import android.database.sqlite.SQLiteConstraintException
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration1To2Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        GymQuestDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migratesVersionOneAndEnforcesOneActiveWorkoutSession() {
        helper.createDatabase(TEST_DATABASE_NAME, 1).close()

        val database = helper.runMigrationsAndValidate(
            TEST_DATABASE_NAME,
            2,
            true,
            DatabaseMigrations.MIGRATION_1_2,
        )
        database.execSQL(
            """
            INSERT INTO workout_sessions
                (startedAt, endedAt, durationSeconds, status, createdAt, updatedAt)
            VALUES ('2026-09-04T10:15:30Z', NULL, 0, 'ACTIVE', '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')
            """,
        )

        assertThrows(SQLiteConstraintException::class.java) {
            database.execSQL(
                """
                INSERT INTO workout_sessions
                    (startedAt, endedAt, durationSeconds, status, createdAt, updatedAt)
                VALUES ('2026-09-04T10:16:30Z', NULL, 0, 'ACTIVE', '2026-09-04T10:16:30Z', '2026-09-04T10:16:30Z')
                """,
            )
        }
        seedMartialStyle(database)
        database.execSQL(
            """
            INSERT INTO martial_practice_sessions (id, martialStyleId, startedAt, durationSeconds, createdAt, updatedAt)
            VALUES (1, 1, '2026-09-04T10:15:30Z', 0, '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')
            """,
        )
        database.execSQL(
            """
            INSERT INTO martial_ranks (martialStyleId, name, rankOrder, isCurrent, createdAt, updatedAt)
            VALUES (1, 'White belt', 1, 1, '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')
            """,
        )
        assertThrows(SQLiteConstraintException::class.java) {
            database.execSQL(
                """
                INSERT INTO martial_ranks (martialStyleId, name, rankOrder, isCurrent, createdAt, updatedAt)
                VALUES (1, 'Yellow belt', 2, 1, '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')
                """,
            )
        }
        assertThrows(SQLiteConstraintException::class.java) {
            database.execSQL(
                """
                INSERT INTO martial_practice_items (practiceSessionId, contentId, techniqueId, practiceType, createdAt, updatedAt)
                VALUES (1, NULL, NULL, 'practice', '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')
                """,
            )
        }
        assertThrows(SQLiteConstraintException::class.java) {
            database.execSQL(
                """
                INSERT INTO martial_secondary_missions (technicalContentId, techniqueId, scheduledFor, status, xpAwarded, createdAt, updatedAt)
                VALUES (NULL, NULL, '2026-09-04T10:15:30Z', 'scheduled', 0, '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')
                """,
            )
        }
        seedContentAndTechniques(database)
        assertThrows(SQLiteConstraintException::class.java) {
            database.execSQL(
                """
                INSERT INTO martial_practice_items (practiceSessionId, contentId, techniqueId, practiceType, createdAt, updatedAt)
                VALUES (1, 1, 1, 'practice', '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')
                """,
            )
        }
        assertThrows(SQLiteConstraintException::class.java) {
            database.execSQL(
                """
                INSERT INTO martial_secondary_missions (technicalContentId, techniqueId, scheduledFor, status, xpAwarded, createdAt, updatedAt)
                VALUES (1, 1, '2026-09-04T10:15:30Z', 'scheduled', 0, '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')
                """,
            )
        }
        database.execSQL("INSERT INTO martial_content_techniques (contentId, techniqueId, orderIndex) VALUES (1, 1, 0)")
        assertThrows(SQLiteConstraintException::class.java) {
            database.execSQL("INSERT INTO martial_content_techniques (contentId, techniqueId, orderIndex) VALUES (1, 2, 0)")
        }
        database.execSQL(
            """
            INSERT INTO martial_reminder_settings (id, enabled, missionsPerDay, privacyMode, maxDailyXp, createdAt, updatedAt)
            VALUES (1, 0, 1, 'generic', 20, '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')
            """,
        )
        database.execSQL(
            """
            INSERT INTO martial_reminder_windows (settingsId, startLocalTime, endLocalTime, enabled, sortOrder, createdAt, updatedAt)
            VALUES (1, '09:00', '10:00', 1, 0, '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')
            """,
        )
        assertThrows(SQLiteConstraintException::class.java) {
            database.execSQL(
                """
                INSERT INTO martial_reminder_windows (settingsId, startLocalTime, endLocalTime, enabled, sortOrder, createdAt, updatedAt)
                VALUES (1, '11:00', '12:00', 1, 0, '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')
                """,
            )
        }
        database.close()
    }

    private fun seedMartialStyle(database: androidx.sqlite.db.SupportSQLiteDatabase) {
        database.execSQL(
            "INSERT INTO martial_arts (id, name, isArchived, createdAt, updatedAt) VALUES (1, 'Karate', 0, '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')",
        )
        database.execSQL(
            "INSERT INTO martial_styles (id, martialArtId, name, isArchived, createdAt, updatedAt) VALUES (1, 1, 'Shotokan', 0, '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')",
        )
    }

    private fun seedContentAndTechniques(database: androidx.sqlite.db.SupportSQLiteDatabase) {
        database.execSQL(
            "INSERT INTO martial_technical_contents (id, martialStyleId, name, contentType, progressStatus, isArchived, createdAt, updatedAt) VALUES (1, 1, 'Heian Shodan', 'kata', 'learning', 0, '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')",
        )
        database.execSQL(
            "INSERT INTO martial_techniques (id, martialStyleId, name, isArchived, createdAt, updatedAt) VALUES (1, 1, 'Gedan Barai', 0, '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')",
        )
        database.execSQL(
            "INSERT INTO martial_techniques (id, martialStyleId, name, isArchived, createdAt, updatedAt) VALUES (2, 1, 'Oi Tsuki', 0, '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')",
        )
    }

    private companion object {
        const val TEST_DATABASE_NAME = "migration-1-2-test"
    }
}
