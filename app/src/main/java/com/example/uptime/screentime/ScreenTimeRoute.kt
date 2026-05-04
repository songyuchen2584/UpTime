package com.example.uptime.screentime

import android.app.Application
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.uptime.screentime.viewmodel.ScreenTimeViewModel

private const val TAG = "ScreenTimeRoute"

@Composable
fun ScreenTimeRoute(
    updateScreenTime: (Int) -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val factory = remember(application, updateScreenTime) {
        ScreenTimeViewModelFactory(
            application = application,
            updateScreenTime = { snapshot ->
                val totalMinutes = (snapshot.totalTrackedTimeMs / 60_000L).toInt()
                Log.d(TAG, "Dashboard screen time update requested: totalMinutes=$totalMinutes, trackedApps=${snapshot.trackedApps.size}")
                updateScreenTime(totalMinutes)
            }
        )
    }

    val viewModel: ScreenTimeViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                Log.d(TAG, "Lifecycle ON_RESUME observed; refreshing screen time")
                viewModel.onReturnedFromSettings()
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    ScreenTimeScreen(
        uiState = uiState,
        onOpenUsageAccessSettings = {
            Log.d(TAG, "Opening Usage Access settings")
            ScreenTimePermission.openUsageAccessSettings(context)
        },
        onTogglePackage = { packageName, isSelected ->
            Log.d(TAG, "Package selection changed: packageName=$packageName, isSelected=$isSelected")
            viewModel.togglePackage(packageName, isSelected)
        },
        onRefresh = {
            Log.d(TAG, "Manual screen time refresh requested")
            viewModel.refresh()
        }
    )
}