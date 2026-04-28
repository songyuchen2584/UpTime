package com.example.uptime.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.uptime.data.DailyLog
import com.example.uptime.R
import com.example.uptime.data.UpTimeDatabase
import com.example.uptime.screentime.ScreenTimePreferences
import com.example.uptime.screentime.repository.ScreenTimeRepository
import com.example.uptime.walking.repository.WalkingRepositoryProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class NotificationMonitorService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private lateinit var notificationPrefs: NotificationPreferences
    private lateinit var screenTimePrefs: ScreenTimePreferences
    private lateinit var screenTimeRepository: ScreenTimeRepository

    private val channelId = "uptime_tracking_notifications"

    private var lastWalkingReminderDate: String? = null
    private var lastWalkingCompleteDate: String? = null
    private var lastScreenWarningDate: String? = null

    override fun onCreate() {
        super.onCreate()

        notificationPrefs = NotificationPreferences(applicationContext)
        screenTimePrefs = ScreenTimePreferences(applicationContext)
        screenTimeRepository = ScreenTimeRepository(applicationContext)

        createChannel()

        startForeground(
            100,
            buildPersistentNotification()
        )

        scope.launch {
            while (true) {
                checkAndSendNotifications()
                delay(10 * 60 * 1000L)
            }
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private suspend fun checkAndSendNotifications() {
        val settings = notificationPrefs.settingsFlow.first()

        if (!settings.screenWarningEnabled && !settings.walkingReminderEnabled) {
            stopSelf()
            return
        }

        val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        val now = LocalTime.now()

        val db = UpTimeDatabase.getDatabase(applicationContext)
        val dao = db.dailyLogDao()

        val log = dao.getLogForDate(today) ?: DailyLog(date = today)

        val selectedPackages = screenTimePrefs.resolveEffectivePackages()
        val screenSnapshot = screenTimeRepository.buildTodaySnapshot(selectedPackages)
        val screenMins = (screenSnapshot.totalTrackedTimeMs / 60_000L).toInt()

        val startOfDay = LocalDate.now()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        val walkingRepo = WalkingRepositoryProvider.get(application)
        val walkingStats = walkingRepo.getWalkingStats(
            startOfDay,
            System.currentTimeMillis()
        )

        val walkingMins = walkingStats.totalWalkingMinutes.toInt()

        dao.upsertLog(
            log.copy(
                screenTimeMinutes = screenMins,
                walkingMinutes = walkingMins,
                streakMaintained = screenMins <= log.screenTimeGoal &&
                        walkingMins >= log.walkingGoal
            )
        )

        if (settings.screenWarningEnabled) {
            val remaining = log.screenTimeGoal - screenMins

            if (
                remaining in 0..settings.screenWarningThresholdMinutes &&
                lastScreenWarningDate != today
            ) {
                sendNotification(
                    id = 201,
                    title = "Screen time warning",
                    message = "You only have $remaining minutes of screen time left today."
                )

                lastScreenWarningDate = today
            }
        }

        if (settings.walkingReminderEnabled) {
            val reminderTime = LocalTime.of(
                settings.walkingReminderHour,
                settings.walkingReminderMinute
            )

            if (
                walkingMins >= log.walkingGoal &&
                lastWalkingCompleteDate != today
            ) {
                sendNotification(
                    id = 202,
                    title = "Walking goal complete",
                    message = "Nice! You completed your ${log.walkingGoal} minute walking goal today."
                )

                lastWalkingCompleteDate = today
            }

            if (
                !now.isBefore(reminderTime) &&
                lastWalkingReminderDate != today
            ) {
                val remaining = (log.walkingGoal - walkingMins).coerceAtLeast(0)

                val message = if (remaining == 0) {
                    "You completed your walking goal for today."
                } else {
                    "You have walked $walkingMins/${log.walkingGoal} minutes. $remaining minutes left."
                }

                sendNotification(
                    id = 203,
                    title = "Walking progress",
                    message = message
                )

                lastWalkingReminderDate = today
            }
        }
    }

    private fun buildPersistentNotification(): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Uptime notifications active")
            .setContentText("Checking screen time and walking progress.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun sendNotification(
        id: Int,
        title: String,
        message: String
    ) {
        val manager = getSystemService(NotificationManager::class.java)

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        manager.notify(id, notification)
    }

    private fun createChannel() {
        val manager = getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            channelId,
            "Uptime Notifications",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        manager.createNotificationChannel(channel)
    }
}