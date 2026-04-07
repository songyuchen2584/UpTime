package com.example.uptime

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val db  = UpTimeDatabase.getDatabase(application)
    private val dao = db.dailyLogDao()
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    private fun todayString() = LocalDate.now().format(formatter)

    val repository = UserStatsRepository(dao)

    val todayLog: Flow<DailyLog?> = dao.observeLogForDate(todayString())

    val userStats: StateFlow<UserStatsRepository.UserStats> = repository.userStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),
            UserStatsRepository.UserStats()
        )

    init {
        viewModelScope.launch {
            if (dao.getLogForDate(todayString()) == null) {
                dao.upsertLog(DailyLog(date = todayString()))
            }
        }
    }

    fun updateScreenTime(minutes: Int) {
        viewModelScope.launch {
            val today = todayString()
            val log = dao.getLogForDate(today) ?: DailyLog(date = today)
            dao.upsertLog(log.copy(
                screenTimeMinutes = minutes,
                streakMaintained  = minutes <= log.screenTimeGoal
                        && log.walkingMinutes >= log.walkingGoal
            ))
        }
    }

    fun updateWalking(minutes: Int) {
        viewModelScope.launch {
            val today = todayString()
            val log = dao.getLogForDate(today) ?: DailyLog(date = today)
            dao.upsertLog(log.copy(
                walkingMinutes   = minutes,
                streakMaintained = log.screenTimeMinutes <= log.screenTimeGoal
                        && minutes >= log.walkingGoal
            ))
        }
    }

    fun updateGoals(screenTimeGoal: Int, walkingGoal: Int) {
        viewModelScope.launch {
            val today = todayString()
            val log = dao.getLogForDate(today) ?: DailyLog(date = today)
            dao.upsertLog(log.copy(
                screenTimeGoal   = screenTimeGoal,
                walkingGoal      = walkingGoal,
                streakMaintained = log.screenTimeMinutes <= screenTimeGoal
                        && log.walkingMinutes >= walkingGoal
            ))
        }
    }
}