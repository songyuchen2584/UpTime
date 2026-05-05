package com.example.uptime.dashboard

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uptime.R
import com.example.uptime.room.RoomViewModel
import com.example.uptime.ui.theme.Coral40
import com.example.uptime.walking.viewmodel.WalkingViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.compose.ui.res.painterResource

private val Context.dashboardOnboardingDataStore by preferencesDataStore(
    name = "dashboard_onboarding"
)

private val completedOnboardingTasksKey =
    stringSetPreferencesKey("completed_onboarding_tasks")

// onboarding components
enum class DashboardOnboardingTask(
    val title: String,
    val description: String,
    val icon: Int
) {
    WALKING(
        title = "Set up walking tracker",
        description = "Choose how UpTime tracks your walking progress.",
        icon = R.drawable.directions_walk_24px
    ),
    SCREEN_TIME(
        title = "Set up screen time tracker",
        description = "Allow our app to track your screen time and select which apps you want to monitor.",
        icon = R.drawable.analytics_24px
    ),
    NOTIFICATIONS(
        title = "Set up notifications",
        description = "Enable reminders and goal warnings.",
        icon = R.drawable.round_add_alert_24
    ),
    SIGN_IN(
        title = "Sign up",
        description = "Personalize your profile and experience.",
        icon = R.drawable.person_24px
    ),
    ROOM(
        title = "Explore rooms",
        description = "Visit other users' rooms and see their progress.",
        icon = R.drawable.door_sliding_24px
    )
}

// UI state for the dashboard
data class DashboardState(
    val screenTimeUsed: Int = 0,
    val screenTimeGoal: Int = 30,
    val walkingDone: Int = 0,
    val walkingGoal: Int = 30,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0
)

@Composable
fun screenTimeColor(used: Int, goal: Int): Color {
    val remaining = 1f - (used.toFloat() / goal)
    return when {
        remaining > 0.50f -> Color(0xFF4CAF50)
        remaining > 0.25f -> Color(0xFFFFC107)
        else              -> Color(0xFFFF5722)
    }
}

@Composable
fun walkingColor(done: Int, goal: Int): Color {
    val progress = done.toFloat() / goal
    return when {
        progress >= 1.00f -> Color(0xFF4CAF50)
        progress >= 0.80f -> Color(0xFFADC34A)
        progress >= 0.50f -> Color(0xFFFFC107)
        else              -> Color(0xFFFF5722)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    dashboardViewModel: DashboardViewModel = viewModel(),
    walkingViewModel: WalkingViewModel = viewModel(),
    onNavigateToStreak: () -> Unit,
    onNavigateToWalkingProgress: () -> Unit,
    onNavigateToScreenTime: () -> Unit,
    onNavigateToNotifications: () -> Unit = {},
    onNavigateToSignIn: () -> Unit = {},
    onNavigateToRoom: () -> Unit = {},
) {
    LaunchedEffect(Unit) {
        dashboardViewModel.refreshLiveStats(walkingViewModel)
    }

    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner, dashboardViewModel, walkingViewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                dashboardViewModel.refreshLiveStats(walkingViewModel)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // collect live data from Room via ViewModel
    val log by dashboardViewModel.todayLog.collectAsState(initial = null)
    val stats by dashboardViewModel.userStats.collectAsState()

    val streak = stats.currentStreak
    val best = stats.bestStreak

    // build UI state from database
    val state = DashboardState(
        screenTimeUsed = log?.screenTimeMinutes ?: 0,
        screenTimeGoal = log?.screenTimeGoal ?: 30,
        walkingDone = log?.walkingMinutes ?: 0,
        walkingGoal = log?.walkingGoal ?: 30,
        currentStreak = streak,
        bestStreak = best
    )

    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.Companion.height(8.dp))

        //Streak Banner
        StreakCard(
            currentStreak = state.currentStreak,
            bothGoalsMet = state.screenTimeUsed <= state.screenTimeGoal
                    && state.walkingDone >= state.walkingGoal,
            onClick = onNavigateToStreak
        )

        // onboarding checklist
        Spacer(modifier = Modifier.Companion.height(16.dp))

        OnboardingChecklist(
            onNavigateToWalkingProgress = onNavigateToWalkingProgress,
            onNavigateToScreenTime = onNavigateToScreenTime,
            onNavigateToNotifications = onNavigateToNotifications,
            onNavigateToSignIn = onNavigateToSignIn,
            onNavigateToRoom = onNavigateToRoom,
        )

        Spacer(modifier = Modifier.Companion.height(24.dp))

        // Progress Rings
        Row(
            modifier = Modifier.Companion.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Screen time (lower is better)
            val screenFraction = state.screenTimeUsed.toFloat() / state.screenTimeGoal
            val screenOver = state.screenTimeUsed > state.screenTimeGoal

            ProgressRing(
                label = "Screen Time",
                value = "${state.screenTimeUsed}",
                unit = "min",
                subtitle = "${state.screenTimeGoal - state.screenTimeUsed} min left",
                progress = screenFraction.coerceIn(0f, 1f),
                ringColor = if (!screenOver) screenTimeColor(
                    state.screenTimeUsed,
                    state.screenTimeGoal
                ) else Coral40,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                onClick = onNavigateToScreenTime
            )

            // Walking (higher is better)
            val walkFraction = state.walkingDone.toFloat() / state.walkingGoal
            val walkMet = state.walkingDone >= state.walkingGoal

            ProgressRing(
                label = "Walking",
                value = "${state.walkingDone}",
                unit = "min",
                subtitle = if (walkMet) "Goal reached!"
                else "${state.walkingGoal - state.walkingDone} min to go",
                progress = walkFraction.coerceIn(0f, 1f),
                ringColor = walkingColor(state.walkingDone, state.walkingGoal),
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                onClick = onNavigateToWalkingProgress
            )
        }

        Spacer(modifier = Modifier.Companion.height(28.dp))

        // daily status
        DailyStatusCard(state, onNavigateToWalkingProgress, onNavigateToScreenTime)

        Spacer(modifier = Modifier.Companion.height(24.dp))
    }
}

