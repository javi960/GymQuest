package com.gymquest.app.domain.model

import java.time.Instant

data class BackupMetadata(
    val schemaVersion: Int,
    val databaseSchemaVersion: Int,
    val exportedAt: Instant,
    val format: String = "gymquest-backup",
)
