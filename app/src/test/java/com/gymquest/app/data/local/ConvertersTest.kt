package com.gymquest.app.data.local

import com.gymquest.app.domain.model.enums.SetType
import com.gymquest.app.domain.model.enums.WeightComparisonType
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun `round trips instant values`() {
        val instant = Instant.parse("2026-09-04T10:15:30Z")

        assertEquals(instant, converters.storageValueToInstant(converters.instantToStorageValue(instant)))
    }

    @Test
    fun `round trips set type values`() {
        assertEquals(SetType.TOP_SET, converters.storageValueToSetType(converters.setTypeToStorageValue(SetType.TOP_SET)))
    }

    @Test
    fun `round trips every weight comparison type`() {
        WeightComparisonType.entries.forEach { type ->
            assertEquals(type, converters.storageValueToWeightComparisonType(converters.weightComparisonTypeToStorageValue(type)))
        }
    }

    @Test
    fun `preserves nullable values`() {
        assertNull(converters.instantToStorageValue(null))
        assertNull(converters.storageValueToSetType(null))
    }
}
