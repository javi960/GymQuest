package com.gymquest.app.data.external

import com.gymquest.app.domain.model.enums.EquipmentType
import kotlinx.serialization.Serializable

/** Metadata-only import boundary. It deliberately has no remote URL or media field. */
@Serializable
data class ExternalExerciseMetadata(
    val id: String,
    val name: String,
    val muscle: String? = null,
    val bodyPart: String? = null,
    val equipment: String? = null,
    val category: String? = null,
    val instructions: List<String> = emptyList(),
)

data class GymQuestExerciseMetadata(
    val sourceId: String,
    val name: String,
    val primaryMuscleFilter: String,
    val bodyPartFilter: String,
    val equipmentType: EquipmentType,
    val categoryFilter: String,
    val instructions: List<String>,
)

/** Normalizes known external taxonomy values into the finite filters supported by GymQuest. */
object ExternalExerciseMetadataAdapter {
    fun adapt(metadata: ExternalExerciseMetadata): GymQuestExerciseMetadata = GymQuestExerciseMetadata(
        sourceId = metadata.id.trim().also { require(it.isNotEmpty()) { "El identificador externo es obligatorio." } },
        name = metadata.name.trim().also { require(it.isNotEmpty()) { "El nombre externo es obligatorio." } },
        primaryMuscleFilter = muscleFilter(metadata.muscle),
        bodyPartFilter = bodyPartFilter(metadata.bodyPart),
        equipmentType = equipmentType(metadata.equipment),
        categoryFilter = categoryFilter(metadata.category),
        instructions = metadata.instructions.map(String::trim).filter(String::isNotBlank),
    )

    private fun muscleFilter(value: String?): String = when (value.normalized()) {
        "chest", "pectorals" -> "Pecho"
        "back", "lats" -> "Espalda"
        "legs", "quadriceps", "hamstrings", "glutes", "calves" -> "Piernas"
        "shoulders", "delts" -> "Hombros"
        "biceps", "triceps", "forearms", "arms" -> "Brazos"
        "abs", "core", "waist" -> "Core"
        else -> "General"
    }

    private fun bodyPartFilter(value: String?): String = when (value.normalized()) {
        "upper arms" -> "Brazos superiores"
        "lower arms" -> "Antebrazos"
        "upper legs" -> "Piernas superiores"
        "lower legs" -> "Piernas inferiores"
        "arms" -> "Brazos"
        "legs" -> "Piernas"
        "chest" -> "Pecho"
        "back" -> "Espalda"
        "core", "waist" -> "Core"
        "shoulders" -> "Hombros"
        "cardio" -> "Cardio"
        "neck" -> "Cuello"
        else -> "General"
    }

    private fun equipmentType(value: String?): EquipmentType = when (value.normalized()) {
        "barbell", "ez bar", "ez-bar", "ez barbell" -> EquipmentType.BARBELL
        "dumbbell", "dumbbells" -> EquipmentType.DUMBBELL
        "cable" -> EquipmentType.CABLE
        "machine", "lever", "leverage machine", "sled" -> EquipmentType.MACHINE
        "bodyweight", "body weight" -> EquipmentType.BODYWEIGHT
        "band", "resistance band" -> EquipmentType.ELASTIC_BAND
        "kettlebell" -> EquipmentType.KETTLEBELL
        "smith", "smith machine", "multipower" -> EquipmentType.MULTIPOWER
        else -> EquipmentType.OTHER
    }

    private fun categoryFilter(value: String?): String = when (value.normalized()) {
        "strength" -> "Fuerza"
        "stretching" -> "Movilidad"
        "cardio" -> "Cardio"
        "plyometrics" -> "Pliometría"
        else -> "General"
    }

    private fun String?.normalized(): String = this.orEmpty().trim().lowercase()
}
