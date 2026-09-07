package com.gymquest.app.core.result

sealed interface AppError {
    val message: String

    data class Validation(
        override val message: String
    ) : AppError

    data class NotFound(
        override val message: String
    ) : AppError

    data class Storage(
        override val message: String,
        val cause: Throwable? = null
    ) : AppError

    data class Unexpected(
        override val message: String,
        val cause: Throwable? = null
    ) : AppError
}
