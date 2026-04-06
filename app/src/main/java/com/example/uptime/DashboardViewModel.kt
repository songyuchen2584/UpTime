package com.example.uptime

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = Room.databaseBuilder(
        application,
        UpTimeDatabase::class.java,
        "uptime_db"
    ).build().dailyLogDao()

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    private fun todayString(): String = LocalDate.now().format(formatter)

    // observe today's log reactively
    val todayLog: Flow<DailyLog?> = dao.observeLogForDate(todayString())

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak

    private val _bestStreak = MutableStateFlow(0)
    val bestStreak: StateFlow<Int> = _bestStreak

    init {
        // ensure today's row exists
        viewModelScope.launch {
            if (dao.getLogForDate(todayString()) == null) {
                dao.upsertLog(DailyLog(date = todayString()))
            }
            recalculateStreaks()
        }
    }

    fun updateScreenTime(minutes: Int) {
        viewModelScope.launch {
            val today = todayString()
            val log = dao.getLogForDate(today) ?: DailyLog(date = today)
            val updated = log.copy(
                screenTimeMinutes = minutes,
                streakMaintained = minutes <= log.screenTimeGoal
                        && log.walkingMinutes >= log.walkingGoal
            )
            dao.upsertLog(updated)
            recalculateStreaks()
        }
    }

    fun updateWalking(minutes: Int) {
        viewModelScope.launch {
            val today = todayString()
            val log = dao.getLogForDate(today) ?: DailyLog(date = today)
            val updated = log.copy(
                walkingMinutes = minutes,
                streakMaintained = log.screenTimeMinutes <= log.screenTimeGoal
                        && minutes >= log.walkingGoal
            )
            dao.upsertLog(updated)
            recalculateStreaks()
        }
    }

    fun updateGoals(screenTimeGoal: Int, walkingGoal: Int) {
        viewModelScope.launch {
            val today = todayString()
            val log = dao.getLogForDate(today) ?: DailyLog(date = today)
            val updated = log.copy(
                screenTimeGoal = screenTimeGoal,
                walkingGoal = walkingGoal,
                streakMaintained = log.screenTimeMinutes <= screenTimeGoal
                        && log.walkingMinutes >= walkingGoal
            )
            dao.upsertLog(updated)
            recalculateStreaks()
        }
    }

    // walk backwards through recent logs to count consecutive streak days
    private suspend fun recalculateStreaks() {
        val logs = dao.getRecentLogs(90)
        val streakDates = logs.filter { it.streakMaintained }.map { it.date }.toSet()

        var streak = 0
        var checkDate = LocalDate.now()

        // if today is complete, count it; otherwise start from yesterday
        if (todayString() in streakDates) {
            streak++
            checkDate = checkDate.minusDays(1)
        } else {
            checkDate = checkDate.minusDays(1)
        }

        while (checkDate.format(formatter) in streakDates) {
            streak++
            checkDate = checkDate.minusDays(1)
        }

        _currentStreak.value = streak

        // calculate best streak from all logs
        var best = 0
        var run = 0
        val sortedDates = logs.sortedBy { it.date }
        for (i in sortedDates.indices) {
            if (sortedDates[i].streakMaintained) {
                run++
                if (run > best) best = run
            } else {
                run = 0
            }
        }
        _bestStreak.value = best
    }
}