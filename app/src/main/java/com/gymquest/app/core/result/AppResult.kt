package com.gymquest.app.core.result

sealed interface AppResult<out T> {
    data class Success<T>(
        val value: T
    ) : AppResult<T>

    data class Failure(
        val error: AppError
    ) : AppResult<Nothing>

    val isSuccess: Boolean
        get() = this is Success<T>
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> =
    when (this) {
        is AppResult.Success -> AppResult.Success(transform(value))
        is AppResult.Failure -> this
    }
