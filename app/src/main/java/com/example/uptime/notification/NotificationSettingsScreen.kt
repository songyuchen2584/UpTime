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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationSettingsScreen(
    settings: NotificationSettings,
    onScreenWarningToggle: (Boolean) -> Unit,
    onWalkingReminderToggle: (Boolean) -> Unit,
    onWalkingTimeChange: (Int, Int) -> Unit,
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
                        onThresholdChange(settings.screenWarningThresholdMinutes - 1)
                    }) {
                        Text("-1")
                    }

                    Text(
                        "${settings.screenWarningThresholdMinutes} min",
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )

                    Button(onClick = {
                        onThresholdChange(settings.screenWarningThresholdMinutes + 1)
                    }) {
                        Text("+1")
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

                var showTimePicker by remember { mutableStateOf(false) }

                Button(onClick = { showTimePicker = true }) {
                    Text("Change reminder time")
                }

                if (showTimePicker) {
                    val timePickerState = rememberTimePickerState(
                        initialHour = settings.walkingReminderHour,
                        initialMinute = settings.walkingReminderMinute,
                        is24Hour = false
                    )

                    AlertDialog(
                        onDismissRequest = { showTimePicker = false },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    onWalkingTimeChange(
                                        timePickerState.hour,
                                        timePickerState.minute
                                    )
                                    showTimePicker = false
                                }
                            ) {
                                Text("Save")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showTimePicker = false }) {
                                Text("Cancel")
                            }
                        },
                        title = {
                            Text("Choose walking reminder time")
                        },
                        text = {
                            TimePicker(state = timePickerState)
                        }
                    )
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