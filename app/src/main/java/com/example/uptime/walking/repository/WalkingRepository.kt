package com.example.uptime.walking

import com.example.uptime.walking.datasource.DeviceSensorStepsDataSource
import com.example.uptime.walking.datasource.HealthConnectStepsDataSource
import com.example.uptime.walking.merge.WalkingMergeEngine
import com.example.uptime.walking.model.WalkingStats


class WalkingRepository(
    private val healthConnectSource: HealthConnectStepsDataSource,
    private val deviceSensorSource: DeviceSensorStepsDataSource
) {
    private val enabledMethods = linkedSetOf<TrackingMethod>()

    fun isMethodEnabled(method: TrackingMethod): Boolean = method in enabledMethods

    fun setMethodEnabled(method: TrackingMethod, enabled: Boolean) {
        if (enabled) enabledMethods += method else enabledMethods -= method
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

        val usedFallback = measuredMinutes == 0L && totalSteps > 0L
        val finalMinutes = when {
            measuredMinutes > 0L -> measuredMinutes
            totalSteps > 0L -> maxOf(1L, totalSteps / 100L)
            else -> 0L
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