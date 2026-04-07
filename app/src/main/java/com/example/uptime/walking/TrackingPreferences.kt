package com.example.uptime.walking

import android.content.Context

class TrackingPreferences(context: Context) {
    private val prefs =
        context.applicationContext.getSharedPreferences("walking_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_HEALTH_CONNECT_ENABLED = "health_connect_enabled"
        private const val KEY_DEVICE_SENSOR_ENABLED = "device_sensor_enabled"
    }

    fun isHealthConnectEnabled(): Boolean {
        return prefs.getBoolean(KEY_HEALTH_CONNECT_ENABLED, false)
    }

    fun setHealthConnectEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HEALTH_CONNECT_ENABLED, enabled).apply()
    }

    fun isDeviceSensorEnabled(): Boolean {
        return prefs.getBoolean(KEY_DEVICE_SENSOR_ENABLED, false)
    }

    fun setDeviceSensorEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DEVICE_SENSOR_ENABLED, enabled).apply()
    }
}