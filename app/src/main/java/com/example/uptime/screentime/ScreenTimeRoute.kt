package com.example.uptime.screentime

import android.app.Application
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
            ScreenTimePermission.openUsageAccessSettings(context)
        },
        onTogglePackage = { packageName, isSelected ->
            viewModel.togglePackage(packageName, isSelected)
        },
        onRefresh = {
            viewModel.refresh()
        }
    )
}