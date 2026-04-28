package com.example.uptime.notification

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class NotificationSettingsViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val prefs = NotificationPreferences(application)

    val settings = prefs.settingsFlow.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        NotificationSettings()
    )

    fun setScreenWarningEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setScreenWarningEnabled(enabled)
            syncService()
        }
    }

    fun setWalkingReminderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            prefs.setWalkingReminderEnabled(enabled)
            syncService()
        }
    }

    fun setWalkingReminderTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            prefs.setWalkingReminderTime(hour, minute)
        }
    }

    fun setScreenWarningThreshold(minutes: Int) {
        viewModelScope.launch {
            prefs.setScreenWarningThreshold(minutes)
        }
    }

    private fun syncService() {
        val context = getApplication<Application>()
        val enabled = settings.value.screenWarningEnabled ||
                settings.value.walkingReminderEnabled

        val intent = Intent(context, NotificationMonitorService::class.java)

        if (enabled) {
            ContextCompat.startForegroundService(context, intent)
        } else {
            context.stopService(intent)
        }
    }
}