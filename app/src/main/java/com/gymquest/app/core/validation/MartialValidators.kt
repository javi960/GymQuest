package com.gymquest.app.core.validation

object MartialValidators {
    fun validatePracticeDurationSeconds(durationSeconds: Long): ValidationResult =
        if (durationSeconds > 0) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(
                listOf(ValidationError("durationSeconds", "La practica debe durar mas de cero segundos."))
            )
        }

    fun validateDifficulty(value: Int?): ValidationResult =
        validateOptionalScale(
            value = value,
            field = "difficulty",
            label = "La dificultad"
        )

    fun validateConfidence(value: Int?): ValidationResult =
        validateOptionalScale(
            value = value,
            field = "confidence",
            label = "La confianza"
        )

    private fun validateOptionalScale(
        value: Int?,
        field: String,
        label: String
    ): ValidationResult {
        if (value == null || value in MIN_SCALE..MAX_SCALE) {
            return ValidationResult.Valid
        }

        return ValidationResult.Invalid(
            listOf(ValidationError(field, "$label debe estar entre $MIN_SCALE y $MAX_SCALE."))
        )
    }

    private const val MIN_SCALE = 1
    private const val MAX_SCALE = 5
}
