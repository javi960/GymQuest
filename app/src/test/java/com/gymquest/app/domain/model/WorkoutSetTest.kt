package com.gymquest.app.domain.model

import com.gymquest.app.domain.model.enums.SetType
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Test

class WorkoutSetTest {
    @Test
    fun volume_isCalculatedFromWeightAndReps() {
        val set = workoutSet(weightValue = 80.0, reps = 8)

        assertEquals(640.0, set.volume, 0.0)
    }

    @Test
    fun volume_updatesWhenSetIsCopiedWithNewWeightOrReps() {
        val original = workoutSet(weightValue = 80.0, reps = 8)

        val updated = original.copy(weightValue = 100.0, reps = 5)

        assertEquals(500.0, updated.volume, 0.0)
    }

    private fun workoutSet(
        weightValue: Double,
        reps: Int
    ): WorkoutSet {
        val now = Instant.parse("2026-09-04T10:00:00Z")
        return WorkoutSet(
            workoutExerciseId = 1,
            setNumber = 1,
            weightValue = weightValue,
            reps = reps,
            setType = SetType.WORK,
            createdAt = now,
            updatedAt = now
        )
    }
}
