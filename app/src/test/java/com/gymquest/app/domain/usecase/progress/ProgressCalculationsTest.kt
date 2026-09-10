package com.gymquest.app.domain.usecase.progress

import com.gymquest.app.domain.model.WorkoutSet
import com.gymquest.app.domain.model.enums.SetType
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressCalculationsTest {
    private val now = Instant.parse("2026-09-07T10:00:00Z")

    @Test
    fun `set volume multiplies non-negative weight and reps`() {
        assertEquals(600.0, CalculateSetVolumeUseCase()(50.0, 12), 0.0)
        assertEquals(0.0, CalculateSetVolumeUseCase()(0.0, 12), 0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `set volume rejects a negative weight`() {
        CalculateSetVolumeUseCase()(-0.5, 5)
    }

    @Test
    fun `set xp follows the documented base and volume formula`() {
        val useCase = CalculateSetXpUseCase()

        assertEquals(5L, useCase(workoutSet(weight = 10.0, reps = 9)))
        assertEquals(11L, useCase(workoutSet(weight = 50.0, reps = 12)))
        assertEquals(36L, useCase(workoutSet(weight = 50.0, reps = 12), isNewDiscovery = true))
        assertEquals(46L, useCase(workoutSet(weight = 50.0, reps = 12), isNewWeightRecord = true, isNewVolumeRecord = true))
    }

    @Test
    fun `warmup sets do not receive xp until a rule is explicitly approved`() {
        assertEquals(0L, CalculateSetXpUseCase()(workoutSet(setType = SetType.WARM_UP)))
    }

    @Test
    fun `character level changes exactly at documented thresholds`() {
        val useCase = CalculateCharacterLevelUseCase()

        assertEquals(1, useCase(0))
        assertEquals(1, useCase(99))
        assertEquals(2, useCase(100))
        assertEquals(2, useCase(399))
        assertEquals(3, useCase(400))
        assertEquals(5, useCase(1_600))
    }

    private fun workoutSet(
        weight: Double = 50.0,
        reps: Int = 12,
        setType: SetType = SetType.WORK,
    ) = WorkoutSet(
        workoutExerciseId = 1,
        setNumber = 1,
        weightValue = weight,
        reps = reps,
        setType = setType,
        createdAt = now,
        updatedAt = now,
    )
}
