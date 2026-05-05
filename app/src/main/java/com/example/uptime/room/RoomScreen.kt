package com.example.uptime.room

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowSizeClass
import com.example.uptime.profile.FriendProfile
import com.example.uptime.R
import com.example.uptime.auth.AuthViewModel
import com.example.uptime.profile.UserProfileOverlay
import com.example.uptime.room.catalogs.AchievementCatalog
import com.example.uptime.room.catalogs.MetalThemeCatalog
import com.example.uptime.room.catalogs.RoomAnchorCatalog
import com.example.uptime.room.catalogs.RoomItemCatalog
import com.example.uptime.room.catalogs.RoomLayoutCatalog
import com.example.uptime.room.catalogs.RoomThemeCatalog
import com.example.uptime.room.catalogs.TrophyCaseCatalog
import com.example.uptime.room.catalogs.WoodThemeCatalog
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlin.Int
import kotlin.collections.filter
import kotlin.collections.find
import kotlin.collections.forEachIndexed
import kotlin.math.absoluteValue

// Placeholder data for now
data class RoomItem(
    val id: String,
    val name: String,
    val icon: Int,
    val category: RoomItemCategory,
    val pointCost: Int,
    val widthFraction: Float = 0.15f,
    val heightFraction: Float = 0.20f
)

enum class RoomMode { View, Edit, Visit }

enum class RoomItemCategory { Floor, Wall, Floating }

data class RoomItemAnchor(
    val id: String,
    val category: RoomItemCategory,
    val xFraction: Float,
    val yFraction: Float
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
    val pointCost: Int = 0
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val tier: AchievementTier = AchievementTier.Bronze,
    val category: AchievementCategory = AchievementCategory.Streak,
    val size: AchievementSize = AchievementSize.Small
)

enum class AchievementTier {
    Bronze,
    Silver,
    Gold,
    Diamond
}

enum class AchievementCategory {
    Streak,
    WalkingTime,
    ScreenTime,
    Exchange,
    Special,
    Secret
}

data class MetalTheme(
    val base: Color = Color(0xFFFFB300),
    val dark: Color = Color(0xFFE78318),
    val highlight: Color = Color(0xFFF8E8C9)
)

data class MetalThemeOption(
    val tier: AchievementTier,
    val theme: MetalTheme
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

enum class RoomPanel { Achievements, Exchange, Visit }

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

enum class AchievementSize { Small, Medium, Large }

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun RoomScreen(
    roomViewModel: RoomViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel(),
    onVisitRandomRoom: () -> Unit,
    onVisitUserRoom: (String) -> Unit,
    onReturn: () -> Unit
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isShortLandscape = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
            && !windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)
    val state by roomViewModel.roomState.collectAsState()

    if (state == null) {
        RoomLoadingScreen()
        return
    }

    val roomState = state!!

    val activeRoomTheme = RoomThemeCatalog.all
        .find { it.id == roomState.selectedRoomThemeId }?.theme ?: RoomTheme()

    val activeWoodTheme = WoodThemeCatalog.all
        .find { it.id == roomState.selectedWoodThemeId }?.theme ?: WoodTheme()

    val trophyCaseId = RoomLayoutCatalog.all
        .find { it.id == roomState.selectedRoomLayoutId }?.trophyCaseId ?: "default"

    var roomMode by rememberSaveable { mutableStateOf(RoomMode.View) }
    val isOwner = roomViewModel.userId == "me"
    var showVisitorProfile by remember { mutableStateOf(false) }
    var activePanel by rememberSaveable { mutableStateOf<RoomPanel?>(null) }
    var showRoomThemePicker by rememberSaveable { mutableStateOf(false) }
    var showWoodThemePicker by rememberSaveable { mutableStateOf(false) }
    var showRoomItemPicker by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(roomViewModel.userId) {
        if (isOwner) {roomMode = RoomMode.View; showVisitorProfile = false} else {roomMode = RoomMode.Visit;}
        Log.d("RoomScreen", "RoomMode is currently $roomMode")
        activePanel = null
    }

    val friends by roomViewModel.friendsRepository.observeFriends().collectAsState(emptyList())

    val activeTrophyCaseSlots = TrophyCaseCatalog.all
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
        RoomScaffold(
            roomState,
            activeRoomTheme,
            activeWoodTheme,
            activeTrophyCaseSlots,
            roomMode,
            roomViewModel
        )

        if (isShortLandscape) {
            // Side panel layout for short landscape
            if (roomMode != RoomMode.Edit && roomMode != RoomMode.Visit) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AchievementsPanel(onClick = { activePanel = if (activePanel == RoomPanel.Achievements) null else RoomPanel.Achievements })
                    ExchangePanel(
                        onClick = { activePanel = if (activePanel == RoomPanel.Exchange) null else RoomPanel.Exchange },
                        points = roomState.currentPoints,
                        roomMode = roomMode
                    )
                }
            }
            if (roomMode != RoomMode.Edit) {
                Column(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VisitPanel(onClick = { activePanel = if (activePanel == RoomPanel.Visit) null else RoomPanel.Visit })
                    if (roomMode == RoomMode.Visit) {
                        val visitingId = roomViewModel.userId
                        val isFriend = friends.any { it.uid == visitingId }
                        FriendPanel(isFriend = isFriend, onClick = { if(isFriend) roomViewModel.removeFriendById(visitingId) else roomViewModel.addFriendById(visitingId) })
                        ProfilePanel(onClick = { showVisitorProfile = true })
                        ReturnPanel(onClick = onReturn)
                    }
                }
            }
        } else {
        if (roomMode != RoomMode.Edit) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .padding(top = 36.dp), horizontalArrangement = Arrangement.End
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    VisitPanel(onClick = {
                        activePanel = if (activePanel == RoomPanel.Visit) null
                        else RoomPanel.Visit
                    })
                    if (roomMode == RoomMode.Visit) {
                        val visitingId = roomViewModel.userId
                        val isFriend = friends.any { it.uid == visitingId }
                        if (!authViewModel.state.collectAsState().value.isAnonymous) {
                            FriendPanel(
                                isFriend = isFriend,
                                onClick = {
                                    if (isFriend) roomViewModel.removeFriendById(visitingId) else roomViewModel.addFriendById(
                                        visitingId
                                    )
                                })
                        }
                        ProfilePanel(onClick = { showVisitorProfile = true })
                        ReturnPanel(onClick = onReturn)
                    }
                }
            }
        }
        if (roomMode != RoomMode.Visit) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(12.dp), horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                AchievementsPanel(onClick = {
                    activePanel = if (activePanel == RoomPanel.Achievements) null
                    else RoomPanel.Achievements
                })
                CustomizePanel(
                    isActive = roomMode == RoomMode.Edit,
                    onClick = {
                        roomMode = if (roomMode == RoomMode.Edit) RoomMode.View else RoomMode.Edit
                        showRoomThemePicker = false
                        showWoodThemePicker = false
                        showRoomItemPicker = false
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
                    EditToolButton(
                        icon = R.drawable.responsive_layout_24px,
                        label = "Layout",
                        onClick = {
                            showWoodThemePicker = false
                            showRoomItemPicker = false
                            showRoomThemePicker = false
                        })
                    EditToolButton(
                        icon = R.drawable.room_theme_24px,
                        label = "Theme",
                        isActive = showRoomThemePicker,
                        onClick = {
                            showWoodThemePicker = false
                            showRoomItemPicker = false
                            showRoomThemePicker = !showRoomThemePicker
                        })
                    EditToolButton(
                        icon = R.drawable.shelves_24px,
                        label = "Wood",
                        isActive = showWoodThemePicker,
                        onClick = {
                            showRoomThemePicker = false
                            showRoomItemPicker = false
                            showWoodThemePicker = !showWoodThemePicker
                        })
                    EditToolButton(
                        icon = R.drawable.package_2_24px,
                        label = "Items",
                        isActive = showRoomItemPicker,
                        onClick = {
                        showRoomThemePicker = false
                        showWoodThemePicker = false
                        showRoomItemPicker = !showRoomItemPicker
                    })
                }
            }

            AnimatedVisibility(
                visible = showRoomThemePicker && roomMode == RoomMode.Edit,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                RoomThemePickerRow(
                    allRoomThemes = RoomThemeCatalog.all,
                    unlockedRoomThemeIds = roomState.unlockedRoomThemeIds,
                    modifier = Modifier
                        .padding(bottom = 96.dp),
                    selectedThemeId = roomState.selectedRoomThemeId,
                    onThemeSelected = { themeId ->
                        roomViewModel.selectRoomTheme(themeId)
                    }
                )
            }

            AnimatedVisibility(
                visible = showWoodThemePicker && roomMode == RoomMode.Edit,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                WoodThemePickerRow(
                    allWoodThemes = WoodThemeCatalog.all,
                    unlockedWoodThemeIds = roomState.unlockedWoodThemeIds,
                    modifier = Modifier
                        .padding(bottom = 96.dp),
                    selectedThemeId = roomState.selectedWoodThemeId,
                    onThemeSelected = { themeId ->
                        roomViewModel.selectWoodTheme(themeId)
                    }
                )
            }

            AnimatedVisibility(
                visible = showRoomItemPicker && roomMode == RoomMode.Edit,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                RoomItemPickerRow(
                    viewModel = roomViewModel,
                    allRoomItems = RoomItemCatalog.all,
                    unlockedRoomItemIds = roomState.unlockedRoomItemIds,
                    modifier = Modifier
                        .padding(bottom = 96.dp),
                    placedRoomItems = roomState.placedRoomItems
                )
            }
        } else {
            AnimatedVisibility(
                visible = roomMode == RoomMode.Visit,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                VisitTrophyDisplay(roomState.placedAchievements)
            }
        }
    }
    if (showVisitorProfile && !roomViewModel.isOwner) {
            UserProfileOverlay(
                profile = roomViewModel.getFriendProfileById(userId = roomViewModel.userId),
                isFriend = friends.any { it.uid == roomViewModel.userId },
                onAddFriend = { roomViewModel.addFriendById(roomViewModel.userId) },
                onRemoveFriend = { roomViewModel.removeFriendById(roomViewModel.userId) },
                onVisitRoom = { showVisitorProfile = false },
                onDismiss = { showVisitorProfile = false },
                isAnon = authViewModel.state.collectAsState().value.isAnonymous
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
                allAchievements = AchievementCatalog.all,
                unlockedAchievementIds = roomState.unlockedAchievementIds,
                placedAchievements = roomState.placedAchievements,
                viewModel = roomViewModel,
                onClose = { activePanel = null }
            )
            RoomPanel.Exchange -> ExchangeDisplay(
                currentPoints = roomState.currentPoints,
                unlockedRoomThemeIds = roomState.unlockedRoomThemeIds,
                unlockedRoomItemIds = roomState.unlockedRoomItemIds,
                unlockedWoodThemeIds = roomState.unlockedWoodThemeIds,
                viewModel = roomViewModel,
                onClose = { activePanel = null }
            )
            RoomPanel.Visit -> VisitDisplay(
                friendsList = friends,
                authViewModel = authViewModel,
                onVisitRandomRoom = {onVisitRandomRoom(); activePanel = null;
                    Log.d("RoomScreen", "Visiting a random user's room")},
                onVisitUserRoom = onVisitUserRoom,
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
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(9f / 16f)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            RoomCanvas(
                activeRoomTheme = activeRoomTheme,
                placedAchievements = state.placedAchievements,
                placedRoomItems = state.placedRoomItems,
                layoutId = state.selectedRoomLayoutId,
                activeTrophyCaseSlots = activeTrophyCaseSlots,
                woodTheme = activeWoodTheme
            )
        }
    }

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

    if (mode == RoomMode.Visit) {
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
                modifier = Modifier.padding(top = 46.dp)
            ) {
                Text(
                    text = "Visiting",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }
    }

    NameHeader(mode, state, viewModel)

    ForDemo(viewModel, mode)
}

