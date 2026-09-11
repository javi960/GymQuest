package com.gymquest.app.data.seed

import android.content.Context
import androidx.room.withTransaction
import com.gymquest.app.core.time.ClockProvider
import com.gymquest.app.data.external.ExternalExerciseMetadata
import com.gymquest.app.data.external.ExternalExerciseMetadataAdapter
import com.gymquest.app.data.local.GymQuestDatabase
import com.gymquest.app.data.local.dao.ExerciseDao
import com.gymquest.app.data.local.entity.ExerciseBaseEntity
import com.gymquest.app.data.local.entity.ExerciseVariantEntity
import com.gymquest.app.data.local.entity.MuscleGroupEntity
import com.gymquest.app.domain.model.enums.WeightComparisonType
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val SEED_ASSET = "seed/exercises-dataset-v1.json"
private const val DATASET_SCHEMA_VERSION = 1
private const val SEED_VERSION = 6
private const val PREFERENCES_NAME = "catalog_seed"
private const val VERSION_KEY = "exercise_dataset_version"
private const val EXERCISE_LIBRARY_GIF_PREFIX = "file:///android_asset/exercise_gifs/"

internal data class ExerciseDatasetSeed(
    val schemaVersion: Int,
    val source: String,
    val sourceRevision: String,
    val license: String,
    val exercises: List<ExerciseDatasetRecord>,
)

internal data class ExerciseDatasetRecord(
    val id: String,
    val name: String,
    val name_es: String? = null,
    val category: String,
    val body_part: String,
    val equipment: String,
    val target: String,
    val muscle_group: String,
    val secondary_muscles: List<String> = emptyList(),
    val instruction_steps: Map<String, List<String>> = emptyMap(),
    val gif_url: String? = null,
)

data class CatalogSeedEntry(
    val exerciseName: String,
    val sourceExerciseName: String,
    val muscleGroupName: String,
    val description: String,
    val secondaryMuscles: String?,
    val instructions: String?,
    val builtInGifUrl: String?,
    val variantName: String,
    val equipmentType: com.gymquest.app.domain.model.enums.EquipmentType,
)

internal object ExerciseDatasetSeedMapper {
    fun map(record: ExerciseDatasetRecord): CatalogSeedEntry {
        val normalized = ExternalExerciseMetadataAdapter.adapt(
            ExternalExerciseMetadata(
                id = record.id,
                name = record.name,
                muscle = record.muscle_group,
                bodyPart = record.body_part,
                equipment = record.equipment,
                category = record.category,
                instructions = record.instruction_steps["es"].orEmpty(),
            ),
        )
        val secondary = record.secondary_muscles.joinToString().ifBlank { "No especificados" }
        return CatalogSeedEntry(
            exerciseName = record.name_es?.takeIf(String::isNotBlank) ?: normalized.name,
            sourceExerciseName = record.name,
            muscleGroupName = normalized.bodyPartFilter,
            description = buildString {
                append("Objetivo: ${record.target}. Grupo muscular: ${normalized.primaryMuscleFilter}. ")
                append("Consulta la guía paso a paso para la ejecución.")
            },
            secondaryMuscles = secondary.takeUnless { it == "No especificados" },
            instructions = normalized.instructions.joinToString(separator = "\n").ifBlank { null },
            builtInGifUrl = record.gif_url?.takeIf(::isBundledExerciseLibraryGifUrl),
            variantName = normalized.equipmentType.displayName(),
            equipmentType = normalized.equipmentType,
        )
    }
}

private fun com.gymquest.app.domain.model.enums.EquipmentType.displayName() = when (this) {
    com.gymquest.app.domain.model.enums.EquipmentType.BARBELL -> "Barra"
    com.gymquest.app.domain.model.enums.EquipmentType.DUMBBELL -> "Mancuernas"
    com.gymquest.app.domain.model.enums.EquipmentType.MACHINE -> "Máquina"
    com.gymquest.app.domain.model.enums.EquipmentType.CABLE -> "Polea"
    com.gymquest.app.domain.model.enums.EquipmentType.BODYWEIGHT -> "Peso corporal"
    com.gymquest.app.domain.model.enums.EquipmentType.MULTIPOWER -> "Multipower"
    com.gymquest.app.domain.model.enums.EquipmentType.KETTLEBELL -> "Kettlebell"
    com.gymquest.app.domain.model.enums.EquipmentType.ELASTIC_BAND -> "Banda elástica"
    com.gymquest.app.domain.model.enums.EquipmentType.OTHER -> "Otro"
}

private fun JsonObject.toSeed(): ExerciseDatasetSeed = ExerciseDatasetSeed(
    schemaVersion = string("schemaVersion").toInt(),
    source = string("source"),
    sourceRevision = string("sourceRevision"),
    license = string("license"),
    exercises = getValue("exercises").jsonArray.map { it.jsonObject.toRecord() },
)

private fun JsonObject.toRecord(): ExerciseDatasetRecord = ExerciseDatasetRecord(
    id = string("id"),
    name = string("name"),
    name_es = get("name_es")?.jsonPrimitive?.content,
    category = string("category"),
    body_part = string("body_part"),
    equipment = string("equipment"),
    target = string("target"),
    muscle_group = string("muscle_group"),
    secondary_muscles = getValue("secondary_muscles").jsonArray.strings(),
    instruction_steps = getValue("instruction_steps").jsonObject.mapValues { (_, value) ->
        value.jsonArray.strings()
    },
    gif_url = get("gif_url")?.jsonPrimitive?.content,
)

private fun JsonObject.string(name: String): String = getValue(name).jsonPrimitive.content

private fun JsonArray.strings(): List<String> = map { it.jsonPrimitive.content }

private fun isBundledExerciseLibraryGifUrl(value: String): Boolean =
    value.startsWith(EXERCISE_LIBRARY_GIF_PREFIX) &&
        value.endsWith(".gif")

/** Imports only MIT metadata/instructions. The source media is intentionally absent from the asset. */
class ExerciseCatalogSeedRepository(
    private val context: Context,
    private val database: GymQuestDatabase,
    private val clock: ClockProvider,
) {
    suspend fun seedIfNeeded() = withContext(Dispatchers.IO) {
        val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
        if (preferences.getInt(VERSION_KEY, 0) >= SEED_VERSION) return@withContext
        val seed = Json.parseToJsonElement(
            context.assets.open(SEED_ASSET).bufferedReader().use { it.readText() },
        ).jsonObject.toSeed()
        require(seed.schemaVersion == DATASET_SCHEMA_VERSION) { "Versión de dataset de ejercicios no compatible." }
        val entries = seed.exercises.map(ExerciseDatasetSeedMapper::map)
        database.withTransaction {
            database.exerciseDao().seedBuiltInCatalog(entries, clock.now())
        }
        check(preferences.edit().putInt(VERSION_KEY, SEED_VERSION).commit()) {
            "No se pudo registrar la versión del catálogo importado."
        }
    }
}
