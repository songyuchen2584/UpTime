package com.example.uptime.notification

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun NotificationSettingsScreen(
    settings: NotificationSettings,
    onScreenWarningToggle: (Boolean) -> Unit,
    onWalkingReminderToggle: (Boolean) -> Unit,
    onWalkingHourChange: (Int) -> Unit,
    onWalkingMinuteChange: (Int) -> Unit,
    onThresholdChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Notifications", style = MaterialTheme.typography.headlineSmall)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Screen time warning")
                        Text(
                            "Warn me when I have less than ${settings.screenWarningThresholdMinutes} minutes left.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Switch(
                        checked = settings.screenWarningEnabled,
                        onCheckedChange = onScreenWarningToggle
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        onThresholdChange(settings.screenWarningThresholdMinutes - 5)
                    }) {
                        Text("-5")
                    }

                    Text(
                        "${settings.screenWarningThresholdMinutes} min",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    Button(onClick = {
                        onThresholdChange(settings.screenWarningThresholdMinutes + 5)
                    }) {
                        Text("+5")
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Walking reminder")
                        Text(
                            "Send walking progress reminder at ${formatTime(settings.walkingReminderHour, settings.walkingReminderMinute)}.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Switch(
                        checked = settings.walkingReminderEnabled,
                        onCheckedChange = onWalkingReminderToggle
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        onWalkingHourChange(settings.walkingReminderHour - 1)
                    }) {
                        Text("Hour -")
                    }

                    Button(onClick = {
                        onWalkingHourChange(settings.walkingReminderHour + 1)
                    }) {
                        Text("Hour +")
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        onWalkingMinuteChange(settings.walkingReminderMinute - 5)
                    }) {
                        Text("Min -")
                    }

                    Button(onClick = {
                        onWalkingMinuteChange(settings.walkingReminderMinute + 5)
                    }) {
                        Text("Min +")
                    }
                }
            }
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val suffix = if (hour >= 12) "PM" else "AM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }

    return "%d:%02d %s".format(displayHour, minute, suffix)
}