package com.example.uptime.room.catalogs

import androidx.compose.ui.graphics.Color
import com.example.uptime.room.RoomTheme
import com.example.uptime.room.RoomThemeOption

object RoomThemeCatalog {
    val allRoomThemes: List<RoomThemeOption> = listOf(
        RoomThemeOption(
            "default",
            "Default",
            RoomTheme(Color(0xFFDCCDC5), Color(0xFFCB8B50), Color(0xFFE0C4B3)),
            0
        ),
        RoomThemeOption(
            "moody",
            "Moody",
            RoomTheme(Color(0xFF606791), Color(0xFF403E4B), Color(0xFF6374A1)),
            0
        ),
        RoomThemeOption(
            "warm",
            "Warm",
            RoomTheme(Color(0xFFAF6D50), Color(0xFF6B4226), Color(0xFFE0A882)),
            0
        ),
        RoomThemeOption(
            "icy",
            "Icy",
            RoomTheme(Color(0xFF79B9C4), Color(0xFF4B719A), Color(0xFF93C5D2)),
            50
        ),
        RoomThemeOption(
            "fiery",
            "Fiery",
            RoomTheme(Color(0xFFEC9A6E), Color(0xFF9F463B), Color(0xFFD56A61)),
            50
        ),
        RoomThemeOption(
            "forest",
            "Forest",
            RoomTheme(Color(0xFF4A7C59), Color(0xFF2D4A35), Color(0xFF7AAF8A)),
            100
        ),
        RoomThemeOption(
            "ocean",
            "Ocean",
            RoomTheme(Color(0xFF3A6B8A), Color(0xFF1A3A4A), Color(0xFF5B9BBF)),
            100
        ),
        RoomThemeOption(
            "midnight",
            "Midnight",
            RoomTheme(Color(0xFF2C2C54), Color(0xFF1A1A2E), Color(0xFF4A4A8A)),
            150
        ),
        RoomThemeOption(
            "opulent",
            "Opulent",
            RoomTheme(Color(0xFF542970), Color(0xFFD99D43), Color(0xFFE0AE65)),
            9999
        ),
    )
}