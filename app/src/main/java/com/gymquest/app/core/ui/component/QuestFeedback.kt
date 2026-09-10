package com.gymquest.app.core.ui.component

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/** Shared transient feedback host. Persistent failures belong next to their relevant control. */
@Composable
fun QuestSnackbar(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = hostState, modifier = modifier)
}

sealed interface QuestDataState<out T> {
    data object Loading : QuestDataState<Nothing>
    data object Empty : QuestDataState<Nothing>
    data object Offline : QuestDataState<Nothing>
    data class Error(val message: String) : QuestDataState<Nothing>
    data class Ready<T>(val data: T) : QuestDataState<T>
}

/** Standardizes loading, empty, error, offline, no-result and ready content. */
@Composable
fun <T> QuestDataStateContent(
    state: QuestDataState<T>,
    onRetry: (() -> Unit)? = null,
    ready: @Composable (T) -> Unit,
    noResults: Boolean = false,
) {
    when (state) {
        QuestDataState.Loading -> EmptyAdventureState("Cargando", "Preparando tus datos locales.")
        QuestDataState.Empty -> EmptyAdventureState("Aún no hay datos", "Crea el primer elemento para continuar.")
        QuestDataState.Offline -> EmptyAdventureState("Sin conexión", "Esta pantalla funciona con datos guardados. Vuelve a intentarlo cuando tengas conexión si la necesitas.")
        is QuestDataState.Error -> EmptyAdventureState("No se pudo cargar", state.message, action = onRetry?.let { retry -> { QuestButton("Reintentar", retry) } })
        is QuestDataState.Ready -> if (noResults) EmptyAdventureState("Sin resultados", "Cambia la búsqueda o los filtros.") else ready(state.data)
    }
}
