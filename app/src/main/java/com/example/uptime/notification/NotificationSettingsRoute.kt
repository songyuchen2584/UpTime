package com.example.uptime.notification

import android.Manifest
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel

private const val TAG = "NotificationRoute"

@Composable
fun NotificationSettingsRoute(
    viewModel: NotificationSettingsViewModel = viewModel()
) {
    var pendingNotificationToggle by remember { mutableStateOf<String?>(null) }
    val settings by viewModel.settings.collectAsState()
    LaunchedEffect(Unit) {
        if (pendingNotificationToggle == null) {
            viewModel.syncNotificationPermissionState()
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                if (pendingNotificationToggle == null) {
                    viewModel.syncNotificationPermissionState()
                } else {
                    Log.d(TAG, "Skipping sync: permission request in progress")
                }
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        Log.d(TAG, "POST_NOTIFICATIONS permission result: granted=$granted")
        when (pendingNotificationToggle) {
            "screen" -> {
                if (granted) {
                    viewModel.setScreenWarningEnabled(true)
                } else {
                    viewModel.syncNotificationPermissionState()
                    Log.d(TAG, "Notification permission denied; leaving requested setting disabled")
                }
            }

            "walking" -> {
                if (granted) {
                    viewModel.setWalkingReminderEnabled(true)
                } else {
                    viewModel.syncNotificationPermissionState()
                    Log.d(TAG, "Notification permission denied; leaving requested setting disabled")
                }
            }
        }
        pendingNotificationToggle = null
    }

    NotificationSettingsScreen(
        settings = settings,
        onScreenWarningToggle = { enabled ->
            Log.d(TAG, "Screen warning toggle changed: enabled=$enabled")
            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Log.d(TAG, "Requesting POST_NOTIFICATIONS permission for screen warning")
                pendingNotificationToggle = "screen"
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

            } else {
                viewModel.setScreenWarningEnabled(enabled)
            }
        },
        onWalkingReminderToggle = { enabled ->
            Log.d(TAG, "Walking reminder toggle changed: enabled=$enabled")
            if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Log.d(TAG, "Requesting POST_NOTIFICATIONS permission for walking reminder")
                pendingNotificationToggle = "walking"
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)

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