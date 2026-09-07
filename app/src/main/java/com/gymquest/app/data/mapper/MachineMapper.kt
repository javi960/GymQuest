package com.gymquest.app.data.mapper

import com.gymquest.app.data.local.entity.GymEntity
import com.gymquest.app.data.local.entity.GymMachineEntity
import com.gymquest.app.domain.model.Gym
import com.gymquest.app.domain.model.GymMachine

object MachineMapper {
    fun toDomain(entity: GymEntity) = Gym(
        id = entity.id,
        name = entity.name,
        notes = entity.notes,
        isDefault = entity.isDefault,
        isArchived = entity.isArchived,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )

    fun toEntity(model: Gym) = GymEntity(
        id = model.id,
        name = model.name,
        notes = model.notes,
        isDefault = model.isDefault,
        isArchived = model.isArchived,
        createdAt = model.createdAt,
        updatedAt = model.updatedAt,
    )

    fun toDomain(entity: GymMachineEntity) = GymMachine(
        id = entity.id,
        gymId = entity.gymId,
        exerciseVariantId = entity.exerciseVariantId,
        name = entity.name,
        brand = entity.brand,
        loadType = entity.loadType,
        weightComparisonType = entity.weightComparisonType,
        notes = entity.notes,
        isArchived = entity.isArchived,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )

    fun toEntity(model: GymMachine) = GymMachineEntity(
        id = model.id,
        gymId = model.gymId,
        exerciseVariantId = model.exerciseVariantId,
        name = model.name,
        brand = model.brand,
        loadType = model.loadType,
        weightComparisonType = model.weightComparisonType,
        notes = model.notes,
        isArchived = model.isArchived,
        createdAt = model.createdAt,
        updatedAt = model.updatedAt,
    )
}
