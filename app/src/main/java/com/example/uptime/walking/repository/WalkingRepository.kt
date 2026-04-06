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

        val totalSteps = when {
            useHealthConnect ->
                healthConnectSource.getTotalSteps(startMillis, endMillis)
            useSensor ->
                deviceSensorSource.getTotalSteps(startMillis, endMillis)
            else -> 0L
        }

        val sessionCandidates = buildList {
            if (useHealthConnect) {
                addAll(healthConnectSource.getWalkingSessions(startMillis, endMillis))
            }
            if (useSensor) {
                addAll(deviceSensorSource.getWalkingSessions(startMillis, endMillis))
            }
        }

        val mergedSessions = WalkingMergeEngine.mergeSessions(sessionCandidates)
        val totalMinutes = WalkingMergeEngine.totalMinutes(mergedSessions)

        return WalkingStats(
            totalSteps = totalSteps,
            totalWalkingMinutes = totalMinutes,
            mergedSessions = mergedSessions
        )
    }

    suspend fun getStepCount(startMillis: Long, endMillis: Long): Long {
        return getWalkingStats(startMillis, endMillis).totalSteps
    }

    suspend fun getWalkingMinutes(startMillis: Long, endMillis: Long): Long {
        return getWalkingStats(startMillis, endMillis).totalWalkingMinutes
    }
}