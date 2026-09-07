package com.gymquest.app.core.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsMartialArts
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector

enum class QuestActionRole {
    Primary,
    Secondary,
    Positive,
    Destructive,
}

enum class QuestAction(
    val label: String,
    val contentDescription: String,
    val role: QuestActionRole,
    val requiresConfirmation: Boolean = false,
) {
    Add("Anadir", "Anadir elemento", QuestActionRole.Primary),
    Edit("Editar", "Editar elemento", QuestActionRole.Secondary),
    Save("Guardar", "Guardar cambios", QuestActionRole.Positive),
    Cancel("Cancelar", "Cancelar accion", QuestActionRole.Secondary),
    Delete("Eliminar", "Eliminar elemento", QuestActionRole.Destructive, requiresConfirmation = true),
    Back("Volver", "Volver atras", QuestActionRole.Secondary),
    Search("Buscar", "Buscar", QuestActionRole.Secondary),
    Filter("Filtrar", "Filtrar resultados", QuestActionRole.Secondary),
    Sort("Ordenar", "Ordenar resultados", QuestActionRole.Secondary),
    OpenDetail("Ver detalle", "Abrir detalle", QuestActionRole.Secondary),
    More("Mas opciones", "Mostrar mas opciones", QuestActionRole.Secondary),
    Start("Iniciar", "Iniciar sesion", QuestActionRole.Primary),
    Pause("Pausar", "Pausar", QuestActionRole.Secondary),
    Resume("Reanudar", "Reanudar", QuestActionRole.Primary),
    Finish("Completar", "Completar sesion", QuestActionRole.Positive),
    Home("Inicio", "Ir a inicio", QuestActionRole.Secondary),
    Session("Sesion", "Ir a sesion", QuestActionRole.Primary),
    Catalog("Catalogo", "Ir a catalogo", QuestActionRole.Secondary),
    History("Historial", "Ir a historial", QuestActionRole.Secondary),
    Progress("Progreso", "Ir a progreso", QuestActionRole.Secondary),
    Settings("Ajustes", "Ir a ajustes", QuestActionRole.Secondary),
    MartialArts("Artes marciales", "Ir a artes marciales", QuestActionRole.Secondary),
}

fun QuestAction.icon(): ImageVector =
    when (this) {
        QuestAction.Add -> Icons.Filled.Add
        QuestAction.Edit -> Icons.Filled.Edit
        QuestAction.Save -> Icons.Filled.Save
        QuestAction.Cancel -> Icons.Filled.Close
        QuestAction.Delete -> Icons.Filled.Delete
        QuestAction.Back -> Icons.AutoMirrored.Filled.ArrowBack
        QuestAction.Search -> Icons.Filled.Search
        QuestAction.Filter -> Icons.Filled.FilterList
        QuestAction.Sort -> Icons.AutoMirrored.Filled.Sort
        QuestAction.OpenDetail -> Icons.AutoMirrored.Filled.OpenInNew
        QuestAction.More -> Icons.Filled.MoreVert
        QuestAction.Start -> Icons.Filled.PlayArrow
        QuestAction.Pause -> Icons.Filled.Pause
        QuestAction.Resume -> Icons.Filled.Replay
        QuestAction.Finish -> Icons.Filled.Check
        QuestAction.Home -> Icons.Filled.Home
        QuestAction.Session -> Icons.Filled.FitnessCenter
        QuestAction.Catalog -> Icons.AutoMirrored.Filled.MenuBook
        QuestAction.History -> Icons.Filled.History
        QuestAction.Progress -> Icons.AutoMirrored.Filled.TrendingUp
        QuestAction.Settings -> Icons.Filled.Settings
        QuestAction.MartialArts -> Icons.Filled.SportsMartialArts
    }

enum class QuestSymbol(val icon: ImageVector, val contentDescription: String?) {
    Xp(Icons.Filled.Star, "XP"),
    Mission(Icons.Filled.Flag, "Mision"),
    Achievement(Icons.Filled.EmojiEvents, "Logro"),
    Rest(Icons.Filled.Timer, "Descanso"),
}
