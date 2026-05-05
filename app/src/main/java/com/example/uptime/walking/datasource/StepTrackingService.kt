package com.example.uptime.walking.datasource

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.uptime.walking.TrackingPreferences

class StepTrackingService : Service() {

    companion object {
        private const val CHANNEL_ID = "walking_tracking_channel"
        private const val CHANNEL_NAME = "Walking Tracking"
        private const val NOTIFICATION_ID = 1001
    }

    private lateinit var sensorDataSource: DeviceSensorStepsDataSource
    private fun hasActivityRecognitionPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    override fun onCreate() {
        super.onCreate()
        if (!hasActivityRecognitionPermission()) {
            Log.d("StepTrackingService", "Physical activity permission missing; stopping service")

            TrackingPreferences(applicationContext)
                .setDeviceSensorEnabled(false)

            stopSelf()
            return
        }
        sensorDataSource = DeviceSensorStepsDataSource.getInstance(applicationContext)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        sensorDataSource.startTracking()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!hasActivityRecognitionPermission()) {
            Log.d("StepTrackingService", "Physical activity permission missing in onStartCommand; stopping service")

            TrackingPreferences(applicationContext)
                .setDeviceSensorEnabled(false)

            if (::sensorDataSource.isInitialized) {
                sensorDataSource.stopTracking()
            }

            stopSelf()
            return START_NOT_STICKY
        }

        if (!::sensorDataSource.isInitialized) {
            sensorDataSource = DeviceSensorStepsDataSource.getInstance(applicationContext)
        }

        sensorDataSource.startTracking()
        return START_STICKY
    }

    override fun onDestroy() {
        if (::sensorDataSource.isInitialized) {
            sensorDataSource.stopTracking()
        }
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