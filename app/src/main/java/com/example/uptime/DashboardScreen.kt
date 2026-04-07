package com.example.uptime

import android.R.attr.onClick
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uptime.ui.theme.Amber40
import com.example.uptime.ui.theme.Coral40
import com.example.uptime.ui.theme.UpTimeTheme

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
    viewModel: DashboardViewModel = viewModel(),
    onNavigateToStreak: () -> Unit,
    onNavigateToWalkingProgress: () -> Unit,
    onNavigateToScreenTime: () -> Unit
) {
    // collect live data from Room via ViewModel
    val log by viewModel.todayLog.collectAsState(initial = null)
    val stats by viewModel.userStats.collectAsState()

    val streak = stats.currentStreak
    val best = stats.bestStreak
    val quote by viewModel.quote.collectAsState()

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
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        //Streak Banner
        StreakCard(
            currentStreak = state.currentStreak,
            bestStreak = state.bestStreak,
            bothGoalsMet = state.screenTimeUsed <= state.screenTimeGoal
                    && state.walkingDone >= state.walkingGoal,
            onClick = onNavigateToStreak
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Progress Rings
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                ringColor = if (!screenOver) screenTimeColor(state.screenTimeUsed, state.screenTimeGoal) else Coral40,
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

        Spacer(modifier = Modifier.height(28.dp))

        // daily status
        DailyStatusCard(state, onNavigateToWalkingProgress, {})

        Spacer(modifier = Modifier.height(16.dp))

        //Today's goals checklist
        GoalsCard(state)

        Spacer(modifier = Modifier.height(16.dp))

        // daily motivational quote from API
        quote?.let {
            QuoteCard(it)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// ── Streak Card ──

@Composable
fun StreakCard(currentStreak: Int, bestStreak: Int, bothGoalsMet: Boolean, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (bothGoalsMet)
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flame / streak icon area
            Text(
                text = if (currentStreak > 0) "🔥" else "⏸️",
                fontSize = 36.sp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$currentStreak day streak",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (bothGoalsMet) "Both goals met today — keep it up!"
                    else "Complete both goals to keep your streak",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Best streak badge
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$bestStreak",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Amber40
                )
                Text(
                    text = "best",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Progress Ring ──

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
    modifier: Modifier = Modifier
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
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)

    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(140.dp)
        ) {
            Canvas(modifier = Modifier.size(140.dp)) {
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
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Progress arc
                drawArc(
                    color = ringColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Center text
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = ringColor,
            fontWeight = FontWeight.Medium
        )
    }
}

//Daily Status Card

@Composable
fun DailyStatusCard(state: DashboardState, onClickWalking: () -> Unit, onClickScreenTime: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Today's Progress",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Screen time bar
            ProgressRow(
                label = "Screen Time",
                current = state.screenTimeUsed,
                goal = state.screenTimeGoal,
                unit = "min",
                onClick = onClickScreenTime,
                isInverted = true  // lower is better
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Walking bar
            ProgressRow(
                label = "Walking",
                current = state.walkingDone,
                goal = state.walkingGoal,
                unit = "min",
                onClick = onClickWalking,
                isInverted = false  // higher is better
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
    onClick: () -> Unit,
    isInverted: Boolean
) {
    val fraction = (current.toFloat() / goal).coerceIn(0f, 1f)
    val isOver = if (isInverted) current > goal else current < goal
    val barColor = when {
        isInverted && current > goal -> Coral40
        !isInverted && current >= goal -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.secondary
    }

    Column(modifier = Modifier.clickable(onClick=onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
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
                .height(8.dp),
            color = barColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            strokeCap = StrokeCap.Round
        )
    }
}

//Goals Checklist Card

@Composable
fun GoalsCard(state: DashboardState) {
    val screenTimeMet = state.screenTimeUsed <= state.screenTimeGoal
    val walkingMet = state.walkingDone >= state.walkingGoal

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Daily Goals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            GoalRow(
                icon = if (screenTimeMet) "✅" else "⬜",
                text = "Stay under ${state.screenTimeGoal} min of screen time",
                isDone = screenTimeMet
            )

            Spacer(modifier = Modifier.height(8.dp))

            GoalRow(
                icon = if (walkingMet) "✅" else "⬜",
                text = "Walk at least ${state.walkingGoal} min",
                isDone = walkingMet
            )

            if (screenTimeMet && walkingMet) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "🎉 Streak maintained for today!",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun GoalRow(icon: String, text: String, isDone: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = icon, fontSize = 20.sp)
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = if (isDone) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "+ 50 pts", // can change later
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 10.dp)
        )
    }
}


@Composable
fun QuoteCard(quote: Quote) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "\"${quote.text}\"",
                style = MaterialTheme.typography.bodyMedium,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "— ${quote.author}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
        }
    }
}