@Composable
fun ForDemo(viewModel: RoomViewModel, roomMode: RoomMode) {
    if (roomMode == RoomMode.View) {
        Button(
            onClick = { viewModel.updatePoints(50) },
            modifier = Modifier.padding(top = 450.dp, start = 12.dp),
            colors = ButtonColors(
                MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                contentColor = MaterialTheme.colorScheme.primaryContainer,
                disabledContainerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
            )
        ) {
            Text("For demo: + 50 pts", style = MaterialTheme.typography.labelSmall)
        }
    }
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

        Text("Loading the room...")
        Log.d("RoomScreen", "Loading the room...")
    }
}

@Composable
fun RoomThemePickerRow(
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
            RoomThemeCard(
                option = option,
                isUnlocked = isUnlocked,
                isSelected = option.id == selectedThemeId,
                onSelect = {if (isUnlocked) onThemeSelected(option.id)}
            )
        }
    }
}

@Composable
fun WoodThemePickerRow(
    allWoodThemes: List<WoodThemeOption>,
    unlockedWoodThemeIds: Set<String>,
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
        items(allWoodThemes) { option ->
            val isUnlocked = option.id in unlockedWoodThemeIds
            WoodThemeCard(
                option = option,
                isUnlocked = isUnlocked,
                isSelected = option.id == selectedThemeId,
                onSelect = {if (isUnlocked) onThemeSelected(option.id)}
            )
        }
    }
}

@Composable
fun RoomItemPickerRow(
    viewModel: RoomViewModel,
    allRoomItems: List<RoomItem>,
    unlockedRoomItemIds: Set<String>,
    modifier: Modifier,
    placedRoomItems: Map<String, String>
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        items(allRoomItems) { item ->
            val isUnlocked = item.id in unlockedRoomItemIds
            val isPlaced = item.id in placedRoomItems.values
            RoomItemCard(
                item = item,
                isUnlocked = isUnlocked,
                isPlaced = isPlaced,
                onSelect = {if (isPlaced && isUnlocked) viewModel.removeRoomItem(item.id) else if (isUnlocked) viewModel.placeRoomItem(item.id, item.category)}
            )
        }
    }
}

@Composable
fun RoomThemeCard(
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
fun WoodThemeCard(
    option: WoodThemeOption,
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
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Show theme's Colors
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    option.theme.woodFront,
                    option.theme.woodTop
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

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    option.theme.woodSide,
                    option.theme.woodDark
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
fun RoomItemCard(
    item: RoomItem,
    isUnlocked: Boolean,
    isPlaced: Boolean,
    onSelect: () -> Unit
) {
    Card(
        onClick = onSelect,
        shape = RoundedCornerShape(12.dp),
        border = if (isPlaced) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
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
            // Show item icon
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    painterResource(item.icon),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                item.name,
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
fun RoomCanvas(
    activeRoomTheme: RoomTheme,
    placedAchievements: Map<String, String>,
    placedRoomItems: Map<String, String>,
    layoutId: String,
    activeTrophyCaseSlots: List<TrophyCaseCatalog.ShelfSlot>,
    modifier: Modifier = Modifier,
    woodTheme: WoodTheme) {
    when (layoutId) {
        "default" -> DefaultRoomCanvas(activeRoomTheme, placedAchievements, placedRoomItems, activeTrophyCaseSlots, modifier, woodTheme)
    }
}

@Composable
fun DefaultRoomCanvas(
    theme: RoomTheme,
    placedAchievements: Map<String, String>,
    placedRoomItems: Map<String, String>,
    shelfSlots: List<TrophyCaseCatalog.ShelfSlot>,
    modifier: Modifier = Modifier,
    woodTheme: WoodTheme) {
    val transition = rememberInfiniteTransition(label = "medalShimmer")
    val pathData = "M422,728L629,480L469,480L498,253L313,520L452,520L422,728ZM343.08,857.69L383.08,580L198.08,580L527.69,104.23L576.92,104.23L537.31,420L757.3,420L392.31,857.69L343.08,857.69ZM471,490L471,490L471,490L471,490L471,490L471,490Z"
    val lightningPath = PathParser().parsePathString(pathData).toPath()

    val shimmerProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerWithDelay"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        clipRect(
            left = 0f,
            top = 0f,
            right = w,
            bottom = h
        ) {
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

            drawPlacedWallItems(
                placedRoomItems = placedRoomItems,
                woodTheme = woodTheme,
                canvasWidth = w,
                canvasHeight = h,
                iconPath = lightningPath
            )

            translate(left = w * 0.06f, top = h * -0.05f) {
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
                    size = Size(windowWidth - windowDepth / 2f, frameWidth * 1.55f)
                )

                // Window Pane
                drawRect(
                    color = Color(0xFFC1E3F3),
                    topLeft = Offset(windowX, windowY),
                    size = Size(windowWidth - windowDepth, windowHeight)
                )

                // Pane details
                val reflectionPath = Path().apply {
                    moveTo(windowX, windowY + windowHeight)
                    lineTo(windowX + windowWidth - frameWidth * 1.1f, windowY * 1.6f)
                    close()
                }
                drawPath(
                    reflectionPath,
                    color = windowTop.copy(alpha = 0.3f),
                    style = Stroke(width = 40f)
                )

                val reflectionPath2 = Path().apply {
                    moveTo(windowX * 1.2f, windowY + windowHeight)
                    lineTo(windowX + windowWidth - frameWidth, windowY * 2.7f)
                    close()
                }
                drawPath(
                    reflectionPath2,
                    color = windowTop.copy(alpha = 0.3f),
                    style = Stroke(width = 25f)
                )

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
                    lineTo(windowX + windowDepth, windowY + windowHeight + windowDepth)
                    close()
                }
                drawPath(leftSidePath, color = windowSide)

                // Top & Bottom
                for (i in 0..1) {
                    val y = when (i) {
                        0 -> 0f
                        1 -> windowHeight - frameWidth
                        else -> {
                            0f
                        }
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
                            topLeft = Offset(
                                windowX + windowDepth,
                                windowY + y + frameWidth + windowDepth
                            ),
                            size = Size(windowWidth - windowDepth * 2, frameWidth * 0.4f)
                        )
                    }
                }

                // Fill in Top
                val topFacePath = Path().apply {
                    moveTo(windowX - windowDepth * 0.5f, windowY + windowDepth)
                    lineTo(windowX + windowDepth - windowDepth * 0.5f, windowY)
                    lineTo(windowX + windowWidth - windowDepth + frameWidth, windowY)
                    lineTo(windowX + windowWidth - windowDepth * 0.5f, windowY + windowDepth)
                    close()
                }
                drawPath(topFacePath, color = windowTop)

                // Right Side
                // Front face
                drawRect(
                    color = windowFront,
                    topLeft = Offset(
                        windowX + windowWidth - windowDepth * 2,
                        windowY + windowDepth
                    ),
                    size = Size(frameWidth, windowHeight)
                )
                // Side face
                val rightSidePath = Path().apply {
                    moveTo(windowX + windowWidth - windowDepth / 2f, windowY + windowDepth)
                    lineTo(windowX + windowWidth + windowDepth / 2f, windowY)
                    lineTo(windowX + windowWidth + windowDepth / 2f, windowY + windowHeight)
                    lineTo(
                        windowX + windowWidth - windowDepth / 2f,
                        windowY + windowHeight + windowDepth
                    )
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
                    lineTo(shelfDepth, shelfHeight + shelfDepth)
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

            drawShelfTrophies(
                shelfSlots,
                placedAchievements,
                shelfWidth,
                shelfHeight,
                shelfDepth,
                shelfThickness,
                shelfSpacing,
                shimmerProgress,
                shelfOffsetX,
                shelfOffsetY
            )

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
                            topLeft = Offset(
                                rugWidth * detailInset - 4f,
                                rugHeight * detailInset + 15f
                            ),
                            size = Size(
                                rugWidth * (1f - detailInset * 2.2f),
                                rugHeight * (1f - detailInset * 2)
                            ),
                        )
                    }
                }
            }

            drawPlacedItems(
                placedRoomItems = placedRoomItems,
                woodTheme = woodTheme,
                canvasWidth = w,
                canvasHeight = h,
                iconPath = lightningPath
            )
        }
    }
}

