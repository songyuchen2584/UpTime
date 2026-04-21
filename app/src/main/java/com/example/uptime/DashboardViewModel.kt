package com.example.uptime

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.uptime.screentime.ScreenTimePreferences
import com.example.uptime.screentime.repository.ScreenTimeRepository
import com.example.uptime.walking.viewmodel.WalkingViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val db  = UpTimeDatabase.getDatabase(application)
    private val dao = db.dailyLogDao()
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    private fun todayString() = LocalDate.now().format(formatter)

    val repository = UserStatsRepository(dao)

    private val _currentDate = MutableStateFlow(todayString())
    val currentDate: StateFlow<String> = _currentDate

    @OptIn(ExperimentalCoroutinesApi::class)
    val todayLog: Flow<DailyLog?> = _currentDate.flatMapLatest { date ->
        dao.observeLogForDate(date)
    }

    val userStats: StateFlow<UserStatsRepository.UserStats> = repository.userStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000),
            UserStatsRepository.UserStats()
        )

    // daily motivational quote from API
    private val _quote = MutableStateFlow<Quote?>(null)
    val quote: StateFlow<Quote?> = _quote

    init {
        viewModelScope.launch {
            ensureTodayLogExists()
            catchUpMissedFinalizations()
            watchForDateChange()
        }
        fetchQuote()
    }

    private suspend fun watchForDateChange() {
        while (true) {
            delay(60_000)
            val newDate = todayString()
            if (newDate != _currentDate.value) {
                _currentDate.value = newDate
                ensureTodayLogExists()
                catchUpMissedFinalizations()
            }
        }
    }

    fun refreshLiveStats(walkingViewModel: WalkingViewModel) {
        viewModelScope.launch {
            val today = todayString()
            val log = dao.getLogForDate(today) ?: DailyLog(date = today)

            // Read selected packages directly from preferences
            val preferences = ScreenTimePreferences(getApplication())
            val selectedPackages = preferences.selectedPackagesFlow.first()

            // Fresh screen time
            val screenTimeRepo = ScreenTimeRepository(getApplication())
            val snapshot = screenTimeRepo.buildTodaySnapshot(selectedPackages)
            val screenMins = (snapshot.totalTrackedTimeMs / 60_000).toInt()

            // Fresh walking
            val startOfDay = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val walkMins = walkingViewModel.getWalkingMinutes(
                startOfDay, System.currentTimeMillis()
            ).toInt()

            println("walking time for ${Date(startOfDay)} to ${Date(System.currentTimeMillis())}: $walkMins min")

            dao.upsertLog(log.copy(
                screenTimeMinutes = screenMins,
                walkingMinutes = walkMins,
                streakMaintained = screenMins <= log.screenTimeGoal
                        && walkMins >= log.walkingGoal
            ))
        }
    }

    private suspend fun ensureTodayLogExists() {
        if (dao.getLogForDate(todayString()) == null) {
            dao.upsertLog(DailyLog(date = todayString()))
        }
    }

    private suspend fun catchUpMissedFinalizations() {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val today = LocalDate.now()

        // Check the last 7 days for any unfinalized logs
        (1..7).forEach { daysAgo ->
            val date = today.minusDays(daysAgo.toLong()).format(formatter)
            val log = dao.getLogForDate(date) ?: return@forEach

            // Finalize if streakMaintained might not have been set correctly
            val shouldHaveMaintained = log.screenTimeMinutes <= log.screenTimeGoal
                    && log.walkingMinutes >= log.walkingGoal

            if (log.streakMaintained != shouldHaveMaintained) {
                dao.upsertLog(log.copy(streakMaintained = shouldHaveMaintained))
            }
        }
    }

    private fun fetchQuote() {
        viewModelScope.launch {
            try {
                val result = QuoteApi.service.getRandomQuote()
                _quote.value = result.firstOrNull()
            } catch (_: Exception) {
                // no quote if offline
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