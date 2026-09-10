package com.gymquest.app.domain.usecase.progress

import com.gymquest.app.domain.model.PersonalRecords
import com.gymquest.app.domain.model.WorkoutSet

data class PersonalRecordDetection(
    val records: PersonalRecords,
    val isWeightRecord: Boolean,
    val isRepsRecord: Boolean,
    val isVolumeRecord: Boolean,
)

class DetectPersonalRecordsUseCase {
    operator fun invoke(previous: PersonalRecords?, candidate: WorkoutSet): PersonalRecordDetection {
        require(candidate.weightValue.isFinite() && candidate.weightValue >= 0.0 && candidate.reps >= 0) {
            "La serie no es valida para records."
        }
        val volume = candidate.volume
        val isWeightRecord = previous?.weight == null || candidate.weightValue > previous.weight
        val isRepsRecord = previous?.reps == null || candidate.reps > previous.reps
        val isVolumeRecord = previous?.volume == null || volume > previous.volume
        return PersonalRecordDetection(
            records = PersonalRecords(
                weight = maxOf(previous?.weight ?: 0.0, candidate.weightValue),
                reps = maxOf(previous?.reps ?: 0, candidate.reps),
                volume = maxOf(previous?.volume ?: 0.0, volume),
            ),
            isWeightRecord = isWeightRecord,
            isRepsRecord = isRepsRecord,
            isVolumeRecord = isVolumeRecord,
        )
    }
}
