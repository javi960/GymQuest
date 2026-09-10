package com.gymquest.app.app

import android.app.Application
import android.util.Log
import com.gymquest.app.data.local.GymQuestDatabase
import com.gymquest.app.data.repository.ExerciseRepositoryImpl
import com.gymquest.app.data.repository.ProgressRepositoryImpl
import com.gymquest.app.data.repository.WorkoutRepositoryImpl
import com.gymquest.app.data.repository.RoutineRepositoryImpl
import com.gymquest.app.data.repository.BackupRepositoryImpl
import com.gymquest.app.data.repository.MartialArtsRepositoryImpl
import com.gymquest.app.data.repository.MartialReminderRepositoryImpl
import com.gymquest.app.data.repository.MartialMiniGameRepositoryImpl
import com.gymquest.app.data.repository.MediaRepositoryImpl
import com.gymquest.app.data.seed.ExerciseCatalogSeedRepository
import com.gymquest.app.data.reminder.AlarmManagerReminderScheduler
import com.gymquest.app.data.reminder.MartialReminderCoordinator
import com.gymquest.app.data.reminder.NotificationPermissionManager
import com.gymquest.app.core.time.SystemClockProvider
import com.gymquest.app.domain.usecase.martial.*
import com.gymquest.app.domain.usecase.catalog.CreateExerciseBaseUseCase
import com.gymquest.app.domain.usecase.catalog.CreateExerciseVariantUseCase
import com.gymquest.app.domain.usecase.catalog.CreateMuscleGroupUseCase
import com.gymquest.app.domain.usecase.catalog.UpdateExerciseBaseUseCase
import com.gymquest.app.domain.usecase.catalog.UpdateExerciseVariantUseCase
import com.gymquest.app.domain.usecase.catalog.ArchiveExerciseBaseUseCase
import com.gymquest.app.domain.usecase.catalog.ArchiveExerciseVariantUseCase
import com.gymquest.app.domain.usecase.catalog.GetVariantLastPerformanceUseCase
import com.gymquest.app.domain.usecase.catalog.ObserveActiveExerciseVariantsUseCase
import com.gymquest.app.domain.usecase.catalog.ObserveExerciseCatalogUseCase
import com.gymquest.app.domain.usecase.catalog.ObserveMuscleGroupsUseCase
import com.gymquest.app.domain.usecase.catalog.ObserveExerciseBaseMediaUseCase
import com.gymquest.app.domain.usecase.catalog.RemoveExerciseBaseMediaUseCase
import com.gymquest.app.domain.usecase.catalog.ReplaceExerciseBaseMediaUseCase
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
import com.gymquest.app.domain.usecase.routine.ObserveRoutinesUseCase
import com.gymquest.app.domain.usecase.routine.SaveRoutineUseCase
import com.gymquest.app.domain.usecase.routine.StartSessionFromRoutineDayUseCase
import com.gymquest.app.domain.usecase.progress.ApplyWorkoutProgressUseCase
import com.gymquest.app.domain.usecase.progress.ObserveProgressSummaryUseCase
import com.gymquest.app.domain.usecase.progress.ObserveVariantHistoryUseCase
import com.gymquest.app.domain.usecase.progress.RecalculateProgressUseCase
import com.gymquest.app.domain.usecase.backup.BuildBackupSnapshotUseCase
import com.gymquest.app.domain.usecase.backup.ExportBackupJsonUseCase
import com.gymquest.app.domain.usecase.backup.ValidateBackupSchemaUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class GymQuestApp : Application() {
    val appContainer: AppContainer by lazy { AppContainer(this) }
}

class AppContainer(application: Application) {
    private val database = GymQuestDatabase.create(application)
    private val exerciseRepository = ExerciseRepositoryImpl(database.exerciseDao())
    private val mediaRepository = MediaRepositoryImpl(database.mediaFileDao())
    private val catalogSeedRepository = ExerciseCatalogSeedRepository(application, database, SystemClockProvider)

