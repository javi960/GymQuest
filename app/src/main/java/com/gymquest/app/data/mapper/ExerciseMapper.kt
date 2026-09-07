package com.gymquest.app.data.mapper

import com.gymquest.app.data.local.entity.ExerciseBaseEntity
import com.gymquest.app.data.local.entity.ExerciseVariantEntity
import com.gymquest.app.data.local.entity.MuscleGroupEntity
import com.gymquest.app.data.local.relation.ExerciseBaseWithVariants
import com.gymquest.app.domain.model.ExerciseBase
import com.gymquest.app.domain.model.ExerciseCatalogEntry
import com.gymquest.app.domain.model.ExerciseVariant
import com.gymquest.app.domain.model.MuscleGroup

object ExerciseMapper {
    fun toDomain(entity: MuscleGroupEntity) = MuscleGroup(
        id = entity.id,
        name = entity.name,
        description = entity.description,
        sortOrder = entity.sortOrder,
        isArchived = entity.isArchived,
    )

    fun toEntity(model: MuscleGroup) = MuscleGroupEntity(
        id = model.id,
        name = model.name,
        description = model.description,
        sortOrder = model.sortOrder,
        isArchived = model.isArchived,
    )

    fun toDomain(entity: ExerciseBaseEntity) = ExerciseBase(
        id = entity.id,
        name = entity.name,
        primaryMuscleGroupId = entity.primaryMuscleGroupId,
        description = entity.description,
        isBuiltIn = entity.isBuiltIn,
        isArchived = entity.isArchived,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )

    fun toEntity(model: ExerciseBase) = ExerciseBaseEntity(
        id = model.id,
        name = model.name,
        primaryMuscleGroupId = model.primaryMuscleGroupId,
        description = model.description,
        isBuiltIn = model.isBuiltIn,
        isArchived = model.isArchived,
        createdAt = model.createdAt,
        updatedAt = model.updatedAt,
    )

    fun toDomain(entity: ExerciseVariantEntity) = ExerciseVariant(
        id = entity.id,
        exerciseBaseId = entity.exerciseBaseId,
        name = entity.name,
        equipmentType = entity.equipmentType,
        weightComparisonType = entity.weightComparisonType,
        notes = entity.notes,
        isBuiltIn = entity.isBuiltIn,
        isArchived = entity.isArchived,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt,
    )

    fun toEntity(model: ExerciseVariant) = ExerciseVariantEntity(
        id = model.id,
        exerciseBaseId = model.exerciseBaseId,
        name = model.name,
        equipmentType = model.equipmentType,
        weightComparisonType = model.weightComparisonType,
        notes = model.notes,
        isBuiltIn = model.isBuiltIn,
        isArchived = model.isArchived,
        createdAt = model.createdAt,
        updatedAt = model.updatedAt,
    )

    fun toCatalogEntry(relation: ExerciseBaseWithVariants) = ExerciseCatalogEntry(
        exerciseBase = toDomain(relation.exerciseBase),
        variants = relation.variants.map(::toDomain),
    )
}
