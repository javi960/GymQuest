package com.gymquest.app.domain.model

/** Portable, lossless representation of the rows stored by Room at export time. */
data class BackupSnapshot(
    val metadata: BackupMetadata,
    val tables: Map<String, List<Map<String, BackupValue>>>,
)

sealed interface BackupValue {
    data class IntegerValue(val value: Long) : BackupValue
    data class RealValue(val value: Double) : BackupValue
    data class TextValue(val value: String) : BackupValue
    data class BlobValue(val value: ByteArray) : BackupValue {
        override fun equals(other: Any?): Boolean = other is BlobValue && value.contentEquals(other.value)
        override fun hashCode(): Int = value.contentHashCode()
    }
    data object NullValue : BackupValue
}
