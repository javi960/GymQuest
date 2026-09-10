package com.gymquest.app.domain.model

data class RoutineExerciseDetail(
    val exercise: RoutineExercise,
    val plannedSets: List<RoutinePlannedSet>,
)

data class RoutineDayDetail(
    val day: RoutineDay,
    val exercises: List<RoutineExerciseDetail>,
)

data class WorkoutRoutineDetail(
    val routine: WorkoutRoutine,
    val days: List<RoutineDayDetail>,
)
