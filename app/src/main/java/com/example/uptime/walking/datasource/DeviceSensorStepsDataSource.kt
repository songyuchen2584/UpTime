package com.example.uptime.walking.datasource

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.core.content.ContextCompat
import com.example.uptime.walking.model.WalkingSessionInterval
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DeviceSensorStepsDataSource private constructor(
    private val context: Context
) : SensorEventListener {

    data class SensorSession(
        val startMillis: Long,
        val endMillis: Long,
        val steps: Long
    )

    companion object {
        @Volatile
        private var instance: DeviceSensorStepsDataSource? = null

        private const val INACTIVITY_TIMEOUT_MS = 90_000L
        private const val MIN_SESSION_STEPS = 10L

        fun getInstance(context: Context): DeviceSensorStepsDataSource {
            return instance ?: synchronized(this) {
                instance ?: DeviceSensorStepsDataSource(
                    context.applicationContext
                ).also { instance = it }
            }
        }
    }

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val stepCounterSensor: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val mutex = Mutex()
    private val closedSessions = mutableListOf<SensorSession>()

    private var registered = false
    private var lastSensorValue: Float? = null
    private var sessionStart: Long? = null
    private var sessionSteps: Long = 0L
    private var lastStepTimestamp: Long? = null

    fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isSensorAvailable(): Boolean = stepCounterSensor != null

    fun isTracking(): Boolean = registered

    fun startTracking() {
        if (registered || stepCounterSensor == null) return
        sensorManager.registerListener(
            this,
            stepCounterSensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        registered = true
    }

    fun stopTracking() {
        if (!registered) return
        sensorManager.unregisterListener(this)
        registered = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val newValue = event?.values?.firstOrNull() ?: return
        val now = System.currentTimeMillis()

        val previous = lastSensorValue
        lastSensorValue = newValue

        if (previous == null) return

        val delta = (newValue - previous).toLong()
        if (delta <= 0L) {
            closeSessionIfExpired(now)
            return
        }

        val gap = lastStepTimestamp?.let { now - it } ?: Long.MAX_VALUE
        if (sessionStart == null || gap > INACTIVITY_TIMEOUT_MS) {
            closeSession(lastStepTimestamp ?: now)
            sessionStart = now
            sessionSteps = 0L
        }

        sessionSteps += delta
        lastStepTimestamp = now
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun closeSessionIfExpired(now: Long) {
        val last = lastStepTimestamp ?: return
        if (now - last > INACTIVITY_TIMEOUT_MS) {
            closeSession(last)
        }
    }

    private fun closeSession(endTime: Long) {
        val start = sessionStart ?: return
        if (endTime > start && sessionSteps >= MIN_SESSION_STEPS) {
            closedSessions += SensorSession(
                startMillis = start,
                endMillis = endTime,
                steps = sessionSteps
            )
        }
        sessionStart = null
        sessionSteps = 0L
        lastStepTimestamp = null
    }

    private fun currentOpenSession(now: Long): SensorSession? {
        val start = sessionStart ?: return null
        val last = lastStepTimestamp ?: return null
        val effectiveEnd = maxOf(last, now)
        if (effectiveEnd <= start || sessionSteps < MIN_SESSION_STEPS) return null
        return SensorSession(
            startMillis = start,
            endMillis = effectiveEnd,
            steps = sessionSteps
        )
    }

    suspend fun getTotalSteps(
        startMillis: Long,
        endMillis: Long
    ): Long {
        return mutex.withLock {
            closeSessionIfExpired(System.currentTimeMillis())
            val sessions = buildList {
                addAll(closedSessions)
                currentOpenSession(System.currentTimeMillis())?.let { add(it) }
            }
            sessions
                .filter { it.endMillis > startMillis && it.startMillis < endMillis }
                .sumOf { it.steps }
        }
    }

    suspend fun getWalkingSessions(
        startMillis: Long,
        endMillis: Long
    ): List<WalkingSessionInterval> {
        return mutex.withLock {
            closeSessionIfExpired(System.currentTimeMillis())
            val sessions = buildList {
                addAll(closedSessions)
                currentOpenSession(System.currentTimeMillis())?.let { add(it) }
            }
            sessions
                .filter { it.endMillis > startMillis && it.startMillis < endMillis }
                .map {
                    WalkingSessionInterval(
                        startMillis = it.startMillis,
                        endMillis = it.endMillis
                    )
                }
        }
    }
}