package com.example.uptime.walking.model

import com.example.uptime.walking.TrackingMethod

data class WalkingSessionInterval(
    val startMillis: Long,
    val endMillis: Long
) {
    val durationMinutes: Long
        get() = ((endMillis - startMillis).coerceAtLeast(0L)) / 60_000L
}

data class WalkingStats(
    val totalSteps: Long = 0L,
    val totalWalkingMinutes: Long = 0L,
    val mergedSessions: List<WalkingSessionInterval> = emptyList()
)