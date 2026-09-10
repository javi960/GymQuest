package com.gymquest.app.core.ui.theme

enum class GymQuestThemePreset {
    ClassicQuest,
    MinimalGym,
    DarkDungeon,
    MartialDojo,
}

/** Boundary for future local persistence (DataStore/Room); features never read it directly. */
interface ThemePresetPreferenceStore {
    suspend fun read(): GymQuestThemePreset
    suspend fun save(preset: GymQuestThemePreset)
}
