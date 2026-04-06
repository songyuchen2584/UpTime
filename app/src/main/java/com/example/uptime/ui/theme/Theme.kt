package com.example.uptime.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    secondary = Blue80,
    tertiary = Amber80,
    background = DarkSurface,
    surface = CardDark,
    error = Coral40
)

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    secondary = Blue40,
    tertiary = Amber40,
    background = LightSurface,
    surface = CardLight,
    error = Coral40
)

@Composable
fun UpTimeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}