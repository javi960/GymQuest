package com.gymquest.app.domain.usecase.backup

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.repository.BackupRepository
import java.io.OutputStream

/** Call only from an explicit user export action after the system picker provides [destination]. */
class ExportBackupJsonUseCase(private val repository: BackupRepository) {
    suspend operator fun invoke(destination: OutputStream): AppResult<Unit> =
        when (val snapshot = repository.buildSnapshot()) {
            is AppResult.Failure -> snapshot
            is AppResult.Success -> when (val json = repository.serialize(snapshot.value)) {
                is AppResult.Failure -> json
                is AppResult.Success -> when (val validation = repository.validate(json.value)) {
                    is AppResult.Failure -> validation
                    is AppResult.Success -> repository.write(json.value, destination)
                }
            }
        }
}
