package com.gymquest.app.domain.usecase.progress

import com.gymquest.app.domain.model.WorkoutSet
import com.gymquest.app.domain.model.enums.MasteryRank
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressRecordsTest {
    private val now = Instant.parse("2026-09-07T10:00:00Z")

    @Test
    fun `records compare only the same exercise variant history`() {
        val useCase = DetectPersonalRecordsUseCase()
        val first = useCase(null, set(weight = 60.0, reps = 8))
        val same = useCase(first.records, set(weight = 60.0, reps = 8))
        val heavier = useCase(first.records, set(weight = 65.0, reps = 8))

        assertTrue(first.isWeightRecord)
        assertTrue(first.isVolumeRecord)
        assertFalse(same.isWeightRecord)
        assertFalse(same.isVolumeRecord)
        assertTrue(heavier.isWeightRecord)
        assertTrue(heavier.isVolumeRecord)
    }

    @Test
    fun `mastery derives totals records and rank from valid sets`() {
        val mastery = UpdateExerciseMasteryUseCase()(exerciseVariantId = 7, sets = List(10) { index ->
            set(weight = 50.0 + index, reps = 10, offsetSeconds = index.toLong())
        }, updatedAt = now)

        assertEquals(10, mastery.totalSets)
        assertEquals(100, mastery.totalReps)
        assertEquals(5450.0, mastery.accumulatedVolume, 0.0)
        assertEquals(59.0, mastery.personalRecordWeight ?: -1.0, 0.0)
        assertEquals(MasteryRank.APPRENTICE, mastery.masteryRank)
        assertEquals(2, mastery.level)
    }

    private fun set(weight: Double, reps: Int, offsetSeconds: Long = 0) = WorkoutSet(
        workoutExerciseId = 1,
        setNumber = 1,
        weightValue = weight,
        reps = reps,
        createdAt = now.plusSeconds(offsetSeconds),
        updatedAt = now.plusSeconds(offsetSeconds),
    )
}
