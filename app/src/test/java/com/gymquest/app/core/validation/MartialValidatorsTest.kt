package com.gymquest.app.core.validation

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MartialValidatorsTest {
    @Test
    fun validatePracticeDurationSeconds_acceptsPositiveDuration() {
        assertTrue(MartialValidators.validatePracticeDurationSeconds(60).isValid)
    }

    @Test
    fun validatePracticeDurationSeconds_rejectsZeroDuration() {
        assertFalse(MartialValidators.validatePracticeDurationSeconds(0).isValid)
    }

    @Test
    fun validateConfidence_acceptsNullOrScaleValue() {
        assertTrue(MartialValidators.validateConfidence(null).isValid)
        assertTrue(MartialValidators.validateConfidence(3).isValid)
    }

    @Test
    fun validateDifficulty_rejectsValueOutsideScale() {
        assertFalse(MartialValidators.validateDifficulty(6).isValid)
    }
}