// onboarding composable
@Composable
fun OnboardingChecklist(
    onNavigateToWalkingProgress: () -> Unit,
    onNavigateToScreenTime: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onNavigateToRoom: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val completedTasks by context.dashboardOnboardingDataStore.data
        .map { preferences ->
            preferences[completedOnboardingTasksKey] ?: emptySet()
        }
        .collectAsState(initial = emptySet())

    val visibleTasks = DashboardOnboardingTask.entries.filter {
        it.name !in completedTasks
    }

    if (visibleTasks.isEmpty()) return

    Card(
        modifier = Modifier.Companion.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
        )
    ) {
        Column(
            modifier = Modifier.Companion.padding(20.dp)
        ) {
            Text(
                text = "Finish setting up UpTime",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Companion.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.Companion.height(4.dp))

            Text(
                text = "${visibleTasks.size} setup task${if (visibleTasks.size == 1) "" else "s"} remaining",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
            )

            Spacer(modifier = Modifier.Companion.height(14.dp))

            visibleTasks.forEach { task ->
                OnboardingTaskRow(
                    task = task,
                    onClick = {
                        scope.launch {
                            markOnboardingTaskComplete(context, task)
                        }

                        when (task) {
                            DashboardOnboardingTask.WALKING -> onNavigateToWalkingProgress()
                            DashboardOnboardingTask.SCREEN_TIME -> onNavigateToScreenTime()
                            DashboardOnboardingTask.NOTIFICATIONS -> onNavigateToNotifications()
                            DashboardOnboardingTask.SIGN_IN -> onNavigateToSignIn()
                            DashboardOnboardingTask.ROOM -> onNavigateToRoom()
                        }
                    },
                    onDismiss = {
                        scope.launch {
                            markOnboardingTaskComplete(context, task)
                        }
                    }
                )

                Spacer(modifier = Modifier.Companion.height(10.dp))
            }
        }
    }
}

