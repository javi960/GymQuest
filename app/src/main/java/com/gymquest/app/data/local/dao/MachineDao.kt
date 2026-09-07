package com.gymquest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.gymquest.app.data.local.entity.GymEntity
import com.gymquest.app.data.local.entity.GymMachineEntity
import java.time.Instant
import kotlinx.coroutines.flow.Flow

@Dao
interface MachineDao {
    @Insert
    suspend fun insertGym(entity: GymEntity): Long

    @Update
    suspend fun updateGym(entity: GymEntity): Int

    @Insert
    suspend fun insertMachine(entity: GymMachineEntity): Long

    @Update
    suspend fun updateMachine(entity: GymMachineEntity): Int

    @Query(
        "SELECT * FROM gyms WHERE isDefault = 1 AND isArchived = 0 ORDER BY id LIMIT 1",
    )
    fun observeActiveDefaultGym(): Flow<GymEntity?>

    @Query(
        """
        SELECT gym_machines.*
        FROM gym_machines
        INNER JOIN gyms ON gyms.id = gym_machines.gymId
        WHERE gym_machines.gymId = :gymId
            AND gym_machines.isArchived = 0
            AND gyms.isArchived = 0
        ORDER BY gym_machines.name, gym_machines.id
        """,
    )
    fun observeActiveMachinesForGym(gymId: Long): Flow<List<GymMachineEntity>>

    @Query("SELECT * FROM gym_machines WHERE id = :machineId LIMIT 1")
    suspend fun getMachineById(machineId: Long): GymMachineEntity?

    @Query(
        "UPDATE gym_machines SET isArchived = 1, updatedAt = :updatedAt WHERE id = :machineId",
    )
    suspend fun archiveMachine(machineId: Long, updatedAt: Instant): Int

    @Query("SELECT EXISTS(SELECT 1 FROM gyms WHERE id = :gymId AND isArchived = 0)")
    suspend fun isActiveGym(gymId: Long): Boolean

    @Query("UPDATE gyms SET isDefault = 0, updatedAt = :updatedAt WHERE isDefault = 1")
    suspend fun clearDefaultGym(updatedAt: Instant): Int

    @Query(
        "UPDATE gyms SET isDefault = 1, updatedAt = :updatedAt WHERE id = :gymId AND isArchived = 0",
    )
    suspend fun markGymAsDefault(gymId: Long, updatedAt: Instant): Int

    /**
     * Atomically switches the default gym. An archived or absent target leaves the current
     * default unchanged, so a failed request cannot leave the app without an active default.
     */
    @Transaction
    suspend fun setDefaultGym(gymId: Long, updatedAt: Instant): Boolean {
        if (!isActiveGym(gymId)) return false

        clearDefaultGym(updatedAt)
        return markGymAsDefault(gymId, updatedAt) == 1
    }
}
