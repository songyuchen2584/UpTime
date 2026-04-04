package com.example.uptime

import android.R.attr.y
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

// Placeholder data for now
data class RoomItem(
    val id: String,
    val name: String,
    val icon: Int,
    val isPlaced: Boolean = false,
    val placedCoordinates: LayoutCoordinates // subject to change if this doesn't work how I think it does
)

data class RoomTheme(
    val wallColor: Color = Color(0xFF606791),
    val floorColor: Color = Color(0xFF403E4B),
    val accentColor: Color = Color(0xFF6374A1)
)

data class RoomThemeOption(
    val id: String,
    val name: String,
    val theme: RoomTheme,
    val isUnlocked: Boolean = false
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: Int,
    val isUnlocked: Boolean = false
)

val achievements = listOf(
    Achievement("streak_7",
        "One Week Streak",
        "7 day streak",
        R.drawable.trophy_24px,
        isUnlocked = true),
    Achievement("streak_14",
        "Two Week Streak",
        "14 day streak",
        R.drawable.trophy_24px,
        isUnlocked = false),
    Achievement("streak_21",
        "Three Week Streak",
        "21 day streak",
        R.drawable.trophy_24px,
        isUnlocked = false),
    Achievement("streak_28",
        "One Month Streak",
        "28 day streak",
        R.drawable.trophy_24px,
        isUnlocked = false),
    Achievement("walk_60",
        "One Hour Walker",
        "Walk 60 total mins",
        R.drawable.trophy_24px,
        isUnlocked = true),
    Achievement("walk_120",
        "Two Hour Walker",
        "Walk 120 total mins",
        R.drawable.trophy_24px,
        isUnlocked = true),
)

val themeOptions = listOf(
    RoomThemeOption("default",     "Default",       RoomTheme(Color(0xFFDCCDC5), Color(0xFFD58B46), Color(0xFFE0C4B3)), isUnlocked = true),
    RoomThemeOption("moody",  "Moody",    RoomTheme(Color(0xFF606791), Color(0xFF403E4B), Color(0xFF6374A1)), isUnlocked = true),
    RoomThemeOption("warm",     "Warm",       RoomTheme(Color(0xFFC4856A), Color(0xFF6B4226), Color(0xFFE0A882)), isUnlocked = true),
    RoomThemeOption("icy",     "Icy",       RoomTheme(Color(0xFF79B9C4), Color(0xFF4B719A), Color(0xFF93C5D2)), isUnlocked = true),
    RoomThemeOption("forest",   "Forest",     RoomTheme(Color(0xFF4A7C59), Color(0xFF2D4A35), Color(0xFF7AAF8A)), isUnlocked = false),
    RoomThemeOption("ocean",    "Ocean",      RoomTheme(Color(0xFF3A6B8A), Color(0xFF1A3A4A), Color(0xFF5B9BBF)), isUnlocked = false),
    RoomThemeOption("midnight", "Midnight",   RoomTheme(Color(0xFF2C2C54), Color(0xFF1A1A2E), Color(0xFF4A4A8A)), isUnlocked = false),
)

data class RoomState(
    val layout: RoomLayout = RoomLayout.Default,
    val theme: RoomTheme = RoomTheme(),
    val availableItems: List<RoomItem> = emptyList(),
    val achievements: List<Achievement> = com.example.uptime.achievements,
    val themeOptions: List<RoomThemeOption> = com.example.uptime.themeOptions,
    val displayName: String = "My Room",
    val currentPoints: Int = 0
)

enum class RoomLayout {
    Default;
    // Future layouts: aquarium, garden, etc.
}

enum class RoomPanel { Achievements, Exchange }

