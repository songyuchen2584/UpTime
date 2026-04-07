package com.example.uptime.walking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient

@Composable
fun WalkingScreen(
    state: WalkingUiState,
    sdkStatus: Int,
    sensorAvailable: Boolean,
    sensorTracking: Boolean,
    onToggleHealthConnect: (Boolean) -> Unit,
    onToggleSensor: (Boolean) -> Unit,
    onInstallHealthConnect: () -> Unit,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Walking", style = MaterialTheme.typography.headlineSmall)

        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Sources", style = MaterialTheme.typography.titleMedium)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Health Connect")
                        Text(
                            when (sdkStatus) {
                                HealthConnectClient.SDK_AVAILABLE -> "Available"
                                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> "Needs install/update"
                                else -> "Unavailable"
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = state.useHealthConnect,
                        onCheckedChange = onToggleHealthConnect
                    )
                }

                if (sdkStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
                    OutlinedButton(onClick = onInstallHealthConnect) {
                        Text("Install / Update Health Connect")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("Device Step Sensor")
                        Text(
                            when {
                                !sensorAvailable -> "Unavailable"
                                sensorTracking -> "Tracking in background"
                                else -> "Available"
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = state.useDeviceSensor,
                        onCheckedChange = if (sensorAvailable) onToggleSensor else { {} }
                    )
                }

                if (!state.useHealthConnect && !state.useDeviceSensor) {
                    Text(
                        "Enable Health Connect and/or Device Step Sensor to start tracking.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Card {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("Today", style = MaterialTheme.typography.titleMedium)
                Text("Steps: ${state.statsToday.totalSteps}")
                Text("Walking minutes: ${state.statsToday.totalWalkingMinutes}")

                if (state.statsToday.usedEstimatedMinutesFallback) {
                    Text(
                        "Minutes estimated from steps because no walking sessions were available.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(onClick = onRefresh, enabled = !state.loading) {
                    Text("Refresh")
                }
            }
        }

        if (state.loading) {
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }

        state.error?.let {
            Text(
                "Error: $it",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}