package com.gymquest.app.core.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gymquest.app.core.ui.theme.QuestTheme

@Composable
fun QuestScreen(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val tokens = QuestTheme.tokens
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(tokens.colors.panel, tokens.colors.background),
                ),
            ),
    ) {
        content()
    }
}

@Composable
fun QuestPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val tokens = QuestTheme.tokens
    Surface(
        color = tokens.colors.panel,
        contentColor = tokens.colors.textPrimary,
        shape = RoundedCornerShape(tokens.radii.panel),
        border = BorderStroke(1.dp, tokens.colors.panelBorder),
        modifier = modifier
            .fillMaxWidth()
            .shadow(3.dp, RoundedCornerShape(tokens.radii.panel), clip = false),
    ) {
        Column(
            modifier = Modifier.padding(tokens.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(tokens.spacing.sm),
            content = content,
        )
    }
}

@Composable
fun QuestSectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleLarge)
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = QuestTheme.tokens.colors.textSecondary,
            )
        }
    }
}

@Composable
fun StatBadge(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    val tokens = QuestTheme.tokens
    Surface(
        color = tokens.colors.surface,
        contentColor = tokens.colors.textPrimary,
        shape = RoundedCornerShape(tokens.radii.badge),
        border = BorderStroke(1.dp, tokens.colors.panelBorder),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = value, style = MaterialTheme.typography.labelLarge)
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = tokens.colors.textSecondary)
        }
    }
}

@Composable
fun StatBadgeRow(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )
}

@Composable
fun XpProgressBar(
    currentXp: Int,
    targetXp: Int,
    level: Int,
    modifier: Modifier = Modifier,
) {
    val safeTarget = targetXp.coerceAtLeast(1)
    val progress = (currentXp.toFloat() / safeTarget).coerceIn(0f, 1f)
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("Nivel $level", style = MaterialTheme.typography.labelLarge)
            Text("$currentXp / $targetXp XP", style = MaterialTheme.typography.labelLarge)
        }
        LinearProgressIndicator(
            progress = { progress },
            color = QuestTheme.tokens.colors.xpGold,
            trackColor = QuestTheme.tokens.colors.panelBorder,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
fun CharacterHeader(
    name: String,
    level: Int,
    currentXp: Int,
    targetXp: Int,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    QuestPanel(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                color = QuestTheme.tokens.colors.blueStructure,
                contentColor = androidx.compose.ui.graphics.Color.White,
                shape = RoundedCornerShape(18.dp),
            ) {
                Icon(
                    imageVector = QuestSymbol.Achievement.icon,
                    contentDescription = QuestSymbol.Achievement.contentDescription,
                    modifier = Modifier.padding(12.dp),
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(name, style = MaterialTheme.typography.headlineSmall)
                Text(subtitle, color = QuestTheme.tokens.colors.textSecondary)
            }
        }
        XpProgressBar(currentXp = currentXp, targetXp = targetXp, level = level)
    }
}

@Composable
fun MissionCard(
    title: String,
    description: String,
    reward: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    QuestPanel(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = QuestSymbol.Mission.icon,
                contentDescription = QuestSymbol.Mission.contentDescription,
                tint = QuestTheme.tokens.colors.xpGold,
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(description, color = QuestTheme.tokens.colors.textSecondary)
                Text(reward, style = MaterialTheme.typography.labelLarge, color = QuestTheme.tokens.colors.positive)
                if (action != null) {
                    action()
                }
            }
        }
    }
}

@Composable
fun EmptyAdventureState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    QuestPanel(modifier = modifier) {
        Icon(
            imageVector = QuestSymbol.Mission.icon,
            contentDescription = null,
            tint = QuestTheme.tokens.colors.blueStructure,
        )
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(description, color = QuestTheme.tokens.colors.textSecondary)
        if (action != null) {
            action()
        }
    }
}
