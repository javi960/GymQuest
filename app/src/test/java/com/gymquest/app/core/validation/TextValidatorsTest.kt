package com.gymquest.app.core.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextValidatorsTest {
    @Test
    fun required_rejectsBlankText() {
        val result = TextValidators.required("   ", field = "name")

        assertFalse(result.isValid)
    }

    @Test
    fun required_acceptsTrimmedTextWithinLimit() {
        val result = TextValidators.required(" Sentadilla ", field = "name")

        assertTrue(result.isValid)
    }

    @Test
    fun optional_rejectsTextLongerThanLimit() {
        val result = TextValidators.optional("abcd", field = "notes", maxLength = 3)

        assertFalse(result.isValid)
    }
}
