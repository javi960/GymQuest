package com.gymquest.app.core.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gymquest.app.core.ui.theme.QuestTheme

enum class QuestButtonState { Normal, Pressed, Focused, Selected, Loading, Disabled }

/** Stable, testable states for the shared interactive-button contract. */
enum class QuestButtonVisualState { Normal, Pressed, Focused, Selected, Loading, Disabled, Destructive }

object QuestButtonSemantics {
    val visualStateKey = SemanticsPropertyKey<QuestButtonVisualState>("QuestButtonVisualState")
    val actionRoleKey = SemanticsPropertyKey<QuestActionRole>("QuestActionRole")
    val requiresConfirmationKey = SemanticsPropertyKey<Boolean>("QuestButtonRequiresConfirmation")

    fun apply(
        receiver: SemanticsPropertyReceiver,
        visualState: QuestButtonVisualState,
        action: QuestAction,
    ) {
        receiver[visualStateKey] = visualState
        receiver[actionRoleKey] = action.role
        receiver[requiresConfirmationKey] = action.requiresConfirmation
    }
}

private fun QuestAction.visualState(state: QuestButtonState): QuestButtonVisualState = when {
    role == QuestActionRole.Destructive -> QuestButtonVisualState.Destructive
    else -> when (state) {
        QuestButtonState.Normal -> QuestButtonVisualState.Normal
        QuestButtonState.Pressed -> QuestButtonVisualState.Pressed
        QuestButtonState.Focused -> QuestButtonVisualState.Focused
        QuestButtonState.Selected -> QuestButtonVisualState.Selected
        QuestButtonState.Loading -> QuestButtonVisualState.Loading
        QuestButtonState.Disabled -> QuestButtonVisualState.Disabled
    }
}

/**
 * Presentation contract: inside a panel by default; use as FAB only for one
 * screen-level create action and as a bar button only for the primary session action.
 */

@Composable
fun QuestActionButton(
    action: QuestAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = action.localizedLabel(),
    enabled: Boolean = true,
    state: QuestButtonState = QuestButtonState.Normal,
) {
    val tokens = QuestTheme.tokens
    val accessibleDescription = action.localizedContentDescription()
    val visualState = action.visualState(state)
    val colors = when (action.role) {
        QuestActionRole.Primary -> ButtonDefaults.buttonColors(
            containerColor = tokens.colors.blueStructure,
            contentColor = androidx.compose.ui.graphics.Color.White,
        )
        QuestActionRole.Positive -> ButtonDefaults.buttonColors(
            containerColor = tokens.colors.positive,
            contentColor = androidx.compose.ui.graphics.Color.White,
        )
        QuestActionRole.Destructive -> ButtonDefaults.buttonColors(
            containerColor = tokens.colors.error,
            contentColor = androidx.compose.ui.graphics.Color.White,
        )
        QuestActionRole.Secondary -> ButtonDefaults.buttonColors(
            containerColor = tokens.colors.parchment,
            contentColor = tokens.colors.textPrimary,
        )
    }
    Button(
        onClick = onClick,
        enabled = enabled && state != QuestButtonState.Loading && state != QuestButtonState.Disabled,
        shape = RoundedCornerShape(tokens.radii.button),
        colors = colors,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
        modifier = modifier
            .defaultMinSize(minHeight = tokens.sizes.touchTarget)
            .semantics {
                contentDescription = accessibleDescription
                QuestButtonSemantics.apply(this, visualState, action)
            },
    ) {
        if (state == QuestButtonState.Loading) {
            CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White, strokeWidth = 2.dp, modifier = Modifier.defaultMinSize(minWidth = 20.dp, minHeight = 20.dp))
        } else Icon(imageVector = action.icon(), contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = if (state == QuestButtonState.Loading) "Cargando" else label)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestIconButton(
    action: QuestAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val accessibleDescription = action.localizedContentDescription()
    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(action.localizedLabel()) } },
        state = rememberTooltipState(),
    ) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .defaultMinSize(
                minWidth = QuestTheme.tokens.sizes.touchTarget,
                minHeight = QuestTheme.tokens.sizes.touchTarget,
            )
            .semantics {
                contentDescription = accessibleDescription
                QuestButtonSemantics.apply(
                    this,
                    if (enabled) QuestButtonVisualState.Normal else QuestButtonVisualState.Disabled,
                    action,
                )
            },
    ) {
        Icon(
            imageVector = action.icon(),
            contentDescription = null,
        )
    }
    }
}

@Composable
fun QuestButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    action: QuestAction? = null,
    enabled: Boolean = true,
    state: QuestButtonState = QuestButtonState.Normal,
) {
    val effectiveEnabled = enabled && state != QuestButtonState.Loading && state != QuestButtonState.Disabled
    val actionDescription = action?.localizedContentDescription()
    OutlinedButton(
        onClick = onClick,
        enabled = effectiveEnabled,
        shape = RoundedCornerShape(QuestTheme.tokens.radii.button),
        modifier = modifier
            .defaultMinSize(minHeight = QuestTheme.tokens.sizes.touchTarget)
            .semantics {
                action?.let {
                    contentDescription = actionDescription.orEmpty()
                    QuestButtonSemantics.apply(this, it.visualState(state), it)
                }
            },
    ) {
        if (action != null) {
            Icon(imageVector = action.icon(), contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text)
    }
}
