package com.example.uptime.room.catalogs

import androidx.compose.ui.graphics.Color
import com.example.uptime.room.AchievementTier
import com.example.uptime.room.MetalTheme
import com.example.uptime.room.MetalThemeOption

object MetalThemeCatalog {
    val all: List<MetalThemeOption> = listOf(
        MetalThemeOption(
            tier = AchievementTier.Bronze,
            theme = MetalTheme(
                base = Color(0xFFCE8146),
                dark = Color(0xFF824A2C),
                highlight = Color(0xFFFFB06F)
            )
        ),
        MetalThemeOption(
            tier = AchievementTier.Silver,
            theme = MetalTheme(
                base = Color(0xFFABB6B2),
                dark = Color(0xFF788383),
                highlight = Color(0xFFEAEEEE)
            )
        ),
        MetalThemeOption(
            tier = AchievementTier.Gold,
            theme = MetalTheme(
                base = Color(0xFFE8AB12),
                dark = Color(0xFF9B550A),
                highlight = Color(0xFFF8E9A0)
            )
        ),
        MetalThemeOption(
            tier = AchievementTier.Diamond,
            theme = MetalTheme(
                base = Color(0xFF99DFE8),
                dark = Color(0xFF449FB0),
                highlight = Color(0xFFEBF8FF)
            )
        ),
    )
}