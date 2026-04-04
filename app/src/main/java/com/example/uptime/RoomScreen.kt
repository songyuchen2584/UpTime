package com.example.uptime

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val wallColor: Color = Color(0xFF009688),
    val floorColor: Color = Color(0xFF009688)
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

data class RoomState(
    val theme: RoomTheme = RoomTheme(),
    val availableItems: List<RoomItem> = emptyList(),
    val achievements: List<Achievement> = com.example.uptime.achievements,
    val displayName: String = "My Room",
    val currentPoints: Int = 0
)

enum class RoomPanel { Achievements, Exchange }
@Composable
fun RoomScreen(state: RoomState = RoomState()) {
    var roomMode by remember { mutableStateOf(RoomMode.View) }
    var activePanel by remember { mutableStateOf<RoomPanel?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        RoomScaffold(state, roomMode)

        Row(modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
            AchievementsPanel(onClick = {
                activePanel = if (activePanel == RoomPanel.Achievements) null
                else RoomPanel.Achievements
            })
            CustomizePanel(isActive = roomMode == RoomMode.Edit,
                onClick  = {
                    roomMode = if (roomMode == RoomMode.Edit) RoomMode.View else RoomMode.Edit
                })
            ExchangePanel(onClick = {
                activePanel = if (activePanel == RoomPanel.Exchange) null
                else RoomPanel.Exchange
            })
        }
        //TODO("A place to display your achievements and customize your avatar/home")
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
            modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
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

    if (mode == RoomMode.Edit) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
        )
        Text(
            text = "Edit Mode",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().padding(top = 52.dp),
            textAlign = TextAlign.Center
        )
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