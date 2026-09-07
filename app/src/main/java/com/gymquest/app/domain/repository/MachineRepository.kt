package com.gymquest.app.domain.repository

import com.gymquest.app.core.result.AppResult
import com.gymquest.app.domain.model.Gym
import com.gymquest.app.domain.model.GymMachine
import java.time.Instant
import kotlinx.coroutines.flow.Flow

interface MachineRepository {
    fun observeDefaultGym(): Flow<Gym?>

    fun observeMachines(gymId: Long): Flow<List<GymMachine>>

    suspend fun findMachineById(machineId: Long): AppResult<GymMachine?>

    suspend fun createGym(gym: Gym): AppResult<Long>

    suspend fun updateGym(gym: Gym): AppResult<Unit>

    suspend fun setDefaultGym(gymId: Long, updatedAt: Instant): AppResult<Unit>

    suspend fun createMachine(machine: GymMachine): AppResult<Long>

    suspend fun updateMachine(machine: GymMachine): AppResult<Unit>

    suspend fun archiveMachine(machineId: Long, updatedAt: Instant): AppResult<Unit>
}
