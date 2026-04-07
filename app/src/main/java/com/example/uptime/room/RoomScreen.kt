package com.example.uptime.room

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uptime.R
import com.example.uptime.room.catalogs.AchievementCatalog
import com.example.uptime.room.catalogs.RoomItemCatalog
import com.example.uptime.room.catalogs.RoomLayoutCatalog
import com.example.uptime.room.catalogs.RoomThemeCatalog
import com.example.uptime.room.catalogs.TrophyCaseCatalog
import com.example.uptime.room.catalogs.WoodThemeCatalog
import kotlinx.coroutines.launch
import kotlin.Int
import kotlin.collections.filter
import kotlin.collections.find
import kotlin.collections.forEachIndexed

// Placeholder data for now
data class RoomItem(
    val id: String,
    val name: String,
    val icon: Int,
    val pointCost: Int
)

enum class RoomMode { View, Edit }

data class RoomTheme(
    val wallColor: Color = Color(0xFF606791),
    val floorColor: Color = Color(0xFF403E4B),
    val accentColor: Color = Color(0xFF6374A1)
)

data class RoomThemeOption(
    val id: String,
    val name: String,
    val theme: RoomTheme,
    val pointCost: Int = 0
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: Int, // Unused for now, may delete later
    val size: AchievementSize = AchievementSize.Small
)

data class RoomState(
    val selectedRoomLayoutId: String = "default",
    val selectedRoomThemeId: String = "default",
    val selectedWoodThemeId: String = "default",
    val displayName: String = "My Room",
    val placedAchievements: Map<String, String> = emptyMap(),
    val placedRoomItems: Map<String, String> = emptyMap(),
    val unlockedRoomItemIds: Set<String> = emptySet(),
    val unlockedRoomThemeIds: Set<String> = setOf("default"),
    val unlockedWoodThemeIds: Set<String> = setOf("oak"),
    val unlockedAchievementIds: Set<String> = emptySet(),
    val unlockedRoomLayoutIds: Set<String> = setOf("default"),
    val currentPoints: Int = 0,
)

data class RoomLayoutOption (
    val id: String = "default",
    val name: String = "Default",
    val trophyCaseId: String = "default"
)

enum class RoomPanel { Achievements, Exchange }

data class WoodTheme(
    val woodFront: Color = Color(0xFF8B5E3C),
    val woodTop: Color = Color(0xFFA0714F),
    val woodSide: Color = Color(0xFF6B4226),
    val woodDark: Color = Color(0xFF4E2E14)
)

data class WoodThemeOption(
    val id: String,
    val name: String,
    val theme: WoodTheme,
    val pointCost: Int = 0
)

enum class AchievementSize { Large, Medium, Small }

