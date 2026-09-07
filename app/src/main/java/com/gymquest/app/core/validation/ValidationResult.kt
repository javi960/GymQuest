package com.gymquest.app.core.validation

sealed interface ValidationResult {
    data object Valid : ValidationResult

    data class Invalid(
        val errors: List<ValidationError>
    ) : ValidationResult

    val isValid: Boolean
        get() = this is Valid
}

data class ValidationError(
    val field: String,
    val message: String
)
