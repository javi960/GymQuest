package com.gymquest.app.data.mapper

import com.gymquest.app.data.local.entity.WorkoutExerciseEntity
import com.gymquest.app.data.local.entity.WorkoutSessionEntity
import com.gymquest.app.data.local.entity.WorkoutSetEntity
import com.gymquest.app.data.local.relation.WorkoutExerciseWithSets
import com.gymquest.app.data.local.relation.WorkoutSessionWithExercises
import com.gymquest.app.domain.model.WorkoutExercise
import com.gymquest.app.domain.model.WorkoutExerciseDetail
import com.gymquest.app.domain.model.WorkoutSession
import com.gymquest.app.domain.model.WorkoutSessionDetail
import com.gymquest.app.domain.model.WorkoutSet

object WorkoutMapper {
    fun toDomain(entity: WorkoutSessionEntity) = WorkoutSession(
        id = entity.id,
        gymId = entity.gymId,
        startedAt = entity.startedAt,
        endedAt = entity.endedAt,
        durationSeconds = entity.durationSeconds,
        status = entity.status,
        notes = entity.notes,
        perceivedEnergy = entity.perceivedEnergy,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )

    fun toEntity(model: WorkoutSession) = WorkoutSessionEntity(
        id = model.id,
        gymId = model.gymId,
        startedAt = model.startedAt,
        endedAt = model.endedAt,
        durationSeconds = model.durationSeconds,
        status = model.status,
        notes = model.notes,
        perceivedEnergy = model.perceivedEnergy,
        createdAt = model.createdAt,
        updatedAt = model.updatedAt,
    )

    fun toDomain(entity: WorkoutExerciseEntity) = WorkoutExercise(
        id = entity.id,
        workoutSessionId = entity.workoutSessionId,
        exerciseVariantId = entity.exerciseVariantId,
        gymMachineId = entity.gymMachineId,
        orderIndex = entity.orderIndex,
        notes = entity.notes,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )

    fun toEntity(model: WorkoutExercise) = WorkoutExerciseEntity(
        id = model.id,
        workoutSessionId = model.workoutSessionId,
        exerciseVariantId = model.exerciseVariantId,
        gymMachineId = model.gymMachineId,
        orderIndex = model.orderIndex,
        notes = model.notes,
        createdAt = model.createdAt,
        updatedAt = model.updatedAt,
    )

    fun toDomain(entity: WorkoutSetEntity) = WorkoutSet(
        id = entity.id,
        workoutExerciseId = entity.workoutExerciseId,
        setNumber = entity.setNumber,
        weightValue = entity.weightValue,
        reps = entity.reps,
        setType = entity.setType,
        startedAt = entity.startedAt,
        endedAt = entity.endedAt,
        restBeforeSeconds = entity.restBeforeSeconds,
        restAfterSeconds = entity.restAfterSeconds,
        notes = entity.notes,
        xpAwarded = entity.xpAwarded,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )

    fun toEntity(model: WorkoutSet) = WorkoutSetEntity(
        id = model.id,
        workoutExerciseId = model.workoutExerciseId,
        setNumber = model.setNumber,
        weightValue = model.weightValue,
        reps = model.reps,
        setType = model.setType,
        startedAt = model.startedAt,
        endedAt = model.endedAt,
        restBeforeSeconds = model.restBeforeSeconds,
        restAfterSeconds = model.restAfterSeconds,
        notes = model.notes,
        volume = model.volume,
        xpAwarded = model.xpAwarded,
        createdAt = model.createdAt,
        updatedAt = model.updatedAt,
    )

    fun toExerciseDetail(relation: WorkoutExerciseWithSets) = WorkoutExerciseDetail(
        workoutExercise = toDomain(relation.workoutExercise),
        sets = relation.sets.sortedBy { it.setNumber }.map(::toDomain),
    )

    fun toSessionDetail(relation: WorkoutSessionWithExercises) = WorkoutSessionDetail(
        session = toDomain(relation.workoutSession),
        exercises = relation.exercises
            .sortedBy { it.workoutExercise.orderIndex }
            .map(::toExerciseDetail),
    )
}
