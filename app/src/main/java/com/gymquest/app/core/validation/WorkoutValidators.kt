package com.gymquest.app.core.validation

import com.gymquest.app.domain.model.WorkoutSession
import com.gymquest.app.domain.model.WorkoutSet
import com.gymquest.app.domain.model.enums.SessionStatus

object WorkoutValidators {
    fun validateSet(set: WorkoutSet): ValidationResult {
        val errors = buildList {
            if (set.workoutExerciseId <= 0) {
                add(ValidationError("workoutExerciseId", "La serie debe pertenecer a un ejercicio."))
            }
            if (set.setNumber <= 0) {
                add(ValidationError("setNumber", "El numero de serie debe ser mayor que cero."))
            }
            if (set.weightValue < 0.0) {
                add(ValidationError("weightValue", "El peso no puede ser negativo."))
            }
            if (set.reps <= 0) {
                add(ValidationError("reps", "Las repeticiones deben ser mayores que cero."))
            }
            if ((set.restBeforeSeconds ?: 0) < 0) {
                add(ValidationError("restBeforeSeconds", "El descanso previo no puede ser negativo."))
            }
            if ((set.restAfterSeconds ?: 0) < 0) {
                add(ValidationError("restAfterSeconds", "El descanso posterior no puede ser negativo."))
            }
            if (set.startedAt != null && set.endedAt != null && set.endedAt.isBefore(set.startedAt)) {
                add(ValidationError("endedAt", "La serie no puede terminar antes de empezar."))
            }
            if (set.updatedAt.isBefore(set.createdAt)) {
                add(ValidationError("updatedAt", "La fecha de actualizacion no puede ser anterior a la creacion."))
            }
        }

        return errors.toValidationResult()
    }

    fun validateSession(session: WorkoutSession): ValidationResult {
        val errors = buildList {
            if (session.durationSeconds < 0) {
                add(ValidationError("durationSeconds", "La duracion no puede ser negativa."))
            }
            if (session.perceivedEnergy != null && session.perceivedEnergy !in MIN_ENERGY..MAX_ENERGY) {
                add(ValidationError("perceivedEnergy", "La energia percibida debe estar entre $MIN_ENERGY y $MAX_ENERGY."))
            }
            if (session.endedAt != null && session.endedAt.isBefore(session.startedAt)) {
                add(ValidationError("endedAt", "La sesion no puede terminar antes de empezar."))
            }
            if (session.status == SessionStatus.FINISHED && session.endedAt == null) {
                add(ValidationError("endedAt", "Una sesion finalizada debe tener fecha de fin."))
            }
            if (session.updatedAt.isBefore(session.createdAt)) {
                add(ValidationError("updatedAt", "La fecha de actualizacion no puede ser anterior a la creacion."))
            }
        }

        return errors.toValidationResult()
    }

    private const val MIN_ENERGY = 1
    private const val MAX_ENERGY = 5
}
