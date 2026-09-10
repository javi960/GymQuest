package com.gymquest.app.domain.usecase.backup

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.repository.BackupRepository

class ValidateBackupSchemaUseCase(private val repository: BackupRepository) {
    operator fun invoke(json: String): AppResult<Unit> = repository.validate(json)
}
