package com.example.uptime.walking.datasource

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class StepTrackingService : Service() {

    companion object {
        private const val CHANNEL_ID = "walking_tracking_channel"
        private const val CHANNEL_NAME = "Walking Tracking"
        private const val NOTIFICATION_ID = 1001
    }

    private lateinit var sensorDataSource: DeviceSensorStepsDataSource

    override fun onCreate() {
        super.onCreate()
        sensorDataSource = DeviceSensorStepsDataSource.getInstance(applicationContext)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        sensorDataSource.startTracking()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        sensorDataSource.startTracking()
        return START_STICKY
    }

    override fun onDestroy() {
        sensorDataSource.stopTracking()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("UpTime walking tracking")
            .setContentText("Tracking walking activity in the background")
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }
    }
}