@Composable
fun RoomScreen(viewModel: RoomViewModel = viewModel()) {
    val state by viewModel.roomState.collectAsState()

    if (state == null) {
        RoomLoadingScreen()
        return
    }

    val roomState = state!!

    val activeRoomTheme = RoomThemeCatalog.allRoomThemes
        .find { it.id == roomState.selectedRoomThemeId }?.theme ?: RoomTheme()

    val activeWoodTheme = WoodThemeCatalog.allWoodThemes
        .find { it.id == roomState.selectedWoodThemeId }?.theme ?: WoodTheme()

    val trophyCaseId = RoomLayoutCatalog.allRoomLayouts
        .find { it.id == roomState.selectedRoomLayoutId }?.trophyCaseId ?: "default"

    var roomMode by rememberSaveable { mutableStateOf(RoomMode.View) }
    var activePanel by rememberSaveable { mutableStateOf<RoomPanel?>(null) }
    var showThemePicker by rememberSaveable { mutableStateOf(false) }

    val activeTrophyCaseSlots = TrophyCaseCatalog.allTrophyCases
        .find { it.id == trophyCaseId }?.shelfSlots ?: listOf(
        // Top area: 2 medium
        TrophyCaseCatalog.ShelfSlot(
            "top_large",
            TrophyCaseCatalog.ShelfSection.TopRow,
            listOf(AchievementSize.Large)
        ),
        TrophyCaseCatalog.ShelfSlot(
            "top_med_1",
            TrophyCaseCatalog.ShelfSection.TopRow,
            listOf(AchievementSize.Medium)
        ),
        TrophyCaseCatalog.ShelfSlot(
            "top_med_2",
            TrophyCaseCatalog.ShelfSection.TopRow,
            listOf(AchievementSize.Medium)
        ),
        // First row: 3 small
        TrophyCaseCatalog.ShelfSlot(
            "mid1_1",
            TrophyCaseCatalog.ShelfSection.MidRow1,
            listOf(AchievementSize.Small)
        ),
        TrophyCaseCatalog.ShelfSlot(
            "mid1_2",
            TrophyCaseCatalog.ShelfSection.MidRow1,
            listOf(AchievementSize.Small)
        ),
        TrophyCaseCatalog.ShelfSlot(
            "mid1_3",
            TrophyCaseCatalog.ShelfSection.MidRow1,
            listOf(AchievementSize.Small)
        ),
        // Second row: 3 small
        TrophyCaseCatalog.ShelfSlot(
            "mid2_1",
            TrophyCaseCatalog.ShelfSection.MidRow2,
            listOf(AchievementSize.Small)
        ),
        TrophyCaseCatalog.ShelfSlot(
            "mid2_2",
            TrophyCaseCatalog.ShelfSection.MidRow2,
            listOf(AchievementSize.Small)
        ),
        TrophyCaseCatalog.ShelfSlot(
            "mid2_3",
            TrophyCaseCatalog.ShelfSection.MidRow2,
            listOf(AchievementSize.Small)
        ),
        // Bottom area: 2 medium
        TrophyCaseCatalog.ShelfSlot(
            "bot_large",
            TrophyCaseCatalog.ShelfSection.BottomRow,
            listOf(AchievementSize.Large)
        ),
        TrophyCaseCatalog.ShelfSlot(
            "bot_med_1",
            TrophyCaseCatalog.ShelfSection.BottomRow,
            listOf(AchievementSize.Medium)
        ),
        TrophyCaseCatalog.ShelfSlot(
            "bot_med_2",
            TrophyCaseCatalog.ShelfSection.BottomRow,
            listOf(AchievementSize.Medium)
        ),
    )

    Box(modifier = Modifier.fillMaxSize()) {
        RoomScaffold(roomState, activeRoomTheme, activeWoodTheme, activeTrophyCaseSlots, roomMode, viewModel)

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
            }, points = roomState.currentPoints, roomMode = roomMode)
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
                    icon = R.drawable.room_theme_24px,
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
                allRoomThemes = RoomThemeCatalog.allRoomThemes,
                unlockedRoomThemeIds = roomState.unlockedRoomThemeIds,
                modifier = Modifier
                    .padding(bottom = 96.dp),
                selectedThemeId = roomState.selectedRoomThemeId,
                onThemeSelected = { themeId ->
                    viewModel.selectRoomTheme(themeId)
                }
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
                allAchievements = AchievementCatalog.allAchievements,
                unlockedAchievementIds = roomState.unlockedAchievementIds,
                placedAchievements = roomState.placedAchievements,
                viewModel = viewModel,
                onClose = { activePanel = null }
            )
            RoomPanel.Exchange -> ExchangeDisplay(
                currentPoints = roomState.currentPoints,
                unlockedRoomThemeIds = roomState.unlockedRoomThemeIds,
                unlockedRoomItemIds = roomState.unlockedRoomItemIds,
                unlockedWoodThemeIds = roomState.unlockedWoodThemeIds,
                viewModel = viewModel,
                onClose = { activePanel = null }
            )
            null -> Unit
        }
    }
}

@Composable
fun RoomScaffold(state: RoomState, activeRoomTheme: RoomTheme, activeWoodTheme: WoodTheme, activeTrophyCaseSlots: List<TrophyCaseCatalog.ShelfSlot>, mode: RoomMode, viewModel: RoomViewModel) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
    RoomCanvas(activeRoomTheme = activeRoomTheme, state.placedAchievements, layoutId = state.selectedRoomLayoutId, activeTrophyCaseSlots = activeTrophyCaseSlots, woodTheme = activeWoodTheme)

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
                modifier = Modifier.padding(top = 68.dp)
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

    NameHeader(mode, state, viewModel)
}

@Composable
fun RoomLoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        // Loading indicator
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Text("Loading your room...")
    }
}

@Composable
fun ThemePickerRow(
    allRoomThemes: List<RoomThemeOption>,
    unlockedRoomThemeIds: Set<String>,
    modifier: Modifier,
    selectedThemeId: String,
    onThemeSelected: (String) -> Unit
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        items(allRoomThemes) { option ->
            val isUnlocked = option.id in unlockedRoomThemeIds
            ThemeCard(
                option = option,
                isUnlocked = isUnlocked,
                isSelected = option.id == selectedThemeId,
                onSelect = {if (isUnlocked) onThemeSelected(option.id)}
            )
        }
    }
}

