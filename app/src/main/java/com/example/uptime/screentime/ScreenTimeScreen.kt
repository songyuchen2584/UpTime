package com.example.uptime.screentime

import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.example.uptime.screentime.models.ScreenTimeUiState

@Composable
fun ScreenTimeScreen(
    uiState: ScreenTimeUiState,
    onOpenUsageAccessSettings: () -> Unit,
    onTogglePackage: (String, Boolean) -> Unit,
    onRefresh: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showRecommendedOnly by remember { mutableStateOf(false) }

    val filteredApps = uiState.installedApps.filter { app ->
        val matchesSearch =
            app.appLabel.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)

        val matchesFilter =
            !showRecommendedOnly || app.isRecommendedSocial

        matchesSearch && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Screen Time",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (!uiState.hasUsageAccess) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Usage Access Required",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Enable Usage Access so the app can read screen-time data for selected apps."
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(onClick = onOpenUsageAccessSettings) {
                        Text("Open Settings")
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Tracked today: ${formatDuration(uiState.totalTrackedTimeMs)}",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Today's usage for selected apps",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (uiState.todayUsage.isEmpty()) {
                    Text(
                        text = "No usage recorded yet.",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    uiState.todayUsage.take(3).forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item.appLabel,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall
                            )

                            Text(
                                text = formatDuration(item.totalTimeMs),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    if (uiState.todayUsage.size > 3) {
                        Text(
                            text = "+${uiState.todayUsage.size - 3} more tracked apps",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onRefresh,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Refresh Screen Time")
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(12.dp))
        }

        Text(
            text = "Choose apps to track",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Search apps...") }
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Show recommended social apps only",
                modifier = Modifier.weight(1f)
            )

            Switch(
                checked = showRecommendedOnly,
                onCheckedChange = { showRecommendedOnly = it }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 🔥 INNER SCROLLABLE LIST (fixed height)
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp) // adjust height as needed
        ) {
            items(
                items = filteredApps,
                key = { it.packageName }
            ) { app ->
                val checked = app.packageName in uiState.selectedPackages

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onTogglePackage(app.packageName, !checked)
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AppIcon(
                            icon = app.icon,
                            appLabel = app.appLabel
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = app.appLabel,
                                    style = MaterialTheme.typography.bodyLarge
                                )

                                if (app.isRecommendedSocial) {
                                    Spacer(modifier = Modifier.width(8.dp))

                                    AssistChip(
                                        onClick = {},
                                        label = {
                                            Text("Social")
                                        }
                                    )
                                }
                            }

                            Text(
                                text = app.packageName,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Checkbox(
                            checked = checked,
                            onCheckedChange = { isChecked ->
                                onTogglePackage(app.packageName, isChecked)
                            }
                        )
                    }

                    Divider(modifier = Modifier.padding(top = 8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun AppIcon(
    icon: Drawable?,
    appLabel: String
) {
    if (icon != null) {
        Image(
            bitmap = icon.toBitmap().asImageBitmap(),
            contentDescription = appLabel,
            modifier = Modifier.size(40.dp)
        )
    } else {
        Card(
            modifier = Modifier.size(40.dp)
        ) {
            Text(
                text = appLabel.firstOrNull()?.uppercase() ?: "?",
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalMinutes = ms / 60_000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return if (hours > 0) {
        "${hours}h ${minutes}m"
    } else {
        "${minutes}m"
    }
}