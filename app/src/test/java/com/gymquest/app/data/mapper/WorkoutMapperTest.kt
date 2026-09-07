package com.gymquest.app.data.mapper

import com.gymquest.app.data.local.entity.WorkoutExerciseEntity
import com.gymquest.app.data.local.entity.WorkoutSessionEntity
import com.gymquest.app.data.local.entity.WorkoutSetEntity
import com.gymquest.app.data.local.relation.WorkoutExerciseWithSets
import com.gymquest.app.data.local.relation.WorkoutSessionWithExercises
import com.gymquest.app.domain.model.enums.SetType
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutMapperTest {
    private val now = Instant.parse("2026-09-04T10:00:00Z")

    @Test
    fun workoutMapper_roundTripsEntitiesAndCalculatesPersistedSetVolume() {
        val session = WorkoutSessionEntity(id = 1, startedAt = now, createdAt = now, updatedAt = now)
        val exercise = WorkoutExerciseEntity(
            id = 2,
            workoutSessionId = 1,
            exerciseVariantId = 3,
            orderIndex = 1,
            createdAt = now,
            updatedAt = now,
        )
        val set = WorkoutSetEntity(
            id = 4,
            workoutExerciseId = 2,
            setNumber = 1,
            weightValue = 80.0,
            reps = 8,
            setType = SetType.WORK,
            volume = 640.0,
            createdAt = now,
            updatedAt = now,
        )

        assertEquals(session, WorkoutMapper.toEntity(WorkoutMapper.toDomain(session)))
        assertEquals(exercise, WorkoutMapper.toEntity(WorkoutMapper.toDomain(exercise)))
        assertEquals(set, WorkoutMapper.toEntity(WorkoutMapper.toDomain(set)))
    }

    @Test
    fun toSessionDetail_sortsExercisesAndSets() {
        val firstExercise = workoutExercise(id = 1, orderIndex = 1)
        val secondExercise = workoutExercise(id = 2, orderIndex = 2)
        val relation = WorkoutSessionWithExercises(
            workoutSession = WorkoutSessionEntity(id = 10, startedAt = now, createdAt = now, updatedAt = now),
            exercises = listOf(
                WorkoutExerciseWithSets(secondExercise, listOf(workoutSet(2, secondExercise.id), workoutSet(1, secondExercise.id))),
                WorkoutExerciseWithSets(firstExercise, listOf(workoutSet(1, firstExercise.id))),
            ),
        )

        val detail = WorkoutMapper.toSessionDetail(relation)

        assertEquals(listOf(1L, 2L), detail.exercises.map { it.workoutExercise.id })
        assertEquals(listOf(1, 2), detail.exercises.last().sets.map { it.setNumber })
    }

    private fun workoutExercise(id: Long, orderIndex: Int) = WorkoutExerciseEntity(
        id = id,
        workoutSessionId = 10,
        exerciseVariantId = 20,
        orderIndex = orderIndex,
        createdAt = now,
        updatedAt = now,
    )

    private fun workoutSet(setNumber: Int, workoutExerciseId: Long) = WorkoutSetEntity(
        id = setNumber.toLong(),
        workoutExerciseId = workoutExerciseId,
        setNumber = setNumber,
        weightValue = 10.0,
        reps = 5,
        volume = 50.0,
        createdAt = now,
        updatedAt = now,
    )
}
