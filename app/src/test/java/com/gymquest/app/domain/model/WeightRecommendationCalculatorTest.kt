package com.gymquest.app.domain.model

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WeightRecommendationCalculatorTest {
    private val now = Instant.parse("2026-09-11T10:00:00Z")

    @Test fun `returns no recommendation without comparable history`() {
        assertNull(WeightRecommendationCalculator.recommend(null, 8, now))
    }

    @Test fun `reduces a recommendation by ten percent after more than thirty days`() {
        val result = WeightRecommendationCalculator.recommend(RecordedWorkingSet(80.0, 8, now.minusSeconds(31 * 86_400)), 8, now)!!
        assertEquals(72.0, result.suggestedWeight, 0.01)
        assertEquals(10, result.reductionPercent)
    }

    @Test fun `adapts the suggestion to the target repetitions`() {
        val result = WeightRecommendationCalculator.recommend(RecordedWorkingSet(80.0, 8, now.minusSeconds(86_400)), 5, now)!!
        assertEquals(87.0, result.suggestedWeight, 0.01)
    }
}
