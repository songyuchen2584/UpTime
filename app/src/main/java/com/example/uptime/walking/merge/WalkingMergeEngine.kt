package com.example.uptime.walking.merge

import com.example.uptime.walking.model.WalkingSessionInterval

object WalkingMergeEngine {

    fun mergeSessions(
        sessions: List<WalkingSessionInterval>
    ): List<WalkingSessionInterval> {
        if (sessions.isEmpty()) return emptyList()

        val sorted = sessions.sortedBy { it.startMillis }
        val merged = mutableListOf<WalkingSessionInterval>()
        var current = sorted.first()

        for (next in sorted.drop(1)) {
            val overlaps = next.startMillis <= current.endMillis
            current = if (overlaps) {
                WalkingSessionInterval(
                    startMillis = minOf(current.startMillis, next.startMillis),
                    endMillis = maxOf(current.endMillis, next.endMillis)
                )
            } else {
                merged += current
                next
            }
        }

        merged += current
        return merged
    }

    fun totalMinutes(
        sessions: List<WalkingSessionInterval>
    ): Long {
        return sessions.sumOf { it.durationMinutes }
    }
}
