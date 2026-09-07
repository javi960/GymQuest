package com.gymquest.app.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class Migration2To3Test {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        GymQuestDatabase::class.java,
        emptyList(),
        FrameworkSQLiteOpenHelperFactory(),
    )

    @Test
    fun migratesLegacyWeightComparisonTypesWithoutLosingCatalogRows() {
        val database = helper.createDatabase(TEST_DATABASE_NAME, 2)
        database.execSQL("INSERT INTO muscle_groups (id, name, sortOrder, isArchived) VALUES (1, 'Chest', 0, 0)")
        database.execSQL(
            """
            INSERT INTO exercise_bases
                (id, name, primaryMuscleGroupId, isBuiltIn, isArchived, createdAt, updatedAt)
            VALUES (1, 'Press', 1, 0, 0, '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')
            """.trimIndent(),
        )
        database.execSQL(
            """
            INSERT INTO exercise_variants
                (id, exerciseBaseId, name, equipmentType, weightComparisonType, isBuiltIn, isArchived, createdAt, updatedAt)
            VALUES (1, 1, 'Barbell', 'BARBELL', 'DIRECT_WEIGHT', 0, 0, '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')
            """.trimIndent(),
        )
        database.execSQL(
            """
            INSERT INTO exercise_variants
                (id, exerciseBaseId, name, equipmentType, weightComparisonType, isBuiltIn, isArchived, createdAt, updatedAt)
            VALUES (3, 1, 'Bodyweight', 'BODYWEIGHT', 'BODYWEIGHT', 0, 0, '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')
            """.trimIndent(),
        )
        database.execSQL(
            """
            INSERT INTO exercise_variants
                (id, exerciseBaseId, name, equipmentType, weightComparisonType, isBuiltIn, isArchived, createdAt, updatedAt)
            VALUES (2, 1, 'Assisted', 'MACHINE', 'ASSISTED_BODYWEIGHT', 0, 0, '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')
            """.trimIndent(),
        )
        database.execSQL(
            """
            INSERT INTO gym_machines
                (id, gymId, name, weightComparisonType, isArchived, createdAt, updatedAt)
            VALUES (3, 1, 'Bodyweight station', 'BODYWEIGHT', 0, '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')
            """.trimIndent(),
        )
        database.execSQL(
            """
            INSERT INTO gyms (id, name, isDefault, isArchived, createdAt, updatedAt)
            VALUES (1, 'Gym', 1, 0, '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')
            """.trimIndent(),
        )
        database.execSQL(
            """
            INSERT INTO gym_machines
                (id, gymId, name, weightComparisonType, isArchived, createdAt, updatedAt)
            VALUES (1, 1, 'Cable', 'MACHINE_SPECIFIC', 0, '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')
            """.trimIndent(),
        )
        database.execSQL(
            """
            INSERT INTO gym_machines
                (id, gymId, name, weightComparisonType, isArchived, createdAt, updatedAt)
            VALUES (2, 1, 'Legacy machine', 'NOT_COMPARABLE', 0, '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')
            """.trimIndent(),
        )
        database.close()

        val migrated = helper.runMigrationsAndValidate(
            TEST_DATABASE_NAME,
            3,
            true,
            DatabaseMigrations.MIGRATION_2_3,
        )

        assertWeightComparisonType(migrated, "exercise_variants", 1, "TOTAL_WEIGHT")
        assertWeightComparisonType(migrated, "exercise_variants", 2, "ASSISTED_WEIGHT")
        assertWeightComparisonType(migrated, "exercise_variants", 3, "BODYWEIGHT")
        assertWeightComparisonType(migrated, "gym_machines", 1, "NOT_COMPARABLE_BETWEEN_MACHINES")
        assertWeightComparisonType(migrated, "gym_machines", 2, "NOT_COMPARABLE_BETWEEN_MACHINES")
        assertWeightComparisonType(migrated, "gym_machines", 3, "BODYWEIGHT")
        migrated.close()
    }

    @Test
    fun rejectsUnknownLegacyWeightComparisonTypes() {
        val database = helper.createDatabase(UNKNOWN_VALUE_DATABASE_NAME, 2)
        database.execSQL("INSERT INTO muscle_groups (id, name, sortOrder, isArchived) VALUES (1, 'Chest', 0, 0)")
        database.execSQL(
            """
            INSERT INTO exercise_bases
                (id, name, primaryMuscleGroupId, isBuiltIn, isArchived, createdAt, updatedAt)
            VALUES (1, 'Press', 1, 0, 0, '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')
            """.trimIndent(),
        )
        database.execSQL(
            """
            INSERT INTO exercise_variants
                (id, exerciseBaseId, name, equipmentType, weightComparisonType, isBuiltIn, isArchived, createdAt, updatedAt)
            VALUES (1, 1, 'Unknown', 'BARBELL', 'UNRECOGNIZED', 0, 0, '2026-09-04T10:15:30Z', '2026-09-04T10:15:30Z')
            """.trimIndent(),
        )
        database.close()

        assertThrows(IllegalStateException::class.java) {
            helper.runMigrationsAndValidate(
                UNKNOWN_VALUE_DATABASE_NAME,
                3,
                true,
                DatabaseMigrations.MIGRATION_2_3,
            )
        }
    }

    private fun assertWeightComparisonType(
        database: androidx.sqlite.db.SupportSQLiteDatabase,
        table: String,
        id: Long,
        expected: String,
    ) {
        database.query("SELECT weightComparisonType FROM $table WHERE id = $id").use { cursor ->
            cursor.moveToFirst()
            assertEquals(expected, cursor.getString(0))
            assertEquals(
                com.gymquest.app.domain.model.enums.WeightComparisonType.valueOf(expected),
                Converters().storageValueToWeightComparisonType(cursor.getString(0)),
            )
        }
    }

    private companion object {
        const val TEST_DATABASE_NAME = "migration-2-3-test"
        const val UNKNOWN_VALUE_DATABASE_NAME = "migration-2-3-unknown-value-test"
    }
}
