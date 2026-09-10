package com.gymquest.app.data.backup

import com.gymquest.app.domain.model.BackupMetadata
import com.gymquest.app.domain.model.BackupSnapshot
import com.gymquest.app.domain.model.BackupValue
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupJsonSerializerTest {
    private val serializer = BackupJsonSerializer()

    @Test
    fun `serializes a versioned snapshot without losing sqlite value types`() {
        val snapshot = BackupSnapshot(
            metadata = BackupMetadata(
                schemaVersion = 1,
                databaseSchemaVersion = 6,
                exportedAt = Instant.parse("2026-09-09T09:00:00Z"),
            ),
            tables = mapOf(
                "workout_sets" to listOf(
                    mapOf(
                        "id" to BackupValue.IntegerValue(7),
                        "weightValue" to BackupValue.RealValue(82.5),
                        "notes" to BackupValue.NullValue,
                        "setType" to BackupValue.TextValue("WORK"),
                    ),
                ),
            ),
        )

        val json = serializer.serialize(snapshot)
        val decoded = serializer.deserialize(json)

        assertEquals(snapshot, decoded)
        assertTrue(json.contains("\"schemaVersion\""))
        assertTrue(json.contains("2026-09-09T09:00:00Z"))
    }

    @Test
    fun `rejects unsupported schema and malformed payloads`() {
        assertTrue(!serializer.validate("{\"schemaVersion\":99}").isSuccess)
        assertTrue(!serializer.validate("not json").isSuccess)
    }
}
