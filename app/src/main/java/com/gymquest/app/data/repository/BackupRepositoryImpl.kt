package com.gymquest.app.data.repository

import androidx.room.withTransaction
import com.gymquest.app.core.result.AppError
import com.gymquest.app.core.result.AppResult
import com.gymquest.app.data.backup.BackupFileWriter
import com.gymquest.app.data.backup.BackupJsonSerializer
import com.gymquest.app.data.local.DatabaseConstants
import com.gymquest.app.data.local.GymQuestDatabase
import com.gymquest.app.domain.model.BackupMetadata
import com.gymquest.app.domain.model.BackupSnapshot
import com.gymquest.app.domain.model.BackupValue
import com.gymquest.app.domain.repository.BackupRepository
import java.io.OutputStream
import java.time.Clock

class BackupRepositoryImpl(
    private val database: GymQuestDatabase,
    private val clock: Clock = Clock.systemUTC(),
    private val serializer: BackupJsonSerializer = BackupJsonSerializer(),
    private val fileWriter: BackupFileWriter = BackupFileWriter(),
) : BackupRepository {
    override suspend fun buildSnapshot(): AppResult<BackupSnapshot> = try {
        val snapshot = database.withTransaction {
            val sql = database.openHelper.readableDatabase
            val tables = sql.query(
                "SELECT name FROM sqlite_master WHERE type = 'table' " +
                    "AND name NOT LIKE 'sqlite_%' " +
                    "AND name NOT IN ('android_metadata', 'room_master_table', 'media_files') ORDER BY name",
            ).use { cursor ->
                buildMap {
                    while (cursor.moveToNext()) {
                        val table = cursor.getString(0)
                        put(table, sql.query("SELECT * FROM `" + table.replace("`", "``") + "`").use(::readRows))
                    }
                }
            }
            BackupSnapshot(
                metadata = BackupMetadata(
                    schemaVersion = BackupJsonSerializer.CURRENT_SCHEMA_VERSION,
                    databaseSchemaVersion = DatabaseConstants.SCHEMA_VERSION,
                    exportedAt = clock.instant(),
                ),
                tables = tables,
            )
        }
        AppResult.Success(snapshot)
    } catch (error: Exception) {
        AppResult.Failure(AppError.Storage("No se pudo preparar la copia de seguridad.", error))
    }

    override fun serialize(snapshot: BackupSnapshot): AppResult<String> = try {
        AppResult.Success(serializer.serialize(snapshot))
    } catch (error: Exception) {
        AppResult.Failure(AppError.Validation("No se pudo serializar el backup: ${error.message ?: "datos no validos"}"))
    }

    override fun validate(json: String): AppResult<Unit> = serializer.validate(json)

    override fun write(json: String, destination: OutputStream): AppResult<Unit> =
        fileWriter.write(json, destination)

    private fun readRows(cursor: android.database.Cursor): List<Map<String, BackupValue>> = buildList {
        val columns = cursor.columnNames
        while (cursor.moveToNext()) {
            add(buildMap {
                columns.forEachIndexed { index, name ->
                    put(name, cursorValue(cursor, index))
                }
            })
        }
    }

    private fun cursorValue(cursor: android.database.Cursor, index: Int): BackupValue = when (cursor.getType(index)) {
        android.database.Cursor.FIELD_TYPE_NULL -> BackupValue.NullValue
        android.database.Cursor.FIELD_TYPE_INTEGER -> BackupValue.IntegerValue(cursor.getLong(index))
        android.database.Cursor.FIELD_TYPE_FLOAT -> BackupValue.RealValue(cursor.getDouble(index))
        android.database.Cursor.FIELD_TYPE_BLOB -> BackupValue.BlobValue(cursor.getBlob(index))
        else -> BackupValue.TextValue(cursor.getString(index))
    }
}
