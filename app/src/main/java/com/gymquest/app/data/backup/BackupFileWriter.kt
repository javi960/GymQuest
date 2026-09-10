package com.gymquest.app.data.backup

import com.gymquest.app.core.result.AppError
import com.gymquest.app.core.result.AppResult
import java.io.OutputStream

class BackupFileWriter {
    fun write(json: String, destination: OutputStream): AppResult<Unit> = try {
        destination.write(json.toByteArray(Charsets.UTF_8))
        destination.flush()
        AppResult.Success(Unit)
    } catch (error: Exception) {
        AppResult.Failure(AppError.Storage("No se pudo escribir la exportacion seleccionada.", error))
    }
}