private fun DrawScope.drawPlacedItems(
    placedRoomItems: Map<String, String>,
    woodTheme: WoodTheme,
    canvasWidth: Float,
    canvasHeight: Float,
    iconPath: Path
) {
    placedRoomItems.forEach { (anchorId, itemId) ->
        val anchor = RoomAnchorCatalog.all.find { it.id == anchorId }?: return@forEach
        val item = RoomItemCatalog.all.find { it.id == itemId && it.category != RoomItemCategory.Wall }?: return@forEach

        val cx = canvasWidth * anchor.xFraction
        val cy = canvasHeight * anchor.yFraction
        val iw = canvasWidth * item.widthFraction
        val ih = canvasHeight * item.heightFraction

        when (itemId) {
            "plant_pot" -> drawPottedPlant(cx, cy, iw, ih)
            "lamp" -> drawFloorLamp(cx, cy, iw, ih, woodTheme)
            else -> drawGenericItem(cx, cy, iw, ih)
        }
    }
}

private fun DrawScope.drawPlacedWallItems(
    placedRoomItems: Map<String, String>,
    woodTheme: WoodTheme,
    canvasWidth: Float,
    canvasHeight: Float,
    iconPath: Path
) {
    placedRoomItems.forEach { (anchorId, itemId) ->
        val anchor = RoomAnchorCatalog.all.find { it.id == anchorId }?: return@forEach
        val item = RoomItemCatalog.all.find { it.id == itemId && it.category == RoomItemCategory.Wall }?: return@forEach

        val cx = canvasWidth * anchor.xFraction
        val cy = canvasHeight * anchor.yFraction
        val iw = canvasWidth * item.widthFraction
        val ih = canvasHeight * item.heightFraction

        when (itemId) {
            "poster_uptime" -> drawUptimePoster(cx, cy, iw, ih, woodTheme, iconPath)
            "poster_band" -> drawBandPoster(cx, cy, iw, ih, woodTheme)
            "poster_movie" -> drawMoviePoster(cx, cy, iw, ih, woodTheme)
            else -> drawGenericItem(cx, cy, iw, ih)
        }
    }
}

private fun DrawScope.drawPottedPlant(
    cx: Float, baseY: Float,
    width: Float, height: Float
) {
    val potColor = Color(0xFFC1440E)
    val potDark = Color(0xFF8B3010)
    val soilColor = Color(0xFF4A3728)
    val stemColor = Color(0xFF4A7C59)
    val leafColor = Color(0xFF5A9A6A)
    val leafDark = Color(0xFF3D7A50)

    val potBottom = baseY
    val potTop = baseY - height * 0.3f
    val potW = width * 0.7f

    // Shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.12f),
        topLeft = Offset(cx - potW * 0.575f, potBottom - height * 0.03f),
        size = Size(potW * 1.15f, height * 0.125f)
    )

    drawOval(
        color = potColor,
        topLeft = Offset(cx - potW * 0.5f, potBottom - height * 0.05f),
        size = Size(potW, height * 0.1f)
    )
    // Pot
    val potPath = Path().apply {
        moveTo(cx - potW * 0.35f, potTop)
        lineTo(cx + potW * 0.35f, potTop)
        lineTo(cx + potW * 0.5f,  potBottom)
        lineTo(cx - potW * 0.5f,  potBottom)
        close()
    }
    drawPath(potPath, color = potColor)

    // Pot side shading
    val potShadePath = Path().apply {
        moveTo(cx + potW * 0.35f, potTop)
        lineTo(cx + potW * 0.5f,  potBottom)
        lineTo(cx + potW * 0.3f,  potBottom  + height * 0.025f)
        lineTo(cx + potW * 0.2f,  potTop)
        close()
    }
    drawPath(potShadePath, color = potDark.copy(alpha = 0.5f))

    // Pot rim
    drawRoundRect(
        color = potDark,
        topLeft = Offset(cx - potW * 0.4f, potTop - height * 0.04f),
        size = Size(potW * 0.8f, height * 0.07f),
        cornerRadius = CornerRadius(3f)
    )
    drawOval(
        color = potDark,
        topLeft = Offset(cx - potW * 0.4f, potTop - height * 0.02f),
        size = Size(potW * 0.8f, height * 0.07f)
    )
    drawOval(
        color = potColor,
        topLeft = Offset(cx - potW * 0.4f, potTop - height * 0.08f),
        size = Size(potW * 0.8f, height * 0.1f)
    )
    // Rim top highlight
    drawRoundRect(
        color = potColor.copy(alpha = 0.6f),
        topLeft = Offset(cx - potW * 0.4f, potTop - height * 0.04f),
        size = Size(potW * 0.8f, height * 0.025f),
        cornerRadius = CornerRadius(3f)
    )

    // Soil
    drawOval(
        color = soilColor,
        topLeft = Offset(cx - potW * 0.32f, potTop - height * 0.07f),
        size = Size(potW * 0.64f, height * 0.05f)
    )

    // Stem
    drawLine(
        color = stemColor,
        start = Offset(cx, potTop - height * 0.04f),
        end = Offset(cx, potTop - height * 0.35f),
        strokeWidth = width * 0.06f
    )

    // Leaves
    listOf(
        Triple(cx - width * 0.23f, potTop - height * 0.2f,  -30f),
        Triple(cx + width * 0.18f, potTop - height * 0.28f,  25f),
    ).forEach { (lx, ly, angle) ->
        rotate(degrees = angle, pivot = Offset(lx, ly)) {
            drawOval(
                color = leafColor,
                topLeft = Offset(lx - width * 0.18f, ly - height * 0.1f),
                size = Size(width * 0.46f, height * 0.17f)
            )
            drawLine(
                color = leafDark,
                start = Offset(lx - width * 0.1f, ly * 0.995f),
                end = Offset(lx + width * 0.22f, ly * 0.995f),
                strokeWidth = 1.5f
            )
        }
    }
}

