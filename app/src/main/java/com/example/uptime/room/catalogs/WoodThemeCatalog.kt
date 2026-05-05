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
            150
        ),
        WoodThemeOption(
            id = "cherry",
            name = "Cherry",
            theme = WoodTheme(
                woodFront = Color(0xFFA1473B),
                woodTop = Color(0xFFB7584B),
                woodSide = Color(0xFF883B2D),
                woodDark = Color(0xFF651F18)
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
        WoodThemeOption(
            id = "ebony",
            name = "Ebony",
            theme = WoodTheme(
                woodFront = Color(0xFF342920),
                woodTop = Color(0xFF3B3228),
                woodSide = Color(0xFF231911),
                woodDark = Color(0xFF170D08)
            ),
            150
        ),
        WoodThemeOption(
            id = "birch",
            name = "Birch",
            theme = WoodTheme(
                woodFront = Color(0xFFA1825D),
                woodTop = Color(0xFFC4A076),
                woodSide = Color(0xFF8F6F4D),
                woodDark = Color(0xFF75563B)
            ),
            50
        ),
        WoodThemeOption(
            id = "grey",
            name = "Millennial",
            theme = WoodTheme(
                woodFront = Color(0xFF808080),
                woodTop = Color(0xFF949494),
                woodSide = Color(0xFF676767),
                woodDark = Color(0xFF565656)
            ),
            25
        ),
        WoodThemeOption(
            id = "ash",
            name = "White Ash",
            theme = WoodTheme(
                woodFront = Color(0xFFDED0BF),
                woodTop = Color(0xFFD5C5B4),
                woodSide = Color(0xFFAD9D8B),
                woodDark = Color(0xFF8D7C6D)
            ),
            200
        ),
        WoodThemeOption(
            id = "twilight",
            name = "Twilight",
            theme = WoodTheme(
                woodFront = Color(0xFF3E4062),
                woodTop = Color(0xFF484D6D),
                woodSide = Color(0xFF333562),
                woodDark = Color(0xFF2B193D)
            ),
            250
        ),
    )
}