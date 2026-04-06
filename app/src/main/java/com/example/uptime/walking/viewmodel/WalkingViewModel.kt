package com.example.uptime.walking.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.uptime.walking.TrackingMethod
import com.example.uptime.walking.WalkingRepository
import com.example.uptime.walking.datasource.DeviceSensorStepsDataSource
import com.example.uptime.walking.datasource.HealthConnectStepsDataSource
import com.example.uptime.walking.model.WalkingStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class WalkingUiState(
    val statsToday: WalkingStats = WalkingStats(),
    val useHealthConnect: Boolean = false,
    val useDeviceSensor: Boolean = false,
    val loading: Boolean = false,
    val error: String? = null
)

class WalkingViewModel(app: Application) : AndroidViewModel(app) {

    private val healthConnectSource = HealthConnectStepsDataSource(app.applicationContext)
    private val deviceSensorSource = DeviceSensorStepsDataSource(app.applicationContext)
    private val repository = WalkingRepository(healthConnectSource, deviceSensorSource)

    private val _state = MutableStateFlow(
        WalkingUiState(
            useHealthConnect = repository.isMethodEnabled(TrackingMethod.HEALTH_CONNECT),
            useDeviceSensor = repository.isMethodEnabled(TrackingMethod.DEVICE_SENSOR)
        )
    )
    val state: StateFlow<WalkingUiState> = _state

    val healthConnectPermissions = healthConnectSource.permissions
    fun healthConnectPermissionContract() = healthConnectSource.permissionRequestContract()
    fun healthConnectSdkStatus() = healthConnectSource.sdkStatus()
    suspend fun grantedHealthConnectPermissions() = healthConnectSource.grantedPermissions()
    fun healthConnectInstallIntent() = healthConnectSource.installIntentIfNeeded()
    fun hasSensorPermission() = deviceSensorSource.hasPermission()
    fun isSensorAvailable() = deviceSensorSource.isSensorAvailable()

    fun setMethodEnabled(method: TrackingMethod, enabled: Boolean) {
        repository.setMethodEnabled(method, enabled)
        _state.update {
            it.copy(
                useHealthConnect = repository.isMethodEnabled(TrackingMethod.HEALTH_CONNECT),
                useDeviceSensor = repository.isMethodEnabled(TrackingMethod.DEVICE_SENSOR)
            )
        }

        if (method == TrackingMethod.DEVICE_SENSOR) {
            if (enabled) deviceSensorSource.startTracking() else deviceSensorSource.stopTracking()
        }
    }

    fun refreshToday() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        try {
            val now = System.currentTimeMillis()
            val startOfDay = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val stats = repository.getWalkingStats(startOfDay, now)
            _state.update { it.copy(statsToday = stats, loading = false) }
        } catch (t: Throwable) {
            _state.update { it.copy(loading = false, error = t.message ?: "Unknown error") }
        }
    }

    suspend fun getStepCount(startMillis: Long, endMillis: Long): Long {
        return repository.getStepCount(startMillis, endMillis)
    }

    suspend fun getWalkingMinutes(startMillis: Long, endMillis: Long): Long {
        return repository.getWalkingMinutes(startMillis, endMillis)
    }

    override fun onCleared() {
        super.onCleared()
        deviceSensorSource.stopTracking()
    }
}