private fun DrawScope.drawFloorLamp(
    cx: Float, baseY: Float,
    width: Float, height: Float,
    woodTheme: WoodTheme
) {
    val metalColor = Color(0xFFB0BEC5)
    val metalDark = Color(0xFF78909C)
    val shadeColor = Color(0xFFFFF9C4)
    val shadeDark = Color(0xFFE6CC6A)

    val shadeTop = baseY - height
    val shadeH = height * 0.2f
    val poleTopY = shadeTop + shadeH
    val poleW = width * 0.1f
    val shadeW = width * 0.85f

    val ovalDepth = shadeW * 0.13f

    // Shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.12f),
        topLeft = Offset(cx - width * 0.4f, baseY - 3f),
        size = Size(width * 0.8f, 15f)
    )

    // Base
    val baseTopY    = baseY - height * 0.07f
    val baseHalfTop = width * 0.25f
    val baseHalfBot = width * 0.35f

    val basePath = Path().apply {
        moveTo(cx - baseHalfTop, baseTopY)
        lineTo(cx + baseHalfTop, baseTopY)
        lineTo(cx + baseHalfBot, baseY)
        // Curved bottom edge
        cubicTo(
            cx + baseHalfBot * 0.5f, baseY + ovalDepth * 0.6f,
            cx - baseHalfBot * 0.5f, baseY + ovalDepth * 0.6f,
            cx - baseHalfBot, baseY
        )
        close()
    }
    drawPath(basePath, color = metalDark)
    drawOval(
        color   = metalColor,
        topLeft = Offset(cx - baseHalfTop, baseTopY - ovalDepth * 0.4f),
        size    = Size(baseHalfTop * 2f, ovalDepth * 0.8f)
    )
    drawOval(
        color   = Color.White.copy(alpha = 0.25f),
        topLeft = Offset(cx - baseHalfTop * 0.7f, baseTopY - ovalDepth * 0.35f),
        size    = Size(baseHalfTop * 1.4f, ovalDepth * 0.35f)
    )

    // Pole
    drawRoundRect(
        color = metalColor,
        topLeft = Offset(cx - poleW / 2f, poleTopY),
        size = Size(poleW, baseTopY - poleTopY),
        cornerRadius = CornerRadius(poleW / 2f)
    )
    // Pole highlight
    drawRoundRect(
        color = Color.White.copy(alpha = 0.3f),
        topLeft = Offset(cx - poleW / 2f, poleTopY),
        size = Size(poleW * 0.3f, baseTopY - poleTopY),
        cornerRadius = CornerRadius(poleW / 2f)
    )


    //  Shade bottom metal
    drawOval(
        color   = metalDark,
        topLeft = Offset(cx - shadeW * 0.546f, shadeTop + shadeH - ovalDepth),
        size    = Size(shadeW * 1.092f, ovalDepth * 2.175f)
    )

    //  Shade
    val shadePath = Path().apply {
        moveTo(cx - shadeW * 0.3f, shadeTop)
        lineTo(cx + shadeW * 0.3f, shadeTop)
        lineTo(cx + shadeW * 0.5f, shadeTop + shadeH)
        // Curved bottom edge
        cubicTo(
            cx + shadeW * 0.25f, shadeTop + shadeH + ovalDepth,
            cx - shadeW * 0.25f, shadeTop + shadeH + ovalDepth,
            cx - shadeW * 0.5f, shadeTop + shadeH
        )
        close()
    }
    drawPath(shadePath, color = shadeColor)

    // Shade side face
    val shadeSidePath = Path().apply {
        moveTo(cx + shadeW * 0.3f, shadeTop)
        lineTo(cx + shadeW * 0.5f, shadeTop + shadeH)
        lineTo(cx + shadeW * 0.38f, shadeTop + shadeH)
        lineTo(cx + shadeW * 0.22f, shadeTop)
        close()
    }
    drawPath(shadeSidePath, color = shadeDark.copy(alpha = 0.45f))

    drawOval(
        color   = shadeDark.copy(alpha = 0.7f),
        topLeft = Offset(cx - shadeW * 0.3f, shadeTop - ovalDepth * 0.9f),
        size    = Size(shadeW * 0.6f, ovalDepth * 1.1f)
    )
    drawOval(
        color = metalDark,
        topLeft = Offset(cx - shadeW * 0.32f, shadeTop - ovalDepth * 0.85f),
        size    = Size(shadeW * 0.64f, ovalDepth * 1.15f)
    )
    // Inner opening highlight
    drawOval(
        color   = Color.Black.copy(alpha = 0.3f),
        topLeft = Offset(cx - shadeW * 0.18f, shadeTop - ovalDepth * 0.54f),
        size    = Size(shadeW * 0.36f, ovalDepth * 0.7f)
    )
}

private fun DrawScope.drawUptimePoster(
    cx: Float, cy: Float,
    width: Float, height: Float,
    woodTheme: WoodTheme,
    iconPath: Path
) {
    val lightningPath = iconPath
    val bounds = lightningPath.getBounds()

    val left = cx - width / 2f
    val top = cy - height / 2f
    val right = cx + width / 2f
    val bottom = cy + height / 2f

    val primary = Color(0xFF359B60)

    // Frame Shadow
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.18f),
        topLeft = Offset(left + 3f, top + 3f),
        size = Size(width, height),
        cornerRadius = CornerRadius(4f)
    )

    // Frame
    val frameThickness = width * 0.06f
    drawRoundRect(
        color = woodTheme.woodFront,
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(4f)
    )
    // Frame top
    drawRoundRect(
        color = woodTheme.woodTop,
        topLeft = Offset(left, top),
        size = Size(width, frameThickness * 0.5f),
        cornerRadius = CornerRadius(2f)
    )

    // Poster background
    drawRoundRect(
        color = primary,
        topLeft = Offset(left + frameThickness, top + frameThickness),
        size = Size(width - frameThickness * 2, height - frameThickness * 2),
        cornerRadius = CornerRadius(2f)
    )

    val logoR = width * 0.22f
    val logoX = cx
    val logoY = cy - height * 0.08f

    // Circle background
    drawCircle(
        color = Color.White.copy(alpha = 0.15f),
        radius = logoR * 1.15f,
        center = Offset(logoX, logoY)
    )
    drawCircle(
        color = Color.White,
        radius = logoR,
        center = Offset(logoX, logoY)
    )

    val iconScale = (logoR * 1.4f) / bounds.height
    withTransform({
        translate(logoX, logoY)
        scale(iconScale, iconScale, pivot = Offset.Zero)
        translate(-bounds.center.x, -bounds.center.y)
    }) {
        drawPath(
            path = lightningPath,
            color = Color(0xFF9BD2A9)
        )
    }

    val textY = logoY + logoR + height * 0.1f
    val textW = width * 0.55f
    val barH = height * 0.055f

    // Title bar
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(cx - textW / 2f, textY),
        size = Size(textW, barH),
        cornerRadius = CornerRadius(barH / 2f)
    )
    // Subtitle bar
    drawRoundRect(
        color = Color.White.copy(alpha = 0.55f),
        topLeft = Offset(cx - textW * 0.35f, textY + barH + height * 0.03f),
        size = Size(textW * 0.7f, barH * 0.55f),
        cornerRadius = CornerRadius(barH / 2f)
    )

    // Hanging wire at top
    val wireY = top - height * 0.035f
    drawLine(
        color = woodTheme.woodDark,
        start = Offset(left + width * 0.3f, top),
        end = Offset(cx - width * 0.05f, wireY),
        strokeWidth = 2.5f
    )
    drawLine(
        color = woodTheme.woodDark,
        start = Offset(right - width * 0.3f, top),
        end = Offset(cx + width * 0.05f, wireY),
        strokeWidth = 2.5f
    )
    // Nail
    drawCircle(
        color = woodTheme.woodDark,
        radius = 3.5f,
        center = Offset(cx, wireY)
    )
}

private fun DrawScope.drawBandPoster(
    cx: Float, cy: Float,
    width: Float, height: Float,
    woodTheme: WoodTheme
) {
    val left = cx - width / 2f
    val top = cy - height / 2f
    val right = cx + width / 2f
    val bottom = cy + height / 2f

    val bg = Color(0xFF28283F)
    val accent1 = Color(0xFF009688)
    val accent2 = Color(0xFF8BC34A)

    val frameThickness = width * 0.06f

    // Shadow
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.18f),
        topLeft = Offset(left + 3f, top + 3f),
        size = Size(width, height),
        cornerRadius = CornerRadius(4f)
    )

    clipRect(
        top = top,
        left = left,
        bottom = bottom,
        right = right) {
        // Frame
        drawRoundRect(
            color = woodTheme.woodFront,
            topLeft = Offset(left, top),
            size = Size(width, height),
            cornerRadius = CornerRadius(4f)
        )
        // Frame top
        drawRoundRect(
            color = woodTheme.woodTop,
            topLeft = Offset(left, top),
            size = Size(width, frameThickness * 0.5f),
            cornerRadius = CornerRadius(2f)
        )

        // Poster background
        drawRoundRect(
            color = bg,
            topLeft = Offset(left + frameThickness, top + frameThickness),
            size = Size(width - frameThickness * 2, height - frameThickness * 2),
            cornerRadius = CornerRadius(2f)
        )

        val innerLeft = left + frameThickness
        val innerTop = top + frameThickness
        val innerWidth = width - frameThickness * 2
        val innerHeight = height - frameThickness * 2

        val stripeWidth = innerWidth * 0.25f
        for (i in 0..3) {
            if (i == 0 || i == 3) {
                drawRect(
                    color = if (i % 2 == 0) accent1 else accent2,
                    topLeft = Offset(
                        innerLeft + i * stripeWidth,
                        innerTop
                    ),
                    size = Size(stripeWidth, innerHeight),
                    alpha = 0.3f
                )
            }
        }

        // Central abstract "band logo" shape (circle + cut)
        val center = Offset(cx, cy - innerHeight * 0.1f)
        val r = innerWidth * 0.18f

        drawCircle(
            color = Color.White,
            radius = r,
            center = center
        )

        drawRect(
            color = bg,
            topLeft = Offset(center.x - r, center.y * 1.01f),
            size = Size(r * 2, r)
        )

        // Bottom text bars
        val barW = innerWidth * 0.6f
        val barH = innerHeight * 0.05f
        val baseY = innerTop + innerHeight * 0.7f

        drawRoundRect(
            color = Color.White,
            topLeft = Offset(cx - barW / 2, baseY),
            size = Size(barW, barH),
            cornerRadius = CornerRadius(barH / 2)
        )

        drawRoundRect(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Offset(cx - barW * 0.35f, baseY + barH + 10f),
            size = Size(barW * 0.7f, barH * 0.6f),
            cornerRadius = CornerRadius(barH / 2)
        )
    }
}

