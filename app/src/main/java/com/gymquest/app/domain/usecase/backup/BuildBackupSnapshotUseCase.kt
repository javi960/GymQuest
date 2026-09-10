package com.gymquest.app.domain.usecase.backup

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.BackupSnapshot
import com.gymquest.app.domain.repository.BackupRepository

class BuildBackupSnapshotUseCase(private val repository: BackupRepository) {
    suspend operator fun invoke(): AppResult<BackupSnapshot> = repository.buildSnapshot()
}
