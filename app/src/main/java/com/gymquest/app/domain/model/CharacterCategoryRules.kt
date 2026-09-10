package com.gymquest.app.domain.model

/**
 * Progressive character categories. Category 1 covers levels 1..10; each
 * following category needs ten more levels than the previous transition.
 */
object CharacterCategoryRules {
    data class Definition(
        val number: Int,
        val name: String,
        val firstLevel: Int,
        val imageFileName: String,
    )

    fun categoryFor(level: Int): Int {
        require(level >= 1) { "El nivel debe ser al menos uno." }
        var category = 1
        var nextThreshold = FIRST_CATEGORY_LEVEL + FIRST_CATEGORY_SPAN
        var span = FIRST_CATEGORY_SPAN * 2
        while (level >= nextThreshold && category < FINAL_CATEGORY_NUMBER) {
            category++
            nextThreshold += span
            span += FIRST_CATEGORY_SPAN
        }
        return category
    }

    fun firstLevelFor(category: Int): Int {
        require(category >= 1) { "La categoria debe ser al menos uno." }
        val previousTransitions = category - 1L
        return (FIRST_CATEGORY_LEVEL + FIRST_CATEGORY_SPAN * previousTransitions * (previousTransitions + 1) / 2)
            .coerceAtMost(Int.MAX_VALUE.toLong())
            .toInt()
    }

    fun definitionFor(level: Int): Definition = definitionForCategory(categoryFor(level))

    fun definitionForCategory(category: Int): Definition {
        require(category in 1..definitions.size) { "La categoria no existe." }
        return definitions[category - 1]
    }

    val definitions: List<Definition> = listOf(
        "Recluta", "Aspirante", "Iniciado", "Aprendiz", "Cadete", "Combatiente",
        "Soldado", "Soldado veterano", "Luchador", "Luchador disciplinado", "Guerrero",
        "Guerrero veterano", "Guerrero curtido", "Guerrero de hierro", "Guerrero de acero",
        "Campeón", "Campeón veterano", "Duelista", "Duelista experto", "Maestro de armas",
        "Caballero", "Caballero veterano", "Caballero de élite", "Guardián", "Guardián superior",
        "Defensor imperial", "Vanguardia", "Vanguardia de élite", "Comandante", "Comandante veterano",
        "Señor de la batalla", "Señor de la guerra", "Conquistador", "Conquistador veterano",
        "Héroe de batalla", "Héroe legendario", "Parangón", "Parangón de guerra", "Avatar guerrero",
        "Avatar de batalla", "Maestro supremo", "Gran maestro", "Gran maestro de guerra",
        "Titán guerrero", "Titán de batalla", "Ascendido", "Inmortal", "Leyenda viviente",
        "Leyenda de las eras", "Leyenda suprema",
    ).mapIndexed { index, name ->
        Definition(
            number = index + 1,
            name = name,
            firstLevel = firstLevelFor(index + 1),
            imageFileName = imageFileNameFor(name),
        )
    } + Definition(
        number = FINAL_CATEGORY_NUMBER,
        name = "Eterno",
        firstLevel = firstLevelFor(FINAL_CATEGORY_NUMBER),
        imageFileName = "eterno.png",
    )

    private const val FIRST_CATEGORY_LEVEL = 1L
    private const val FIRST_CATEGORY_SPAN = 10L
    private const val FINAL_CATEGORY_NUMBER = 51

    private fun imageFileNameFor(name: String): String = name
        .lowercase()
        .replace("á", "a")
        .replace("é", "e")
        .replace("í", "i")
        .replace("ó", "o")
        .replace("ú", "u")
        .replace("ñ", "n")
        .replace(" ", "_") + ".png"

}
