package com.gymquest.app.data.repository

import com.gymquest.app.core.result.AppError
import com.gymquest.app.core.result.AppResult
import com.gymquest.app.core.validation.TextValidators
import com.gymquest.app.core.validation.ValidationResult
import com.gymquest.app.data.local.dao.MachineDao
import com.gymquest.app.data.mapper.MachineMapper
import com.gymquest.app.domain.model.Gym
import com.gymquest.app.domain.model.GymMachine
import com.gymquest.app.domain.repository.MachineRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MachineRepositoryImpl(
    private val machineDao: MachineDao,
) : MachineRepository {
    override fun observeDefaultGym(): Flow<Gym?> =
        machineDao.observeActiveDefaultGym().map { entity -> entity?.let(MachineMapper::toDomain) }

    override fun observeMachines(gymId: Long): Flow<List<GymMachine>> =
        machineDao.observeActiveMachinesForGym(gymId).map { entities -> entities.map(MachineMapper::toDomain) }

    override suspend fun findMachineById(machineId: Long): AppResult<GymMachine?> =
        storage { machineDao.getMachineById(machineId)?.let(MachineMapper::toDomain) }

    override suspend fun createGym(gym: Gym): AppResult<Long> =
        validateGym(gym, requireExistingId = false) { machineDao.insertGym(MachineMapper.toEntity(gym)) }

    override suspend fun updateGym(gym: Gym): AppResult<Unit> =
        validateGym(gym, requireExistingId = true) {
            if (machineDao.updateGym(MachineMapper.toEntity(gym)) == 0) {
                throw NoSuchElementException("El gimnasio no existe.")
            }
            Unit
        }

    override suspend fun setDefaultGym(gymId: Long, updatedAt: Instant): AppResult<Unit> {
        if (gymId <= 0) return invalid("El identificador del gimnasio debe ser positivo.")
        return storage {
            if (!machineDao.setDefaultGym(gymId, updatedAt)) {
                throw NoSuchElementException("No existe un gimnasio activo con ese identificador.")
            }
        }
    }

    override suspend fun createMachine(machine: GymMachine): AppResult<Long> =
        validateMachine(machine, requireExistingId = false) {
            require(machineDao.isActiveGym(machine.gymId)) {
                "El gimnasio seleccionado no existe o esta archivado."
            }
            machineDao.insertMachine(MachineMapper.toEntity(machine))
        }

    override suspend fun updateMachine(machine: GymMachine): AppResult<Unit> =
        validateMachine(machine, requireExistingId = true) {
            if (machineDao.updateMachine(MachineMapper.toEntity(machine)) == 0) {
                throw NoSuchElementException("La maquina no existe.")
            }
            Unit
        }

    override suspend fun archiveMachine(machineId: Long, updatedAt: Instant): AppResult<Unit> {
        if (machineId <= 0) return invalid("El identificador de la maquina debe ser positivo.")
        return storage {
            if (machineDao.archiveMachine(machineId, updatedAt) == 0) {
                throw NoSuchElementException("No existe una maquina con ese identificador.")
            }
        }
    }

    private suspend fun <T> validateGym(
        gym: Gym,
        requireExistingId: Boolean,
        operation: suspend () -> T,
    ): AppResult<T> = validate(
        validations = listOf(
            TextValidators.required(gym.name, "name"),
            TextValidators.optional(gym.notes, "notes"),
        ),
        checks = listOf(
            (!gym.isDefault) to "Configura el gimnasio predeterminado mediante la accion especifica.",
            (!requireExistingId || gym.id > 0) to "El gimnasio debe existir para actualizarse.",
            (!gym.updatedAt.isBefore(gym.createdAt)) to "La fecha de actualizacion no puede ser anterior a la de creacion.",
        ),
        operation = operation,
    )

    private suspend fun <T> validateMachine(
        machine: GymMachine,
        requireExistingId: Boolean,
        operation: suspend () -> T,
    ): AppResult<T> = validate(
        validations = listOf(
            TextValidators.required(machine.name, "name"),
            TextValidators.optional(machine.brand, "brand"),
            TextValidators.optional(machine.loadType, "loadType"),
            TextValidators.optional(machine.notes, "notes"),
        ),
        checks = listOf(
            (machine.gymId > 0) to "La maquina debe pertenecer a un gimnasio.",
            (!requireExistingId || machine.id > 0) to "La maquina debe existir para actualizarse.",
            (!machine.updatedAt.isBefore(machine.createdAt)) to "La fecha de actualizacion no puede ser anterior a la de creacion.",
        ),
        operation = operation,
    )

    private suspend fun <T> validate(
        validations: List<ValidationResult>,
        checks: List<Pair<Boolean, String>>,
        operation: suspend () -> T,
    ): AppResult<T> {
        val validationError = validations
            .filterIsInstance<ValidationResult.Invalid>()
            .firstOrNull()
            ?.errors
            ?.firstOrNull()
            ?.message
        if (validationError != null) return invalid(validationError)
        val checkError = checks.firstOrNull { !it.first }?.second
        return if (checkError == null) storage(operation) else invalid(checkError)
    }

    private fun <T> invalid(message: String): AppResult<T> = AppResult.Failure(AppError.Validation(message))

    private suspend fun <T> storage(operation: suspend () -> T): AppResult<T> =
        try {
            AppResult.Success(operation())
        } catch (error: NoSuchElementException) {
            AppResult.Failure(AppError.NotFound(error.message ?: "No se encontro el dato solicitado."))
        } catch (error: IllegalArgumentException) {
            AppResult.Failure(AppError.Validation(error.message ?: "Los datos no son validos."))
        } catch (error: Exception) {
            AppResult.Failure(AppError.Storage("No se pudieron guardar los datos del gimnasio.", error))
        }
}
