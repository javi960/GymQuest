package com.gymquest.app.core.result

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppResultTest {
    @Test
    fun success_reportsSuccessfulStateAndMapsValue() {
        val result = AppResult.Success(2).map { it * 2 }

        assertTrue(result.isSuccess)
        assertEquals(AppResult.Success(4), result)
    }

    @Test
    fun failure_reportsFailureStateAndKeepsOriginalErrorWhenMapped() {
        val error = AppError.Validation("Dato invalido")
        val result = AppResult.Failure(error).map { value: Int -> value * 2 }

        assertFalse(result.isSuccess)
        assertEquals(AppResult.Failure(error), result)
    }
}