private fun DrawScope.drawMoviePoster(
    cx: Float, cy: Float,
    width: Float, height: Float,
    woodTheme: WoodTheme
) {
    val left = cx - width / 2f
    val top = cy - height / 2f
    val right = cx + width / 2f
    val bottom = cy + height / 2f

    val frameThickness = width * 0.06f

    val sky = Color(0xFFF6FAFA)
    val water = Color(0xFF0078A7)

    // Shadow
    drawRoundRect(
        color = Color.Black.copy(alpha = 0.18f),
        topLeft = Offset(left + 3f, top + 3f),
        size = Size(width, height),
        cornerRadius = CornerRadius(4f)
    )

    // Frame
    drawRoundRect(
        color = woodTheme.woodFront,
        topLeft = Offset(left, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(4f)
    )
    // Frame top
    drawRoundRect(
        color = woodTheme.woodTop,
        topLeft = Offset(left, top),
        size = Size(width, frameThickness * 0.5f),
        cornerRadius = CornerRadius(2f)
    )

    val innerLeft = left + frameThickness
    val innerRight = right - frameThickness
    val innerTop = top + frameThickness
    val innerWidth = width - frameThickness * 2
    val innerHeight = height - frameThickness * 2
    val innerBottom = bottom - frameThickness

    clipRect(
        top = innerTop,
        left = innerLeft,
        right = innerRight,
        bottom = innerBottom
    )
    {
        // Sky background
        drawRoundRect(
            color = sky,
            topLeft = Offset(innerLeft, innerTop),
            size = Size(innerWidth, innerHeight),
            cornerRadius = CornerRadius(2f)
        )

        // Horizon
        val horizonY = innerTop + innerHeight * 0.275f

        drawRect(
            color = water.copy(alpha = 0.4f),
            topLeft = Offset(innerLeft, horizonY),
            size = Size(innerWidth, innerHeight - (horizonY - innerTop))
        )


        // Shark
        val peakHeight = innerHeight * 0.56f
        drawCircle(
            color = Color.Black,
            center = Offset(cx - innerWidth * 0.2f, (innerBottom - peakHeight*0.4f)),
            radius = innerWidth * 0.1f
        )
        drawCircle(
            color = Color.Black,
            center = Offset(cx + innerWidth * 0.2f, (innerBottom - peakHeight*0.4f)),
            radius = innerWidth * 0.1f
        )

        drawPath(
            path = Path().apply {
                moveTo(cx - innerWidth * 0.35f, innerBottom)
                lineTo(cx - innerWidth * 0.25f, innerBottom - peakHeight*0.52f)
                lineTo(cx, innerBottom - peakHeight)
                lineTo(cx + innerWidth * 0.25f, innerBottom - peakHeight*0.52f)
                lineTo(cx + innerWidth * 0.35f, innerBottom)
                close()
            },
            color = Color(0xFF9BA1A4)
        )

        drawOval(
            color = Color.Black,
            size = Size(innerWidth * 0.4f, innerHeight * 0.44f),
            topLeft = Offset(cx - innerWidth * 0.2f, innerBottom - innerHeight * 0.25f),
        )

        drawOval(
            color = Color(0xFF9BA1A4),
            size = Size(innerWidth * 0.44f, innerHeight * 0.37f),
            topLeft = Offset(cx - innerWidth * 0.22f, innerBottom - innerHeight * 0.17f),
        )

        // Title bar
        val barW = innerWidth * 0.7f
        val barH = innerHeight * 0.125f

        val textY = innerTop + innerHeight * 0.075f

        drawRoundRect(
            color = Color.Red,
            topLeft = Offset(cx - barW / 2, textY),
            size = Size(barW, barH),
            cornerRadius = CornerRadius(barH / 2)
        )

        drawRect(
            color = water.copy(alpha = 0.2f),
            topLeft = Offset(innerLeft, horizonY),
            size = Size(innerWidth, innerHeight - (horizonY - innerTop))
        )
    }
}

private fun DrawScope.drawGenericItem(cx: Float, cy: Float, width: Float, height: Float) {
    drawRoundRect(
        color = Color.Gray.copy(alpha = 0.4f),
        topLeft = Offset(cx - width / 2f, cy - height / 2f),
        size = Size(width, height),
        cornerRadius = CornerRadius(4f)
    )
}

private fun DrawScope.drawShelfTrophies(
    slots: List<TrophyCaseCatalog.ShelfSlot>,
    placedAchievements: Map<String, String>,
    shelfWidth: Float,
    shelfHeight: Float,
    shelfDepth: Float,
    shelfThickness: Float,
    shelfSpacing: Float,
    shimmerProgress: Float,
    shelfOffsetX: Float,
    shelfOffsetY: Float
) {
    val innerWidth = shelfWidth - shelfDepth * 4
    val innerLeft = shelfDepth * 2 + shelfOffsetX

    val topSlots = slots.filter { it.section == TrophyCaseCatalog.ShelfSection.TopRow }
    val mid1Slots = slots.filter { it.section == TrophyCaseCatalog.ShelfSection.MidRow1 }
    val mid2Slots = slots.filter { it.section == TrophyCaseCatalog.ShelfSection.MidRow2 }
    val bottomSlots = slots.filter { it.section == TrophyCaseCatalog.ShelfSection.BottomRow }

    val topShelfFloor = 0.5f * shelfThickness + shelfOffsetY
    val mid1ShelfFloor = shelfSpacing + 0.5f * shelfThickness + shelfOffsetY
    val bottomShelfFloor = shelfHeight - 0.5f * shelfThickness + shelfOffsetY

    drawSectionTrophies(topSlots, placedAchievements, innerLeft, innerWidth, topShelfFloor, isLargeSection = true, shimmerProgress)
    drawSectionTrophies(mid1Slots, placedAchievements, innerLeft, innerWidth, mid1ShelfFloor, isLargeSection = false, shimmerProgress)
    drawSectionTrophies(mid2Slots, placedAchievements, innerLeft, innerWidth, mid1ShelfFloor + shelfSpacing, isLargeSection = false, shimmerProgress)
    drawSectionTrophies(bottomSlots, placedAchievements, innerLeft, innerWidth, bottomShelfFloor, isLargeSection = true, shimmerProgress)
}

private fun DrawScope.drawSectionTrophies(
    slots: List<TrophyCaseCatalog.ShelfSlot>,
    placedAchievements: Map<String, String>,
    innerLeft: Float,
    innerWidth: Float,
    shelfFloorY: Float,
    isLargeSection: Boolean,
    shimmerProgress: Float
) {
    val filledSlots = slots.filter { placedAchievements[it.id] != null }
    if (filledSlots.isEmpty()) return
    if (isLargeSection) {
        val largeSlot = filledSlots.find { it.acceptedSizes.contains(AchievementSize.Large) }
        val medSlots = filledSlots.filter { it.acceptedSizes.contains(AchievementSize.Medium) }

        if (largeSlot != null && placedAchievements[largeSlot.id] != null) {
            // find trophy theme by extracting its id and finding in all trophies
            val trophyID = placedAchievements[largeSlot.id]
            val achievement = AchievementCatalog.all.find { it.id == trophyID }
            if (achievement == null) return
            else {
                val metalTheme = MetalThemeCatalog.all.find { it.tier == achievement.tier }?.theme
                if (metalTheme == null) return
                else {
                    val category = achievement.category
                    val localProgress = getLocalProgress(shimmerProgress, trophyID)

                    // Single large trophy centered
                    drawTrophyModel(
                        x = innerLeft + innerWidth / 2f,
                        floorY = shelfFloorY,
                        size = AchievementSize.Large,
                        metalTheme = metalTheme,
                        category = category,
                        shimmerProgress = localProgress
                    )
                }
            }
        } else {
            // Up to 2 medium trophies
            medSlots.forEachIndexed { i, slot ->
                if (placedAchievements[slot.id]  != null) {
                    // find trophy theme by extracting its id and finding in all trophies
                    val trophyID = placedAchievements[slot.id]
                    val achievement = AchievementCatalog.all.find { it.id == trophyID }
                    if (achievement == null) return
                    else {
                        val metalTheme =
                            MetalThemeCatalog.all.find { it.tier == achievement.tier }?.theme
                        if (metalTheme == null) return
                        else {
                            val category = achievement.category
                            val localProgress = getLocalProgress(shimmerProgress, trophyID)
                            val x = innerLeft + innerWidth * (if (i == 0) 0.3f else 0.7f)
                            drawTrophyModel(
                                x = x,
                                floorY = shelfFloorY,
                                size = AchievementSize.Medium,
                                metalTheme = metalTheme,
                                category = category,
                                shimmerProgress = localProgress
                            )
                        }
                    }
                }
            }
        }
    } else {
        // 3 small slots evenly spaced
        val newInnerLeft = innerLeft * 0.5f
        val spacing = ((innerWidth) * 1.2f) / (slots.size + 1)
        slots.forEachIndexed { i, slot ->
            if (placedAchievements[slot.id] != null) {
                // find trophy theme by extracting its id and finding in all trophies
                val trophyID = placedAchievements[slot.id]
                val achievement = AchievementCatalog.all.find { it.id == trophyID }
                if (achievement == null) return
                else {
                    val metalTheme =
                        MetalThemeCatalog.all.find { it.tier == achievement.tier }?.theme
                    if (metalTheme == null) return
                    else {
                        val category = achievement.category
                        val localProgress = getLocalProgress(shimmerProgress, trophyID)
                        val x = newInnerLeft + spacing * (i + 1)
                        drawTrophyModel(
                            x = x,
                            floorY = shelfFloorY,
                            size = AchievementSize.Small,
                            metalTheme = metalTheme,
                            category = category,
                            shimmerProgress = localProgress
                        )
                    }
                }
            }
        }
    }
}

private fun getLocalProgress(globalProgress: Float, trophyId: String?): Float {
    if (trophyId == null) return 0f

    // Give each trophy a unique start time
    val hash = trophyId.hashCode().absoluteValue
    val startTime = (hash % 100) / 100f

    // How long shimmer lasts relative to global clock progress
    val duration = 0.095f

    val currentPos = (globalProgress - startTime + 1f) % 1f

    return if (currentPos < duration) {
        currentPos / duration
    } else {
        // Return number >1 to keep shimmer off screen
        2f
    }
}


private fun DrawScope.drawTrophyModel(x: Float, floorY: Float, size: AchievementSize, metalTheme: MetalTheme, category: AchievementCategory, shimmerProgress: Float) {
    when (size) {
        AchievementSize.Small -> drawMedalModel(x, floorY, metalTheme, category, shimmerProgress)
        AchievementSize.Medium -> drawCupModel(x, floorY, metalTheme, category, shimmerProgress)
        AchievementSize.Large -> drawGrandTrophyModel(x, floorY, metalTheme, category, shimmerProgress)
    }
}

private fun DrawScope.drawCupModel(x: Float, floorY: Float, metalTheme: MetalTheme, category: AchievementCategory, shimmerProgress: Float) {
    val scale = 3f
    val cupW = 28f * scale
    val cupH = 24f * scale
    val stemH = 10f * scale
    val stemW = 6f  * scale
    val baseW = 22f * scale
    val baseH = 5f  * scale
    val baseColor = metalTheme.base
    val darkColor = metalTheme.dark
    val highlightColor = metalTheme.highlight

    // Shadow on shelf
    drawOval(
        color = Color.Black.copy(alpha = 0.15f),
        topLeft = Offset(x - baseW * 0.6f, floorY - 3f),
        size = Size(baseW * 1.2f, 8f)
    )

    drawIntoCanvas { canvas ->
        canvas.saveLayer(
            Rect(
                left = -size.width,
                top = -size.height,
                right = size.width * 2,
                bottom = size.height * 2),
            Paint().apply {
            blendMode = BlendMode.SrcOver
        })
        // Base
        drawRoundRect(
            color = baseColor.copy(alpha = 0.85f),
            topLeft = Offset(x - baseW / 2, floorY - baseH),
            size = Size(baseW, baseH),
            cornerRadius = CornerRadius(2f)
        )
        // Stem
        drawRect(
            color = baseColor.copy(alpha = 0.9f),
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
                x + cupW * 0.5f, cupTop + cupH * 0.75f,
                x + cupW * 0.25f, cupTop + cupH
            )
            cubicTo(
                x + cupW * 0.1f, cupTop + cupH * 1.08f,
                x - cupW * 0.1f, cupTop + cupH * 1.08f,
                x - cupW * 0.25f, cupTop + cupH
            )
            cubicTo(
                x - cupW * 0.5f, cupTop + cupH * 0.75f,
                x - cupW * 0.55f, cupTop + cupH * 0.3f,
                x - cupW * 0.4f, cupTop
            )
            close()
        }
        drawPath(cupPath, color = baseColor)

        val sideFacePath = Path().apply {
            moveTo(x + cupW * 0.4f, cupTop)
            cubicTo(
                x + cupW * 0.55f, cupTop + cupH * 0.3f,
                x + cupW * 0.5f, cupTop + cupH * 0.75f,
                x + cupW * 0.25f, cupTop + cupH
            )
            cubicTo(
                x + cupW * 0.45f, cupTop + cupH * 0.7f,
                x + cupW * 0.48f, cupTop + cupH * 0.25f,
                x + cupW * 0.32f, cupTop
            )
            close()
        }
        drawPath(sideFacePath, color = darkColor)

        // Cup highlight
        val highlightPath = Path().apply {
            moveTo(x - cupW * 0.3f, cupTop + 3f)
            lineTo(x - cupW * 0.05f, cupTop + 3f)
            lineTo(x - cupW * 0.1f, cupTop + cupH * 0.5f)
            lineTo(x - cupW * 0.35f, cupTop + cupH * 0.5f)
            close()
        }
        drawPath(highlightPath, color = highlightColor.copy(alpha = 0.35f))
        // Handles
        drawArc(
            color = baseColor.copy(alpha = 0.85f),
            topLeft = Offset(x + cupW * 0.35f, cupTop + cupH * 0.1f),
            size = Size(cupW * 0.3f, cupH * 0.6f),
            startAngle = -135f, sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = 3f * scale)
        )
        drawArc(
            color = baseColor.copy(alpha = 0.85f),
            topLeft = Offset(x - cupW * 0.65f, cupTop + cupH * 0.1f),
            size = Size(cupW * 0.3f, cupH * 0.6f),
            startAngle = 45f, sweepAngle = 270f,
            useCenter = false,
            style = Stroke(width = 3f * scale)
        )

        val shimmerWidth = 40f * scale
        val startX = x - cupW * 3
        val endX = x + cupW * 3
        val currentX = startX + (shimmerProgress * (endX - startX))

        val shimmerBrush = Brush.linearGradient(
            0.425f to Color.Transparent,
            0.5f to Color.White.copy(0.5f),
            0.575f to Color.Transparent,
            start = Offset(currentX, floorY - cupW * 2),
            end = Offset(currentX + shimmerWidth, floorY)
        )

        drawRect(
            brush = shimmerBrush,
            blendMode = BlendMode.SrcAtop
        )

        canvas.restore()
    }
}

