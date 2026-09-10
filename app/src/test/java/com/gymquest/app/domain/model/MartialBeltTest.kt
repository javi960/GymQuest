package com.gymquest.app.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class MartialBeltTest {
    @Test
    fun `each manual belt resolves to its owner supplied avatar`() {
        assertEquals("cinturon_blanco.png", MartialBelt.WHITE.avatarFileName)
        assertEquals("cinturon_marron.png", MartialBelt.BROWN.avatarFileName)
        assertEquals("cinturon_negro.png", MartialBelt.BLACK.avatarFileName)
    }
}
