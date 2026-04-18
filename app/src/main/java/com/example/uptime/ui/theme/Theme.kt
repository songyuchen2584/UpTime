package com.example.uptime.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.uptime.ui.theme.Coral60

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    onPrimary = Green30,
    primaryContainer = Green40,
    onPrimaryContainer = Color.White,

    secondary = Blue80,
    onSecondary = Blue20,
    secondaryContainer = Blue40,
    onSecondaryContainer = Blue80,

    tertiary = Amber80,
    onTertiary = Neutral90,

    background = Neutral90,
    onBackground = Neutral0,

    surface = Neutral80,
    onSurface = Neutral0,

    surfaceVariant = Neutral70,
    onSurfaceVariant = Neutral10,

    error = Coral40,
    onError = Neutral0,
    errorContainer = Coral60
)

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = Color.White,
    primaryContainer = Green80,
    onPrimaryContainer = Green30,

    secondary = Blue40,
    onSecondary = Color.White,
    secondaryContainer = Blue80,
    onSecondaryContainer = Blue20,

    tertiary = Amber40,
    onTertiary = Color.Black,
    tertiaryContainer = Amber80,

    background = Neutral10,
    onBackground = Neutral90,

    surface = Neutral10,
    onSurface = Neutral90,

    surfaceVariant = Neutral0,
    onSurfaceVariant = Neutral80,

    error = Coral40,
    onError = Color.White,
    errorContainer = Coral60
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