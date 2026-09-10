package com.gymquest.app.domain.usecase.progress

import com.gymquest.app.domain.model.WorkoutSet
import com.gymquest.app.domain.model.enums.SetType

class CalculateSetXpUseCase(
    private val calculateSetVolume: CalculateSetVolumeUseCase = CalculateSetVolumeUseCase(),
) {
    operator fun invoke(
        set: WorkoutSet,
        isNewDiscovery: Boolean = false,
        isNewWeightRecord: Boolean = false,
        isNewVolumeRecord: Boolean = false,
    ): Long {
        if (set.setType == SetType.WARM_UP) return 0L
        val volumeXp = (calculateSetVolume(set.weightValue, set.reps) / VOLUME_PER_XP_KG).toLong()
        return BASE_SET_XP + volumeXp +
            (if (isNewDiscovery) DISCOVERY_XP else 0L) +
            (if (isNewWeightRecord) WEIGHT_RECORD_XP else 0L) +
            (if (isNewVolumeRecord) VOLUME_RECORD_XP else 0L)
    }

    private companion object {
        const val BASE_SET_XP = 5L
        const val VOLUME_PER_XP_KG = 100.0
        const val DISCOVERY_XP = 25L
        const val WEIGHT_RECORD_XP = 20L
        const val VOLUME_RECORD_XP = 15L
    }
}
