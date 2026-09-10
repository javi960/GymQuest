package com.gymquest.app.domain.repository

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.BackupSnapshot
import java.io.OutputStream

interface BackupRepository {
    suspend fun buildSnapshot(): AppResult<BackupSnapshot>
    fun serialize(snapshot: BackupSnapshot): AppResult<String>
    fun validate(json: String): AppResult<Unit>
    fun write(json: String, destination: OutputStream): AppResult<Unit>
}
