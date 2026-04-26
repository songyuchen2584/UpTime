package com.example.uptime

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.uptime.room.Achievement
import com.example.uptime.room.RoomViewModel.Companion.DAILY_COMPLETION_POINTS
import com.example.uptime.room.UserInventory
import com.example.uptime.room.catalogs.AchievementCatalog
import com.example.uptime.screentime.ScreenTimePreferences
import com.example.uptime.screentime.repository.ScreenTimeRepository
import com.example.uptime.walking.TrackingMethod
import com.example.uptime.walking.TrackingPreferences
import com.example.uptime.walking.WalkingRepository
import com.example.uptime.walking.datasource.DeviceSensorStepsDataSource
import com.example.uptime.walking.datasource.HealthConnectStepsDataSource
import com.example.uptime.walking.repository.WalkingRepositoryProvider
import com.google.android.play.integrity.internal.u
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

// Checks stats at midnight and finalizes goal progress
class DailyFinalizationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val db = UpTimeDatabase.getDatabase(applicationContext)
        val dao = db.dailyLogDao()
        val invDao = db.userInventoryDao()
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val today = LocalDate.now().format(formatter)
        val yesterday = LocalDate.now().minusDays(1).format(formatter)

        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        // Fetch fresh screen time for yesterday
        val screenTimePreferences = ScreenTimePreferences(applicationContext)
        val selectedPackages = screenTimePreferences.selectedPackagesFlow.first()

        val screenTimeRepo = ScreenTimeRepository(applicationContext)
        val screenTimeSnapshot = screenTimeRepo.buildYesterdaySnapshot(selectedPackages)
        val screenTimeMinutes = (screenTimeSnapshot.totalTrackedTimeMs / 60_000).toInt()

        // Fetch fresh walking minutes for yesterday
        val walkingRepository = WalkingRepositoryProvider.get(applicationContext)

        val trackingPrefs = TrackingPreferences(applicationContext)
        val hcEnabled = trackingPrefs.isHealthConnectEnabled()
        val sensorEnabled = trackingPrefs.isDeviceSensorEnabled()
        if (hcEnabled)
            walkingRepository.setMethodEnabled(TrackingMethod.HEALTH_CONNECT, true)
        if (sensorEnabled)
            walkingRepository.setMethodEnabled(TrackingMethod.DEVICE_SENSOR, true)

        Log.d("DailyWorker", "healthConnect=$hcEnabled deviceSensor=$sensorEnabled")

        if (!hcEnabled && !sensorEnabled) {
            Log.d("DailyWorker", "No methods enabled, falling back to saved value")
        }

        val yesterdayStart = LocalDate.now().minusDays(1)
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val yesterdayEnd = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val walkingMinutes = if (!trackingPrefs.isHealthConnectEnabled() && !trackingPrefs.isDeviceSensorEnabled()) {
            // No methods configured, fall back to last saved value
            dao.getLogForDate(yesterday)?.walkingMinutes ?: 0
        } else {
            try {
                walkingRepository.getWalkingMinutes(yesterdayStart, yesterdayEnd).toInt()
            } catch (e: Exception) {
                dao.getLogForDate(yesterday)?.walkingMinutes ?: 0
            }
        }

        // Update yesterday's log with fresh values
        val existingLog = dao.getLogForDate(yesterday) ?: DailyLog(date = yesterday)
        val finalizedLog = existingLog.copy(
            screenTimeMinutes = screenTimeMinutes,
            walkingMinutes = walkingMinutes,
            streakMaintained = screenTimeMinutes <= existingLog.screenTimeGoal
                    && walkingMinutes >= existingLog.walkingGoal
        )
        dao.upsertLog(finalizedLog)
        val inventory = invDao.getInventory(currentUserId!!) ?: UserInventory(currentUserId)
        // Build stats and check achievements
        val statsRepo = UserStatsRepository(dao)
        val stats = statsRepo.userStats.first()
        val streak = stats.currentStreak
        val isMilestone = streak > 0 && streak % 7 == 0

        if (finalizedLog.streakMaintained) {
            invDao.upsertInventory(
                inventory.copy(
                    currentPoints = if (isMilestone) inventory.currentPoints + 2*DAILY_COMPLETION_POINTS else inventory.currentPoints + DAILY_COMPLETION_POINTS
                )
            )
            Log.d("DailyWorker", "Awarded $DAILY_COMPLETION_POINTS points for completing yesterday")
            if (isMilestone) Log.d("DailyWorker", "Awarded an extra $DAILY_COMPLETION_POINTS points for completing a 7-day streak")
        }

        // Ensure today's log exists
        if (dao.getLogForDate(today) == null) {
            dao.upsertLog(DailyLog(date = today))
        }

        val screenTimeOver = if (finalizedLog.screenTimeMinutes > finalizedLog.screenTimeGoal)
            finalizedLog.screenTimeMinutes - finalizedLog.screenTimeGoal else -1
        val walkingUnder = if (finalizedLog.walkingMinutes < finalizedLog.walkingGoal)
            finalizedLog.walkingGoal - finalizedLog.walkingMinutes else -1

        val statsWithMargins = stats.copy(
            screenTimeOverBy = screenTimeOver,
            walkingUnderBy   = walkingUnder
        )
        val alreadyUnlocked = inventory.unlockedAchievementIds

        val newlyUnlocked = AchievementCatalog.all
            .filter { it.id !in alreadyUnlocked }
            .filter { achievement ->
                meetsCondition(achievement, statsWithMargins, isEndOfDay = true)
            }
            .map { it.id }

        if (newlyUnlocked.isNotEmpty()) {
            invDao.upsertInventory(
                inventory.copy(
                    unlockedAchievementIds = alreadyUnlocked + newlyUnlocked
                )
            )
        }

        // Schedule next midnight run
        scheduleMidnightWork(applicationContext)

        return Result.success()
    }

    // Version of RoomViewModel function just for end of day checks
    private fun meetsCondition(
        achievement: Achievement,
        stats: UserStatsRepository.UserStats,
        isEndOfDay: Boolean
    ): Boolean {
        return when (achievement.id) {
            // Streak
            "streak_1" -> stats.currentStreak >= 1
            "streak_2" -> stats.currentStreak >= 2
            "streak_3" -> stats.currentStreak >= 3
            "streak_7" -> stats.currentStreak >= 7
            "streak_14" -> stats.currentStreak >= 14
            "streak_21" -> stats.currentStreak >= 21
            "streak_28" -> stats.currentStreak >= 28
            "streak_50" -> stats.currentStreak >= 50
            "streak_100" -> stats.currentStreak >= 100
            "streak_150" -> stats.currentStreak >= 150
            "streak_365" -> stats.currentStreak >= 365
            "streak_500" -> stats.currentStreak >= 500
            "streak_1000" -> stats.currentStreak >= 1000
            "streak_1825" -> stats.currentStreak >= 1825

            // Screen
            "screen_fail" -> stats.screenTimeFailCount >= 1
            "screen_7" -> stats.consecutiveScreenTimeSuccess >= 7
            "screen_14" -> stats.consecutiveScreenTimeSuccess >= 14
            "screen_inv_7" -> stats.screenTimeFailCount >= 7

            // Secret
            "screen_31" -> isEndOfDay && stats.screenTimeOverBy == 1
            "walk_29" -> isEndOfDay && stats.walkingUnderBy == 1
            else -> false
        }
    }

    companion object {
        const val WORK_NAME = "daily_finalization"

        fun scheduleMidnightWork(context: Context) {
            val now = LocalDateTime.now()
            val nextMidnight = now.toLocalDate().plusDays(1).atStartOfDay()
            // Actually runs 1 min after midnight to let data sources settle
            val delayMillis = ChronoUnit.MILLIS.between(now, nextMidnight) + (1 * 60 * 1000)

            val request = OneTimeWorkRequestBuilder<DailyFinalizationWorker>()
                .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiresBatteryNotLow(false)
                        .build()
                )
                .addTag(WORK_NAME)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}