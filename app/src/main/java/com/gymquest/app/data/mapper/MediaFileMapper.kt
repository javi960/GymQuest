package com.gymquest.app.data.mapper

import com.gymquest.app.data.local.entity.MediaFileEntity
import com.gymquest.app.domain.model.MediaFile
import com.gymquest.app.domain.model.enums.MediaOwnerType
import com.gymquest.app.domain.model.enums.MediaType

/** Stable Room representation. Do not use enum names as persisted data. */
object MediaFileMapper {
    fun toEntity(model: MediaFile) = MediaFileEntity(
        id = model.id,
        ownerType = model.ownerType.storageValue,
        ownerId = model.ownerId,
        mediaType = model.mediaType.storageValue,
        localUri = model.localUri,
        thumbnailUri = model.thumbnailUri,
        title = model.title,
        notes = model.notes,
        createdAt = model.createdAt,
        isArchived = model.isArchived,
    )

    fun toDomain(entity: MediaFileEntity) = MediaFile(
        id = entity.id,
        ownerType = MediaOwnerType.fromStorageValue(entity.ownerType),
        ownerId = entity.ownerId,
        mediaType = MediaType.fromStorageValue(entity.mediaType),
        localUri = entity.localUri,
        thumbnailUri = entity.thumbnailUri,
        title = entity.title,
        notes = entity.notes,
        createdAt = entity.createdAt,
        isArchived = entity.isArchived,
    )
}

val MediaOwnerType.storageValue: String
    get() = name.lowercase()

fun MediaOwnerType.Companion.fromStorageValue(value: String): MediaOwnerType =
    MediaOwnerType.entries.firstOrNull { it.storageValue == value }
        ?: throw IllegalArgumentException("Tipo de propietario multimedia desconocido: $value")

val MediaType.storageValue: String
    get() = name.lowercase()

fun MediaType.Companion.fromStorageValue(value: String): MediaType =
    MediaType.entries.firstOrNull { it.storageValue == value }
        ?: throw IllegalArgumentException("Tipo multimedia desconocido: $value")