@Composable
fun RoomScreen(state: RoomState = RoomState()) {
    var roomMode by remember { mutableStateOf(RoomMode.View) }
    var activePanel by remember { mutableStateOf<RoomPanel?>(null) }
    var showThemePicker by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        RoomScaffold(state, roomMode)

        Row(modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(12.dp), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            AchievementsPanel(onClick = {
                activePanel = if (activePanel == RoomPanel.Achievements) null
                else RoomPanel.Achievements
            })
            CustomizePanel(isActive = roomMode == RoomMode.Edit,
                onClick  = {
                    roomMode = if (roomMode == RoomMode.Edit) RoomMode.View else RoomMode.Edit
                    showThemePicker = false
                })
            ExchangePanel(onClick = {
                activePanel = if (activePanel == RoomPanel.Exchange) null
                else RoomPanel.Exchange
            })
        }

        AnimatedVisibility(
            visible = roomMode == RoomMode.Edit,
            enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
            exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it }),
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EditToolButton(icon = R.drawable.responsive_layout_24px, label = "Layout", onClick = {})
                EditToolButton(
                    icon = R.drawable.format_paint_24px,
                    label = "Theme",
                    isActive = showThemePicker,
                    onClick = { showThemePicker = !showThemePicker })
                EditToolButton(icon = R.drawable.package_2_24px, label = "Items", onClick = {})
            }
        }

        AnimatedVisibility(
            visible = showThemePicker && roomMode == RoomMode.Edit,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            ThemePickerRow(
                themes = state.themeOptions,
                modifier = Modifier
                    .padding(bottom = 96.dp)
            )
        }
    }

    AnimatedVisibility(
        visible = activePanel != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
        modifier = Modifier.fillMaxSize()
    ) {
        when (activePanel) {
            RoomPanel.Achievements -> AchievementsDisplay(
                achievements = state.achievements,
                onClose = { activePanel = null }
            )
            RoomPanel.Exchange -> ExchangeDisplay(
                currentPoints = state.currentPoints,
                availableItems = state.availableItems,
                onClose = { activePanel = null }
            )
            null -> Unit
        }
    }
}

@Composable
fun ThemePickerRow(
    themes: List<RoomThemeOption>,
    modifier: Modifier
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(themes) { option ->
            ThemeCard(
                option = option,
                isSelected = option.id == "moody", // replace with data logic later
                onSelect = {}
            )
        }
    }
}

