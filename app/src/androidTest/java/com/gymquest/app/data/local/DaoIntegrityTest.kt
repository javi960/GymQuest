package com.gymquest.app.data.local

import androidx.room.Room
import android.database.sqlite.SQLiteConstraintException
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gymquest.app.data.local.entity.ExerciseBaseEntity
import com.gymquest.app.data.local.entity.ExerciseVariantEntity
import com.gymquest.app.data.local.entity.MartialArtEntity
import com.gymquest.app.data.local.entity.MartialContentStepEntity
import com.gymquest.app.data.local.entity.MartialReminderSettingsEntity
import com.gymquest.app.data.local.entity.MartialReminderWindowEntity
import com.gymquest.app.data.local.entity.MartialSecondaryMissionEntity
import com.gymquest.app.data.local.entity.MartialStyleEntity
import com.gymquest.app.data.local.entity.MartialTechnicalContentEntity
import com.gymquest.app.data.local.entity.MuscleGroupEntity
import com.gymquest.app.data.local.entity.WorkoutExerciseEntity
import com.gymquest.app.data.local.entity.WorkoutSessionEntity
import com.gymquest.app.data.local.entity.WorkoutSetEntity
import com.gymquest.app.data.repository.WorkoutRepositoryImpl
import com.gymquest.app.core.result.AppError
import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.WorkoutExercise
import com.gymquest.app.domain.model.enums.EquipmentType
import com.gymquest.app.domain.model.enums.MartialDirection
import com.gymquest.app.domain.model.enums.SessionStatus
import com.gymquest.app.domain.model.enums.WeightComparisonType
import java.time.Instant
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DaoIntegrityTest {
    private lateinit var database: GymQuestDatabase
    private val timestamp = Instant.parse("2026-09-04T10:15:30Z")
    private var fixtureSequence = 0

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

    @Test
    fun appliesTheDailyXpCapAtomicallyAndOnlyOnce() = runBlocking {
        val contentId = createMartialContent()
        val missionId = database.martialReminderDao().insertSecondaryMission(
            MartialSecondaryMissionEntity(technicalContentId = contentId, scheduledFor = timestamp, createdAt = timestamp, updatedAt = timestamp),
        )
        val granted = database.martialReminderDao().completeMissionWithDailyCap(
            missionId, timestamp.minusSeconds(1), timestamp.plusSeconds(86_400), maxDailyXp = 3, now = timestamp,
        )
        val repeated = database.martialReminderDao().completeMissionWithDailyCap(
            missionId, timestamp.minusSeconds(1), timestamp.plusSeconds(86_400), maxDailyXp = 3, now = timestamp,
        )
        assertEquals(3L, granted)
        assertEquals(0L, repeated)
        assertEquals(3L, database.martialReminderDao().awardedXp(timestamp.minusSeconds(1), timestamp.plusSeconds(86_400)))
    }

    @Test
    fun updatesAndDeletesWorkoutSetsOnlyWhileTheirSessionIsActive() = runBlocking {
        val activeSet = createWorkoutSet()
        val activeUpdate = database.workoutDao().updateWorkoutSetIfSessionActive(
            activeSet.copy(weightValue = 82.5, volume = 495.0, updatedAt = timestamp.plusSeconds(1)),
        )
        assertEquals(1, activeUpdate)
        assertEquals(82.5, database.workoutDao().getWorkoutSet(activeSet.id)?.weightValue ?: 0.0, 0.0)

        val finishedSet = activeSet
        val finishedExercise = database.workoutDao().getWorkoutExercise(finishedSet.workoutExerciseId)!!
        val finishedSession = database.workoutDao().getSession(finishedExercise.workoutSessionId)!!
        database.workoutDao().updateSession(
            finishedSession.copy(
                status = SessionStatus.FINISHED,
                endedAt = timestamp.plusSeconds(30),
                durationSeconds = 30,
                updatedAt = timestamp.plusSeconds(30),
            ),
        )

        assertEquals(0, database.workoutDao().updateWorkoutSetIfSessionActive(finishedSet.copy(weightValue = 90.0)))
        assertEquals(0, database.workoutDao().deleteWorkoutSetIfSessionActive(finishedSet.id))
        assertEquals(82.5, database.workoutDao().getWorkoutSet(finishedSet.id)?.weightValue ?: 0.0, 0.0)

        val cancelledSet = createWorkoutSet()
        val cancelledExercise = database.workoutDao().getWorkoutExercise(cancelledSet.workoutExerciseId)!!
        val cancelledSession = database.workoutDao().getSession(cancelledExercise.workoutSessionId)!!
        database.workoutDao().updateSession(
            cancelledSession.copy(
                status = SessionStatus.CANCELLED,
                endedAt = timestamp.plusSeconds(60),
                durationSeconds = 60,
                updatedAt = timestamp.plusSeconds(60),
            ),
        )

        assertEquals(0, database.workoutDao().updateWorkoutSetIfSessionActive(cancelledSet.copy(weightValue = 90.0)))
        assertEquals(0, database.workoutDao().deleteWorkoutSetIfSessionActive(cancelledSet.id))
    }

    @Test
    fun transitionsAnActiveSessionToFinishedOnlyOnce() = runBlocking {
        val sessionId = createActiveSession()
        val finishedAt = timestamp.plusSeconds(30)

        val firstTransition = database.workoutDao().transitionActiveSession(
            sessionId = sessionId,
            endedAt = finishedAt,
            durationSeconds = 30,
            status = SessionStatus.FINISHED,
            notes = "First completion",
            perceivedEnergy = 4,
            updatedAt = finishedAt,
        )
        val repeatedTransition = database.workoutDao().transitionActiveSession(
            sessionId = sessionId,
            endedAt = timestamp.plusSeconds(60),
            durationSeconds = 60,
            status = SessionStatus.FINISHED,
            notes = "Repeated completion",
            perceivedEnergy = 1,
            updatedAt = timestamp.plusSeconds(60),
        )

        assertEquals(1, firstTransition)
        assertEquals(0, repeatedTransition)
        val session = database.workoutDao().getSession(sessionId)!!
        assertEquals(SessionStatus.FINISHED, session.status)
        assertEquals(finishedAt, session.endedAt)
        assertEquals("First completion", session.notes)
    }

    @Test
    fun rejectsCrossTransitionsAfterAnActiveSessionHasEnded() = runBlocking {
        val finishedSessionId = createActiveSession()

        assertEquals(
            1,
            database.workoutDao().transitionActiveSession(
                sessionId = finishedSessionId,
                endedAt = timestamp.plusSeconds(30),
                durationSeconds = 30,
                status = SessionStatus.FINISHED,
                notes = null,
                perceivedEnergy = null,
                updatedAt = timestamp.plusSeconds(30),
            ),
        )
        assertEquals(
            0,
            database.workoutDao().transitionActiveSession(
                sessionId = finishedSessionId,
                endedAt = timestamp.plusSeconds(60),
                durationSeconds = 60,
                status = SessionStatus.CANCELLED,
                notes = "Should not replace completion",
                perceivedEnergy = null,
                updatedAt = timestamp.plusSeconds(60),
            ),
        )

        val cancelledSessionId = createActiveSession()
        assertEquals(
            1,
            database.workoutDao().transitionActiveSession(
                sessionId = cancelledSessionId,
                endedAt = timestamp.plusSeconds(30),
                durationSeconds = 30,
                status = SessionStatus.CANCELLED,
                notes = "Stopped early",
                perceivedEnergy = null,
                updatedAt = timestamp.plusSeconds(30),
            ),
        )
        assertEquals(
            0,
            database.workoutDao().transitionActiveSession(
                sessionId = cancelledSessionId,
                endedAt = timestamp.plusSeconds(45),
                durationSeconds = 45,
                status = SessionStatus.CANCELLED,
                notes = "Repeated cancellation",
                perceivedEnergy = null,
                updatedAt = timestamp.plusSeconds(45),
            ),
        )
        assertEquals(
            0,
            database.workoutDao().transitionActiveSession(
                sessionId = cancelledSessionId,
                endedAt = timestamp.plusSeconds(60),
                durationSeconds = 60,
                status = SessionStatus.FINISHED,
                notes = "Should not replace cancellation",
                perceivedEnergy = 5,
                updatedAt = timestamp.plusSeconds(60),
            ),
        )

        assertEquals(SessionStatus.FINISHED, database.workoutDao().getSession(finishedSessionId)?.status)
        assertEquals(SessionStatus.CANCELLED, database.workoutDao().getSession(cancelledSessionId)?.status)
    }

    @Test
    fun allowsOnlyOneConcurrentActiveSessionTransition() = runBlocking {
        val sessionId = createActiveSession()

        val updatedRows = coroutineScope {
            listOf(
                async(Dispatchers.Default) {
                    database.workoutDao().transitionActiveSession(
                        sessionId, timestamp.plusSeconds(30), 30, SessionStatus.FINISHED,
                        "Finished", null, timestamp.plusSeconds(30),
                    )
                },
                async(Dispatchers.Default) {
                    database.workoutDao().transitionActiveSession(
                        sessionId, timestamp.plusSeconds(45), 45, SessionStatus.CANCELLED,
                        "Cancelled", null, timestamp.plusSeconds(45),
                    )
                },
            ).awaitAll()
        }

        assertEquals(1, updatedRows.sum())
        assertTrue(database.workoutDao().getSession(sessionId)?.status in setOf(SessionStatus.FINISHED, SessionStatus.CANCELLED))
    }

    @Test
    fun addsExercisesOnlyForExistingActiveVariantsAndBases() = runBlocking {
        val repository = WorkoutRepositoryImpl(database)
        val sessionId = createActiveSession()
        fun exercise(variantId: Long, orderIndex: Int) = WorkoutExercise(
            workoutSessionId = sessionId,
            exerciseVariantId = variantId,
            orderIndex = orderIndex,
            createdAt = timestamp,
            updatedAt = timestamp,
        )

        val missingVariant = repository.addExerciseToSession(exercise(999, 0))
        assertTrue(missingVariant is AppResult.Failure && missingVariant.error is AppError.NotFound)

        val activeBaseId = createExerciseBase()
        val activeVariantId = database.exerciseDao().insertExerciseVariant(createVariant(activeBaseId, "Active"))
        assertTrue(repository.addExerciseToSession(exercise(activeVariantId, 0)) is AppResult.Success)

        database.exerciseDao().archiveExerciseVariant(activeVariantId, timestamp.plusSeconds(1))
        val archivedVariant = repository.addExerciseToSession(exercise(activeVariantId, 1))
        assertTrue(archivedVariant is AppResult.Failure && archivedVariant.error is AppError.Validation)

        val archivedBaseId = createExerciseBase()
        val archivedBaseVariantId = database.exerciseDao().insertExerciseVariant(createVariant(archivedBaseId, "Archived base"))
        database.exerciseDao().archiveExerciseBase(archivedBaseId, timestamp.plusSeconds(1))
        val archivedBase = repository.addExerciseToSession(exercise(archivedBaseVariantId, 1))
        assertTrue(archivedBase is AppResult.Failure && archivedBase.error is AppError.Validation)
    }

    @Test
    fun deletingAnIntermediateKataStepRenumbersTheRemainingSequence() = runBlocking {
        val contentId = createMartialContent()
        val firstId = database.martialArtsDao().insertContentStep(contentStep(contentId, 0))
        val middleId = database.martialArtsDao().insertContentStep(contentStep(contentId, 1))
        val lastId = database.martialArtsDao().insertContentStep(contentStep(contentId, 2))

        assertEquals(1, database.martialArtsDao().deleteContentStep(middleId))

        val steps = database.martialArtsDao().getContentSteps(contentId)
        assertEquals(listOf(firstId, lastId), steps.map { it.id })
        assertEquals(listOf(0, 1), steps.map { it.orderIndex })
    }

    private suspend fun createExerciseBase(): Long {
        fixtureSequence += 1
        val muscleGroupId = database.exerciseDao().insertMuscleGroup(
            MuscleGroupEntity(name = "Legs $fixtureSequence", sortOrder = fixtureSequence),
        )
        return database.exerciseDao().insertExerciseBase(
            ExerciseBaseEntity(
                name = "Squat $fixtureSequence",
                primaryMuscleGroupId = muscleGroupId,
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
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

    private suspend fun createWorkoutSet(): WorkoutSetEntity {
        val baseId = createExerciseBase()
        val variantId = database.exerciseDao().insertExerciseVariant(createVariant(baseId, "Workout"))
        val sessionId = database.workoutDao().insertSession(
            WorkoutSessionEntity(startedAt = timestamp, createdAt = timestamp, updatedAt = timestamp),
        )
        val exerciseId = database.workoutDao().insertWorkoutExercise(
            WorkoutExerciseEntity(
                workoutSessionId = sessionId,
                exerciseVariantId = variantId,
                orderIndex = 0,
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
        )
        val set = WorkoutSetEntity(
            workoutExerciseId = exerciseId,
            setNumber = 1,
            weightValue = 80.0,
            reps = 6,
            volume = 480.0,
            createdAt = timestamp,
            updatedAt = timestamp,
        )
        return set.copy(id = database.workoutDao().insertWorkoutSet(set))
    }

    private suspend fun createActiveSession(): Long =
        database.workoutDao().insertSession(
            WorkoutSessionEntity(startedAt = timestamp, createdAt = timestamp, updatedAt = timestamp),
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

    private fun contentStep(contentId: Long, orderIndex: Int) = MartialContentStepEntity(
        technicalContentId = contentId,
        orderIndex = orderIndex,
        direction = MartialDirection.FRONT,
        createdAt = timestamp,
        updatedAt = timestamp,
    )
}
