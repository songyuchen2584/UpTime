package com.example.uptime

import androidx.compose.foundation.Canvas
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uptime.ui.theme.Amber40
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun StreakScreen(viewModel: DashboardViewModel = viewModel()) {
    val stats by viewModel.userStats.collectAsState()
    val logs by viewModel.repository.allLogs.collectAsState(initial = emptyList())

    // last 7 days of logs
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    val today = LocalDate.now()
    val last7Days = (0L..6L).map { today.minusDays(it).format(formatter) }
    val recentLogs = last7Days.map { date ->
        logs.find { it.date == date } ?: DailyLog(date = date)
    }

    // weekly totals
    val weeklyWalking = recentLogs.sumOf { it.walkingMinutes }
    val weeklyScreenTime = recentLogs.sumOf { it.screenTimeMinutes }
    val daysCompleted = recentLogs.count { it.streakMaintained }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // streak overview
        StreakOverviewCard(
            currentStreak = stats.currentStreak,
            bestStreak = stats.bestStreak
        )

        Spacer(modifier = Modifier.height(16.dp))

        // weekly summary
        WeeklySummaryCard(
            weeklyWalking = weeklyWalking,
            weeklyScreenTime = weeklyScreenTime,
            daysCompleted = daysCompleted
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 7-day history
        DailyHistoryCard(recentLogs)

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun StreakOverviewCard(currentStreak: Int, bestStreak: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StreakStat(
                value = currentStreak,
                label = "Current streak",
                emoji = if (currentStreak > 0) "🔥" else "⏸️"
            )
            StreakStat(
                value = bestStreak,
                label = "Best streak",
                emoji = "🏆"
            )
        }
    }
}

@Composable
fun StreakStat(value: Int, label: String, emoji: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = emoji, fontSize = 28.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$value",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun WeeklySummaryCard(weeklyWalking: Int, weeklyScreenTime: Int, daysCompleted: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "This week",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                WeeklyStat(value = "$daysCompleted/7", label = "Goals met")
                WeeklyStat(value = "${weeklyWalking}m", label = "Walking")
                WeeklyStat(value = "${weeklyScreenTime}m", label = "Screen time")
            }
        }
    }
}

@Composable
fun WeeklyStat(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun DailyHistoryCard(logs: List<DailyLog>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Last 7 days",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))

            logs.forEach { log ->
                DayRow(log)
                if (log != logs.last()) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun DayRow(log: DailyLog) {
    val date = LocalDate.parse(log.date, DateTimeFormatter.ISO_LOCAL_DATE)
    val today = LocalDate.now()
    val dayLabel = when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }

    val statusIcon = when {
        log.streakMaintained -> "✅"
        log.screenTimeMinutes == 0 && log.walkingMinutes == 0 -> "—"
        else -> "❌"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.width(100.dp)
        ) {
            Text(text = statusIcon, fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = dayLabel,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = "🚶 ${log.walkingMinutes}m",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = "📱 ${log.screenTimeMinutes}m",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
