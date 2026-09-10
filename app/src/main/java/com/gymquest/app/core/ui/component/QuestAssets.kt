package com.gymquest.app.core.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.gymquest.app.R
import com.gymquest.app.core.ui.theme.QuestTheme

/** Project-owned, monochrome vectors. They are tinted by the active theme. */
enum class QuestAsset(@get:DrawableRes val drawableRes: Int) {
    Character(R.drawable.asset_character),
    Xp(R.drawable.asset_xp),
    Mission(R.drawable.asset_mission),
    Achievement(R.drawable.asset_achievement),
    EmptyState(R.drawable.asset_empty_state),
    ExercisePlaceholder(R.drawable.asset_exercise_placeholder),
    MartialPlaceholder(R.drawable.asset_martial_placeholder),
}

@Composable
fun QuestAssetIcon(
    asset: QuestAsset,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: androidx.compose.ui.graphics.Color = QuestTheme.tokens.colors.blueStructure,
) {
    Icon(
        painter = painterResource(asset.drawableRes),
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier,
    )
}

/** Neutral local fallback for exercises without downloaded or user-provided media. */
@Composable
fun QuestExerciseMediaPlaceholder(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        QuestAssetIcon(
            asset = QuestAsset.ExercisePlaceholder,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = QuestTheme.tokens.colors.textSecondary,
        )
        Text(
            text = "Sin multimedia para este ejercicio",
            style = MaterialTheme.typography.bodySmall,
            color = QuestTheme.tokens.colors.textSecondary,
        )
    }
}
