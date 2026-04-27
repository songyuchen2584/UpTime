package com.example.uptime.walking

import com.example.uptime.walking.datasource.DeviceSensorStepsDataSource
import com.example.uptime.walking.datasource.HealthConnectStepsDataSource
import com.example.uptime.walking.merge.WalkingMergeEngine
import com.example.uptime.walking.model.WalkingStats


class WalkingRepository(
    private val healthConnectSource: HealthConnectStepsDataSource,
    private val deviceSensorSource: DeviceSensorStepsDataSource,
    private val prefs: TrackingPreferences
) {
    private val enabledMethods = linkedSetOf<TrackingMethod>().apply {
        if (prefs.isHealthConnectEnabled()) add(TrackingMethod.HEALTH_CONNECT)
        if (prefs.isDeviceSensorEnabled()) add(TrackingMethod.DEVICE_SENSOR)
    }

    fun isMethodEnabled(method: TrackingMethod): Boolean = method in enabledMethods

    fun setMethodEnabled(method: TrackingMethod, enabled: Boolean) {
        if (enabled) enabledMethods += method else enabledMethods -= method

        when (method) {
            TrackingMethod.HEALTH_CONNECT -> prefs.setHealthConnectEnabled(enabled)
            TrackingMethod.DEVICE_SENSOR -> prefs.setDeviceSensorEnabled(enabled)
        }
    }
    suspend fun getWalkingStats(
        startMillis: Long,
        endMillis: Long
    ): WalkingStats {
        val useHealthConnect = TrackingMethod.HEALTH_CONNECT in enabledMethods
        val useSensor = TrackingMethod.DEVICE_SENSOR in enabledMethods

        if (!useHealthConnect && !useSensor) {
            return WalkingStats()
        }

        val healthConnectSteps = if (useHealthConnect) {
            healthConnectSource.getTotalSteps(startMillis, endMillis)
        } else {
            0L
        }

        val sensorSteps = if (useSensor) {
            deviceSensorSource.getTotalSteps(startMillis, endMillis)
        } else {
            0L
        }

        val totalSteps = maxOf(healthConnectSteps, sensorSteps)

        val sessionCandidates = buildList {
            if (useHealthConnect) {
                addAll(healthConnectSource.getWalkingSessions(startMillis, endMillis))
            }
            if (useSensor) {
                addAll(deviceSensorSource.getWalkingSessions(startMillis, endMillis))
            }
        }

        val mergedSessions = WalkingMergeEngine.mergeSessions(sessionCandidates)
        val measuredMinutes = WalkingMergeEngine.totalMinutes(mergedSessions)

        val estimatedMinutes = totalSteps / 100L

        val usedFallback = when {
            totalSteps == 0L -> false
            measuredMinutes == 0L -> true
            else -> {
                val ratio = measuredMinutes.toDouble() / estimatedMinutes.coerceAtLeast(1)
                ratio < 0.5   // threshold (tune this)
            }
        }

        val finalMinutes = if (usedFallback) {
            maxOf(1L, estimatedMinutes)
        } else {
            measuredMinutes
        }

        return WalkingStats(
            totalSteps = totalSteps,
            totalWalkingMinutes = finalMinutes,
            mergedSessions = mergedSessions,
            usedEstimatedMinutesFallback = usedFallback
        )
    }

    suspend fun getStepCount(startMillis: Long, endMillis: Long): Long {
        return getWalkingStats(startMillis, endMillis).totalSteps
    }

    suspend fun getWalkingMinutes(startMillis: Long, endMillis: Long): Long {
        return getWalkingStats(startMillis, endMillis).totalWalkingMinutes
    }
}