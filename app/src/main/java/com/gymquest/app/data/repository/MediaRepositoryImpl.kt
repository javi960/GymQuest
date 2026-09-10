package com.gymquest.app.data.repository

import com.gymquest.app.core.result.AppError
import com.gymquest.app.core.result.AppResult
import com.gymquest.app.data.local.dao.MediaFileDao
import com.gymquest.app.data.mapper.MediaFileMapper
import com.gymquest.app.data.mapper.storageValue
import com.gymquest.app.domain.model.MediaFile
import com.gymquest.app.domain.model.enums.MediaOwnerType
import com.gymquest.app.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MediaRepositoryImpl(private val dao: MediaFileDao) : MediaRepository {
    override fun observeActiveForOwnerType(ownerType: MediaOwnerType): Flow<List<MediaFile>> =
        dao.observeActiveForOwnerType(ownerType.storageValue).map { entities ->
            entities.map(MediaFileMapper::toDomain)
        }

    override fun observeActiveForOwner(ownerType: MediaOwnerType, ownerId: Long): Flow<List<MediaFile>> =
        dao.observeActiveForOwner(ownerType.storageValue, ownerId).map { entities ->
            entities.map(MediaFileMapper::toDomain)
        }

    override suspend fun replaceForOwner(mediaFile: MediaFile): AppResult<Long> = guarded {
        require(mediaFile.ownerId > 0) { "El propietario multimedia debe existir." }
        require(mediaFile.localUri.startsWith("content://")) { "La guía debe ser un URI local seleccionado por el usuario." }
        dao.replaceForOwner(MediaFileMapper.toEntity(mediaFile))
    }

    override suspend fun removeForOwner(ownerType: MediaOwnerType, ownerId: Long): AppResult<Unit> = guarded {
        require(ownerId > 0) { "El propietario multimedia debe existir." }
        dao.archiveActiveForOwner(ownerType.storageValue, ownerId)
        Unit
    }

    private suspend fun <T> guarded(operation: suspend () -> T): AppResult<T> = try {
        AppResult.Success(operation())
    } catch (error: IllegalArgumentException) {
        AppResult.Failure(AppError.Validation(error.message ?: "Multimedia no válida."))
    } catch (error: Exception) {
        AppResult.Failure(AppError.Storage("No se pudo guardar la multimedia local.", error))
    }
}
