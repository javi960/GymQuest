package com.gymquest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.gymquest.app.data.local.entity.MediaFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaFileDao {
    @Insert
    suspend fun insert(entity: MediaFileEntity): Long

    @Query("SELECT * FROM media_files WHERE ownerType = :ownerType AND ownerId = :ownerId AND isArchived = 0 ORDER BY createdAt DESC")
    fun observeActiveForOwner(ownerType: String, ownerId: Long): Flow<List<MediaFileEntity>>

    @Query("SELECT * FROM media_files WHERE ownerType = :ownerType AND isArchived = 0 ORDER BY ownerId, createdAt DESC")
    fun observeActiveForOwnerType(ownerType: String): Flow<List<MediaFileEntity>>

    @Query("UPDATE media_files SET isArchived = 1 WHERE ownerType = :ownerType AND ownerId = :ownerId AND isArchived = 0")
    suspend fun archiveActiveForOwner(ownerType: String, ownerId: Long): Int

    @Query("SELECT * FROM media_files WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MediaFileEntity?

    @Transaction
    suspend fun replaceForOwner(entity: MediaFileEntity): Long {
        archiveActiveForOwner(entity.ownerType, entity.ownerId)
        return insert(entity)
    }
}