@Composable
fun ThemeCard(
    option: RoomThemeOption,
    isUnlocked: Boolean,
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
                color = if (isUnlocked) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )

            if (!isUnlocked) {
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
fun RoomCanvas(activeRoomTheme: RoomTheme, placedAchievements: Map<String, String>, layoutId: String, activeTrophyCaseSlots: List<TrophyCaseCatalog.ShelfSlot>, modifier: Modifier = Modifier, woodTheme: WoodTheme) {

    when (layoutId) {
        "default" -> DefaultRoomCanvas(activeRoomTheme, placedAchievements, activeTrophyCaseSlots, modifier, woodTheme)
    }
}

@Composable
fun DefaultRoomCanvas(theme: RoomTheme, placedAchievements: Map<String, String>, shelfSlots: List<TrophyCaseCatalog.ShelfSlot>, modifier: Modifier = Modifier, woodTheme: WoodTheme) {
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

        // Shelf setup
        val shelfWidth = w / 2.2f
        val shelfHeight = h / 3
        val shelfDepth = shelfWidth * 0.06f
        val shelfOffsetX = w * 0.035f
        val shelfOffsetY = floorY - shelfHeight + shelfDepth
        val shelfThickness = shelfHeight * 0.04f
        val numShelves = 3
        val shelfSpacing = shelfHeight / (numShelves + 1)

        translate(left = shelfOffsetX, top = shelfOffsetY) {

            val woodFront = woodTheme.woodFront
            val woodTop = woodTheme.woodTop
            val woodSide = woodTheme.woodSide
            val woodDark = woodTheme.woodDark

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

        translate(left = shelfOffsetX, top = shelfOffsetY) {
            drawShelfTrophies(shelfSlots, placedAchievements, shelfWidth, shelfHeight, shelfDepth, shelfThickness, shelfSpacing)
        }
    }
}

private fun DrawScope.drawShelfTrophies(
    slots: List<TrophyCaseCatalog.ShelfSlot>,
    placedAchievements: Map<String, String>,
    shelfWidth: Float,
    shelfHeight: Float,
    shelfDepth: Float,
    shelfThickness: Float,
    shelfSpacing: Float
) {
    val innerWidth = shelfWidth - shelfDepth * 4
    val innerLeft = shelfDepth * 2

    val topSlots = slots.filter { it.section == TrophyCaseCatalog.ShelfSection.TopRow }
    val mid1Slots = slots.filter { it.section == TrophyCaseCatalog.ShelfSection.MidRow1 }
    val mid2Slots = slots.filter { it.section == TrophyCaseCatalog.ShelfSection.MidRow2 }
    val bottomSlots = slots.filter { it.section == TrophyCaseCatalog.ShelfSection.BottomRow }

    val topShelfFloor = 0.5f * shelfThickness
    val mid1ShelfFloor = shelfSpacing + 0.5f * shelfThickness
    val bottomShelfFloor = shelfHeight - 0.5f * shelfThickness

    drawSectionTrophies(topSlots, placedAchievements, innerLeft, innerWidth, topShelfFloor, isLargeSection = true)
    drawSectionTrophies(mid1Slots, placedAchievements, innerLeft, innerWidth, mid1ShelfFloor, isLargeSection = false)
    drawSectionTrophies(mid2Slots, placedAchievements, innerLeft, innerWidth, mid1ShelfFloor + shelfSpacing, isLargeSection = false)
    drawSectionTrophies(bottomSlots, placedAchievements, innerLeft, innerWidth, bottomShelfFloor, isLargeSection = true)
}

private fun DrawScope.drawSectionTrophies(
    slots: List<TrophyCaseCatalog.ShelfSlot>,
    placedAchievements: Map<String, String>,
    innerLeft: Float,
    innerWidth: Float,
    shelfFloorY: Float,
    isLargeSection: Boolean
) {
    val filledSlots = slots.filter { placedAchievements[it.id] != null }
    if (filledSlots.isEmpty()) return

    if (isLargeSection) {
        val largeSlot = filledSlots.find { it.acceptedSizes.contains(AchievementSize.Large) }
        val medSlots  = filledSlots.filter { it.acceptedSizes.contains(AchievementSize.Medium) }

        if (largeSlot != null && placedAchievements[largeSlot.id] != null) {
            // Single large trophy centered
            drawTrophyModel(
                x = innerLeft + innerWidth / 2f,
                floorY = shelfFloorY,
                size = AchievementSize.Large
            )
        } else {
            // Up to 2 medium trophies
            medSlots.forEachIndexed { i, slot ->
                if (placedAchievements[slot.id]  != null) {
                    val x = innerLeft + innerWidth * (if (i == 0) 0.3f else 0.7f)
                    drawTrophyModel(x = x, floorY = shelfFloorY, size = AchievementSize.Medium)
                }
            }
        }
    } else {
        // 3 small slots evenly spaced
        val newInnerLeft = innerLeft * 0.3f
        val spacing = ((innerWidth) * 1.2f) / (slots.size + 1)
        slots.forEachIndexed { i, slot ->
            if (placedAchievements[slot.id] != null) {
                val x = newInnerLeft + spacing * (i + 1)
                drawTrophyModel(x = x, floorY = shelfFloorY, size = AchievementSize.Small)
            }
        }
    }
}

private fun DrawScope.drawTrophyModel(x: Float, floorY: Float, size: AchievementSize) {
    when (size) {
        AchievementSize.Small -> drawMedalModel(x, floorY)
        AchievementSize.Medium -> drawCupModel(x, floorY)
        AchievementSize.Large -> drawGrandTrophyModel(x, floorY)
    }
}

private fun DrawScope.drawCupModel(x: Float, floorY: Float) {
    val scale = 3f
    val cupW = 28f * scale
    val cupH = 24f * scale
    val stemH = 10f * scale
    val stemW = 6f  * scale
    val baseW = 22f * scale
    val baseH = 5f  * scale
    val goldColor = Color(0xFFFFB300)
    val goldDark = Color(0xFFE78318)

    // Shadow on shelf
    drawOval(
        color = Color.Black.copy(alpha = 0.15f),
        topLeft = Offset(x - baseW * 0.6f, floorY - 3f),
        size = Size(baseW * 1.2f, 8f)
    )
    // Base
    drawRoundRect(
        color = goldColor.copy(alpha = 0.85f),
        topLeft = Offset(x - baseW / 2, floorY - baseH),
        size = Size(baseW, baseH),
        cornerRadius = CornerRadius(2f)
    )
    // Stem
    drawRect(
        color = goldColor.copy(alpha = 0.9f),
        topLeft = Offset(x - stemW / 2, floorY - baseH - stemH),
        size = Size(stemW, stemH)
    )
    // Cup body
    val cupTop = floorY - baseH - stemH - cupH
    val cupPath = Path().apply {
        moveTo(x - cupW * 0.4f, cupTop)
        lineTo(x + cupW * 0.4f, cupTop)
        cubicTo(
            x + cupW * 0.55f, cupTop + cupH * 0.3f,
            x + cupW * 0.5f,  cupTop + cupH * 0.75f,
            x + cupW * 0.25f, cupTop + cupH
        )
        cubicTo(
            x + cupW * 0.1f,  cupTop + cupH * 1.08f,
            x - cupW * 0.1f,  cupTop + cupH * 1.08f,
            x - cupW * 0.25f, cupTop + cupH
        )
        cubicTo(
            x - cupW * 0.5f,  cupTop + cupH * 0.75f,
            x - cupW * 0.55f, cupTop + cupH * 0.3f,
            x - cupW * 0.4f,  cupTop
        )
        close()
    }
    drawPath(cupPath, color = goldColor)

    val sideFacePath = Path().apply {
        moveTo(x + cupW * 0.4f, cupTop)
        cubicTo(
            x + cupW * 0.55f, cupTop + cupH * 0.3f,
            x + cupW * 0.5f,  cupTop + cupH * 0.75f,
            x + cupW * 0.25f, cupTop + cupH
        )
        cubicTo(
            x + cupW * 0.45f, cupTop + cupH * 0.7f,
            x + cupW * 0.48f, cupTop + cupH * 0.25f,
            x + cupW * 0.32f, cupTop
        )
        close()
    }
    drawPath(sideFacePath, color = goldDark)

    // Cup highlight
    val highlightPath = Path().apply {
        moveTo(x - cupW * 0.3f, cupTop + 3f)
        lineTo(x - cupW * 0.05f, cupTop + 3f)
        lineTo(x - cupW * 0.1f,  cupTop + cupH * 0.5f)
        lineTo(x - cupW * 0.35f, cupTop + cupH * 0.5f)
        close()
    }
    drawPath(highlightPath, color = Color.White.copy(alpha = 0.25f))
    // Handles
    drawArc(
        color = goldColor.copy(alpha = 0.85f),
        topLeft = Offset(x + cupW * 0.35f, cupTop + cupH * 0.1f),
        size = Size(cupW * 0.3f, cupH * 0.6f),
        startAngle = -135f, sweepAngle = 270f,
        useCenter = false,
        style = Stroke(width = 3f * scale)
    )
    drawArc(
        color = goldColor.copy(alpha = 0.85f),
        topLeft = Offset(x - cupW * 0.65f, cupTop + cupH * 0.1f),
        size = Size(cupW * 0.3f, cupH * 0.6f),
        startAngle = 45f, sweepAngle = 270f,
        useCenter = false,
        style = Stroke(width = 3f * scale)
    )
}

private fun DrawScope.drawMedalModel(x: Float, floorY: Float) {
    val scale = 2f
    val medalR = 12f * scale
    val ribbonW = 16f * scale
    val ribbonH = 12f * scale
    val goldColor = Color(0xFFFFB300)
    val goldDark = Color(0xFFE78318)
    val ribbonColor = Color(0xFF1565C0)
    val ribbonColorDark = Color(0xFF0F519B)
    val medalThickness = medalR * 0.15f

    // Shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.12f),
        topLeft = Offset(x - medalR * 0.8f, floorY - 3f),
        size = Size(medalR * 1.6f + medalThickness, 6f)
    )

    translate(-medalThickness*0.8f, -0.5f * medalThickness) {
        rotate(23f, pivot = Offset(x + medalThickness, floorY - medalR)) {
            // Ribbon left strip
            drawRect(
                color = ribbonColor,
                topLeft = Offset(
                    x - ribbonW * 0.25f + medalThickness * 1.15f,
                    floorY
                ),
                size = Size(ribbonW * 0.5f, ribbonH)
            )
        }

        rotate(-18f, pivot = Offset(x + medalThickness, floorY - medalR)) {
            // Ribbon right strip
            drawRect(
                color = ribbonColorDark,
                topLeft = Offset(x-medalThickness*1.05f, floorY),
                size = Size(ribbonW * 0.5f, ribbonH)
            )
        }
    }

    // Medal circle
    drawCircle(
        color = goldDark,
        radius = medalR,
        center = Offset(x + medalThickness, floorY - medalR)
    )
    drawCircle(
        color = goldColor,
        radius = medalR,
        center = Offset(x, floorY - medalR)
    )
    // Medal inner ring
    drawCircle(
        color = Color.White.copy(alpha = 0.3f),
        radius = medalR * 0.65f,
        center = Offset(x, floorY - medalR),
        style = Stroke(width = 2f * scale)
    )
    // Medal highlight
    drawCircle(
        color = Color.White.copy(alpha = 0.35f),
        radius = medalR * 0.4f,
        center = Offset(x - medalR * 0.2f, floorY - medalR * 1.3f)
    )
}

