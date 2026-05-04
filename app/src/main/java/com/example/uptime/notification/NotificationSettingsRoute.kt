package com.example.uptime.notification

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel

private const val TAG = "NotificationRoute"

@Composable
fun NotificationSettingsRoute(
    viewModel: NotificationSettingsViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        Log.d(TAG, "POST_NOTIFICATIONS permission result: granted=$granted")
        if (granted) {
            viewModel.setScreenWarningEnabled(true)
        } else {
            Log.d(TAG, "Notification permission denied; leaving requested setting disabled")
        }
    }

    NotificationSettingsScreen(
        settings = settings,
        onScreenWarningToggle = { enabled ->
            Log.d(TAG, "Screen warning toggle changed: enabled=$enabled")
            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Log.d(TAG, "Requesting POST_NOTIFICATIONS permission for screen warning")
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                viewModel.setScreenWarningEnabled(enabled)
            }
        },
        onWalkingReminderToggle = { enabled ->
            Log.d(TAG, "Walking reminder toggle changed: enabled=$enabled")
            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Log.d(TAG, "Requesting POST_NOTIFICATIONS permission for walking reminder")
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                viewModel.setWalkingReminderEnabled(true)
            } else {
                viewModel.setWalkingReminderEnabled(enabled)
            }
        },
        onWalkingTimeChange = { hour, minute ->
            Log.d(TAG, "Walking reminder time changed: hour=$hour, minute=$minute")
            viewModel.setWalkingReminderTime(
                hour = hour,
                minute = minute
            )
        },
        onThresholdChange = { minutes ->
            Log.d(TAG, "Screen warning threshold changed: minutes=$minutes")
            viewModel.setScreenWarningThreshold(minutes)
        }
    )
}