private fun DrawScope.drawMedalModel(x: Float, floorY: Float, metalTheme: MetalTheme, category: AchievementCategory, shimmerProgress: Float) {
    val scale = 2f
    val medalR = 12f * scale
    val ribbonW = 16f * scale
    val ribbonH = 12f * scale
    val baseColor = metalTheme.base
    val darkColor = metalTheme.dark
    val highlightColor = metalTheme.highlight
    val ribbonColor = Color(0xFF1565C0)
    val ribbonColorDark = Color(0xFF0F519B)
    val medalThickness = medalR * 0.15f

    // Shadow
    drawOval(
        color = Color.Black.copy(alpha = 0.12f),
        topLeft = Offset(x - medalR * 0.8f, floorY - 3f),
        size = Size(medalR * 1.6f + medalThickness, 6f)
    )

    translate(-medalThickness * 0.8f, -0.5f * medalThickness) {
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
                topLeft = Offset(x - medalThickness * 1.05f, floorY),
                size = Size(ribbonW * 0.5f, ribbonH)
            )
        }
    }

    drawIntoCanvas { canvas ->
        canvas.saveLayer(
            Rect(
                left = -size.width,
                top = -size.height,
                right = size.width * 2,
                bottom = size.height * 2
            ),
            Paint().apply {
            blendMode = BlendMode.SrcOver
        })

        // Medal circle
        drawCircle(
            color = darkColor,
            radius = medalR,
            center = Offset(x + medalThickness, floorY - medalR)
        )
        drawCircle(
            color = baseColor,
            radius = medalR,
            center = Offset(x, floorY - medalR)
        )
        // Medal inner ring
        drawCircle(
            color = highlightColor.copy(alpha = 0.5f),
            radius = medalR * 0.65f,
            center = Offset(x, floorY - medalR),
            style = Stroke(width = 2f * scale)
        )
        // Medal highlight
        drawCircle(
            color = highlightColor.copy(alpha = 0.55f),
            radius = medalR * 0.4f,
            center = Offset(x - medalR * 0.2f, floorY - medalR * 1.3f)
        )

        val shimmerWidth = 40f * scale
        val startX = x - medalR * 3
        val endX = x + medalR * 3
        val currentX = startX + (shimmerProgress * (endX - startX))

        val shimmerBrush = Brush.linearGradient(
                0.425f to Color.Transparent,
                0.5f to Color.White.copy(0.5f),
                0.575f to Color.Transparent,
                start = Offset(currentX, floorY - medalR * 2),
                end = Offset(currentX + shimmerWidth, floorY)
            )

        drawRect(
            brush = shimmerBrush,
            blendMode = BlendMode.SrcAtop
        )

        canvas.restore()
    }
}

