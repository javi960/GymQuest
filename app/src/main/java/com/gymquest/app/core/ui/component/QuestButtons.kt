package com.gymquest.app.core.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.gymquest.app.core.ui.theme.QuestTheme

@Composable
fun QuestActionButton(
    action: QuestAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = action.label,
    enabled: Boolean = true,
) {
    val tokens = QuestTheme.tokens
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
        enabled = enabled,
        shape = RoundedCornerShape(tokens.radii.button),
        colors = colors,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        modifier = modifier
            .defaultMinSize(minHeight = tokens.sizes.touchTarget)
            .semantics { contentDescription = action.contentDescription },
    ) {
        Icon(
            imageVector = action.icon(),
            contentDescription = null,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label)
    }
}

@Composable
fun QuestIconButton(
    action: QuestAction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .defaultMinSize(
                minWidth = QuestTheme.tokens.sizes.touchTarget,
                minHeight = QuestTheme.tokens.sizes.touchTarget,
            )
            .semantics { contentDescription = action.contentDescription },
    ) {
        Icon(
            imageVector = action.icon(),
            contentDescription = null,
        )
    }
}

@Composable
fun QuestButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    action: QuestAction? = null,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(QuestTheme.tokens.radii.button),
        modifier = modifier.defaultMinSize(minHeight = QuestTheme.tokens.sizes.touchTarget),
    ) {
        if (action != null) {
            Icon(imageVector = action.icon(), contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text)
    }
}
