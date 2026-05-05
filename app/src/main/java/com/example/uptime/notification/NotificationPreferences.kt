package com.example.uptime.notification

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.notificationDataStore by preferencesDataStore("notification_prefs")

class NotificationPreferences(private val context: Context) {

    private object Keys {
        val SCREEN_WARNING_ENABLED = booleanPreferencesKey("screen_warning_enabled")
        val WALKING_REMINDER_ENABLED = booleanPreferencesKey("walking_reminder_enabled")
        val WALKING_REMINDER_HOUR = intPreferencesKey("walking_reminder_hour")
        val WALKING_REMINDER_MINUTE = intPreferencesKey("walking_reminder_minute")
        val SCREEN_WARNING_THRESHOLD = intPreferencesKey("screen_warning_threshold")
    }

    val settingsFlow = context.notificationDataStore.data.map { prefs ->
        NotificationSettings(
            screenWarningEnabled = prefs[Keys.SCREEN_WARNING_ENABLED] ?: false,
            walkingReminderEnabled = prefs[Keys.WALKING_REMINDER_ENABLED] ?: false,
            walkingReminderHour = prefs[Keys.WALKING_REMINDER_HOUR] ?: 18,
            walkingReminderMinute = prefs[Keys.WALKING_REMINDER_MINUTE] ?: 0,
            screenWarningThresholdMinutes = prefs[Keys.SCREEN_WARNING_THRESHOLD] ?: 10
        )
    }

    suspend fun setScreenWarningEnabled(enabled: Boolean) {
        context.notificationDataStore.edit {
            it[Keys.SCREEN_WARNING_ENABLED] = enabled
        }
    }

    suspend fun setWalkingReminderEnabled(enabled: Boolean) {
        context.notificationDataStore.edit {
            it[Keys.WALKING_REMINDER_ENABLED] = enabled
        }
    }

    suspend fun setWalkingReminderTime(hour: Int, minute: Int) {
        context.notificationDataStore.edit {
            it[Keys.WALKING_REMINDER_HOUR] = hour.coerceIn(0, 23)
            it[Keys.WALKING_REMINDER_MINUTE] = minute.coerceIn(0, 59)
        }
    }

    // ensure the screen warning threshold is between 1 and 30 minutes
    suspend fun setScreenWarningThreshold(minutes: Int) {
        context.notificationDataStore.edit {
            it[Keys.SCREEN_WARNING_THRESHOLD] = minutes.coerceIn(1,30)
        }
    }
}

data class NotificationSettings(
    val screenWarningEnabled: Boolean = false,
    val walkingReminderEnabled: Boolean = false,
    val walkingReminderHour: Int = 18,
    val walkingReminderMinute: Int = 0,
    val screenWarningThresholdMinutes: Int = 10
)