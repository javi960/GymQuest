package com.gymquest.app.feature.catalog

import com.gymquest.app.domain.model.ExerciseCatalogEntry

data class CatalogEntryPresentation(
    val label: String,
    val detail: String,
)

fun ExerciseCatalogEntry.presentation(): CatalogEntryPresentation =
    if (variants.isEmpty()) {
        CatalogEntryPresentation(
            label = "Requiere variante",
            detail = "Añade una variante con equipamiento para completar la ficha del ejercicio.",
        )
    } else {
        CatalogEntryPresentation(
            label = "Listo para entrenar",
            detail = "${variants.size} variante${if (variants.size == 1) " disponible" else "s disponibles"}",
        )
    }
