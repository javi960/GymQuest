package com.gymquest.app.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.gymquest.app.data.local.entity.RoutineDayEntity
import com.gymquest.app.data.local.entity.RoutineExerciseEntity
import com.gymquest.app.data.local.entity.RoutinePlannedSetEntity
import com.gymquest.app.data.local.entity.WorkoutRoutineEntity

data class RoutineExerciseWithPlannedSets(
    @Embedded val exercise: RoutineExerciseEntity,
    @Relation(parentColumn = "id", entityColumn = "routineExerciseId")
    val plannedSets: List<RoutinePlannedSetEntity>,
)

data class RoutineDayWithExercises(
    @Embedded val day: RoutineDayEntity,
    @Relation(parentColumn = "id", entityColumn = "routineDayId", entity = RoutineExerciseEntity::class)
    val exercises: List<RoutineExerciseWithPlannedSets>,
)

data class WorkoutRoutineWithDays(
    @Embedded val routine: WorkoutRoutineEntity,
    @Relation(parentColumn = "id", entityColumn = "routineId", entity = RoutineDayEntity::class)
    val days: List<RoutineDayWithExercises>,
)
