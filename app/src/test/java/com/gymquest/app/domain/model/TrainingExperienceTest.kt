package com.gymquest.app.domain.model

import com.gymquest.app.data.local.projection.TrainingExperienceSummary
import org.junit.Assert.assertEquals
import org.junit.Test

class TrainingExperienceTest {
    @Test
    fun `awards fifteen xp per completed exercise and forty per completed session`() {
        assertEquals(115L, TrainingExperience.total(TrainingExperienceSummary(completedExercises = 5, completedSessions = 1)))
    }

    @Test
    fun `empty history grants no training xp`() {
        assertEquals(0L, TrainingExperience.total(TrainingExperienceSummary(completedExercises = 0, completedSessions = 0)))
    }
}
