package com.example.uptime.notification

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun NotificationSettingsRoute(
    viewModel: NotificationSettingsViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.setScreenWarningEnabled(true)
        }
    }

    NotificationSettingsScreen(
        settings = settings,
        onScreenWarningToggle = { enabled ->
            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.setScreenWarningEnabled(enabled)
            }
        },
        onWalkingReminderToggle = { enabled ->
            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                viewModel.setWalkingReminderEnabled(true)
            } else {
                viewModel.setWalkingReminderEnabled(enabled)
            }
        },
        onWalkingTimeChange = { hour, minute ->
            viewModel.setWalkingReminderTime(
                hour = hour,
                minute = minute
            )
        },
        onThresholdChange = { minutes ->
            viewModel.setScreenWarningThreshold(minutes)
        }
    )
}