private fun DrawScope.drawGrandTrophyModel(x: Float, floorY: Float, metalTheme: MetalTheme, category: AchievementCategory, shimmerProgress: Float) {
    val scale = 4.25f
    val baseColor = metalTheme.base
    val darkColor = metalTheme.dark
    val highlightColor = metalTheme.highlight

    // Base
    val base1W = 30f * scale
    val base1H = 5f * scale
    val base2W = 22f * scale
    val base2H = 3f * scale

    // Shadow
    drawOval(color = Color.Black.copy(alpha = 0.15f),
        topLeft = Offset(x - base1W * 0.6f, floorY - 3f),
        size = Size(base1W * 1.2f, 8f))

    drawIntoCanvas { canvas ->
        canvas.saveLayer(
            Rect(
                left = -size.width,
                top = -size.height,
                right = size.width * 2,
                bottom = size.height * 2
            ),
            Paint().apply {
            blendMode = BlendMode.SrcOver
        })

        drawRoundRect(
            color = darkColor,
            topLeft = Offset(x - base1W / 2, floorY - base1H),
            size = Size(base1W, base1H),
            cornerRadius = CornerRadius(3f)
        )
        drawRoundRect(
            color = baseColor.copy(alpha = 0.9f),
            topLeft = Offset(x - base2W / 2, floorY - base1H - base2H),
            size = Size(base2W, base2H),
            cornerRadius = CornerRadius(2f)
        )

        val barH = 14f * scale
        val barW = 5f * scale
        val ballR = 5f * scale
        val barTop = floorY - base1H - base2H - barH
        drawRect(
            color = baseColor.copy(alpha = 0.9f),
            topLeft = Offset(x - barW / 2, barTop),
            size = Size(barW, barH)
        )
        drawCircle(
            color = darkColor, radius = ballR,
            center = Offset(x, barTop + barH * 0.5f)
        )
        drawCircle(
            color = baseColor, radius = ballR * 0.65f,
            center = Offset(x, barTop + barH * 0.5f)
        )

        val cupW = 32f * scale;
        val cupH = 28f * scale
        val cupTop = barTop - cupH

        // Right handle
        val rightHandleCenterX = x + cupW * 0.33f + cupW * 0.35f / 2
        val rightHandleCenterY = cupTop + cupH * 0.05f + cupH * 0.65f / 2
        rotate(degrees = 10f, pivot = Offset(rightHandleCenterX, rightHandleCenterY)) {
            drawArc(
                color = darkColor,
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
                color = darkColor,
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
                x + cupW * 0.5f, cupTop + cupH * 0.35f
            )
            lineTo(x + cupW * 0.32f, cupTop + cupH * 0.62f)
            lineTo(x + cupW * 0.45f, cupTop + cupH * 0.88f)
            cubicTo(
                x + cupW * 0.32f, cupTop + cupH * 1.07f,
                x - cupW * 0.32f, cupTop + cupH * 1.07f,
                x - cupW * 0.45f, cupTop + cupH * 0.88f
            )
            lineTo(x - cupW * 0.32f, cupTop + cupH * 0.62f)
            lineTo(x - cupW * 0.5f, cupTop + cupH * 0.35f)
            cubicTo(
                x - cupW * 0.55f, cupTop + cupH * 0.2f,
                x - cupW * 0.45f, cupTop + cupH * 0.08f,
                x - cupW * 0.22f, cupTop
            )
            close()
        }
        drawPath(cupPath, color = baseColor)

        // Depth
        val sidePath = Path().apply {
            moveTo(x + cupW * 0.22f, cupTop)
            cubicTo(
                x + cupW * 0.45f, cupTop + cupH * 0.08f,
                x + cupW * 0.55f, cupTop + cupH * 0.2f,
                x + cupW * 0.5f, cupTop + cupH * 0.35f
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
        drawPath(sidePath, color = darkColor.copy(alpha = 0.55f))

        // Plate on front
        val plateW = cupW * 0.45f;
        val plateH = cupH * 0.3f
        drawRoundRect(
            color = darkColor.copy(alpha = 0.6f),
            topLeft = Offset(x - plateW / 2, cupTop + cupH * 0.6f),
            size = Size(plateW, plateH),
            cornerRadius = CornerRadius(4f)
        )

        // Highlight
        drawOval(
            color = highlightColor.copy(alpha = 0.55f),
            topLeft = Offset(x - cupW * 0.36f, cupTop + 4f),
            size = Size(cupW * 0.45f, cupH * 0.2f)
        )

        // Rim Shading
        drawOval(
            color = darkColor,
            topLeft = Offset(x - cupW * 0.25f, cupTop),
            size = Size(cupW * 0.5f, cupH * 0.1f)
        )

        val shimmerWidth = 40f * scale
        val startX = x - cupW * 3
        val endX = x + cupW * 3
        val currentX = startX + (shimmerProgress * (endX - startX))

        val shimmerBrush = Brush.linearGradient(
            0.425f to Color.Transparent,
            0.5f to Color.White.copy(0.5f),
            0.575f to Color.Transparent,
            start = Offset(currentX, floorY - cupW * 2),
            end = Offset(currentX + shimmerWidth, floorY)
        )

        drawRect(
            brush = shimmerBrush,
            blendMode = BlendMode.SrcAtop
        )

        canvas.restore()
    }
}

@Composable
fun VisitDisplay(
    friendsList: List<FriendProfile>,
    authViewModel: AuthViewModel,
    onVisitRandomRoom: () -> Unit,
    onVisitUserRoom: (String) -> Unit,
    onClose: () -> Unit
) {
    val authState by authViewModel.state.collectAsState()
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
                    Text(
                        "Visit other Rooms", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(onClick = onClose) {
                    Icon(painterResource(R.drawable.close_24px), contentDescription = "Close")
                }
            }

            Column(modifier = Modifier.weight(1f).padding(12.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Your Friends",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                HorizontalDivider()
                if (authState.isAnonymous) {
                    Text(
                        text = "You aren't logged in. Sign up now to add friends!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(6.dp)
                    )
                }
                else if (friendsList.isEmpty()) {
                    Text(
                        text = "No friends yet. Visit a random user's Room and add them!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(6.dp)
                    )
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            friendsList
                        ) { friend ->
                            FriendCardCell(
                                friendProfile = friend,
                                onClick = { onVisitUserRoom(friend.uid)
                                    Log.d("RoomScreen", "Visiting ${if (friend.name != "") friend.name else friend.email}'s room")}
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp), horizontalArrangement = Arrangement.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    FilledIconButton(
                        onClick = onVisitRandomRoom,
                        colors = IconButtonColors(
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                            MaterialTheme.colorScheme.onSurface,
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.primaryContainer,
                        )
                    ) {
                        Icon(
                            painterResource(R.drawable.random_24px),
                            contentDescription = "Random Room"
                        )
                    }
                    Text("Random")
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
fun FriendCardCell(
    friendProfile: FriendProfile,
    onClick: (String) -> Unit) {
    Box {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    shape = RoundedCornerShape(50),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ),
                    modifier = Modifier.size(36.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painterResource(R.drawable.person_24px),
                            contentDescription = "icon",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    if (friendProfile.name != "") friendProfile.name else friendProfile.email,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    painterResource(R.drawable.trophy_24px),
                    contentDescription = "Achievements",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Text(
                    text = "${friendProfile.trophies}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                FilledTonalButton(
                    onClick = { onClick(friendProfile.uid) },
                    modifier = Modifier.height(32.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        "Visit",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
fun VisitTrophyDisplay(placedAchievements: Map<String, String>) {
    LazyRow(
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.fillMaxWidth()
    ) {
        items(placedAchievements.values.toList()) { achievementId ->
            val achievement = AchievementCatalog.all.find { it.id == achievementId }
                    TrophyCell(
                        achievement = achievement!!,
                        isOnShelf = true,
                        isUnlocked = true,
                        canPlace = false,
                        onPlace = {},
                        onRemove = {},
                        isVisiting = true,
                        modifier = Modifier.width(120.dp)
                    )
                }
    }
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
                        "Exchange", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "$currentPoints points",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.weight(0.4f))

                FilledIconButton(
                    onClick = { activeTab = "themesR" },
                    colors = IconButtonColors(
                    if (activeTab != "themesR") MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                    MaterialTheme.colorScheme.onSurface,
                    MaterialTheme.colorScheme.primaryContainer,
                    MaterialTheme.colorScheme.primaryContainer,)
                ) {
                    Icon(painterResource(R.drawable.room_theme_24px), contentDescription = "Room Themes")
                }
                FilledIconButton(
                    onClick = { activeTab = "themesW" },
                    colors = IconButtonColors(
                        if (activeTab != "themesW") MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        MaterialTheme.colorScheme.onSurface,
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(painterResource(R.drawable.shelves_24px), contentDescription = "Wood Themes")
                }
                FilledIconButton(
                    onClick = { activeTab = "items" },
                    colors = IconButtonColors(
                        if (activeTab != "items") MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.25f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
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
                        RoomThemeCatalog.all
                            .filter { theme -> !unlockedRoomThemeIds.contains(theme.id)}
                            .sortedBy { it.pointCost }
                    ) { theme ->
                        RoomThemeCell(
                            roomThemeOption = theme,
                            currentPoints = currentPoints,
                            onClick = {
                                if (theme.pointCost <= currentPoints) {
                                    viewModel.purchaseRoomTheme(theme.id, theme.pointCost)
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
                    items(WoodThemeCatalog.all
                        .filter { theme -> !unlockedWoodThemeIds.contains(theme.id)}
                        .sortedBy { it.pointCost }
                    ) { theme ->
                        WoodThemeCell(
                            woodThemeOption = theme,
                            currentPoints = currentPoints,
                            onClick = {
                                if (theme.pointCost <= currentPoints) {
                                    viewModel.purchaseWoodTheme(theme.id, theme.pointCost)
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
                    items(RoomItemCatalog.all
                        .filter { item -> !unlockedRoomItemIds.contains(item.id)}
                        .sortedBy { it.pointCost }
                    ) { item ->
                        ItemCell(
                            item = item,
                            currentPoints = currentPoints,
                            onClick = {
                                viewModel.purchaseItem(item.id, item.pointCost)
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
                .height(200.dp)
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    item.name,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
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
                        containerColor = if (item.pointCost <= currentPoints) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        contentColor = if (item.pointCost <= currentPoints) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant,
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
                .height(200.dp)
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
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
                        containerColor = if (roomThemeOption.pointCost <= currentPoints) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        contentColor = if (roomThemeOption.pointCost <= currentPoints) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant,
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
                .height(220.dp)
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
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
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
                        containerColor = if (woodThemeOption.pointCost <= currentPoints) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        contentColor = if (woodThemeOption.pointCost <= currentPoints) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant,
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
                FilledTonalButton(onClick = {
                    viewModel.removePlacedAchievements()
                }, colors = ButtonColors(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.85f), MaterialTheme.colorScheme.onErrorContainer, MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.errorContainer)) {
                    Text("Clear Placed", style = MaterialTheme.typography.labelMedium)
                }
                IconButton(onClick = onClose) {
                    Icon(painterResource(R.drawable.close_24px), contentDescription = "Close")
                }
            }

            // Sort order for tiers
            val tierOrder = listOf(AchievementTier.Bronze, AchievementTier.Silver, AchievementTier.Gold, AchievementTier.Diamond)

            // Category display order and labels
            val categoryOrder = listOf(
                AchievementCategory.Streak to "Streak",
                AchievementCategory.WalkingTime to "Walking",
                AchievementCategory.ScreenTime to "Screen Time",
                AchievementCategory.Exchange to "Exchange",
                AchievementCategory.Special to "Special",
                AchievementCategory.Secret to "Secret"
            )

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                categoryOrder.forEach { (category, label) ->
                    val categoryAchievements = AchievementCatalog.all
                        .filter { it.category == category }
                        .sortedWith(compareBy(
                            { it.size.ordinal },
                            { tierOrder.indexOf(it.tier) }
                        ))

                    if (categoryAchievements.isEmpty()) return@forEach

                    item(key = "header_$category") {
                        CategoryHeader(label = label, achievements = categoryAchievements, unlockedIds = unlockedAchievementIds)
                    }

                    val rows = categoryAchievements.chunked(3)
                    items(rows, key = { "row_${category}_${rows.indexOf(it)}" }) { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            row.forEach { achievement ->
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
                                        }
                                    },
                                    onRemove = { viewModel.removeAchievement(achievement.id) },
                                    isVisiting = false,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill remaining slots in last row so cells don't stretch
                            repeat(3 - row.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }

                    item(key = "spacer_$category") {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        SnackbarHost(hostState = snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun CategoryHeader(
    label: String,
    achievements: List<Achievement>,
    unlockedIds: Set<String>
) {
    val unlocked = achievements.count { it.id in unlockedIds }
    val total = achievements.size

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            "$unlocked / $total",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    HorizontalDivider()
    Spacer(modifier = Modifier.height(8.dp))
}

val shape = RoundedCornerShape(16.dp)

@Composable
fun TrophyCell(
    achievement: Achievement,
    isUnlocked: Boolean,
    isOnShelf: Boolean = false,
    canPlace: Boolean = false,
    onPlace: () -> Unit = {},
    onRemove: () -> Unit = {},
    isVisiting: Boolean,
    modifier: Modifier) {
    Box(modifier = modifier) {
        val isSecret = achievement.category == AchievementCategory.Secret
        val size = when(achievement.size) {
            AchievementSize.Large -> 42.dp
            AchievementSize.Medium -> 38.dp
            AchievementSize.Small -> 36.dp
        }
        val tier = achievement.tier
        val metalTheme = MetalThemeCatalog.all.find { it.tier == tier }
        Card(
            shape = shape,
            //border = if (isOnShelf) BorderStroke(tierBorderWidth(achievement.tier)*2, tierBorderColor(achievement.tier)) else if (isUnlocked) BorderStroke(tierBorderWidth(achievement.tier), tierBorderColor(achievement.tier)) else null,
            colors = CardDefaults.cardColors(
                containerColor = if (isUnlocked)
                    MaterialTheme.colorScheme.surfaceVariant
                else
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .shimmerBorder(achievement.tier, isUnlocked, shape)
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
                Icon(
                    painterResource(if (isUnlocked || !isSecret) icon else R.drawable.question_mark_24px),
                    contentDescription = null,
                    tint = if (isUnlocked) metalTheme!!.theme.base
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                    modifier = if (isUnlocked || !isSecret) Modifier
                        .size(size)
                        .iconShimmer(achievement.tier, isUnlocked) else Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isUnlocked || !isSecret) achievement.title else "Secret Achievement",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    color = if (isUnlocked) metalTheme!!.theme.base.copy(alpha = 0.75f)
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.weight(0.5f))
                Text(
                    text = if (isUnlocked || !isSecret) achievement.description else "The way to earn this achievement is a mystery...",
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                        alpha = if (isUnlocked) 1f else 0.4f
                    )
                )

                Spacer(modifier = Modifier.weight(0.5f))

                if (!isVisiting) {
                    if (isOnShelf) {
                        FilledTonalButton(
                            onClick = onRemove,
                            modifier = Modifier.height(32.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
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
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
                                contentColor = if (isUnlocked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(
                                    alpha = 0.3f
                                )
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
}

fun Modifier.shimmerBorder(
    tier: AchievementTier,
    isUnlocked: Boolean,
    shape: Shape
): Modifier = composed {
    if (!isUnlocked) return@composed this

    val duration = 6000

    val transition = rememberInfiniteTransition(label = "smoothShimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f)),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )

    this.drawWithCache {
        val width = size.width
        val height = size.height

        val baseColor = tierBorderColor(tier)
        val strokeWidthPx = tierBorderWidth(tier).toPx()

        val glintWidth = width * 0.5f
        val startX = (-glintWidth - width) * 5f
        val endX = (width + glintWidth) * 5f
        val currentX = startX + (progress * (endX - startX))

        val shimmerBrush = if (tier == AchievementTier.Diamond) {
            // Tried to do something more special for diamond tier
            Brush.linearGradient(
                0.0f to Color.Transparent,
                0.3f to Color(0xFF80DEEA).copy(alpha = 0.4f),
                0.5f to Color.White.copy(alpha = 0.9f),
                0.7f to Color(0xFFF48FB1).copy(alpha = 0.4f),
                1.0f to Color.Transparent,
                start = Offset(x = currentX, y = 0f),
                end = Offset(x = currentX + glintWidth, y = height)
            )
        } else {
            Brush.linearGradient(
                0.0f to Color.Transparent,
                0.5f to Color.White.copy(alpha = 0.7f),
                1.0f to Color.Transparent,
                start = Offset(x = currentX, y = 0f),
                end = Offset(x = currentX + glintWidth, y = height)
            )
        }

        onDrawWithContent {
            drawContent()

            val outline = shape.createOutline(size, layoutDirection, this)

            drawOutline(
                outline = outline,
                color = baseColor,
                style = Stroke(width = strokeWidthPx)
            )

            drawOutline(
                outline = outline,
                brush = shimmerBrush,
                style = Stroke(width = strokeWidthPx)
            )
        }
    }
}


fun Modifier.iconShimmer(tier: AchievementTier, isUnlocked: Boolean): Modifier = composed {
    if (!isUnlocked) return@composed this

    val duration = when (tier) {
        (AchievementTier.Bronze) -> 4000
        (AchievementTier.Silver) -> 3000
        (AchievementTier.Gold) -> 2500
        (AchievementTier.Diamond) -> 2000
    }

    val transition = rememberInfiniteTransition(label = "iconShimmer")
    val translateAnim by transition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslation"
    )

    this
        .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
        .drawWithContent {
            drawContent()

            val shimmerWidth = size.width * 0.45f
            val xOffset = size.width * translateAnim

            drawRect(
                brush = Brush.linearGradient(
                    0.0f to Color.White.copy(alpha = 0f),
                    0.5f to Color.White.copy(alpha = 0.55f),
                    1.0f to Color.White.copy(alpha = 0f),
                    start = Offset(xOffset * 2f, 0f),
                    end = Offset((xOffset + shimmerWidth) * 2f, size.height)
                ),
                blendMode = BlendMode.SrcAtop
            )
        }
}


fun tierBorderColor(tier: AchievementTier): Color = when (tier) {
    AchievementTier.Bronze -> Color(0xFFCD7F32)
    AchievementTier.Silver -> Color(0xFFB0BEC5)
    AchievementTier.Gold -> Color(0xFFFFB300)
    AchievementTier.Diamond -> Color(0xFF80DEEA)
}

fun tierBorderWidth(tier: AchievementTier): Dp = when (tier) {
    AchievementTier.Bronze -> 1.dp
    AchievementTier.Silver -> 1.5.dp
    AchievementTier.Gold -> 2.dp
    AchievementTier.Diamond -> 2.5.dp
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
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun VisitPanel(onClick: () -> Unit = {}) {
    FloatingPanel(icon = R.drawable.visit_24px, label = "Visit", onClick = onClick)
}

@Composable
fun ReturnPanel(onClick: () -> Unit = {}) {
    FloatingPanel(icon = R.drawable.return_24px, label = "Return", onClick = onClick)
}

@Composable
fun FriendPanel(isFriend: Boolean, onClick: () -> Unit = {}) {
    FloatingPanel(icon = if (isFriend) R.drawable.remove_friend_24px else R.drawable.add_friend_24px, label = if (isFriend) "Remove" else "Add Friend", onClick = onClick)
}

@Composable
fun ProfilePanel(onClick: () -> Unit = {}) {
    FloatingPanel(icon = R.drawable.view_profile_24px, label = "View Profile", onClick = onClick
    )
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
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant)
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