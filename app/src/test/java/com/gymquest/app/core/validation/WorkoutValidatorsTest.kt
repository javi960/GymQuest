package com.gymquest.app.core.validation

import com.gymquest.app.domain.model.WorkoutSession
import com.gymquest.app.domain.model.WorkoutSet
import com.gymquest.app.domain.model.enums.SessionStatus
import java.time.Instant
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorkoutValidatorsTest {
    @Test
    fun validateSet_acceptsValidWorkSet() {
        val result = WorkoutValidators.validateSet(workoutSet())

        assertTrue(result.isValid)
    }

    @Test
    fun validateSet_rejectsNegativeWeightAndNonPositiveReps() {
        val result = WorkoutValidators.validateSet(
            workoutSet(weightValue = -1.0, reps = 0)
        )

        assertFalse(result.isValid)
    }

    @Test
    fun validateSession_acceptsActiveSessionWithoutEnd() {
        val result = WorkoutValidators.validateSession(workoutSession())

        assertTrue(result.isValid)
    }

    @Test
    fun validateSession_rejectsEnergyOutsideDefinedRange() {
        val result = WorkoutValidators.validateSession(
            workoutSession(perceivedEnergy = 6)
        )

        assertFalse(result.isValid)
    }

    @Test
    fun validateSession_rejectsFinishedSessionWithoutEnd() {
        val result = WorkoutValidators.validateSession(
            workoutSession(status = SessionStatus.FINISHED)
        )

        assertFalse(result.isValid)
    }

    private fun workoutSet(
        weightValue: Double = 80.0,
        reps: Int = 8
    ): WorkoutSet {
        val now = Instant.parse("2026-09-04T10:00:00Z")
        return WorkoutSet(
            workoutExerciseId = 1,
            setNumber = 1,
            weightValue = weightValue,
            reps = reps,
            createdAt = now,
            updatedAt = now
        )
    }

    private fun workoutSession(
        status: SessionStatus = SessionStatus.ACTIVE,
        perceivedEnergy: Int? = null
    ): WorkoutSession {
        val now = Instant.parse("2026-09-04T10:00:00Z")
        return WorkoutSession(
            startedAt = now,
            status = status,
            perceivedEnergy = perceivedEnergy,
            createdAt = now,
            updatedAt = now
        )
    }
}
