package com.gymquest.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gymquest.app.data.local.entity.CharacterStatsEntity
import com.gymquest.app.data.local.entity.ExerciseBaseEntity
import com.gymquest.app.data.local.entity.ExerciseVariantEntity
import com.gymquest.app.data.local.entity.MartialArtEntity
import com.gymquest.app.data.local.entity.MartialReminderSettingsEntity
import com.gymquest.app.data.local.entity.MuscleGroupEntity
import com.gymquest.app.data.local.entity.UserProfileEntity
import com.gymquest.app.data.local.entity.WorkoutSessionEntity
import com.gymquest.app.domain.model.enums.EquipmentType
import com.gymquest.app.domain.model.enums.WeightComparisonType
import java.time.Instant
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GymQuestDaoTest {
    private lateinit var database: GymQuestDatabase
    private val timestamp = Instant.parse("2026-09-04T10:15:30Z")

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(context, GymQuestDatabase::class.java)
            .addCallback(DatabaseMigrations.INTEGRITY_CALLBACK)
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun persistsAndReadsTheMinimalGymData() = runBlocking {
        val muscleGroupId = database.exerciseDao().insertMuscleGroup(
            MuscleGroupEntity(name = "Chest", sortOrder = 1),
        )
        val baseId = database.exerciseDao().insertExerciseBase(
            ExerciseBaseEntity(
                name = "Bench press",
                primaryMuscleGroupId = muscleGroupId,
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
        )
        database.exerciseDao().insertExerciseVariant(
            ExerciseVariantEntity(
                exerciseBaseId = baseId,
                name = "Barbell",
                equipmentType = EquipmentType.BARBELL,
                weightComparisonType = WeightComparisonType.TOTAL_WEIGHT,
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
        )
        database.workoutDao().insertSession(
            WorkoutSessionEntity(startedAt = timestamp, createdAt = timestamp, updatedAt = timestamp),
        )
        database.progressDao().upsertUserProfile(
            UserProfileEntity(createdAt = timestamp, updatedAt = timestamp),
        )
        database.progressDao().upsertCharacterStats(
            CharacterStatsEntity(userProfileId = UserProfileEntity.LOCAL_USER_ID, updatedAt = timestamp),
        )

        assertEquals(1, database.exerciseDao().getActiveExerciseVariants().size)
        assertEquals(timestamp, database.workoutDao().getActiveSession()?.startedAt)
        assertEquals(1, database.progressDao().getCharacterStats()?.level)
    }

    @Test
    fun excludesVariantsWhoseExerciseBaseIsArchived() = runBlocking {
        val muscleGroupId = database.exerciseDao().insertMuscleGroup(
            MuscleGroupEntity(name = "Back", sortOrder = 1),
        )
        val baseId = database.exerciseDao().insertExerciseBase(
            ExerciseBaseEntity(
                name = "Row",
                primaryMuscleGroupId = muscleGroupId,
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
        )
        database.exerciseDao().insertExerciseVariant(
            ExerciseVariantEntity(
                exerciseBaseId = baseId,
                name = "Cable",
                equipmentType = EquipmentType.CABLE,
                weightComparisonType = WeightComparisonType.TOTAL_WEIGHT,
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
        )

        database.exerciseDao().archiveExerciseBase(baseId, timestamp)

        assertEquals(0, database.exerciseDao().getActiveExerciseVariants().size)
    }

    @Test
    fun observesOnlyActiveCatalogDataInStableOrder() = runBlocking {
        val chestId = database.exerciseDao().insertMuscleGroup(
            MuscleGroupEntity(name = "Chest", sortOrder = 2),
        )
        val backId = database.exerciseDao().insertMuscleGroup(
            MuscleGroupEntity(name = "Back", sortOrder = 1),
        )
        val rowId = database.exerciseDao().insertExerciseBase(
            ExerciseBaseEntity(
                name = "Row",
                primaryMuscleGroupId = backId,
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
        )
        val benchId = database.exerciseDao().insertExerciseBase(
            ExerciseBaseEntity(
                name = "Bench press",
                primaryMuscleGroupId = chestId,
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
        )
        val cableVariantId = database.exerciseDao().insertExerciseVariant(
            ExerciseVariantEntity(
                exerciseBaseId = rowId,
                name = "Cable",
                equipmentType = EquipmentType.CABLE,
                weightComparisonType = WeightComparisonType.TOTAL_WEIGHT,
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
        )
        database.exerciseDao().insertExerciseVariant(
            ExerciseVariantEntity(
                exerciseBaseId = benchId,
                name = "Barbell",
                equipmentType = EquipmentType.BARBELL,
                weightComparisonType = WeightComparisonType.TOTAL_WEIGHT,
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
        )

        assertEquals(listOf("Back", "Chest"), database.exerciseDao().observeActiveMuscleGroups().first().map { it.name })
        assertEquals(listOf("Bench press", "Row"), database.exerciseDao().observeActiveExerciseBases().first().map { it.name })
        assertEquals(listOf("Cable"), database.exerciseDao().observeVariantsForActiveExerciseBase(rowId).first().map { it.name })

        database.exerciseDao().archiveExerciseVariant(cableVariantId, timestamp)

        assertEquals(emptyList<String>(), database.exerciseDao().observeVariantsForActiveExerciseBase(rowId).first().map { it.name })
        assertEquals(listOf("Barbell"), database.exerciseDao().observeActiveExerciseVariants().first().map { it.name })
    }

    @Test
    fun savesProgressUsingItsNaturalKeys() = runBlocking {
        database.progressDao().upsertUserProfile(
            UserProfileEntity(createdAt = timestamp, updatedAt = timestamp),
        )
        database.progressDao().upsertCharacterStats(
            CharacterStatsEntity(userProfileId = UserProfileEntity.LOCAL_USER_ID, level = 1, updatedAt = timestamp),
        )
        database.progressDao().upsertCharacterStats(
            CharacterStatsEntity(userProfileId = UserProfileEntity.LOCAL_USER_ID, level = 2, updatedAt = timestamp),
        )

        assertEquals(2, database.progressDao().getCharacterStats()?.level)
    }

    @Test
    fun persistsAndReadsTheMinimalMartialData() = runBlocking {
        database.martialArtsDao().insertMartialArt(
            MartialArtEntity(name = "Karate", createdAt = timestamp, updatedAt = timestamp),
        )
        database.martialReminderDao().upsertSettings(
            MartialReminderSettingsEntity(createdAt = timestamp, updatedAt = timestamp),
        )

        assertEquals(1, database.martialArtsDao().getActiveMartialArts().size)
        assertEquals(1, database.martialReminderDao().getSettings()?.missionsPerDay)
    }
}
