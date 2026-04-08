package com.example.uptime.room.catalogs

import androidx.compose.ui.graphics.Color
import com.example.uptime.room.WoodTheme
import com.example.uptime.room.WoodThemeOption

object WoodThemeCatalog {
    val all: List<WoodThemeOption> = listOf(
        WoodThemeOption(
            id = "oak",
            name = "Oak",
            theme = WoodTheme(
                woodFront = Color(0xFF8B5E3C),
                woodTop = Color(0xFFA0714F),
                woodSide = Color(0xFF6B4226),
                woodDark = Color(0xFF4E2E14)
            ),
            0
        ),
        WoodThemeOption(
            id = "mahogany",
            name = "Mahogany",
            theme = WoodTheme(
                woodFront = Color(0xFF7B2D2D),
                woodTop = Color(0xFF9E4040),
                woodSide = Color(0xFF5C1E1E),
                woodDark = Color(0xFF3A0E0E)
            ),
            100
        ),
        WoodThemeOption(
            id = "cherry",
            name = "Cherry",
            theme = WoodTheme(
                woodFront = Color(0xFFA1473B),
                woodTop = Color(0xFFB7584B),
                woodSide = Color(0xFF883B2D),
                woodDark = Color(0xFF692019)
            ),
            100
        ),
        WoodThemeOption(
            id = "walnut",
            name = "Walnut",
            theme = WoodTheme(
                woodFront = Color(0xFF4A3728),
                woodTop = Color(0xFF5E4A38),
                woodSide = Color(0xFF342418),
                woodDark = Color(0xFF1E1208)
            ),
            50
        ),
    )
}