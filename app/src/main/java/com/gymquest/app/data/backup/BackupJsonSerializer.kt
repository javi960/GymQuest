package com.gymquest.app.data.backup

import com.gymquest.app.core.result.AppError
import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.BackupMetadata
import com.gymquest.app.domain.model.BackupSnapshot
import com.gymquest.app.domain.model.BackupValue
import java.time.Instant
import java.util.Base64
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.put

class BackupJsonSerializer {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
        private const val FORMAT = "gymquest-backup"
    }

    private val json = Json { prettyPrint = true; explicitNulls = true }

    fun serialize(snapshot: BackupSnapshot): String {
        require(snapshot.metadata.schemaVersion == CURRENT_SCHEMA_VERSION) { "Version de backup no compatible." }
        return json.encodeToString(JsonObject.serializer(), snapshot.toJson())
    }

    fun deserialize(payload: String): BackupSnapshot {
        val root = json.parseToJsonElement(payload).jsonObject
        val metadata = BackupMetadata(
            format = root.requiredString("format"),
            schemaVersion = root.requiredLong("schemaVersion").toInt(),
            databaseSchemaVersion = root.requiredLong("databaseSchemaVersion").toInt(),
            exportedAt = Instant.parse(root.requiredString("exportedAt")),
        )
        require(metadata.format == FORMAT) { "Formato de backup no reconocido." }
        require(metadata.schemaVersion == CURRENT_SCHEMA_VERSION) { "Version de backup no compatible." }
        val tables = root.required("tables").jsonObject.mapValues { (_, rows) ->
            rows.jsonArray.map { row ->
                row.jsonObject.mapValues { (_, value) -> value.toBackupValue() }
            }
        }
        return BackupSnapshot(metadata = metadata, tables = tables)
    }

    fun validate(payload: String): AppResult<Unit> = try {
        deserialize(payload)
        AppResult.Success(Unit)
    } catch (error: Exception) {
        AppResult.Failure(AppError.Validation("El backup JSON no cumple el esquema: ${error.message ?: "contenido no valido"}"))
    }

    private fun BackupSnapshot.toJson(): JsonObject = buildJsonObject {
        put("format", metadata.format)
        put("schemaVersion", metadata.schemaVersion)
        put("databaseSchemaVersion", metadata.databaseSchemaVersion)
        put("exportedAt", metadata.exportedAt.toString())
        put("tables", JsonObject(tables.mapValues { (_, rows) ->
            JsonArray(rows.map { row -> JsonObject(row.mapValues { (_, value) -> value.toJson() }) })
        }))
    }

    private fun BackupValue.toJson(): JsonObject = buildJsonObject {
        when (this@toJson) {
            is BackupValue.IntegerValue -> { put("type", "integer"); put("value", value) }
            is BackupValue.RealValue -> { put("type", "real"); put("value", value) }
            is BackupValue.TextValue -> { put("type", "text"); put("value", value) }
            is BackupValue.BlobValue -> { put("type", "blob"); put("value", Base64.getEncoder().encodeToString(value)) }
            BackupValue.NullValue -> put("type", "null")
        }
    }

    private fun JsonElement.toBackupValue(): BackupValue {
        val value = jsonObject
        return when (value.requiredString("type")) {
            "integer" -> BackupValue.IntegerValue(value.requiredLong("value"))
            "real" -> BackupValue.RealValue(value.required("value").jsonPrimitive.double)
            "text" -> BackupValue.TextValue(value.requiredString("value"))
            "blob" -> BackupValue.BlobValue(Base64.getDecoder().decode(value.requiredString("value")))
            "null" -> BackupValue.NullValue
            else -> error("Tipo SQLite no compatible.")
        }
    }

    private fun JsonObject.required(name: String): JsonElement = get(name) ?: error("Falta $name.")
    private fun JsonObject.requiredString(name: String): String = required(name).jsonPrimitive.content
    private fun JsonObject.requiredLong(name: String): Long = required(name).jsonPrimitive.long
}
