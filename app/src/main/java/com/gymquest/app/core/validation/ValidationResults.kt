package com.gymquest.app.core.validation

internal fun List<ValidationError>.toValidationResult(): ValidationResult =
    if (isEmpty()) ValidationResult.Valid else ValidationResult.Invalid(this)
