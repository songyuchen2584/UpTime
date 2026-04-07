package com.example.uptime

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class UserStatsRepository(private val dao: DailyLogDao) {

    data class UserStats(
        val currentStreak: Int = 0,
        val bestStreak: Int = 0,
        val totalWalkingMins: Int = 0,
        val totalScreenTimeMins: Int = 0
        // add more as needed
    )

    val allLogs: Flow<List<DailyLog>> = dao.observeAllLogs()

    val userStats: Flow<UserStats> = allLogs.map { logs ->
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val today = LocalDate.now().format(formatter)
        val streakDates = logs.filter { it.streakMaintained }.map { it.date }.toSet()

        // Current Streak
        var streak = 0
        var checkDate = LocalDate.now()
        if (today in streakDates) {
            streak++
            checkDate = checkDate.minusDays(1)
        } else {
            checkDate = checkDate.minusDays(1)
        }
        while (checkDate.format(formatter) in streakDates) {
            streak++
            checkDate = checkDate.minusDays(1)
        }

        // Best Streak
        var best = 0
        var run = 0
        for (log in logs.sortedBy { it.date }) {
            if (log.streakMaintained) { run++; if (run > best) best = run }
            else run = 0
        }

        UserStats(
            currentStreak = streak,
            bestStreak = best,
            totalWalkingMins = logs.sumOf { it.walkingMinutes },
            totalScreenTimeMins = logs.sumOf { it.screenTimeMinutes }
        )
    }
}