package com.gymquest.app.core.validation

object TextValidators {
    fun required(
        value: String,
        field: String,
        maxLength: Int = DEFAULT_MAX_LENGTH
    ): ValidationResult {
        val trimmed = value.trim()
        val errors = buildList {
            if (trimmed.isEmpty()) {
                add(ValidationError(field, "El campo no puede estar vacio."))
            }
            if (trimmed.length > maxLength) {
                add(ValidationError(field, "El campo no puede superar $maxLength caracteres."))
            }
        }

        return errors.toValidationResult()
    }

    fun optional(
        value: String?,
        field: String,
        maxLength: Int = DEFAULT_MAX_LENGTH
    ): ValidationResult {
        val length = value?.trim()?.length ?: return ValidationResult.Valid
        return if (length > maxLength) {
            ValidationResult.Invalid(
                listOf(ValidationError(field, "El campo no puede superar $maxLength caracteres."))
            )
        } else {
            ValidationResult.Valid
        }
    }

    private const val DEFAULT_MAX_LENGTH = 120
}
