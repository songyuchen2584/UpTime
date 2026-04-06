package com.example.uptime.walking

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uptime.DashboardViewModel
import com.example.uptime.walking.viewmodel.WalkingViewModel

@Composable
fun WalkingRoute(
    walkingViewModel: WalkingViewModel = viewModel(),
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by walkingViewModel.state.collectAsState()

    val hcPermissionLauncher = rememberLauncherForActivityResult(
        contract = walkingViewModel.healthConnectPermissionContract()
    ) {
        walkingViewModel.refreshToday()
    }

    val sensorPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            walkingViewModel.setMethodEnabled(TrackingMethod.DEVICE_SENSOR, true)
            walkingViewModel.refreshToday()
        }
    }

    LaunchedEffect(Unit) {
        if (state.useDeviceSensor && walkingViewModel.hasSensorPermission()) {
            walkingViewModel.setMethodEnabled(TrackingMethod.DEVICE_SENSOR, true)
        }
        walkingViewModel.refreshToday()
    }

    LaunchedEffect(state.statsToday.totalWalkingMinutes) {
        dashboardViewModel.updateWalking(state.statsToday.totalWalkingMinutes.toInt())
    }

    WalkingScreen(
        state = state,
        sdkStatus = walkingViewModel.healthConnectSdkStatus(),
        sensorAvailable = walkingViewModel.isSensorAvailable(),
        onToggleHealthConnect = { enabled ->
            if (enabled) {
                hcPermissionLauncher.launch(walkingViewModel.healthConnectPermissions)
            }
            walkingViewModel.setMethodEnabled(TrackingMethod.HEALTH_CONNECT, enabled)
            walkingViewModel.refreshToday()
        },
        onToggleSensor = { enabled ->
            if (enabled) {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED

                if (granted) {
                    walkingViewModel.setMethodEnabled(TrackingMethod.DEVICE_SENSOR, true)
                } else {
                    sensorPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                }
            } else {
                walkingViewModel.setMethodEnabled(TrackingMethod.DEVICE_SENSOR, false)
            }
            walkingViewModel.refreshToday()
        },
        onInstallHealthConnect = {
            context.startActivity(walkingViewModel.healthConnectInstallIntent())
        },
        onRefresh = { walkingViewModel.refreshToday() }
    )
}