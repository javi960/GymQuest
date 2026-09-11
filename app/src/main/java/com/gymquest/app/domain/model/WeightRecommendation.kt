package com.gymquest.app.domain.model

import java.time.Duration
import java.time.Instant
import kotlin.math.round

data class RecordedWorkingSet(val weight: Double, val reps: Int, val completedAt: Instant)

data class WeightRecommendation(
    val targetReps: Int,
    val suggestedWeight: Double,
    val source: RecordedWorkingSet,
    val returnToTraining: Boolean,
    val reductionPercent: Int,
)

/** Conservative, unit-agnostic Epley recommendation. The caller must only provide comparable loads. */
object WeightRecommendationCalculator {
    fun recommend(source: RecordedWorkingSet?, targetReps: Int, now: Instant): WeightRecommendation? {
        if (source == null || source.weight <= 0 || source.reps <= 0 || targetReps <= 0) return null
        val days = Duration.between(source.completedAt, now).toDays().coerceAtLeast(0)
        val reduction = when {
            days > 90 -> 20
            days > 60 -> 15
            days > 30 -> 10
            else -> 0
        }
        val estimatedOneRepMax = source.weight * (1.0 + source.reps / 30.0)
        val suggested = estimatedOneRepMax * (1.0 - reduction / 100.0) / (1.0 + targetReps / 30.0)
        return WeightRecommendation(targetReps, round(suggested * 2) / 2, source, reduction > 0, reduction)
    }
}
