package com.gymquest.app.feature.session

import org.junit.Assert.assertEquals
import org.junit.Test

class ManualRestTimerStateTest {
    @Test
    fun `manual timer pauses without losing elapsed rest and resumes`() {
        val started = ManualRestTimerState().advanceBy(75)

        val paused = started.pause().advanceBy(30)
        val resumed = paused.resume().advanceBy(45)

        assertEquals(75, paused.elapsedSeconds)
        assertEquals(120, resumed.elapsedSeconds)
        assertEquals(180, resumed.targetSeconds)
    }

    @Test
    fun `manual timer progress is capped at its configured target`() {
        val timer = ManualRestTimerState(targetSeconds = 90).advanceBy(120)

        assertEquals(1f, timer.progress, 0f)
    }
}
