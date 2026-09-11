package com.gymquest.app.core.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FileDownload
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
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.gymquest.app.R

enum class QuestActionRole {
    Primary,
    Secondary,
    Positive,
    Destructive,
}

enum class QuestAction(
    val label: String,
    val contentDescription: String,
    @get:StringRes val labelRes: Int,
    @get:StringRes val contentDescriptionRes: Int,
    val role: QuestActionRole,
    val requiresConfirmation: Boolean = false,
) {
    Add("Añadir", "Añadir elemento", R.string.action_add, R.string.action_add_description, QuestActionRole.Primary),
    Edit("Editar", "Editar elemento", R.string.action_edit, R.string.action_edit_description, QuestActionRole.Secondary),
    Save("Guardar", "Guardar cambios", R.string.action_save, R.string.action_save_description, QuestActionRole.Positive),
    Export("Exportar", "Exportar copia de seguridad", R.string.action_export, R.string.action_export_description, QuestActionRole.Positive, requiresConfirmation = true),
    Cancel("Cancelar", "Cancelar acción", R.string.action_cancel, R.string.action_cancel_description, QuestActionRole.Secondary),
    Discard("Descartar", "Cancelar sesión y descartar progreso", R.string.action_discard, R.string.action_discard_description, QuestActionRole.Destructive, requiresConfirmation = true),
    Delete("Eliminar", "Eliminar elemento", R.string.action_delete, R.string.action_delete_description, QuestActionRole.Destructive, requiresConfirmation = true),
    Back("Volver", "Volver atrás", R.string.action_back, R.string.action_back_description, QuestActionRole.Secondary),
    Search("Buscar", "Buscar", R.string.action_search, R.string.action_search_description, QuestActionRole.Secondary),
    Filter("Filtrar", "Filtrar resultados", R.string.action_filter, R.string.action_filter_description, QuestActionRole.Secondary),
    Sort("Ordenar", "Ordenar resultados", R.string.action_sort, R.string.action_sort_description, QuestActionRole.Secondary),
    OpenDetail("Ver detalle", "Abrir detalle", R.string.action_open_detail, R.string.action_open_detail_description, QuestActionRole.Secondary),
    More("Más opciones", "Mostrar más opciones", R.string.action_more, R.string.action_more_description, QuestActionRole.Secondary),
    Start("Iniciar", "Iniciar sesión", R.string.action_start, R.string.action_start_description, QuestActionRole.Primary),
    Pause("Pausar", "Pausar", R.string.action_pause, R.string.action_pause_description, QuestActionRole.Secondary),
    Resume("Reanudar", "Reanudar", R.string.action_resume, R.string.action_resume_description, QuestActionRole.Primary),
    Finish("Completar", "Completar sesión", R.string.action_finish, R.string.action_finish_description, QuestActionRole.Positive),
    Home("Inicio", "Ir a inicio", R.string.action_home, R.string.action_home_description, QuestActionRole.Secondary),
    Catalog("Catálogo", "Ir a catálogo", R.string.action_catalog, R.string.action_catalog_description, QuestActionRole.Secondary),
    Routine("Rutinas", "Ir a rutinas", R.string.action_routine, R.string.action_routine_description, QuestActionRole.Secondary),
    Session("Sesiones", "Ir a sesiones", R.string.action_session, R.string.action_session_description, QuestActionRole.Secondary),
    Dojo("Dojo", "Ir al dojo", R.string.action_dojo, R.string.action_dojo_description, QuestActionRole.Secondary),
    Settings("Ajustes", "Ir a ajustes", R.string.action_settings, R.string.action_settings_description, QuestActionRole.Secondary),
    MartialArts("Artes marciales", "Ir a artes marciales", R.string.action_martial_arts, R.string.action_martial_arts_description, QuestActionRole.Secondary),
}

@Composable
fun QuestAction.localizedLabel(): String = stringResource(labelRes)

@Composable
fun QuestAction.localizedContentDescription(): String = stringResource(contentDescriptionRes)

fun QuestAction.icon(): ImageVector =
    when (this) {
        QuestAction.Add -> Icons.Filled.Add
        QuestAction.Edit -> Icons.Filled.Edit
        QuestAction.Save -> Icons.Filled.Save
        QuestAction.Export -> Icons.Filled.FileDownload
        QuestAction.Cancel -> Icons.Filled.Close
        QuestAction.Discard -> Icons.Filled.Close
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
        QuestAction.Catalog -> Icons.AutoMirrored.Filled.MenuBook
        QuestAction.Routine -> Icons.Filled.Assignment
        QuestAction.Session -> Icons.Filled.FitnessCenter
        QuestAction.Dojo -> Icons.Filled.SportsMartialArts
        QuestAction.Settings -> Icons.Filled.Settings
        QuestAction.MartialArts -> Icons.Filled.SportsMartialArts
    }

enum class QuestSymbol(val icon: ImageVector, val contentDescription: String?) {
    Strength(Icons.Filled.FitnessCenter, "Fuerza"),
    Technique(Icons.Filled.SportsMartialArts, "Técnica"),
    Energy(Icons.Filled.Bolt, "Energía"),
    Xp(Icons.Filled.Star, "XP"),
    Mission(Icons.Filled.Flag, "Mision"),
    Character(Icons.Filled.EmojiEvents, "Personaje"),
    Achievement(Icons.Filled.EmojiEvents, "Logro"),
    Catalog(Icons.AutoMirrored.Filled.MenuBook, "Catálogo"),
    Discovery(Icons.AutoMirrored.Filled.TrendingUp, "Descubrimiento"),
    SavedSet(Icons.Filled.CheckCircle, "Serie guardada"),
    Rest(Icons.Filled.Timer, "Descanso"),
}
