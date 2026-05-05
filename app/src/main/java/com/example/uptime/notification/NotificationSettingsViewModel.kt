package com.example.uptime.notification

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val TAG = "NotificationVM"

class NotificationSettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val prefs = NotificationPreferences(application)

    val settings = prefs.settingsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        NotificationSettings()
    )

    private fun hasNotificationPermission(): Boolean {
        val context = getApplication<Application>()

        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
    }

    fun setScreenWarningEnabled(enabled: Boolean) {
        Log.d(TAG, "setScreenWarningEnabled called: enabled=$enabled")
        viewModelScope.launch {
            try {
                prefs.setScreenWarningEnabled(enabled)
                Log.d(TAG, "Screen warning preference saved: enabled=$enabled")
                syncService()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update screen warning preference", e)
            }
        }
    }

    fun setWalkingReminderEnabled(enabled: Boolean) {
        Log.d(TAG, "setWalkingReminderEnabled called: enabled=$enabled")
        viewModelScope.launch {
            try {
                prefs.setWalkingReminderEnabled(enabled)
                Log.d(TAG, "Walking reminder preference saved: enabled=$enabled")
                syncService()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update walking reminder preference", e)
            }
        }
    }

    fun setWalkingReminderTime(hour: Int, minute: Int) {
        Log.d(TAG, "setWalkingReminderTime called: hour=$hour, minute=$minute")
        viewModelScope.launch {
            try {
                prefs.setWalkingReminderTime(hour, minute)
                Log.d(TAG, "Walking reminder time saved")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update walking reminder time", e)
            }
        }
    }

    fun setScreenWarningThreshold(minutes: Int) {
        Log.d(TAG, "setScreenWarningThreshold called: minutes=$minutes")
        viewModelScope.launch {
            try {
                prefs.setScreenWarningThreshold(minutes)
                Log.d(TAG, "Screen warning threshold saved: minutes=$minutes")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update screen warning threshold", e)
            }
        }
    }

    private fun syncService() {
        val context = getApplication<Application>()
        // check notification permission first
        if (!hasNotificationPermission()) {
            viewModelScope.launch {
                Log.d("NotificationVM", "Notification permission missing. Disabling notification preferences.")
                prefs.setScreenWarningEnabled(false)
                prefs.setWalkingReminderEnabled(false)
            }
            context.stopService(Intent(context, NotificationMonitorService::class.java))
            return
        }

        val enabled = settings.value.screenWarningEnabled ||
                settings.value.walkingReminderEnabled

        val intent = Intent(context, NotificationMonitorService::class.java)

        Log.d(TAG, "syncService: screenWarning=${settings.value.screenWarningEnabled}, walkingReminder=${settings.value.walkingReminderEnabled}, serviceEnabled=$enabled")

        try {
            if (enabled) {
                Log.d(TAG, "Starting NotificationMonitorService")
                ContextCompat.startForegroundService(context, intent)
            } else {
                Log.d(TAG, "Stopping NotificationMonitorService")
                context.stopService(intent)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync NotificationMonitorService", e)
        }
    }
}