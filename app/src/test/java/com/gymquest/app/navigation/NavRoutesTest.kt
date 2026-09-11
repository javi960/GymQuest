package com.gymquest.app.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class NavRoutesTest {
    @Test
    fun `add variant route keeps the exercise identifier`() {
        assertEquals("add_variant/42", NavRoutes.addVariant(42))
    }
}
