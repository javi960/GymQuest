package com.gymquest.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    onOpenSession: () -> Unit,
    onOpenCatalog: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenProgress: () -> Unit,
    onOpenMartialArts: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "GymQuest")
        Text(text = "Registro flexible de entrenamientos reales")

        Button(onClick = onOpenSession) {
            Text(text = "Sesión")
        }

        Button(onClick = onOpenCatalog) {
            Text(text = "Catálogo")
        }

        Button(onClick = onOpenHistory) {
            Text(text = "Historial")
        }

        Button(onClick = onOpenProgress) {
            Text(text = "Progreso")
        }

        Button(onClick = onOpenMartialArts) {
            Text(text = "Artes marciales")
        }

        Button(onClick = onOpenSettings) {
            Text(text = "Ajustes")
        }
    }
}