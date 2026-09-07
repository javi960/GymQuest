package com.gymquest.app.data.repository

import com.gymquest.app.core.result.AppError
import com.gymquest.app.core.result.AppResult
import com.gymquest.app.core.validation.TextValidators
import com.gymquest.app.core.validation.ValidationResult
import com.gymquest.app.data.local.dao.ExerciseDao
import com.gymquest.app.data.mapper.ExerciseMapper
import com.gymquest.app.domain.model.ExerciseBase
import com.gymquest.app.domain.model.ExerciseCatalogEntry
import com.gymquest.app.domain.model.ExerciseVariant
import com.gymquest.app.domain.model.MuscleGroup
import com.gymquest.app.domain.repository.ExerciseRepository
import java.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

class ExerciseRepositoryImpl(
    private val exerciseDao: ExerciseDao,
) : ExerciseRepository {
    override fun observeMuscleGroups(): Flow<List<MuscleGroup>> =
        exerciseDao.observeActiveMuscleGroups().map { entities ->
            entities.map(ExerciseMapper::toDomain)
        }

    override fun observeActiveExerciseVariants(): Flow<List<ExerciseVariant>> =
        exerciseDao.observeActiveExerciseVariants().map { entities ->
            entities.map(ExerciseMapper::toDomain)
        }

    override fun observeActiveExerciseCatalog(): Flow<List<ExerciseCatalogEntry>> =
        combine(
            exerciseDao.observeActiveExerciseBases(),
            exerciseDao.observeActiveExerciseVariants(),
        ) { baseEntities, variantEntities ->
            val variantsByBaseId = variantEntities.groupBy { it.exerciseBaseId }
            baseEntities.map { baseEntity ->
                ExerciseCatalogEntry(
                    exerciseBase = ExerciseMapper.toDomain(baseEntity),
                    variants = variantsByBaseId[baseEntity.id].orEmpty().map(ExerciseMapper::toDomain),
                )
            }
        }

    override fun observeVariantsForExerciseBase(exerciseBaseId: Long): Flow<List<ExerciseVariant>> =
        exerciseDao.observeVariantsForActiveExerciseBase(exerciseBaseId).map { entities ->
            entities.map(ExerciseMapper::toDomain)
        }

    override suspend fun findExerciseVariantById(variantId: Long): AppResult<ExerciseVariant?> =
        runStorageOperation { exerciseDao.getExerciseVariant(variantId)?.let(ExerciseMapper::toDomain) }

    override suspend fun createMuscleGroup(muscleGroup: MuscleGroup): AppResult<Long> =
        validateMuscleGroup(muscleGroup) {
            exerciseDao.insertMuscleGroup(ExerciseMapper.toEntity(muscleGroup))
        }

    override suspend fun createExerciseBase(exerciseBase: ExerciseBase): AppResult<Long> =
        validateExerciseBase(exerciseBase, requireExistingId = false) {
            require(exerciseDao.isActiveMuscleGroup(exerciseBase.primaryMuscleGroupId)) {
                "El grupo muscular seleccionado no existe o esta archivado."
            }
            exerciseDao.insertExerciseBase(ExerciseMapper.toEntity(exerciseBase))
        }

    override suspend fun updateExerciseBase(exerciseBase: ExerciseBase): AppResult<Unit> =
        validateExerciseBase(exerciseBase, requireExistingId = true) {
            if (exerciseDao.updateExerciseBase(ExerciseMapper.toEntity(exerciseBase)) == 0) {
                throw NoSuchElementException("El ejercicio no existe.")
            }
            Unit
        }

    override suspend fun archiveExerciseBase(baseId: Long, updatedAt: Instant): AppResult<Unit> =
        validateId(baseId, "baseId") {
            if (exerciseDao.archiveExerciseBase(baseId, updatedAt) == 0) {
                throw NoSuchElementException("El ejercicio no existe.")
            }
            Unit
        }

    override suspend fun createExerciseVariant(exerciseVariant: ExerciseVariant): AppResult<Long> =
        validateExerciseVariant(exerciseVariant, requireExistingId = false) {
            require(exerciseDao.isActiveExerciseBase(exerciseVariant.exerciseBaseId)) {
                "El ejercicio seleccionado no existe o esta archivado."
            }
            exerciseDao.insertExerciseVariant(ExerciseMapper.toEntity(exerciseVariant))
        }

    override suspend fun updateExerciseVariant(exerciseVariant: ExerciseVariant): AppResult<Unit> =
        validateExerciseVariant(exerciseVariant, requireExistingId = true) {
            if (exerciseDao.updateExerciseVariant(ExerciseMapper.toEntity(exerciseVariant)) == 0) {
                throw NoSuchElementException("La variante no existe.")
            }
            Unit
        }

    override suspend fun archiveExerciseVariant(variantId: Long, updatedAt: Instant): AppResult<Unit> =
        validateId(variantId, "variantId") {
            if (exerciseDao.archiveExerciseVariant(variantId, updatedAt) == 0) {
                throw NoSuchElementException("La variante no existe.")
            }
            Unit
        }

    private suspend fun validateMuscleGroup(
        muscleGroup: MuscleGroup,
        operation: suspend () -> Long,
    ): AppResult<Long> =
        validate(
            TextValidators.required(muscleGroup.name, "name"),
            (muscleGroup.sortOrder >= 0) to "El orden del grupo muscular no puede ser negativo.",
            operation = operation,
        )

    private suspend fun <T> validateExerciseBase(
        exerciseBase: ExerciseBase,
        requireExistingId: Boolean,
        operation: suspend () -> T,
    ): AppResult<T> =
        validate(
            TextValidators.required(exerciseBase.name, "name"),
            TextValidators.optional(exerciseBase.description, "description"),
            (exerciseBase.primaryMuscleGroupId > 0) to "El ejercicio debe tener un grupo muscular principal.",
            (!requireExistingId || exerciseBase.id > 0) to "El ejercicio debe existir para actualizarse.",
            (!exerciseBase.updatedAt.isBefore(exerciseBase.createdAt)) to "La fecha de actualizacion no puede ser anterior a la de creacion.",
            operation = operation,
        )

    private suspend fun <T> validateExerciseVariant(
        exerciseVariant: ExerciseVariant,
        requireExistingId: Boolean,
        operation: suspend () -> T,
    ): AppResult<T> =
        validate(
            TextValidators.required(exerciseVariant.name, "name"),
            TextValidators.optional(exerciseVariant.notes, "notes"),
            (exerciseVariant.exerciseBaseId > 0) to "La variante debe pertenecer a un ejercicio.",
            (!requireExistingId || exerciseVariant.id > 0) to "La variante debe existir para actualizarse.",
            (!exerciseVariant.updatedAt.isBefore(exerciseVariant.createdAt)) to "La fecha de actualizacion no puede ser anterior a la de creacion.",
            operation = operation,
        )

    private suspend fun <T> validateId(
        id: Long,
        field: String,
        operation: suspend () -> T,
    ): AppResult<T> =
        validate((id > 0) to "El identificador $field debe ser positivo.", operation = operation)

    private suspend fun <T> validate(
        vararg checks: Pair<Boolean, String>,
        operation: suspend () -> T,
    ): AppResult<T> {
        val textValidation = checks.firstOrNull { !it.first }?.second
        if (textValidation != null) return AppResult.Failure(AppError.Validation(textValidation))
        return runStorageOperation(operation)
    }

    private suspend fun <T> validate(
        nameValidation: ValidationResult,
        vararg checks: Pair<Boolean, String>,
        operation: suspend () -> T,
    ): AppResult<T> {
        if (nameValidation is ValidationResult.Invalid) {
            return AppResult.Failure(AppError.Validation(nameValidation.errors.first().message))
        }
        return validate(*checks, operation = operation)
    }

    private suspend fun <T> validate(
        nameValidation: ValidationResult,
        notesValidation: ValidationResult,
        vararg checks: Pair<Boolean, String>,
        operation: suspend () -> T,
    ): AppResult<T> {
        if (nameValidation is ValidationResult.Invalid) {
            return AppResult.Failure(AppError.Validation(nameValidation.errors.first().message))
        }
        if (notesValidation is ValidationResult.Invalid) {
            return AppResult.Failure(AppError.Validation(notesValidation.errors.first().message))
        }
        return validate(*checks, operation = operation)
    }

    private suspend fun <T> runStorageOperation(operation: suspend () -> T): AppResult<T> =
        try {
            AppResult.Success(operation())
        } catch (error: NoSuchElementException) {
            AppResult.Failure(AppError.NotFound(error.message ?: "No se encontro el dato solicitado."))
        } catch (error: IllegalArgumentException) {
            AppResult.Failure(AppError.Validation(error.message ?: "Los datos no son validos."))
        } catch (error: Exception) {
            AppResult.Failure(AppError.Storage("No se pudieron guardar los datos del catalogo.", error))
        }
}
