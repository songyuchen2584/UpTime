package com.example.uptime.streak

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uptime.R
import com.example.uptime.dashboard.DashboardViewModel
import com.example.uptime.data.DailyLog
import com.example.uptime.room.RoomViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun StreakScreen(viewModel: DashboardViewModel = viewModel()) {
    val stats by viewModel.userStats.collectAsState()
    val logs by viewModel.repository.allLogs.collectAsState(initial = emptyList())

    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    val today = LocalDate.now()
    val logMap = logs.associateBy { it.date }

    // this week's data
    val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)
    val thisWeekLogs = (0L..6L).map { offset ->
        val date = weekStart.plusDays(offset)
        logMap[date.format(formatter)] ?: DailyLog(date = date.format(formatter))
    }
    val weeklyWalking = thisWeekLogs.sumOf { it.walkingMinutes }
    val weeklyScreenTime = thisWeekLogs.sumOf { it.screenTimeMinutes }
    val daysCompleted = thisWeekLogs.count { it.streakMaintained }

    var selectedLog by remember { mutableStateOf<DailyLog?>(null) }
    var selectedDate by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.Companion
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Companion.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.Companion.height(8.dp))

        StatsCard(
            currentStreak = stats.currentStreak,
            totalWalking = stats.totalWalkingMins,
            totalScreenTime = stats.totalScreenTimeMins
        )

        Spacer(modifier = Modifier.Companion.height(16.dp))

        WeeklySummaryCard(
            weeklyWalking = weeklyWalking,
            weeklyScreenTime = weeklyScreenTime,
            daysCompleted = daysCompleted
        )

        Spacer(modifier = Modifier.Companion.height(16.dp))

        MonthCalendarCard(
            logMap = logMap,
            onDayClick = { dateStr, log ->
                if (!LocalDate.parse(dateStr).isAfter(today)) {
                    selectedDate = dateStr
                    selectedLog = log ?: DailyLog(date = dateStr)
                }
            }
        )

        Spacer(modifier = Modifier.Companion.height(24.dp))
    }
    selectedLog?.let { log ->
        DayDetailDialog(
            date = selectedDate ?: log.date,
            log = log,
            onDismiss = { selectedLog = null; selectedDate = null }
        )
    }
}

@Composable
fun StatsCard(currentStreak: Int, totalWalking: Int, totalScreenTime: Int) {
    Card(
        modifier = Modifier.Companion.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(icon = R.drawable.streak_24px, value = "$currentStreak", label = "Day Streak")
            StatItem(
                icon = R.drawable.directions_walk_24px,
                value = "${totalWalking}m",
                label = "Total Walking"
            )
            StatItem(
                icon = R.drawable.timer_24px,
                value = "${totalScreenTime}m",
                label = "Screen Time"
            )
        }
    }
}

@Composable
fun StatItem(icon: Int, value: String, label: String) {
    Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
        Icon(
            painterResource(icon),
            contentDescription = null,
            modifier = Modifier.Companion.size(24.dp)
        )
        Spacer(modifier = Modifier.Companion.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Companion.Bold,
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
        modifier = Modifier.Companion.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.Companion.padding(20.dp)) {
            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Companion.CenterVertically
            ) {
                Text(
                    text = "This week",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Companion.SemiBold
                )
                Text(
                    text = "7-day streak = +${RoomViewModel.Companion.DAILY_COMPLETION_POINTS} pts!",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.Companion.height(16.dp))

            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
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
    Column(horizontalAlignment = Alignment.Companion.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Companion.Bold,
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
fun MonthCalendarCard(logMap: Map<String, DailyLog>, onDayClick: (String, DailyLog?) -> Unit = { _, _ -> }) {
    val today = LocalDate.now()
    val currentMonth = YearMonth.from(today)
    val firstOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    // Monday = 1, Sunday = 7
    val startDayOfWeek = firstOfMonth.dayOfWeek.value
    val dayHeaders = listOf("M", "T", "W", "T", "F", "S", "S")

    Card(
        modifier = Modifier.Companion.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.Companion.padding(20.dp)) {
            Text(
                text = currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) +
                        " " + currentMonth.year,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Companion.SemiBold
            )

            Spacer(modifier = Modifier.Companion.height(16.dp))

            // day of week headers
            Row(
                modifier = Modifier.Companion.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayHeaders.forEach { day ->
                    Box(
                        modifier = Modifier.Companion.size(36.dp),
                        contentAlignment = Alignment.Companion.Center
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Companion.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.Companion.height(4.dp))

            // calendar grid
            var dayCounter = 1
            val totalSlots = startDayOfWeek - 1 + daysInMonth
            val weeks = (totalSlots + 6) / 7

            for (week in 0 until weeks) {
                Row(
                    modifier = Modifier.Companion.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (col in 0..6) {
                        val slotIndex = week * 7 + col
                        val dayOfMonth = slotIndex - (startDayOfWeek - 2)

                        if (dayOfMonth in 1..daysInMonth) {
                            val date = currentMonth.atDay(dayOfMonth)
                            val dateStr = date.format(formatter)
                            val log = logMap[dateStr]
                            val isToday = date == today
                            val isFuture = date.isAfter(today)

                            MonthDayCell(
                                dayNumber = dayOfMonth.toString(),
                                log = log,
                                isToday = isToday,
                                isFuture = isFuture,
                                onClick = { onDayClick(dateStr, log) }
                            )
                        } else {
                            // empty cell for padding
                            Box(modifier = Modifier.Companion.size(36.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.Companion.height(6.dp))
            }
        }
    }
}

@Composable
fun MonthDayCell(dayNumber: String, log: DailyLog?, isToday: Boolean, isFuture: Boolean, onClick: () -> Unit = {}) {
    val hasData = log != null && (log.screenTimeMinutes > 0 || log.walkingMinutes > 0)

    val bgColor = when {
        isFuture -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        log?.streakMaintained == true -> MaterialTheme.colorScheme.primary
        hasData -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when {
        isFuture -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        log?.streakMaintained == true -> MaterialTheme.colorScheme.onPrimary
        hasData -> MaterialTheme.colorScheme.onError
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        contentAlignment = Alignment.Companion.Center,
        modifier = Modifier.Companion
            .size(36.dp)
            .clickable(enabled = !isFuture) { onClick() }
            .clip(CircleShape)
            .background(bgColor)
            .then(
                if (isToday) Modifier.Companion.border(
                    2.dp,
                    MaterialTheme.colorScheme.tertiary,
                    CircleShape
                )
                else Modifier.Companion
            )
    ) {
        Text(
            text = dayNumber,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isToday) FontWeight.Companion.Bold else FontWeight.Companion.Normal,
            color = textColor
        )
    }
}

@Composable
fun DayDetailDialog(date: String, log: DailyLog, onDismiss: () -> Unit) {
    val localDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
    val dayLabel = localDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val monthLabel = localDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("$dayLabel, $monthLabel ${localDate.dayOfMonth}")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Screen Time", style = MaterialTheme.typography.bodyMedium)
                    Text("${log.screenTimeMinutes} / ${log.screenTimeGoal} min",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Walking", style = MaterialTheme.typography.bodyMedium)
                    Text("${log.walkingMinutes} / ${log.walkingGoal} min",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Streak", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        if (log.streakMaintained) "Maintained" else "Missed",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (log.streakMaintained) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}