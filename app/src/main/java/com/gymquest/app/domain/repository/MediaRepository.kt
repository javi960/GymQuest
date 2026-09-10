package com.gymquest.app.domain.repository

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.MediaFile
import com.gymquest.app.domain.model.enums.MediaOwnerType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface MediaRepository {
    fun observeActiveForOwnerType(ownerType: MediaOwnerType): Flow<List<MediaFile>>
    /**
     * Resolves the current local asset for one owner without exposing files from
     * another record. Implementations can override this to query more directly.
     */
    fun observeActiveForOwner(ownerType: MediaOwnerType, ownerId: Long): Flow<List<MediaFile>> =
        observeActiveForOwnerType(ownerType).map { files -> files.filter { it.ownerId == ownerId } }
    suspend fun replaceForOwner(mediaFile: MediaFile): AppResult<Long>
    suspend fun removeForOwner(ownerType: MediaOwnerType, ownerId: Long): AppResult<Unit>
}
