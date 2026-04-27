package com.example.uptime.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class UserStatsRepository(private val dao: DailyLogDao) {

    data class UserStats(
        val currentStreak: Int = 0,
        val bestStreak: Int = 0,
        val totalWalkingMins: Int = 0,
        val totalScreenTimeMins: Int = 0,
        val totalPointsSpent: Int = 0,
        val largestSinglePurchase: Int = 0,
        val screenTimeFailCount: Int = 0,
        val consecutiveScreenTimeSuccess: Int = 0,
        val screenTimeOverBy: Int = -1, // -1 means not checked yet
        val walkingUnderBy: Int = -1,
        // add more as needed
    )

    val allLogs: Flow<List<DailyLog>> = dao.observeAllLogs()

    val userStats: Flow<UserStats> = allLogs.map { logs ->
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val today = LocalDate.now().format(formatter)

        // Filter out today because we can't confirm it was completed yet
        val confirmedLogs = logs.filter { it.date != today }
        val streakDates = confirmedLogs.filter { it.streakMaintained }.map { it.date }.toSet()

        // Current Streak
        var streak = 0
        var checkDate = LocalDate.now().minusDays(1)

        while (checkDate.format(formatter) in streakDates) {
            streak++
            checkDate = checkDate.minusDays(1)
        }

        // Best Streak
        var best = 0
        var run = 0
        for (log in confirmedLogs.sortedBy { it.date }) {
            if (log.streakMaintained) { run++; if (run > best) best = run }
            else run = 0
        }

        val screenFails = logs.count { it.screenTimeMinutes > it.screenTimeGoal }

        var consecutiveScreen = 0
        var screenCheckDate = LocalDate.now().minusDays(1)
        while (true) {
            val dateStr = screenCheckDate.format(formatter)
            val log = confirmedLogs.find { it.date == dateStr } ?: break
            if (log.screenTimeMinutes <= log.screenTimeGoal) {
                consecutiveScreen++
                screenCheckDate = screenCheckDate.minusDays(1)
            } else break
        }

        val todayLog = logs.find { it.date == today }
        val screenTimeOverBy = todayLog?.let {
            if (it.screenTimeMinutes > it.screenTimeGoal)
                it.screenTimeMinutes - it.screenTimeGoal
            else -1
        } ?: -1
        val walkingUnderBy = todayLog?.let {
            if (it.walkingMinutes < it.walkingGoal)
                it.walkingGoal - it.walkingMinutes
            else -1
        } ?: -1

        UserStats(
            currentStreak = streak,
            bestStreak = best,
            totalWalkingMins = logs.sumOf { it.walkingMinutes },
            totalScreenTimeMins = logs.sumOf { it.screenTimeMinutes },
            screenTimeFailCount = screenFails,
            consecutiveScreenTimeSuccess = consecutiveScreen,
            screenTimeOverBy = screenTimeOverBy,
            walkingUnderBy = walkingUnderBy
        )
    }
}