    init {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                catalogSeedRepository.seedIfNeeded()
            } catch (error: Exception) {
                Log.e("GymQuestSeed", "No se pudo importar el catálogo local de ejercicios.", error)
            }
        }
    }
    private val workoutRepository = WorkoutRepositoryImpl(database)
    private val routineRepository = RoutineRepositoryImpl(database)
    private val progressRepository = ProgressRepositoryImpl(database)
    private val martialArtsRepository = MartialArtsRepositoryImpl(database)
    val martialReminderRepository = MartialReminderRepositoryImpl(database.martialReminderDao())
    val martialReminderCoordinator = MartialReminderCoordinator(database, AlarmManagerReminderScheduler(application), NotificationPermissionManager(application), SystemClockProvider)
    private val martialMiniGameRepository = MartialMiniGameRepositoryImpl(database)
    private val backupRepository = BackupRepositoryImpl(database)

    val createMuscleGroupUseCase = CreateMuscleGroupUseCase(exerciseRepository)
    val observeMuscleGroupsUseCase = ObserveMuscleGroupsUseCase(exerciseRepository)
    val createExerciseBaseUseCase = CreateExerciseBaseUseCase(exerciseRepository)
    val createExerciseVariantUseCase = CreateExerciseVariantUseCase(exerciseRepository)
    val updateExerciseBaseUseCase = UpdateExerciseBaseUseCase(exerciseRepository)
    val updateExerciseVariantUseCase = UpdateExerciseVariantUseCase(exerciseRepository)
    val archiveExerciseBaseUseCase = ArchiveExerciseBaseUseCase(exerciseRepository)
    val archiveExerciseVariantUseCase = ArchiveExerciseVariantUseCase(exerciseRepository)
    val observeActiveExerciseVariantsUseCase = ObserveActiveExerciseVariantsUseCase(exerciseRepository)
    val observeExerciseCatalogUseCase = ObserveExerciseCatalogUseCase(exerciseRepository)
    val replaceExerciseBaseMediaUseCase = ReplaceExerciseBaseMediaUseCase(mediaRepository)
    val removeExerciseBaseMediaUseCase = RemoveExerciseBaseMediaUseCase(mediaRepository)
    val observeExerciseBaseMediaUseCase = ObserveExerciseBaseMediaUseCase(mediaRepository)
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
    val observeProgressSummaryUseCase = ObserveProgressSummaryUseCase(progressRepository)
    val observeVariantHistoryUseCase = ObserveVariantHistoryUseCase(workoutRepository)
    private val recalculateProgressUseCase = RecalculateProgressUseCase(workoutRepository, progressRepository)
    val applyWorkoutProgressUseCase = ApplyWorkoutProgressUseCase(recalculateProgressUseCase)
    val observeRoutinesUseCase = ObserveRoutinesUseCase(routineRepository)
    val saveRoutineUseCase = SaveRoutineUseCase(routineRepository)
    val startSessionFromRoutineDayUseCase = StartSessionFromRoutineDayUseCase(routineRepository)
    val getActiveMartialArtsUseCase = GetActiveMartialArtsUseCase(martialArtsRepository)
    val getActiveMartialStylesUseCase = GetActiveMartialStylesUseCase(martialArtsRepository)
    val getActiveMartialTechniquesUseCase = GetActiveMartialTechniquesUseCase(martialArtsRepository)
    val getActiveMartialStancesUseCase = GetActiveMartialStancesUseCase(martialArtsRepository)
    val getMartialContentDetailUseCase = GetMartialContentDetailUseCase(martialArtsRepository)
    val updateMartialArtUseCase = UpdateMartialArtUseCase(martialArtsRepository)
    val updateMartialStyleUseCase = UpdateMartialStyleUseCase(martialArtsRepository)
    val archiveMartialArtUseCase = ArchiveMartialArtUseCase(martialArtsRepository)
    val archiveMartialStyleUseCase = ArchiveMartialStyleUseCase(martialArtsRepository)
    val addMartialContentStepUseCase = AddMartialContentStepUseCase(martialArtsRepository)
    val updateMartialContentStepUseCase = UpdateMartialContentStepUseCase(martialArtsRepository)
    val deleteMartialContentStepUseCase = DeleteMartialContentStepUseCase(martialArtsRepository)
    val replaceMartialContentStepsUseCase = ReplaceMartialContentStepsUseCase(martialArtsRepository)
    val replaceMartialContentStepMediaUseCase = ReplaceMartialContentStepMediaUseCase(martialArtsRepository)
    val removeMartialContentStepMediaUseCase = RemoveMartialContentStepMediaUseCase(martialArtsRepository)
    val observeMartialContentStepMediaUseCase = ObserveMartialContentStepMediaUseCase(mediaRepository)
    val replaceMartialTechnicalContentMediaUseCase = ReplaceMartialTechnicalContentMediaUseCase(mediaRepository)
    val removeMartialTechnicalContentMediaUseCase = RemoveMartialTechnicalContentMediaUseCase(mediaRepository)
    val observeMartialTechnicalContentMediaUseCase = ObserveMartialTechnicalContentMediaUseCase(mediaRepository)
    val replaceMartialTechniqueMediaUseCase = ReplaceMartialTechniqueMediaUseCase(mediaRepository)
    val removeMartialTechniqueMediaUseCase = RemoveMartialTechniqueMediaUseCase(mediaRepository)
    val observeMartialTechniqueMediaUseCase = ObserveMartialTechniqueMediaUseCase(mediaRepository)
    val replaceMartialStanceMediaUseCase = ReplaceMartialStanceMediaUseCase(mediaRepository)
    val removeMartialStanceMediaUseCase = RemoveMartialStanceMediaUseCase(mediaRepository)
    val observeMartialStanceMediaUseCase = ObserveMartialStanceMediaUseCase(mediaRepository)
    val createMartialArtUseCase = CreateMartialArtUseCase(martialArtsRepository)
    val createMartialStyleUseCase = CreateMartialStyleUseCase(martialArtsRepository)
    val ensureShitoRyuCatalogUseCase = EnsureShitoRyuCatalogUseCase(martialArtsRepository)
    val setCurrentMartialRankUseCase = SetCurrentMartialRankUseCase(martialArtsRepository)
    val createMartialContentUseCase = CreateMartialContentUseCase(martialArtsRepository)
    val createMartialTechniqueUseCase = CreateMartialTechniqueUseCase(martialArtsRepository)
    val createMartialStanceUseCase = CreateMartialStanceUseCase(martialArtsRepository)
    val updateMartialContentUseCase = UpdateMartialContentUseCase(martialArtsRepository)
    val updateMartialTechniqueUseCase = UpdateMartialTechniqueUseCase(martialArtsRepository)
    val updateMartialStanceUseCase = UpdateMartialStanceUseCase(martialArtsRepository)
    val linkMartialTechniqueUseCase = LinkMartialTechniqueUseCase(martialArtsRepository)
    val registerMartialPracticeUseCase = RegisterMartialPracticeUseCase(martialArtsRepository)
    val observeMartialProgressUseCase = ObserveMartialProgressUseCase(martialArtsRepository)
    val updateMartialContentProgressUseCase = UpdateMartialContentProgressUseCase(martialArtsRepository)
    val archiveMartialContentUseCase = ArchiveMartialContentUseCase(martialArtsRepository)
    val archiveMartialTechniqueUseCase = ArchiveMartialTechniqueUseCase(martialArtsRepository)
    val archiveMartialStanceUseCase = ArchiveMartialStanceUseCase(martialArtsRepository)
    val deleteMartialArtUseCase = DeleteMartialArtUseCase(martialArtsRepository)
    val deleteMartialStyleUseCase = DeleteMartialStyleUseCase(martialArtsRepository)
    val deleteMartialContentUseCase = DeleteMartialContentUseCase(martialArtsRepository)
    val deleteMartialTechniqueUseCase = DeleteMartialTechniqueUseCase(martialArtsRepository)
    val deleteMartialStanceUseCase = DeleteMartialStanceUseCase(martialArtsRepository)
    val getCurrentMartialBeltUseCase = GetCurrentMartialBeltUseCase(martialArtsRepository)
    val setCurrentMartialBeltUseCase = SetCurrentMartialBeltUseCase(martialArtsRepository)
    val createMartialQuestionUseCase = CreateMartialQuestionUseCase(martialMiniGameRepository)
    val answerMartialQuestionUseCase = AnswerMartialQuestionUseCase(martialMiniGameRepository)
    val buildBackupSnapshotUseCase = BuildBackupSnapshotUseCase(backupRepository)
    val exportBackupJsonUseCase = ExportBackupJsonUseCase(backupRepository)
    val validateBackupSchemaUseCase = ValidateBackupSchemaUseCase(backupRepository)
}