@Composable
fun ThemeCard(
    option: RoomThemeOption,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.width(80.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Show theme's Colors
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    option.theme.wallColor,
                    option.theme.floorColor,
                    option.theme.accentColor
                ).forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(
                                color = color,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
            }

            Text(
                option.name,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = if (option.isUnlocked) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )

            if (!option.isUnlocked) {
                Icon(
                    painterResource(R.drawable.lock_24px),
                    contentDescription = "Locked",
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun RoomCanvas(theme: RoomTheme, layout: RoomLayout, modifier: Modifier = Modifier) {
    when (layout) {
        RoomLayout.Default -> DefaultRoomCanvas(theme, modifier)
    }
}

@Composable
fun DefaultRoomCanvas(theme: RoomTheme, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Back wall
        drawRect(color = theme.wallColor, size = Size(w, h * 0.55f))

        // Floor
        val floorY = h * 0.55f
        drawRect(
            color = theme.floorColor,
            topLeft = Offset(0f, floorY),
            size = Size(w, h * 0.45f)
        )

        // Wall Accent
        drawRect(
            color = theme.accentColor,
            topLeft = Offset(0f, h * 0.53f),
            size = Size(w, h * 0.03f)
        )

        translate(left = w*0.06f, top = h*-0.05f) {
            val windowWidth = w * 0.275f
            val windowHeight = h * 0.25f
            val windowDepth = windowWidth * 0.05f
            val frameWidth = windowDepth * 1.5f

            val windowFront = Color(0xFFD9EAE9)
            val windowTop = Color(0xFFF7FFFE)
            val windowSide = Color(0xFFB8D0CF)

            var windowX = w * 0.6f
            val windowY = h * 0.08f

            // Shadow under sill
            drawRect(
                color = Color.Black.copy(alpha = 0.15f),
                topLeft = Offset(windowX, windowY + windowHeight),
                size = Size(windowWidth - windowDepth/2f, frameWidth * 1.55f)
            )

            // Window Pane
            drawRect(
                color = Color(0xFFC1E3F3),
                topLeft = Offset(windowX, windowY),
                size = Size(windowWidth-windowDepth, windowHeight)
            )

            // Pane details
            val reflectionPath = Path().apply {
                moveTo(windowX, windowY + windowHeight)
                lineTo(windowX + windowWidth - frameWidth * 1.1f, windowY * 1.6f)
                close()
            }
            drawPath(reflectionPath, color = windowTop.copy(alpha = 0.3f), style = Stroke(width = 40f))

            val reflectionPath2 = Path().apply {
                moveTo(windowX * 1.2f, windowY + windowHeight)
                lineTo(windowX + windowWidth - frameWidth, windowY * 2.7f)
                close()
            }
            drawPath(reflectionPath2, color = windowTop.copy(alpha = 0.3f), style = Stroke(width = 25f))

            windowX = w * 0.6f - windowDepth

            // Left Side
            // Front Frame
            drawRect(
                color = windowFront,
                topLeft = Offset(windowX - windowDepth * 0.5f, windowY + windowDepth),
                size = Size(frameWidth, windowHeight)
            )

            val leftSidePath = Path().apply {
                moveTo(windowX + windowDepth, windowY + windowDepth)
                lineTo(windowX + 2 * windowDepth, windowY)
                lineTo(windowX + 2 * windowDepth, windowY + windowHeight)
                lineTo(windowX + windowDepth, windowY +windowHeight + windowDepth)
                close()
            }
            drawPath(leftSidePath, color = windowSide)

            // Top & Bottom
            for (i in 0..1) {
                val y = when (i) {
                    0 -> 0f
                    1 -> windowHeight - frameWidth
                    else -> {0f}
                }

                // Frame Front
                drawRect(
                    color = windowFront,
                    topLeft = Offset(windowX + windowDepth, windowY + y + windowDepth),
                    size = Size(windowWidth - windowDepth * 2, frameWidth)
                )
                // Top
                val topFacePath = Path().apply {
                    moveTo(windowX + windowDepth, windowY + y + windowDepth)
                    lineTo(windowX + 2 * windowDepth, windowY + y)
                    lineTo(windowX + windowWidth, windowY + y)
                    lineTo(windowX + windowWidth - windowDepth, windowY + y + windowDepth)
                    close()
                }
                drawPath(topFacePath, color = windowTop)

                // Shadow
                if (i < 1) {
                    drawRect(
                        color = Color.Black.copy(alpha = 0.15f),
                        topLeft = Offset(windowX + windowDepth, windowY + y + frameWidth + windowDepth),
                        size = Size(windowWidth - windowDepth * 2, frameWidth * 0.4f)
                    )
                }
            }

            // Fill in Top
            val topFacePath = Path().apply {
                moveTo(windowX - windowDepth * 0.5f, windowY + windowDepth)
                lineTo(windowX + windowDepth  - windowDepth * 0.5f, windowY)
                lineTo(windowX + windowWidth - windowDepth + frameWidth, windowY)
                lineTo(windowX + windowWidth - windowDepth * 0.5f, windowY + windowDepth)
                close()
            }
            drawPath(topFacePath, color = windowTop)

            // Right Side
            // Front face
            drawRect(
                color = windowFront,
                topLeft = Offset(windowX + windowWidth - windowDepth * 2, windowY + windowDepth),
                size = Size(frameWidth, windowHeight)
            )
            // Side face
            val rightSidePath = Path().apply {
                moveTo(windowX + windowWidth - windowDepth/2f, windowY + windowDepth)
                lineTo(windowX + windowWidth + windowDepth/2f, windowY)
                lineTo(windowX + windowWidth + windowDepth/2f, windowY + windowHeight)
                lineTo(windowX + windowWidth - windowDepth/2f, windowY + windowHeight + windowDepth)
                close()
            }
            drawPath(rightSidePath, color = windowSide)
        }

        val shelfWidth = w / 2.2f
        val shelfHeight = h / 3
        val shelfDepth = shelfWidth * 0.06f
        val shelfOffsetX = w * 0.035f
        val shelfOffsetY = floorY - shelfHeight + shelfDepth

        translate(left = shelfOffsetX, top = shelfOffsetY) {
            // Shelf setup
            val shelfThickness = shelfHeight * 0.04f
            val numShelves = 3
            val shelfSpacing = shelfHeight / (numShelves + 1)

            val woodFront = Color(0xFF8B5E3C)
            val woodTop = Color(0xFFA0714F)
            val woodSide = Color(0xFF6B4226)
            val woodDark = Color(0xFF4E2E14)

            // Shelf Back
            drawRect(
                color = woodDark,
                topLeft = Offset(shelfDepth, 0f),
                size = Size(shelfWidth - shelfDepth * 2, shelfHeight)
            )

            // Left Side
            // Front face
            drawRect(
                color = woodFront,
                topLeft = Offset(0f, shelfDepth),
                size = Size(shelfDepth, shelfHeight)
            )
            // Side face
            val leftSidePath = Path().apply {
                moveTo(shelfDepth, shelfDepth)
                lineTo(2 * shelfDepth, 0f)
                lineTo(2 * shelfDepth, shelfHeight)
                lineTo(shelfDepth, shelfHeight+shelfDepth)
                close()
            }
            drawPath(leftSidePath, color = woodSide)

            // Shelves
            for (i in 0..numShelves) {
                val y = when (i) {
                    0 -> 0f
                    numShelves -> shelfHeight - shelfThickness
                    else -> shelfSpacing * i
                }

                // Shelf Front
                drawRect(
                    color = woodFront,
                    topLeft = Offset(shelfDepth, y + shelfDepth),
                    size = Size(shelfWidth - shelfDepth * 2, shelfThickness)
                )
                // Top
                val topFacePath = Path().apply {
                    moveTo(shelfDepth, y + shelfDepth)
                    lineTo(2 * shelfDepth, y)
                    lineTo(shelfWidth, y)
                    lineTo(shelfWidth - shelfDepth, y + shelfDepth)
                    close()
                }
                drawPath(topFacePath, color = woodTop)

                // Shadow
                if (i < 3) {
                    drawRect(
                        color = Color.Black.copy(alpha = 0.15f),
                        topLeft = Offset(shelfDepth, y + shelfThickness + shelfDepth),
                        size = Size(shelfWidth - shelfDepth * 2, shelfThickness * 0.4f)
                    )
                }
            }

            // Fill in Top
            val topFacePath = Path().apply {
                moveTo(0f, shelfDepth)
                lineTo(shelfDepth, 0f)
                lineTo(shelfWidth, 0f)
                lineTo(shelfWidth - shelfDepth, shelfDepth)
                close()
            }
            drawPath(topFacePath, color = woodTop)

            // Right Side
            // Front face
            drawRect(
                color = woodFront,
                topLeft = Offset(shelfWidth - shelfDepth * 2, shelfDepth),
                size = Size(shelfDepth, shelfHeight)
            )
            // Side face
            val rightSidePath = Path().apply {
                moveTo(shelfWidth - shelfDepth, shelfDepth)
                lineTo(shelfWidth, 0f)
                lineTo(shelfWidth, shelfHeight)
                lineTo(shelfWidth - shelfDepth, shelfHeight + shelfDepth)
                close()
            }
            drawPath(rightSidePath, color = woodSide)
        }

        // Accent Rug
        scale(scaleX = 1.2f, scaleY = 0.8f) {

            val rugLeft = 1.1f * w
            val rugTop = h * 0.65f
            val rugWidth = w * 0.4f
            val rugHeight = h * 0.38f
            val detailInset = 0.1f

            translate(rugLeft, top = rugTop) {
                rotate(degrees = 90f, pivot = Offset(0f, 0f)) {
                    drawOval(
                        color = theme.accentColor.copy(alpha = 0.8f),
                        topLeft = Offset(0f, 0f),
                        size = Size(rugWidth, rugHeight)
                    )

                    drawOval(
                        color = Color(0x66FFFFFF),
                        topLeft = Offset(rugWidth * detailInset - 4f, rugHeight * detailInset + 15f),
                        size = Size(rugWidth * (1f - detailInset * 2.2f), rugHeight * (1f - detailInset * 2)),
                        //style = Stroke(width = 10f)
                    )
                }
            }
        }
    }
}

@Composable
fun ExchangeDisplay(currentPoints: Int, availableItems: List<RoomItem>, onClose: () -> Unit) {
    // item shop layout?
}

@Composable
fun AchievementsDisplay(
    achievements: List<Achievement>,
    onClose: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun displayAlert(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Trophy Case",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose) {
                    Icon(painterResource(R.drawable.close_24px), contentDescription = "Close")
                }
            }

            Text(
                "${achievements.count { it.isUnlocked }} / ${achievements.size} unlocked",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Trophy grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(achievements) { achievement ->
                    TrophyCell(achievement, onPlace = {
                        if (achievement.isUnlocked) {
                            // handle placement logic later
                        } else {
                            displayAlert("This trophy is locked.")
                        }
                    })
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun TrophyCell(achievement: Achievement, onPlace: () -> Unit = {}) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (achievement.isUnlocked)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painterResource(achievement.icon),
                contentDescription = null,
                tint = if (achievement.isUnlocked) Color(0xFFFFB300)
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                achievement.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                color = if (achievement.isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
            Text(
                achievement.description,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = if (achievement.isUnlocked) 1f else 0.4f
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            FilledTonalButton(
                onClick = onPlace,
                modifier = Modifier.height(32.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (achievement.isUnlocked)
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Place", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun EditToolButton(
    icon: Int,
    label: String,
    onClick: () -> Unit,
    isActive: Boolean = false
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.width(56.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 4.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                painterResource(icon),
                contentDescription = label,
                modifier = Modifier.size(20.dp),
                tint = if (isActive) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

enum class RoomMode { View, Edit }

@Composable
fun FloatingPanel(
    icon: Int,
    label: String,
    onClick: () -> Unit = {},
    isActive: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(90.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painterResource(icon),
                contentDescription = label,
                tint = if (isActive)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun AchievementsPanel(onClick: () -> Unit = {}) {
    FloatingPanel(R.drawable.trophy_24px, "Achievements", onClick)
}

@Composable
fun CustomizePanel(isActive: Boolean = false, onClick: () -> Unit = {}) {
    FloatingPanel(R.drawable.palette_24px, "Customize", onClick, isActive)
}

@Composable
fun ExchangePanel(onClick: () -> Unit = {}) {
    FloatingPanel(R.drawable.redeem_24px, "Exchange", onClick)
}

@Composable
fun RoomScaffold(state: RoomState, mode: RoomMode) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
    RoomCanvas(theme = state.theme, layout = state.layout)

    if (mode == RoomMode.Edit) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(
                        alpha = 0.6f
                    )
                ),
                modifier = Modifier.padding(top = 52.dp)
            ) {
                Text(
                    text = "Edit Mode",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }

    Card(
        modifier = Modifier.padding(6.dp),
        onClick = {changeName(mode)},
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                state.displayName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(6.dp),
                textAlign = TextAlign.Center
            )
            androidx.compose.animation.AnimatedVisibility(
                visible = mode == RoomMode.Edit,
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it }),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(
                    painterResource(R.drawable.edit_20px),
                    "Change Name",
                    modifier = Modifier.padding(end = 6.dp)
                )
            }
        }
    }
}

fun changeName(mode: RoomMode) {
    if (mode == RoomMode.Edit) {
        // Update name in DB
    }
}

@Preview
@Composable
fun RoomLayoutPreview(){
    val theme = RoomTheme(wallColor = Color(0xFF606791),
    floorColor = Color(0xFF403E4B),
    accentColor = Color(0xFF536285))
    DefaultRoomCanvas(theme)
}