package com.gymquest.app.data.mapper

import com.gymquest.app.data.local.entity.CharacterStatsEntity
import com.gymquest.app.data.local.entity.ExerciseMasteryEntity
import com.gymquest.app.domain.model.CharacterStats
import com.gymquest.app.domain.model.ExerciseMastery

object ProgressMapper {
    fun toDomain(entity: CharacterStatsEntity) = CharacterStats(
        userProfileId = entity.userProfileId, level = entity.level, totalXp = entity.totalXp,
        currentStreak = entity.currentStreak, totalWorkouts = entity.totalWorkouts,
        totalTrainingSeconds = entity.totalTrainingSeconds, discoveredVariants = entity.discoveredVariants,
        strengthXp = entity.strengthXp, enduranceXp = entity.enduranceXp,
        consistencyXp = entity.consistencyXp, techniqueXp = entity.techniqueXp,
        disciplineXp = entity.disciplineXp, updatedAt = entity.updatedAt,
    )

    fun toEntity(model: CharacterStats) = CharacterStatsEntity(
        userProfileId = model.userProfileId, level = model.level, totalXp = model.totalXp,
        currentStreak = model.currentStreak, totalWorkouts = model.totalWorkouts,
        totalTrainingSeconds = model.totalTrainingSeconds, discoveredVariants = model.discoveredVariants,
        strengthXp = model.strengthXp, enduranceXp = model.enduranceXp,
        consistencyXp = model.consistencyXp, techniqueXp = model.techniqueXp,
        disciplineXp = model.disciplineXp, updatedAt = model.updatedAt,
    )

    fun toDomain(entity: ExerciseMasteryEntity) = ExerciseMastery(
        exerciseVariantId = entity.exerciseVariantId, level = entity.level, masteryRank = entity.masteryRank,
        accumulatedVolume = entity.accumulatedVolume, totalSets = entity.totalSets, totalReps = entity.totalReps,
        personalRecordWeight = entity.personalRecordWeight, personalRecordReps = entity.personalRecordReps,
        personalRecordVolume = entity.personalRecordVolume, discoveredAt = entity.discoveredAt, updatedAt = entity.updatedAt,
    )

    fun toEntity(model: ExerciseMastery) = ExerciseMasteryEntity(
        exerciseVariantId = model.exerciseVariantId, level = model.level, masteryRank = model.masteryRank,
        accumulatedVolume = model.accumulatedVolume, totalSets = model.totalSets, totalReps = model.totalReps,
        personalRecordWeight = model.personalRecordWeight, personalRecordReps = model.personalRecordReps,
        personalRecordVolume = model.personalRecordVolume, discoveredAt = model.discoveredAt, updatedAt = model.updatedAt,
    )
}
