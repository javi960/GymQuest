package com.gymquest.app.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gymquest.app.core.ui.theme.GymQuestTheme
import com.gymquest.app.navigation.GymQuestNavGraph

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            GymQuestTheme {
                GymQuestNavGraph()
            }
        }
    }
}