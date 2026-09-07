package com.gymquest.app.core.time

import java.time.Instant

object SystemClockProvider : ClockProvider {
    override fun now(): Instant = Instant.now()
}
