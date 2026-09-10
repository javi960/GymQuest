package com.gymquest.app.feature.session

import androidx.lifecycle.ViewModelStore
import com.gymquest.app.core.result.AppResult
import com.gymquest.app.core.result.AppError
import com.gymquest.app.core.time.ClockProvider
import com.gymquest.app.domain.model.CharacterStats
import com.gymquest.app.domain.model.ExerciseBase
import com.gymquest.app.domain.model.ExerciseCatalogEntry
import com.gymquest.app.domain.model.ExerciseMastery
import com.gymquest.app.domain.model.ExerciseVariant
import com.gymquest.app.domain.model.MuscleGroup
import com.gymquest.app.domain.model.ProgressSummary
import com.gymquest.app.domain.model.VariantLastPerformance
import com.gymquest.app.domain.model.WorkoutExercise
import com.gymquest.app.domain.model.WorkoutExerciseDetail
import com.gymquest.app.domain.model.WorkoutSession
import com.gymquest.app.domain.model.WorkoutSessionDetail
import com.gymquest.app.domain.model.WorkoutSet
import com.gymquest.app.domain.model.enums.EquipmentType
import com.gymquest.app.domain.model.enums.SessionStatus
import com.gymquest.app.domain.model.enums.SetType
import com.gymquest.app.domain.model.enums.WeightComparisonType
import com.gymquest.app.domain.repository.ExerciseRepository
import com.gymquest.app.domain.repository.ProgressRepository
import com.gymquest.app.domain.repository.WorkoutRepository
import com.gymquest.app.domain.usecase.catalog.ObserveActiveExerciseVariantsUseCase
import com.gymquest.app.domain.usecase.progress.ApplyWorkoutProgressUseCase
import com.gymquest.app.domain.usecase.progress.RecalculateProgressUseCase
import com.gymquest.app.domain.usecase.session.AddExerciseToSessionUseCase
import com.gymquest.app.domain.usecase.session.CancelWorkoutSessionUseCase
import com.gymquest.app.domain.usecase.session.CompleteWorkoutSessionUseCase
import com.gymquest.app.domain.usecase.session.DeleteWorkoutSetUseCase
import com.gymquest.app.domain.usecase.session.ObserveActiveSessionUseCase
import com.gymquest.app.domain.usecase.session.ResolveRestBeforeNextSetUseCase
import com.gymquest.app.domain.usecase.session.SaveWorkoutSetUseCase
import com.gymquest.app.domain.usecase.session.StartRestAfterSetUseCase
import com.gymquest.app.domain.usecase.session.StartWorkoutSessionUseCase
import com.gymquest.app.domain.usecase.session.UpdateWorkoutSetUseCase
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {
    private val now = Instant.parse("2026-09-07T12:00:00Z")

    @Test
    fun `session start add save update delete and cancel write through use cases`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val activeSession = MutableStateFlow(sessionDetail())
            val workoutRepository = FakeWorkoutRepository(activeSession = activeSession)
            val exerciseRepository = FakeExerciseRepository(
                variants = MutableStateFlow(listOf(variant(id = 5), variant(id = 6))),
            )
            val progressRepository = FakeProgressRepository()
            val viewModel = viewModel(workoutRepository, exerciseRepository, progressRepository)

            advanceUntilIdle()
            viewModel.onAction(SessionAction.StartSession)
            viewModel.onAction(SessionAction.SelectVariant(6))
            viewModel.onAction(SessionAction.AddSelectedVariant)
            viewModel.onAction(SessionAction.SaveSet(2, weightInput = "82,5", repsInput = "8", setType = SetType.WORK))
            viewModel.onAction(SessionAction.UpdateSet(set(id = 11), weightInput = "90", repsInput = "5", setType = SetType.DROPSET))
            viewModel.onAction(SessionAction.DeleteSet(11))
            viewModel.onAction(SessionAction.CancelSession)
            advanceUntilIdle()

            assertEquals(WorkoutSession(startedAt = now, createdAt = now, updatedAt = now), workoutRepository.startedSession)
            assertEquals(
                WorkoutExercise(
                    workoutSessionId = 1,
                    exerciseVariantId = 6,
                    orderIndex = 1,
                    createdAt = now,
                    updatedAt = now,
                ),
                workoutRepository.addedExercise,
            )
            assertEquals(10L, workoutRepository.updatedSets[0].id)
            assertEquals(60L, workoutRepository.updatedSets[0].restAfterSeconds)
            assertEquals(11L, workoutRepository.updatedSets[1].id)
            assertEquals(90.0, workoutRepository.updatedSets[1].weightValue, 0.0)
            assertEquals(5, workoutRepository.updatedSets[1].reps)
            assertEquals(SetType.DROPSET, workoutRepository.updatedSets[1].setType)
            assertEquals(
                WorkoutSet(
                    workoutExerciseId = 2,
                    setNumber = 2,
                    weightValue = 82.5,
                    reps = 8,
                    setType = SetType.WORK,
                    startedAt = now,
                    endedAt = now,
                    restBeforeSeconds = 60,
                    createdAt = now,
                    updatedAt = now,
                ),
                workoutRepository.savedSet,
            )
            assertEquals(11L, workoutRepository.deletedSetId)
            assertEquals(SessionStatus.CANCELLED, workoutRepository.updatedSession?.status)
            ViewModelStore().apply { put("session", viewModel); clear() }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `session completion finishes the active session and recalculates progress`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val workoutRepository = FakeWorkoutRepository(activeSession = MutableStateFlow(sessionDetail()))
            val progressRepository = FakeProgressRepository()
            val viewModel = viewModel(workoutRepository, FakeExerciseRepository(), progressRepository)

            advanceUntilIdle()
            viewModel.onAction(SessionAction.CompleteSession)
            advanceUntilIdle()

            assertEquals(SessionStatus.FINISHED, workoutRepository.updatedSession?.status)
            assertEquals(now, workoutRepository.updatedSession?.endedAt)
            assertEquals(600L, workoutRepository.updatedSession?.durationSeconds)
            assertNotNull(progressRepository.replacedStats)
            assertEquals(1, progressRepository.replaceCalls)
            ViewModelStore().apply { put("session", viewModel); clear() }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `completed and cancelled sessions expose a persistent outcome for the UI`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val activeSession = MutableStateFlow(sessionDetail())
            val workoutRepository = FakeWorkoutRepository(activeSession = activeSession)
            val viewModel = viewModel(workoutRepository, FakeExerciseRepository(), FakeProgressRepository())

            advanceUntilIdle()
            viewModel.onAction(SessionAction.CompleteSession)
            advanceUntilIdle()

            assertEquals(SessionOutcome.Completed, viewModel.uiState.value.outcome)
            assertEquals(SessionOperation.None, viewModel.uiState.value.operation)
            ViewModelStore().apply { put("session", viewModel); clear() }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `manual rest is configured independently per exercise and never changes recorded rest`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val viewModel = viewModel(
                FakeWorkoutRepository(activeSession = MutableStateFlow(sessionDetail())),
                FakeExerciseRepository(),
                FakeProgressRepository(),
            )

            advanceUntilIdle()
            viewModel.onAction(SessionAction.ChangeRestTarget(workoutExerciseId = 2, targetInput = "240"))
            viewModel.onAction(SessionAction.PauseRest(workoutExerciseId = 2))
            viewModel.onAction(SessionAction.AdvanceRest(workoutExerciseId = 2))
            viewModel.onAction(SessionAction.ResumeRest(workoutExerciseId = 2))
            viewModel.onAction(SessionAction.AdvanceRest(workoutExerciseId = 2))

            val timer = viewModel.uiState.value.manualRestTimers.getValue(2)
            assertEquals(240, timer.targetSeconds)
            assertEquals(1, timer.elapsedSeconds)
            assertEquals(true, timer.isRunning)
            assertEquals(null, sessionDetail().exercises.single().sets.single().restAfterSeconds)
            ViewModelStore().apply { put("session", viewModel); clear() }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `failed set save keeps an error state while a successful retry clears it`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repository = FakeWorkoutRepository(
                activeSession = MutableStateFlow(sessionDetail()),
                saveSetResults = ArrayDeque(listOf(
                    AppResult.Failure(AppError.Storage("No se pudo guardar la serie.")),
                    AppResult.Success(3L),
                )),
            )
            val viewModel = viewModel(repository, FakeExerciseRepository(), FakeProgressRepository())

            advanceUntilIdle()
            viewModel.onAction(SessionAction.SaveSet(2, "80", "6", SetType.WORK))
            advanceUntilIdle()
            assertEquals(SetSaveState.Failed("No se pudo guardar la serie."), viewModel.uiState.value.setSaveStates[2])

            viewModel.onAction(SessionAction.SaveSet(2, "80", "6", SetType.WORK))
            advanceUntilIdle()
            assertEquals(SetSaveState.Saved, viewModel.uiState.value.setSaveStates[2])
            assertEquals(2, repository.saveSetCalls)
            ViewModelStore().apply { put("session", viewModel); clear() }
        } finally {
            Dispatchers.resetMain()
        }
    }

    @Test
    fun `saving a set ignores a second tap until the first request completes`() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val saveGate = CompletableDeferred<Unit>()
            val repository = FakeWorkoutRepository(
                activeSession = MutableStateFlow(sessionDetail()),
                saveSetGate = saveGate,
            )
            val viewModel = viewModel(repository, FakeExerciseRepository(), FakeProgressRepository())

            advanceUntilIdle()
            viewModel.onAction(SessionAction.SaveSet(2, "80", "6", SetType.WORK))
            viewModel.onAction(SessionAction.SaveSet(2, "80", "6", SetType.WORK))
            runCurrent()

            assertEquals(SetSaveState.Saving, viewModel.uiState.value.setSaveStates[2])
            assertEquals(1, repository.saveSetCalls)

            saveGate.complete(Unit)
            advanceUntilIdle()
            assertEquals(SetSaveState.Saved, viewModel.uiState.value.setSaveStates[2])
            ViewModelStore().apply { put("session", viewModel); clear() }
        } finally {
            Dispatchers.resetMain()
        }
    }

    private fun viewModel(
        workoutRepository: FakeWorkoutRepository,
        exerciseRepository: FakeExerciseRepository,
        progressRepository: FakeProgressRepository,
    ) = SessionViewModel(
        observeActiveSession = ObserveActiveSessionUseCase(workoutRepository),
        observeActiveExerciseVariants = ObserveActiveExerciseVariantsUseCase(exerciseRepository),
        startWorkoutSession = StartWorkoutSessionUseCase(workoutRepository),
        cancelWorkoutSession = CancelWorkoutSessionUseCase(workoutRepository),
        completeWorkoutSession = CompleteWorkoutSessionUseCase(workoutRepository),
        addExerciseToSession = AddExerciseToSessionUseCase(workoutRepository),
        saveWorkoutSet = SaveWorkoutSetUseCase(workoutRepository),
        updateWorkoutSet = UpdateWorkoutSetUseCase(workoutRepository),
        deleteWorkoutSet = DeleteWorkoutSetUseCase(workoutRepository),
        startRestAfterSet = StartRestAfterSetUseCase(),
        resolveRestBeforeNextSet = ResolveRestBeforeNextSetUseCase(),
        applyWorkoutProgress = ApplyWorkoutProgressUseCase(RecalculateProgressUseCase(workoutRepository, progressRepository)),
        clock = ClockProvider { now },
    )

    private fun sessionDetail() = WorkoutSessionDetail(
        session = WorkoutSession(
            id = 1,
            startedAt = now.minusSeconds(600),
            createdAt = now.minusSeconds(600),
            updatedAt = now.minusSeconds(600),
        ),
        exercises = listOf(
            WorkoutExerciseDetail(
                workoutExercise = WorkoutExercise(
                    id = 2,
                    workoutSessionId = 1,
                    exerciseVariantId = 5,
                    orderIndex = 0,
                    createdAt = now.minusSeconds(600),
                    updatedAt = now.minusSeconds(600),
                ),
                sets = listOf(set(id = 10, endedAt = now.minusSeconds(60))),
            ),
        ),
    )

    private fun set(id: Long, endedAt: Instant? = null) = WorkoutSet(
        id = id,
        workoutExerciseId = 2,
        setNumber = 1,
        weightValue = 80.0,
        reps = 6,
        endedAt = endedAt,
        createdAt = now.minusSeconds(120),
        updatedAt = now.minusSeconds(120),
    )

    private fun variant(id: Long = 5) = ExerciseVariant(
        id = id,
        exerciseBaseId = 3,
        name = "Barra",
        equipmentType = EquipmentType.BARBELL,
        weightComparisonType = WeightComparisonType.TOTAL_WEIGHT,
        createdAt = now,
        updatedAt = now,
    )

    private class FakeWorkoutRepository(
        private val activeSession: Flow<WorkoutSessionDetail?> = flowOf(null),
        private val saveSetResults: ArrayDeque<AppResult<Long>> = ArrayDeque(),
        private val saveSetGate: CompletableDeferred<Unit>? = null,
    ) : WorkoutRepository {
        var startedSession: WorkoutSession? = null
        var updatedSession: WorkoutSession? = null
        var addedExercise: WorkoutExercise? = null
        var savedSet: WorkoutSet? = null
        var saveSetCalls = 0
        val updatedSets = mutableListOf<WorkoutSet>()
        var deletedSetId: Long? = null

        override fun observeActiveSession(): Flow<WorkoutSessionDetail?> = activeSession
        override fun observeSessionDetail(sessionId: Long): Flow<WorkoutSessionDetail?> = flowOf(null)
        override fun observeSessionHistory(limit: Int): Flow<List<WorkoutSession>> = flowOf(emptyList())
        override suspend fun findSessionDetailById(sessionId: Long): AppResult<WorkoutSessionDetail?> = AppResult.Success(null)
        override suspend fun findVariantLastPerformance(variantId: Long): AppResult<VariantLastPerformance?> = AppResult.Success(null)
        override suspend fun findCompletedSessionDetails(): AppResult<List<WorkoutSessionDetail>> = AppResult.Success(emptyList())

        override suspend fun startSession(session: WorkoutSession): AppResult<Long> {
            startedSession = session
            return AppResult.Success(1L)
        }

        override suspend fun updateSession(session: WorkoutSession): AppResult<Unit> {
            updatedSession = session
            return AppResult.Success(Unit)
        }

        override suspend fun addExerciseToSession(workoutExercise: WorkoutExercise): AppResult<Long> {
            addedExercise = workoutExercise
            return AppResult.Success(2L)
        }

        override suspend fun saveWorkoutSet(workoutSet: WorkoutSet): AppResult<Long> {
            saveSetCalls += 1
            savedSet = workoutSet
            saveSetGate?.await()
            return saveSetResults.removeFirstOrNull() ?: AppResult.Success(3L)
        }

        override suspend fun updateWorkoutSet(workoutSet: WorkoutSet): AppResult<Unit> {
            updatedSets += workoutSet
            return AppResult.Success(Unit)
        }

        override suspend fun deleteWorkoutSet(setId: Long): AppResult<Unit> {
            deletedSetId = setId
            return AppResult.Success(Unit)
        }
    }

    private class FakeExerciseRepository(
        private val variants: Flow<List<ExerciseVariant>> = flowOf(emptyList()),
    ) : ExerciseRepository {
        override fun observeMuscleGroups(): Flow<List<MuscleGroup>> = flowOf(emptyList())
        override fun observeActiveExerciseVariants(): Flow<List<ExerciseVariant>> = variants
        override fun observeActiveExerciseCatalog(): Flow<List<ExerciseCatalogEntry>> = flowOf(emptyList())
        override fun observeVariantsForExerciseBase(exerciseBaseId: Long): Flow<List<ExerciseVariant>> = flowOf(emptyList())
        override suspend fun findExerciseVariantById(variantId: Long): AppResult<ExerciseVariant?> = AppResult.Success(null)
        override suspend fun createMuscleGroup(muscleGroup: MuscleGroup): AppResult<Long> = AppResult.Success(0)
        override suspend fun createExerciseBase(exerciseBase: ExerciseBase): AppResult<Long> = AppResult.Success(0)
        override suspend fun updateExerciseBase(exerciseBase: ExerciseBase): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun archiveExerciseBase(baseId: Long, updatedAt: Instant): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun createExerciseVariant(exerciseVariant: ExerciseVariant): AppResult<Long> = AppResult.Success(0)
        override suspend fun updateExerciseVariant(exerciseVariant: ExerciseVariant): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun archiveExerciseVariant(variantId: Long, updatedAt: Instant): AppResult<Unit> = AppResult.Success(Unit)
    }

    private class FakeProgressRepository : ProgressRepository {
        var replaceCalls = 0
        var replacedStats: CharacterStats? = null
        var replacedMastery: List<ExerciseMastery>? = null

        override fun observeCharacterStats(): Flow<CharacterStats> = flowOf(CharacterStats(updatedAt = Instant.EPOCH))
        override fun observeVariantMastery(variantId: Long): Flow<ExerciseMastery?> = flowOf(null)
        override fun observeProgressSummary(): Flow<ProgressSummary> = flowOf(ProgressSummary(CharacterStats(updatedAt = Instant.EPOCH)))
        override suspend fun saveCharacterStats(stats: CharacterStats): AppResult<Unit> = AppResult.Success(Unit)
        override suspend fun saveMastery(mastery: ExerciseMastery): AppResult<Unit> = AppResult.Success(Unit)

        override suspend fun replaceDerivedProgress(
            stats: CharacterStats,
            mastery: List<ExerciseMastery>,
        ): AppResult<Unit> {
            replaceCalls += 1
            replacedStats = stats
            replacedMastery = mastery
            return AppResult.Success(Unit)
        }
    }
}
