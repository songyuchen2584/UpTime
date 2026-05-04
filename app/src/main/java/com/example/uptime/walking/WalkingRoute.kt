package com.example.uptime.walking

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uptime.dashboard.DashboardViewModel
import com.example.uptime.walking.datasource.StepTrackingService
import com.example.uptime.walking.viewmodel.WalkingViewModel

private const val TAG = "WalkingRoute"

@Composable
fun WalkingRoute(
    walkingViewModel: WalkingViewModel = viewModel(),
    dashboardViewModel: DashboardViewModel = viewModel()
) {
    val context = LocalContext.current
    val prefs = TrackingPreferences(context.applicationContext)
    val state by walkingViewModel.state.collectAsState()

    val hcPermissionLauncher = rememberLauncherForActivityResult(
        contract = walkingViewModel.healthConnectPermissionContract()
    ) { permissions ->
        Log.d(TAG, "Health Connect permission result: grantedCount=${permissions.size}")
        walkingViewModel.setMethodEnabled(TrackingMethod.HEALTH_CONNECT, true)
        walkingViewModel.refreshToday()
    }

    val sensorPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        Log.d(TAG, "Activity recognition permission result: granted=$granted")
        if (granted) {
            prefs.setDeviceSensorEnabled(true)
            startStepTrackingService(context)
            walkingViewModel.setMethodEnabled(TrackingMethod.DEVICE_SENSOR, true)
            walkingViewModel.refreshToday()
        } else {
            Log.d(TAG, "Activity recognition permission denied; device sensor remains disabled")
        }
    }

    LaunchedEffect(Unit) {
        Log.d(TAG, "WalkingRoute launched: prefHealthConnect=${prefs.isHealthConnectEnabled()}, prefDeviceSensor=${prefs.isDeviceSensorEnabled()}")
        if (prefs.isHealthConnectEnabled()) {
            walkingViewModel.setMethodEnabled(TrackingMethod.HEALTH_CONNECT, true)
        }

        if (prefs.isDeviceSensorEnabled()) {
            walkingViewModel.setMethodEnabled(TrackingMethod.DEVICE_SENSOR, true)
            startStepTrackingService(context) // ensure background resumes
        }

        walkingViewModel.refreshToday()
    }

    LaunchedEffect(state.statsToday.totalWalkingMinutes) {
        Log.d(TAG, "Dashboard walking update requested: minutes=${state.statsToday.totalWalkingMinutes}")
        dashboardViewModel.updateWalking(state.statsToday.totalWalkingMinutes.toInt())
    }

    WalkingScreen(
        state = state,
        sdkStatus = walkingViewModel.healthConnectSdkStatus(),
        sensorAvailable = walkingViewModel.isSensorAvailable(),
        sensorTracking = walkingViewModel.isSensorTracking(),
        onToggleHealthConnect = { enabled ->
            Log.d(TAG, "Health Connect toggle changed: enabled=$enabled")
            if (enabled) {
                prefs.setHealthConnectEnabled(true)
                hcPermissionLauncher.launch(walkingViewModel.healthConnectPermissions)
            } else {
                prefs.setHealthConnectEnabled(false)
                walkingViewModel.setMethodEnabled(TrackingMethod.HEALTH_CONNECT, false)
                walkingViewModel.refreshToday()
            }
        },
        onToggleSensor = { enabled ->
            Log.d(TAG, "Device sensor toggle changed: enabled=$enabled")
            if (enabled) {
                val granted = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) == PackageManager.PERMISSION_GRANTED

                Log.d(TAG, "Device sensor permission already granted=$granted")

                if (granted) {
                    prefs.setDeviceSensorEnabled(true)

                    startStepTrackingService(context)

                    walkingViewModel.setMethodEnabled(TrackingMethod.DEVICE_SENSOR, true)
                    walkingViewModel.refreshToday()
                } else {
                    sensorPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
                }
            } else {
                prefs.setDeviceSensorEnabled(false)

                stopStepTrackingService(context)

                walkingViewModel.setMethodEnabled(TrackingMethod.DEVICE_SENSOR, false)
                walkingViewModel.refreshToday()
            }
        },
        onInstallHealthConnect = {
            Log.d(TAG, "Opening Health Connect install/update intent")
            context.startActivity(walkingViewModel.healthConnectInstallIntent())
        },
        onRefresh = {
            Log.d(TAG, "Manual walking refresh requested")
            walkingViewModel.refreshToday()
        }
    )
}

private fun startStepTrackingService(context: Context) {
    Log.d(TAG, "Starting StepTrackingService")
    val intent = Intent(context, StepTrackingService::class.java)
    ContextCompat.startForegroundService(context, intent)
}

private fun stopStepTrackingService(context: Context) {
    Log.d(TAG, "Stopping StepTrackingService")
    val intent = Intent(context, StepTrackingService::class.java)
    context.stopService(intent)
}