@Composable
fun OnboardingTaskRow(
    task: DashboardOnboardingTask,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.Companion
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Row(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            Icon(
                painter = painterResource(task.icon),
                contentDescription = task.title,
                modifier = Modifier.size(26.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.Companion.width(14.dp))

            Column(
                modifier = Modifier.Companion.weight(1f)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Companion.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            TextButton(onClick = onDismiss) {
                Text("Skip")
            }
        }
    }
}

private suspend fun markOnboardingTaskComplete(
    context: Context,
    task: DashboardOnboardingTask
) {
    context.dashboardOnboardingDataStore.edit { preferences ->
        val current = preferences[completedOnboardingTasksKey] ?: emptySet()
        preferences[completedOnboardingTasksKey] = current + task.name
    }
}

@Composable
fun StreakCard(currentStreak: Int, bothGoalsMet: Boolean, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        modifier = Modifier.Companion.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (bothGoalsMet)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.Companion.CenterVertically
        ) {
            Icon(
                painterResource(R.drawable.streak_24px),
                contentDescription = "Streak",
                modifier = Modifier.Companion.size(32.dp),
                tint = if (bothGoalsMet) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.Companion.width(16.dp))

            Column(modifier = Modifier.Companion.weight(1f)) {
                Text(
                    text = "$currentStreak day streak",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Companion.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.Companion.height(4.dp))
                Text(
                    text = if (bothGoalsMet) "Both goals met today, keep it up until midnight!"
                    else "Complete Both Goals!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.Companion.width(16.dp))

            // streak report link
            Card(
                onClick = onClick,
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primary)
            ) {
                Column(
                    Modifier.Companion.padding(8.dp),
                    horizontalAlignment = Alignment.Companion.CenterHorizontally
                ) {
                    Text("Monthly", style = MaterialTheme.typography.labelSmall)
                    Text("Report", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun ProgressRing(
    label: String,
    value: String,
    unit: String,
    subtitle: String,
    progress: Float,
    ringColor: Color,
    trackColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier.Companion
) {
    // Animate the arc on first appearance
    var targetProgress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 800),
        label = "ring"
    )
    LaunchedEffect(progress) {
        targetProgress = progress
    }

    Column(
        horizontalAlignment = Alignment.Companion.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)

    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.Companion.height(8.dp))

        Box(
            contentAlignment = Alignment.Companion.Center,
            modifier = Modifier.Companion.size(140.dp)
        ) {
            Canvas(modifier = Modifier.Companion.size(140.dp)) {
                val strokeWidth = 12.dp.toPx()
                val arcSize = size.width - strokeWidth
                val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                // Track (background circle)
                drawArc(
                    color = trackColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Companion.Round)
                )

                // Progress arc
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Companion.Round)
                )
            }

            // Center text
            Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
                Text(
                    text = value,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Companion.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.Companion.height(6.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = ringColor,
            fontWeight = FontWeight.Companion.Medium
        )
    }
}

@Composable
fun DailyStatusCard(state: DashboardState, onClickWalking: () -> Unit, onClickScreenTime: () -> Unit) {
    Card(
        modifier = Modifier.Companion.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.Companion.padding(20.dp)) {
            Text(
                text = "Today's Goals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Companion.SemiBold
            )

            Spacer(modifier = Modifier.Companion.height(16.dp))

            // Screen time bar
            ProgressRow(
                label = "Screen Time",
                current = state.screenTimeUsed,
                goal = state.screenTimeGoal,
                unit = "min",
                onClick = onClickScreenTime,
                isInverted = true  // lower is better
            )

            Spacer(modifier = Modifier.Companion.height(14.dp))

            // Walking bar
            ProgressRow(
                label = "Walking",
                current = state.walkingDone,
                goal = state.walkingGoal,
                unit = "min",
                onClick = onClickWalking,
                isInverted = false  // higher is better
            )

            Spacer(modifier = Modifier.Companion.height(14.dp))

            Text(
                text = "Complete both to earn ${RoomViewModel.Companion.DAILY_COMPLETION_POINTS} pts!",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.Companion.fillMaxWidth(),
                textAlign = TextAlign.Companion.Center
            )
        }
    }
}

@Composable
fun ProgressRow(
    label: String,
    current: Int,
    goal: Int,
    unit: String,
    onClick: () -> Unit = {},
    isInverted: Boolean
) {
    val fraction = (current.toFloat() / goal).coerceIn(0f, 1f)
    val goalMet = if (isInverted) current <= goal else current >= goal
    val barColor = when {
        isInverted && current > goal -> Coral40
        !isInverted && current >= goal -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondary
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(if (goalMet) R.drawable.check_box_checked_24px else R.drawable.check_box_blank_24px),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "$current / $goal $unit",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f),
            strokeCap = StrokeCap.Round
        )
    }
}