package com.gymquest.app.data.local

import androidx.room.Room
import android.database.sqlite.SQLiteConstraintException
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gymquest.app.data.local.entity.ExerciseBaseEntity
import com.gymquest.app.data.local.entity.ExerciseVariantEntity
import com.gymquest.app.data.local.entity.MartialArtEntity
import com.gymquest.app.data.local.entity.MartialReminderSettingsEntity
import com.gymquest.app.data.local.entity.MartialReminderWindowEntity
import com.gymquest.app.data.local.entity.MartialSecondaryMissionEntity
import com.gymquest.app.data.local.entity.MartialStyleEntity
import com.gymquest.app.data.local.entity.MartialTechnicalContentEntity
import com.gymquest.app.data.local.entity.MuscleGroupEntity
import com.gymquest.app.data.local.entity.WorkoutExerciseEntity
import com.gymquest.app.data.local.entity.WorkoutSessionEntity
import com.gymquest.app.domain.model.enums.EquipmentType
import com.gymquest.app.domain.model.enums.WeightComparisonType
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DaoIntegrityTest {
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
    fun rejectsAnExerciseVariantWithoutItsExerciseBase() {
        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking {
                database.exerciseDao().insertExerciseVariant(
                    ExerciseVariantEntity(
                        exerciseBaseId = 999,
                        name = "Invalid",
                        equipmentType = EquipmentType.BARBELL,
                        weightComparisonType = WeightComparisonType.TOTAL_WEIGHT,
                        createdAt = timestamp,
                        updatedAt = timestamp,
                    ),
                )
            }
        }
    }

    @Test
    fun rejectsChildrenWithoutTheirRequiredForeignKeys() {
        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking {
                database.workoutDao().insertWorkoutExercise(
                    WorkoutExerciseEntity(
                        workoutSessionId = 999,
                        exerciseVariantId = 999,
                        orderIndex = 0,
                        createdAt = timestamp,
                        updatedAt = timestamp,
                    ),
                )
            }
        }
        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking {
                database.martialReminderDao().insertReminderWindow(
                    MartialReminderWindowEntity(
                        settingsId = 999,
                        startLocalTime = "09:00",
                        endLocalTime = "10:00",
                        sortOrder = 0,
                        createdAt = timestamp,
                        updatedAt = timestamp,
                    ),
                )
            }
        }
        assertThrows(SQLiteConstraintException::class.java) {
            runBlocking {
                database.martialReminderDao().insertSecondaryMission(
                    MartialSecondaryMissionEntity(
                        technicalContentId = 999,
                        scheduledFor = timestamp,
                        createdAt = timestamp,
                        updatedAt = timestamp,
                    ),
                )
            }
        }
    }

    @Test
    fun archivesAVariantWithoutHidingItsActiveSiblings() = runBlocking {
        val baseId = createExerciseBase()
        val archivedVariantId = database.exerciseDao().insertExerciseVariant(createVariant(baseId, "Archived"))
        database.exerciseDao().insertExerciseVariant(createVariant(baseId, "Active"))

        database.exerciseDao().archiveExerciseVariant(archivedVariantId, timestamp)

        assertEquals(listOf("Active"), database.exerciseDao().getActiveExerciseVariants().map { it.name })
        assertEquals(true, database.exerciseDao().getExerciseVariant(archivedVariantId)?.isArchived)
        assertEquals(listOf("Active", "Archived"), database.exerciseDao().getVariantsForExerciseBase(baseId).map { it.name })
    }

    @Test
    fun returnsWorkoutExercisesAndReminderWindowsInConfiguredOrder() = runBlocking {
        val baseId = createExerciseBase()
        val firstVariantId = database.exerciseDao().insertExerciseVariant(createVariant(baseId, "First"))
        val secondVariantId = database.exerciseDao().insertExerciseVariant(createVariant(baseId, "Second"))
        val sessionId = database.workoutDao().insertSession(
            WorkoutSessionEntity(startedAt = timestamp, createdAt = timestamp, updatedAt = timestamp),
        )
        database.workoutDao().insertWorkoutExercise(
            WorkoutExerciseEntity(
                workoutSessionId = sessionId,
                exerciseVariantId = secondVariantId,
                orderIndex = 1,
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
        )
        database.workoutDao().insertWorkoutExercise(
            WorkoutExerciseEntity(
                workoutSessionId = sessionId,
                exerciseVariantId = firstVariantId,
                orderIndex = 0,
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
        )
        database.martialReminderDao().upsertSettings(
            MartialReminderSettingsEntity(createdAt = timestamp, updatedAt = timestamp),
        )
        database.martialReminderDao().insertReminderWindow(
            MartialReminderWindowEntity(startLocalTime = "18:00", endLocalTime = "19:00", sortOrder = 1, createdAt = timestamp, updatedAt = timestamp),
        )
        database.martialReminderDao().insertReminderWindow(
            MartialReminderWindowEntity(startLocalTime = "09:00", endLocalTime = "10:00", sortOrder = 0, createdAt = timestamp, updatedAt = timestamp),
        )
        database.martialReminderDao().insertReminderWindow(
            MartialReminderWindowEntity(startLocalTime = "12:00", endLocalTime = "13:00", sortOrder = 2, createdAt = timestamp, updatedAt = timestamp),
        )

        assertEquals(listOf(0, 1), database.workoutDao().getWorkoutExercisesForSession(sessionId).map { it.orderIndex })
        assertEquals(listOf(0, 1, 2), database.martialReminderDao().getReminderWindows().map { it.sortOrder })
    }

    @Test
    fun completesEachPendingMissionOnlyOnce() = runBlocking {
        val contentId = createMartialContent()
        val missionId = database.martialReminderDao().insertSecondaryMission(
            MartialSecondaryMissionEntity(
                technicalContentId = contentId,
                scheduledFor = timestamp,
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
        )

        val firstTransition = database.martialReminderDao().transitionPendingMission(
            missionId = missionId,
            newStatus = "completed",
            completedAt = timestamp,
            xpAwarded = 5,
            updatedAt = timestamp,
        )
        val repeatedTransition = database.martialReminderDao().transitionPendingMission(
            missionId = missionId,
            newStatus = "completed",
            completedAt = timestamp,
            xpAwarded = 5,
            updatedAt = timestamp,
        )

        assertEquals(1, firstTransition)
        assertEquals(0, repeatedTransition)
        assertEquals(emptyList<Long>(), database.martialReminderDao().getPendingMissions(timestamp).map { it.id })
        assertEquals("completed", database.martialReminderDao().getSecondaryMission(missionId)?.status)
        assertEquals(5L, database.martialReminderDao().getSecondaryMission(missionId)?.xpAwarded)
    }

    private suspend fun createExerciseBase(): Long {
        val muscleGroupId = database.exerciseDao().insertMuscleGroup(MuscleGroupEntity(name = "Legs", sortOrder = 1))
        return database.exerciseDao().insertExerciseBase(
            ExerciseBaseEntity(name = "Squat", primaryMuscleGroupId = muscleGroupId, createdAt = timestamp, updatedAt = timestamp),
        )
    }

    private fun createVariant(baseId: Long, name: String) = ExerciseVariantEntity(
        exerciseBaseId = baseId,
        name = name,
        equipmentType = EquipmentType.BARBELL,
        weightComparisonType = WeightComparisonType.TOTAL_WEIGHT,
        createdAt = timestamp,
        updatedAt = timestamp,
    )

    private suspend fun createMartialContent(): Long {
        val artId = database.martialArtsDao().insertMartialArt(
            MartialArtEntity(name = "Karate", createdAt = timestamp, updatedAt = timestamp),
        )
        val styleId = database.martialArtsDao().insertMartialStyle(
            MartialStyleEntity(martialArtId = artId, name = "Shotokan", createdAt = timestamp, updatedAt = timestamp),
        )
        return database.martialArtsDao().insertTechnicalContent(
            MartialTechnicalContentEntity(
                martialStyleId = styleId,
                name = "Heian Shodan",
                contentType = "kata",
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
        )
    }
}
