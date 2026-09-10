package com.gymquest.app.domain.usecase.progress

class CalculateSetVolumeUseCase {
    operator fun invoke(weightValue: Double, reps: Int): Double {
        require(weightValue.isFinite() && weightValue >= 0.0) { "El peso no puede ser negativo ni no finito." }
        require(reps >= 0) { "Las repeticiones no pueden ser negativas." }
        return weightValue * reps
    }
}