private fun DrawScope.drawGrandTrophyModel(x: Float, floorY: Float) {
    val scale = 4.25f
    val goldColor = Color(0xFFFFB300)
    val goldDark = Color(0xFFE78318)

    // Base
    val base1W = 30f * scale
    val base1H = 5f * scale
    val base2W = 22f * scale
    val base2H = 3f * scale

    // Shadow
    drawOval(color = Color.Black.copy(alpha = 0.15f),
        topLeft = Offset(x - base1W * 0.6f, floorY - 3f),
        size = Size(base1W * 1.2f, 8f))

    drawRoundRect(color = goldDark,
        topLeft = Offset(x - base1W / 2, floorY - base1H),
        size = Size(base1W, base1H),
        cornerRadius = CornerRadius(3f))
    drawRoundRect(color = goldColor.copy(alpha = 0.9f),
        topLeft = Offset(x - base2W / 2, floorY - base1H - base2H),
        size = Size(base2W, base2H),
        cornerRadius = CornerRadius(2f))

    val barH = 14f * scale
    val barW = 5f * scale
    val ballR = 5f * scale
    val barTop = floorY - base1H - base2H - barH
    drawRect(color = goldColor.copy(alpha = 0.9f),
        topLeft = Offset(x - barW / 2, barTop),
        size = Size(barW, barH))
    drawCircle(color = goldDark, radius = ballR,
        center = Offset(x, barTop + barH * 0.5f))
    drawCircle(color = goldColor, radius = ballR * 0.65f,
        center = Offset(x, barTop + barH * 0.5f))

    val cupW = 32f * scale; val cupH = 28f * scale
    val cupTop = barTop - cupH

    // Right handle
    val rightHandleCenterX = x + cupW * 0.33f + cupW * 0.35f / 2
    val rightHandleCenterY = cupTop + cupH * 0.05f + cupH * 0.65f / 2
    rotate(degrees = 10f, pivot = Offset(rightHandleCenterX, rightHandleCenterY)) {
        drawArc(
            color = goldDark,
            topLeft = Offset(x + cupW * 0.33f, cupTop + cupH * 0.05f),
            size = Size(cupW * 0.35f, cupH * 0.65f),
            startAngle = -135f, sweepAngle = 275f, useCenter = false,
            style = Stroke(width = 3f * scale)
        )
    }

    // Left handle
    val leftHandleCenterX = x - cupW * 0.68f + cupW * 0.35f / 2
    val leftHandleCenterY = cupTop + cupH * 0.05f + cupH * 0.65f / 2
    rotate(degrees = -10f, pivot = Offset(leftHandleCenterX, leftHandleCenterY)) {
        drawArc(
            color = goldDark,
            topLeft = Offset(x - cupW * 0.68f, cupTop + cupH * 0.05f),
            size = Size(cupW * 0.35f, cupH * 0.65f),
            startAngle = 40f, sweepAngle = 275f, useCenter = false,
            style = Stroke(width = 3f * scale)
        )
    }

    // Body
    val cupPath = Path().apply {
        moveTo(x - cupW * 0.22f, cupTop)
        lineTo(x + cupW * 0.22f, cupTop)

        cubicTo(
            x + cupW * 0.45f, cupTop + cupH * 0.08f,
            x + cupW * 0.55f, cupTop + cupH * 0.2f,
            x + cupW * 0.5f,  cupTop + cupH * 0.35f
        )
        lineTo(x + cupW * 0.32f, cupTop + cupH * 0.62f)
        lineTo(x + cupW * 0.45f, cupTop + cupH * 0.88f)
        cubicTo(
            x + cupW * 0.32f, cupTop + cupH * 1.07f,
            x - cupW * 0.32f, cupTop + cupH * 1.07f,
            x - cupW * 0.45f, cupTop + cupH * 0.88f
        )
        lineTo(x - cupW * 0.32f, cupTop + cupH * 0.62f)
        lineTo(x - cupW * 0.5f,  cupTop + cupH * 0.35f)
        cubicTo(
            x - cupW * 0.55f, cupTop + cupH * 0.2f,
            x - cupW * 0.45f, cupTop + cupH * 0.08f,
            x - cupW * 0.22f, cupTop
        )
        close()
    }
    drawPath(cupPath, color = goldColor)

    // Depth
    val sidePath = Path().apply {
        moveTo(x + cupW * 0.22f, cupTop)
        cubicTo(
            x + cupW * 0.45f, cupTop + cupH * 0.08f,
            x + cupW * 0.55f, cupTop + cupH * 0.2f,
            x + cupW * 0.5f,  cupTop + cupH * 0.35f
        )
        lineTo(x + cupW * 0.32f, cupTop + cupH * 0.62f)
        lineTo(x + cupW * 0.45f, cupTop + cupH * 0.88f)

        cubicTo(
            x + cupW * 0.36f, cupTop + cupH * 0.82f,
            x + cupW * 0.32f, cupTop + cupH * 0.68f,
            x + cupW * 0.26f, cupTop + cupH * 0.58f
        )
        lineTo(x + cupW * 0.42f, cupTop + cupH * 0.32f)
        cubicTo(
            x + cupW * 0.46f, cupTop + cupH * 0.18f,
            x + cupW * 0.36f, cupTop + cupH * 0.06f,
            x + cupW * 0.18f, cupTop
        )
        close()
    }
    drawPath(sidePath, color = goldDark.copy(alpha = 0.55f))

    // Plate on front
    val plateW = cupW * 0.45f; val plateH = cupH * 0.3f
    drawRoundRect(color = goldDark.copy(alpha = 0.6f),
        topLeft = Offset(x - plateW / 2, cupTop + cupH * 0.6f),
        size = Size(plateW, plateH),
        cornerRadius = CornerRadius(4f))

    // Highlight
    drawOval(
        color = Color.White.copy(alpha = 0.3f),
        topLeft = Offset(x - cupW * 0.36f, cupTop + 4f),
        size = Size(cupW * 0.45f, cupH * 0.2f)
        )

    // Rim Shading
    drawOval(
        color = goldDark,
        topLeft = Offset(x - cupW * 0.25f, cupTop),
        size = Size(cupW * 0.5f, cupH * 0.1f)
    )
}

