package com.gymquest.app.core.ui.component

import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import com.gymquest.app.core.ui.theme.QuestTheme

data class QuestNavigationDestination(
    val route: String,
    val label: String,
    val action: QuestAction,
)

val questPrimaryDestinations = listOf(
    QuestNavigationDestination("home", "Inicio", QuestAction.Home),
    QuestNavigationDestination("martial_arts", "Dojo", QuestAction.Dojo),
    QuestNavigationDestination("catalog", "Catálogo", QuestAction.Catalog),
    QuestNavigationDestination("routines", "Rutinas", QuestAction.Routine),
    QuestNavigationDestination("sessions", "Sesiones", QuestAction.Session),
)

const val MAX_BOTTOM_DESTINATIONS = 5

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    contextualAction: QuestAction? = null,
    onContextualAction: (() -> Unit)? = null,
) {
    val colors = QuestTheme.tokens.colors
    CenterAlignedTopAppBar(
        title = { Text(title) },
        navigationIcon = {
            if (onNavigateBack != null) {
                QuestIconButton(action = QuestAction.Back, onClick = onNavigateBack)
            }
        },
        actions = {
            if (contextualAction != null && onContextualAction != null) {
                QuestIconButton(action = contextualAction, onClick = onContextualAction)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = colors.panel,
            titleContentColor = colors.textPrimary,
            navigationIconContentColor = colors.blueStructure,
            actionIconContentColor = colors.blueStructure,
        ),
        modifier = modifier,
    )
}

@Composable
fun QuestBottomNavigation(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    destinations: List<QuestNavigationDestination> = questPrimaryDestinations,
    showLabels: Boolean = LocalConfiguration.current.fontScale <= MAXIMUM_LABEL_FONT_SCALE,
) {
    require(destinations.size <= MAX_BOTTOM_DESTINATIONS) { "La navegación inferior admite como máximo $MAX_BOTTOM_DESTINATIONS destinos." }
    val colors = QuestTheme.tokens.colors
    NavigationBar(
        containerColor = colors.panel,
        contentColor = colors.textPrimary,
        modifier = modifier,
    ) {
        destinations.forEach { destination ->
            val accessibleDescription = destination.action.localizedContentDescription()
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onNavigate(destination.route) },
                modifier = Modifier.semantics {
                    contentDescription = accessibleDescription
                    stateDescription = if (currentRoute == destination.route) "Destino actual" else "Ir a destino"
                },
                icon = {
                    androidx.compose.material3.Icon(
                        imageVector = destination.action.icon(),
                        contentDescription = null,
                    )
                },
                label = if (showLabels) ({ Text(destination.action.localizedLabel()) }) else null,
                alwaysShowLabel = showLabels,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.blueStructure,
                    selectedTextColor = colors.blueStructure,
                    indicatorColor = colors.parchment,
                    unselectedIconColor = colors.textSecondary,
                    unselectedTextColor = colors.textSecondary,
                ),
            )
        }
    }
}

private const val MAXIMUM_LABEL_FONT_SCALE = 1.3f
