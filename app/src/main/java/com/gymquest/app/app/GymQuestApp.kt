package com.gymquest.app.app

import android.app.Application
import com.gymquest.app.data.local.GymQuestDatabase
import com.gymquest.app.data.repository.ExerciseRepositoryImpl
import com.gymquest.app.data.repository.WorkoutRepositoryImpl
import com.gymquest.app.domain.usecase.catalog.CreateExerciseBaseUseCase
import com.gymquest.app.domain.usecase.catalog.CreateExerciseVariantUseCase
import com.gymquest.app.domain.usecase.catalog.CreateMuscleGroupUseCase
import com.gymquest.app.domain.usecase.catalog.GetVariantLastPerformanceUseCase
import com.gymquest.app.domain.usecase.catalog.ObserveActiveExerciseVariantsUseCase
import com.gymquest.app.domain.usecase.catalog.ObserveExerciseCatalogUseCase
import com.gymquest.app.domain.usecase.catalog.ObserveMuscleGroupsUseCase
import com.gymquest.app.domain.usecase.session.AddExerciseToSessionUseCase
import com.gymquest.app.domain.usecase.session.CancelWorkoutSessionUseCase
import com.gymquest.app.domain.usecase.session.CompleteWorkoutSessionUseCase
import com.gymquest.app.domain.usecase.session.DeleteWorkoutSetUseCase
import com.gymquest.app.domain.usecase.session.ObserveActiveSessionUseCase
import com.gymquest.app.domain.usecase.session.ObserveSessionDetailUseCase
import com.gymquest.app.domain.usecase.session.ObserveSessionHistoryUseCase
import com.gymquest.app.domain.usecase.session.ResolveRestBeforeNextSetUseCase
import com.gymquest.app.domain.usecase.session.SaveWorkoutSetUseCase
import com.gymquest.app.domain.usecase.session.StartRestAfterSetUseCase
import com.gymquest.app.domain.usecase.session.StartWorkoutSessionUseCase
import com.gymquest.app.domain.usecase.session.UpdateWorkoutSetUseCase

class GymQuestApp : Application() {
    val appContainer: AppContainer by lazy { AppContainer(this) }
}

class AppContainer(application: Application) {
    private val database = GymQuestDatabase.create(application)
    private val exerciseRepository = ExerciseRepositoryImpl(database.exerciseDao())
    private val workoutRepository = WorkoutRepositoryImpl(database)

    val createMuscleGroupUseCase = CreateMuscleGroupUseCase(exerciseRepository)
    val observeMuscleGroupsUseCase = ObserveMuscleGroupsUseCase(exerciseRepository)
    val createExerciseBaseUseCase = CreateExerciseBaseUseCase(exerciseRepository)
    val createExerciseVariantUseCase = CreateExerciseVariantUseCase(exerciseRepository)
    val observeActiveExerciseVariantsUseCase = ObserveActiveExerciseVariantsUseCase(exerciseRepository)
    val observeExerciseCatalogUseCase = ObserveExerciseCatalogUseCase(exerciseRepository)
    val getVariantLastPerformanceUseCase = GetVariantLastPerformanceUseCase(workoutRepository)
    val startWorkoutSessionUseCase = StartWorkoutSessionUseCase(workoutRepository)
    val cancelWorkoutSessionUseCase = CancelWorkoutSessionUseCase(workoutRepository)
    val completeWorkoutSessionUseCase = CompleteWorkoutSessionUseCase(workoutRepository)
    val addExerciseToSessionUseCase = AddExerciseToSessionUseCase(workoutRepository)
    val saveWorkoutSetUseCase = SaveWorkoutSetUseCase(workoutRepository)
    val updateWorkoutSetUseCase = UpdateWorkoutSetUseCase(workoutRepository)
    val deleteWorkoutSetUseCase = DeleteWorkoutSetUseCase(workoutRepository)
    val observeActiveSessionUseCase = ObserveActiveSessionUseCase(workoutRepository)
    val observeSessionDetailUseCase = ObserveSessionDetailUseCase(workoutRepository)
    val observeSessionHistoryUseCase = ObserveSessionHistoryUseCase(workoutRepository)
    val startRestAfterSetUseCase = StartRestAfterSetUseCase()
    val resolveRestBeforeNextSetUseCase = ResolveRestBeforeNextSetUseCase()
}