@Composable
fun ExchangeDisplay(
    currentPoints: Int,
    unlockedRoomItemIds: Set<String>,
    unlockedRoomThemeIds: Set<String>,
    unlockedWoodThemeIds: Set<String>,
    viewModel: RoomViewModel,
    onClose: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var activeTab by rememberSaveable {mutableStateOf("themesR")}

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
                Column {
                    Text(
                        "Spend Points", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$currentPoints points",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.weight(0.4f))

                FilledIconButton(
                    onClick = { activeTab = "themesR" },
                    colors = IconButtonColors(
                    if (activeTab != "themesR") MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    MaterialTheme.colorScheme.onSurface,
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.primaryContainer,)
                ) {
                    Icon(painterResource(R.drawable.room_theme_24px), contentDescription = "Room Themes")
                }
                FilledIconButton(
                    onClick = { activeTab = "themesW" },
                    colors = IconButtonColors(
                        if (activeTab != "themesW") MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.onSurface,
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(painterResource(R.drawable.shelves_24px), contentDescription = "Wood Themes")
                }
                FilledIconButton(
                    onClick = { activeTab = "items" },
                    colors = IconButtonColors(
                        if (activeTab != "items") MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.onSurface,
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(painterResource(R.drawable.floor_lamp_24px), contentDescription = "Items")
                }

                Spacer(modifier = Modifier.weight(0.6f))

                IconButton(onClick = onClose) {
                    Icon(painterResource(R.drawable.close_24px), contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Theme grid
            if (activeTab == "themesR") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Room Themes", style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        RoomThemeCatalog.allRoomThemes
                            .filter { theme -> !unlockedRoomThemeIds.contains(theme.id)}
                            .sortedBy { it.pointCost }
                    ) { theme ->
                        RoomThemeCell(
                            roomThemeOption = theme,
                            currentPoints = currentPoints,
                            onClick = {
                                if (theme.pointCost <= currentPoints) {
                                    viewModel.unlockRoomTheme(theme.id)
                                    viewModel.updatePoints(-theme.pointCost)
                                } else {
                                    displayAlert("You can't afford this.")
                                }
                            }
                        )
                    }
                }
            }

            if (activeTab == "themesW") {
                // Wood Theme grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Wood Themes", style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(WoodThemeCatalog.allWoodThemes
                        .filter { theme -> !unlockedWoodThemeIds.contains(theme.id)}
                        .sortedBy { it.pointCost }
                    ) { theme ->
                        WoodThemeCell(
                            woodThemeOption = theme,
                            currentPoints = currentPoints,
                            onClick = {
                                if (theme.pointCost <= currentPoints) {
                                    viewModel.unlockWoodTheme(theme.id)
                                    viewModel.updatePoints(-theme.pointCost)
                                } else {
                                    displayAlert("You can't afford this.")
                                }
                            }
                        )
                    }
                }
            }

            if (activeTab == "items") {
                // Item grid
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Items", style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(RoomItemCatalog.allAvailableRoomItems
                        .filter { item -> !unlockedRoomItemIds.contains(item.id)}
                        .sortedBy { it.pointCost }
                    ) { item ->
                        ItemCell(
                            item = item,
                            currentPoints = currentPoints,
                            onClick = {
                                viewModel.unlockRoomItem(item.id)
                                viewModel.updatePoints(-item.pointCost)
                            }
                        )
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun ItemCell(
    item: RoomItem,
    currentPoints: Int,
    onClick: () -> Unit) {
    Box {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.625f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painterResource(item.icon),
                    contentDescription = item.name,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    item.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.weight(0.5f))

                Text(
                    text = "${item.pointCost} points",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.weight(0.5f))

                FilledTonalButton(
                    onClick = onClick,
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (item.pointCost <= currentPoints) MaterialTheme.colorScheme.primary.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(
                        "Buy",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
fun RoomThemeCell(
    roomThemeOption: RoomThemeOption,
    currentPoints: Int,
    onClick: () -> Unit) {
    Box {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.625f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painterResource(R.drawable.room_theme_24px),
                    contentDescription = roomThemeOption.name,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp)) {
                    Row(modifier = Modifier. padding(6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            roomThemeOption.theme.wallColor,
                            roomThemeOption.theme.floorColor,
                            roomThemeOption.theme.accentColor
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
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    roomThemeOption.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
                Spacer(modifier = Modifier.weight(0.5f))

                Text(
                    text = "${roomThemeOption.pointCost} points",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.weight(0.5f))

                FilledTonalButton(
                    onClick = onClick,
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (roomThemeOption.pointCost <= currentPoints) MaterialTheme.colorScheme.primary.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(
                        "Buy",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
fun WoodThemeCell(
    woodThemeOption: WoodThemeOption,
    currentPoints: Int,
    onClick: () -> Unit) {
    Box {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.575f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painterResource(R.drawable.shelves_24px),
                    contentDescription = woodThemeOption.name,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(8.dp)) {
                    Column {
                        Row(modifier = Modifier.padding(start = 6.dp, top = 6.dp, end = 6.dp, bottom = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(
                                woodThemeOption.theme.woodFront,
                                woodThemeOption.theme.woodTop
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

                        Row(modifier = Modifier.padding(start = 6.dp, bottom = 6.dp, end = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(
                                woodThemeOption.theme.woodSide,
                                woodThemeOption.theme.woodDark
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
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    woodThemeOption.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )
                Spacer(modifier = Modifier.weight(0.5f))

                Text(
                    text = "${woodThemeOption.pointCost} points",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.weight(0.5f))

                FilledTonalButton(
                    onClick = onClick,
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (woodThemeOption.pointCost <= currentPoints) MaterialTheme.colorScheme.primary.copy(alpha = 0.65f) else MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f),
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(
                        "Buy",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementsDisplay(
    allAchievements: List<Achievement>,
    unlockedAchievementIds: Set<String>,
    placedAchievements: Map<String, String>,
    viewModel: RoomViewModel,
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
                Column {
                    Text("Trophy Case", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                    Text("${unlockedAchievementIds.size} / ${allAchievements.size} unlocked",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = onClose) {
                    Icon(painterResource(R.drawable.close_24px), contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Trophy grid -- Will add categories later
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(AchievementCatalog.allAchievements) { achievement ->
                    val isOnShelf = placedAchievements.containsValue(achievement.id)
                    val hasSpace = viewModel.hasShelfSpace(achievement)
                    val isUnlocked = achievement.id in unlockedAchievementIds
                    TrophyCell(
                        achievement = achievement,
                        isOnShelf = isOnShelf,
                        isUnlocked = isUnlocked,
                        canPlace = !isOnShelf && hasSpace,
                        onPlace = {
                            if (isUnlocked) {
                                if (hasSpace) viewModel.placeAchievement(achievement.id)
                                else displayAlert("No ${achievement.size.name.lowercase()} slots available")
                            } else {
                                displayAlert("This trophy is locked.")
                            }},
                        onRemove = { viewModel.removeAchievement(achievement.id) }
                    )
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun TrophyCell(
    achievement: Achievement,
    isUnlocked: Boolean,
    isOnShelf: Boolean = false,
    canPlace: Boolean = false,
    onPlace: () -> Unit = {},
    onRemove: () -> Unit = {}) {
    Box {
        Card(
            shape = RoundedCornerShape(16.dp),
            border = if (isOnShelf) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
            colors = CardDefaults.cardColors(
                containerColor = if (isUnlocked)
                    MaterialTheme.colorScheme.primaryContainer
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.625f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val icon = when(achievement.size) {
                    AchievementSize.Large -> R.drawable.trophy_dot_24px
                    AchievementSize.Medium -> R.drawable.trophy_24px
                    AchievementSize.Small -> R.drawable.medal_24px
                }
                val size = when(achievement.size) {
                    AchievementSize.Large -> 42.dp
                    AchievementSize.Medium -> 38.dp
                    AchievementSize.Small -> 36.dp
                }
                Icon(
                    painterResource(icon),
                    contentDescription = null,
                    tint = if (isUnlocked) Color(0xFFFFB300)
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                    modifier = Modifier.size(size)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    achievement.title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = if (isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.weight(0.5f))
                Text(
                    achievement.description,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (isUnlocked) 1f else 0.4f
                    )
                )

                Spacer(modifier = Modifier.weight(0.5f))

                if (isOnShelf) {
                    FilledTonalButton(
                        onClick = onRemove,
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.35f)
                        )
                    ) {
                        Text("Remove", style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    FilledTonalButton(
                        onClick = onPlace,
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isUnlocked)
                                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.3f)
                            else
                                MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Text(
                            if (!isUnlocked) "Locked"
                            else if (canPlace) "Place"
                            else "Full",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
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
                tint = MaterialTheme.colorScheme.onSurface
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



@Composable
fun FloatingPanel(
    modifier: Modifier = Modifier,
    icon: Int,
    label: String,
    onClick: () -> Unit = {},
    isActive: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = modifier.width(90.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f)
            else
                MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
        ),
        // Causes a weird box underneath the buttons when they're translucent
        //elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun AchievementsPanel(onClick: () -> Unit = {}) {
    FloatingPanel(icon = R.drawable.trophy_24px, label = "Achievements", onClick = onClick)
}

@Composable
fun CustomizePanel(isActive: Boolean = false, onClick: () -> Unit = {}) {
    FloatingPanel(icon = R.drawable.palette_24px, label = "Customize", onClick = onClick, isActive = isActive)
}

@Composable
fun ExchangePanel(onClick: () -> Unit = {}, points: Int, roomMode: RoomMode) {
    var bobbing by remember { mutableStateOf(false) }
    val offsetY by animateFloatAsState(
        targetValue = if (bobbing) -3f else 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )

    LaunchedEffect(Unit) { bobbing = true }

    Box(contentAlignment = Alignment.TopCenter) {
        if (roomMode == RoomMode.View && points > 0) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .offset(y = offsetY.dp - 32.dp)
                    .zIndex(1f)
                    .align(Alignment.TopCenter)
            ) {
                Text(
                    text = "$points pts",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        FloatingPanel(
            icon = R.drawable.redeem_24px,
            label = "Exchange",
            onClick = onClick
        )
    }
}

@Composable
fun NameHeader(mode: RoomMode, state: RoomState, viewModel: RoomViewModel) {
    var textValue by remember(state.displayName) { mutableStateOf(state.displayName) }

    Card(
        modifier = Modifier.padding(6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (mode == RoomMode.Edit) {
                TextField(
                    value = textValue,
                    onValueChange = {textValue = it
                        viewModel.updateDisplayName(it)},
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Text(
                    state.displayName,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(6.dp),
                    textAlign = TextAlign.Center
                )
            }
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

@Preview
@Composable
fun RoomLayoutPreview(){
    val theme = RoomTheme(wallColor = Color(0xFF606791),
    floorColor = Color(0xFF403E4B),
    accentColor = Color(0xFF536285))
    val woodTheme = WoodTheme(woodFront = Color(0xFF8B5E3C),
    woodTop = Color(0xFFA0714F),
    woodSide = Color(0xFF6B4226),
    woodDark = Color(0xFF4E2E14))
    //DefaultRoomCanvas(theme, shelfSlots = defaultShelfSlots(), woodTheme = woodTheme)
}