package com.gymquest.app.core.time

import java.time.Instant

fun interface ClockProvider {
    fun now(): Instant
}
