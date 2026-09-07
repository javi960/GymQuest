package com.gymquest.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gymquest.app.data.local.entity.ExerciseBaseEntity
import com.gymquest.app.data.local.entity.ExerciseVariantEntity
import com.gymquest.app.data.local.entity.MartialArtEntity
import com.gymquest.app.data.local.entity.MartialContentTechniqueCrossRef
import com.gymquest.app.data.local.entity.MartialPracticeItemEntity
import com.gymquest.app.data.local.entity.MartialPracticeSessionEntity
import com.gymquest.app.data.local.entity.MartialStyleEntity
import com.gymquest.app.data.local.entity.MartialTechnicalContentEntity
import com.gymquest.app.data.local.entity.MartialTechniqueEntity
import com.gymquest.app.data.local.entity.MuscleGroupEntity
import com.gymquest.app.data.local.entity.WorkoutExerciseEntity
import com.gymquest.app.data.local.entity.WorkoutSessionEntity
import com.gymquest.app.data.local.entity.WorkoutSetEntity
import com.gymquest.app.domain.model.enums.EquipmentType
import com.gymquest.app.domain.model.enums.WeightComparisonType
import java.time.Instant
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomRelationsTest {
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
    fun loadsCatalogAndWorkoutSessionRelationsInOrder() = runBlocking {
        val muscleGroupId = database.exerciseDao().insertMuscleGroup(MuscleGroupEntity(name = "Legs", sortOrder = 1))
        val baseId = database.exerciseDao().insertExerciseBase(
            ExerciseBaseEntity(
                name = "Squat",
                primaryMuscleGroupId = muscleGroupId,
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
        )
        val variantId = database.exerciseDao().insertExerciseVariant(
            ExerciseVariantEntity(
                exerciseBaseId = baseId,
                name = "Barbell",
                equipmentType = EquipmentType.BARBELL,
                weightComparisonType = WeightComparisonType.TOTAL_WEIGHT,
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
        )
        val sessionId = database.workoutDao().insertSession(
            WorkoutSessionEntity(startedAt = timestamp, createdAt = timestamp, updatedAt = timestamp),
        )
        val workoutExerciseId = database.workoutDao().insertWorkoutExercise(
            WorkoutExerciseEntity(
                workoutSessionId = sessionId,
                exerciseVariantId = variantId,
                orderIndex = 0,
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
        )
        database.workoutDao().insertWorkoutSet(
            WorkoutSetEntity(
                workoutExerciseId = workoutExerciseId,
                setNumber = 2,
                weightValue = 50.0,
                reps = 8,
                volume = 400.0,
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
        )
        database.workoutDao().insertWorkoutSet(
            WorkoutSetEntity(
                workoutExerciseId = workoutExerciseId,
                setNumber = 1,
                weightValue = 40.0,
                reps = 10,
                volume = 400.0,
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
        )

        val catalog = database.exerciseDao().getExerciseBaseWithVariants(baseId)
        val session = database.workoutDao().getSessionWithExercises(sessionId)
        val orderedSets = database.workoutDao().getWorkoutSetsForExercise(workoutExerciseId)

        assertEquals(listOf("Barbell"), catalog?.variants?.map { it.name })
        assertEquals(setOf(1, 2), session?.exercises?.single()?.sets?.map { it.setNumber }?.toSet())
        assertEquals(listOf(1, 2), orderedSets.map { it.setNumber })
    }

    @Test
    fun loadsMartialContentTechniquesAndPracticeItems() = runBlocking {
        val artId = database.martialArtsDao().insertMartialArt(
            MartialArtEntity(name = "Karate", createdAt = timestamp, updatedAt = timestamp),
        )
        val styleId = database.martialArtsDao().insertMartialStyle(
            MartialStyleEntity(martialArtId = artId, name = "Shotokan", createdAt = timestamp, updatedAt = timestamp),
        )
        val contentId = database.martialArtsDao().insertTechnicalContent(
            MartialTechnicalContentEntity(
                martialStyleId = styleId,
                name = "Heian Shodan",
                contentType = "kata",
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
        )
        val techniqueId = database.martialArtsDao().insertTechnique(
            MartialTechniqueEntity(martialStyleId = styleId, name = "Gedan Barai", createdAt = timestamp, updatedAt = timestamp),
        )
        val secondTechniqueId = database.martialArtsDao().insertTechnique(
            MartialTechniqueEntity(martialStyleId = styleId, name = "Oi Tsuki", createdAt = timestamp, updatedAt = timestamp),
        )
        database.martialArtsDao().insertContentTechniqueCrossRef(
            MartialContentTechniqueCrossRef(contentId, techniqueId, orderIndex = 1),
        )
        database.martialArtsDao().insertContentTechniqueCrossRef(
            MartialContentTechniqueCrossRef(contentId, secondTechniqueId, orderIndex = 0),
        )
        val practiceId = database.martialArtsDao().insertPracticeSession(
            MartialPracticeSessionEntity(martialStyleId = styleId, startedAt = timestamp, createdAt = timestamp, updatedAt = timestamp),
        )
        database.martialArtsDao().insertPracticeItem(
            MartialPracticeItemEntity(
                practiceSessionId = practiceId,
                contentId = contentId,
                createdAt = timestamp,
                updatedAt = timestamp,
            ),
        )

        val style = database.martialArtsDao().getMartialStyleWithContent(styleId)
        val content = database.martialArtsDao().getTechnicalContentWithTechniques(contentId)
        val practice = database.martialArtsDao().getPracticeSessionWithItems(practiceId)
        val orderedTechniques = database.martialArtsDao().getTechniquesForContent(contentId)

        assertEquals(listOf("Heian Shodan"), style?.technicalContents?.map { it.name })
        assertEquals(setOf("Gedan Barai", "Oi Tsuki"), content?.techniques?.map { it.name }?.toSet())
        assertEquals(listOf("Oi Tsuki", "Gedan Barai"), orderedTechniques.map { it.name })
        assertEquals(listOf(contentId), practice?.items?.map { it.contentId })
    }
}
