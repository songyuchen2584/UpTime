package com.example.uptime

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlin.collections.listOf

// Placeholder object for storing room state info for an instance
object RoomRepository {
    var state by mutableStateOf(getPlaceholderState())

    fun updateDisplayName(newName: String) {
        state = state.copy(displayName = newName)
    }

    fun selectTheme(themeId: String) {
        state = state.copy(selectedThemeId = themeId)
    }

    fun getPlaceholderState() = RoomState(
        layout = RoomLayout.Default,
        currentPoints = 320,
        displayName = "Cody's Room",
        achievements = listOf(
            Achievement(
                "streak_7",
                "One Week Streak",
                "7 day streak",
                R.drawable.trophy_24px,
                isUnlocked = true),
            Achievement(
                "streak_14",
                "Two Week Streak",
                "14 day streak",
                R.drawable.trophy_24px,
            isUnlocked = false),
            Achievement(
                "streak_21",
                "Three Week Streak",
                "21 day streak",
                R.drawable.trophy_24px,
                isUnlocked = false),
            Achievement(
                "streak_28",
                "One Month Streak",
                "28 day streak",
                R.drawable.trophy_24px,
                isUnlocked = false),
            Achievement(
                "walk_60",
                "One Hour Walker",
                "Walk 60 total mins",
                R.drawable.trophy_24px,
                isUnlocked = true),
            Achievement(
                "walk_120",
                "Two Hour Walker",
                "Walk 120 total mins",
                R.drawable.trophy_24px,
                isUnlocked = true)),
        availableItems = listOf(),
        themeOptions = listOf(
            RoomThemeOption("default",     "Default",       RoomTheme(Color(0xFFDCCDC5), Color(0xFFD58B46), Color(0xFFE0C4B3)), isUnlocked = true),
            RoomThemeOption("moody",  "Moody",    RoomTheme(Color(0xFF606791), Color(0xFF403E4B), Color(0xFF6374A1)), isUnlocked = true),
            RoomThemeOption("warm",     "Warm",       RoomTheme(Color(0xFFC4856A), Color(0xFF6B4226), Color(0xFFE0A882)), isUnlocked = true),
            RoomThemeOption("icy",     "Icy",       RoomTheme(Color(0xFF79B9C4), Color(0xFF4B719A), Color(0xFF93C5D2)), isUnlocked = true),
            RoomThemeOption("forest",   "Forest",     RoomTheme(Color(0xFF4A7C59), Color(0xFF2D4A35), Color(0xFF7AAF8A)), isUnlocked = false),
            RoomThemeOption("ocean",    "Ocean",      RoomTheme(Color(0xFF3A6B8A), Color(0xFF1A3A4A), Color(0xFF5B9BBF)), isUnlocked = false),
            RoomThemeOption("midnight", "Midnight",   RoomTheme(Color(0xFF2C2C54), Color(0xFF1A1A2E), Color(0xFF4A4A8A)), isUnlocked = false),
            RoomThemeOption("opulent",     "Opulent",       RoomTheme(Color(0xFF642F86), Color(0xFFD99D43), Color(0xFFE0AE65)), isUnlocked = true),
        )
    )
}