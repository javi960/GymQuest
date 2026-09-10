package com.gymquest.app.feature.session

/** UI-only countdown state; recorded rest remains the real elapsed time between sets. */
data class ManualRestTimerState(
    val targetSeconds: Int = DEFAULT_MANUAL_REST_SECONDS,
    val elapsedSeconds: Int = 0,
    val isRunning: Boolean = true,
) {
    val progress: Float
        get() = (elapsedSeconds.toFloat() / targetSeconds.coerceAtLeast(1)).coerceIn(0f, 1f)

    fun advanceBy(seconds: Int): ManualRestTimerState =
        if (isRunning) copy(elapsedSeconds = (elapsedSeconds + seconds.coerceAtLeast(0)).coerceAtMost(Int.MAX_VALUE)) else this

    fun pause(): ManualRestTimerState = copy(isRunning = false)

    fun resume(): ManualRestTimerState = copy(isRunning = true)

    fun withTarget(seconds: Int): ManualRestTimerState = copy(targetSeconds = seconds.coerceAtLeast(1))

    fun restart(): ManualRestTimerState = copy(elapsedSeconds = 0, isRunning = true)
}

const val DEFAULT_MANUAL_REST_SECONDS = 180
