package com.gymquest.app.data.mapper

import com.gymquest.app.data.local.entity.RoutineDayEntity
import com.gymquest.app.data.local.entity.RoutineExerciseEntity
import com.gymquest.app.data.local.entity.RoutinePlannedSetEntity
import com.gymquest.app.data.local.entity.WorkoutRoutineEntity
import com.gymquest.app.data.local.relation.WorkoutRoutineWithDays
import com.gymquest.app.domain.model.RoutineDay
import com.gymquest.app.domain.model.RoutineDayDetail
import com.gymquest.app.domain.model.RoutineExercise
import com.gymquest.app.domain.model.RoutineExerciseDetail
import com.gymquest.app.domain.model.RoutinePlannedSet
import com.gymquest.app.domain.model.WorkoutRoutine
import com.gymquest.app.domain.model.WorkoutRoutineDetail

object RoutineMapper {
    fun toEntity(model: WorkoutRoutine) = WorkoutRoutineEntity(model.id, model.name, model.notes, model.isArchived, model.createdAt, model.updatedAt)
    fun toDomain(entity: WorkoutRoutineEntity) = WorkoutRoutine(entity.id, entity.name, entity.notes, entity.isArchived, entity.createdAt, entity.updatedAt)
    fun toEntity(model: RoutineDay) = RoutineDayEntity(model.id, model.routineId, model.weekday, model.label, model.notes, model.createdAt, model.updatedAt)
    fun toDomain(entity: RoutineDayEntity) = RoutineDay(entity.id, entity.routineId, entity.weekday, entity.label, entity.notes, entity.createdAt, entity.updatedAt)
    fun toEntity(model: RoutineExercise) = RoutineExerciseEntity(model.id, model.routineDayId, model.exerciseVariantId, model.gymMachineId, model.orderIndex, model.notes, model.createdAt, model.updatedAt)
    fun toDomain(entity: RoutineExerciseEntity) = RoutineExercise(entity.id, entity.routineDayId, entity.exerciseVariantId, entity.gymMachineId, entity.orderIndex, entity.notes, entity.createdAt, entity.updatedAt)
    fun toEntity(model: RoutinePlannedSet) = RoutinePlannedSetEntity(model.id, model.routineExerciseId, model.setNumber, model.targetWeight, model.targetReps, model.createdAt, model.updatedAt)
    fun toDomain(entity: RoutinePlannedSetEntity) = RoutinePlannedSet(entity.id, entity.routineExerciseId, entity.setNumber, entity.targetWeight, entity.targetReps, entity.createdAt, entity.updatedAt)

    fun toDetail(relation: WorkoutRoutineWithDays) = WorkoutRoutineDetail(
        routine = toDomain(relation.routine),
        days = relation.days.sortedBy { it.day.weekday.ordinal }.map { dayRelation ->
            RoutineDayDetail(
                day = toDomain(dayRelation.day),
                exercises = dayRelation.exercises.sortedBy { it.exercise.orderIndex }.map { exerciseRelation ->
                    RoutineExerciseDetail(
                        exercise = toDomain(exerciseRelation.exercise),
                        plannedSets = exerciseRelation.plannedSets.sortedBy { it.setNumber }.map(::toDomain),
                    )
                },
            )
        },
    )
}
