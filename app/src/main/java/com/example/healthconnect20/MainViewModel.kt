package com.example.healthconnect20

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val sdkStatus: Int = -1,
    val granted: Set<String> = emptySet(),
    val steps24h: Long? = null,
    val sessions: List<ExerciseSessionUi> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = HealthConnectRepository(app.applicationContext)

    val permissionsToRequest = repo.permissions
    val permissionContract = repo.permissionRequestContract()

    private val _state = MutableStateFlow(UiState(sdkStatus = repo.sdkStatus()))
    val state: StateFlow<UiState> = _state

    fun refreshPermissions() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        try {
            val granted = repo.grantedPermissions()
            _state.update { it.copy(granted = granted, loading = false) }
        } catch (t: Throwable) {
            _state.update { it.copy(loading = false, error = t.message ?: "Unknown error") }
        }
    }

    fun refreshData() = viewModelScope.launch {
        _state.update { it.copy(loading = true, error = null) }
        try {
            val steps = repo.readTotalStepsLast24h()
            val sessions = repo.readExerciseSessionsLast7d()
            _state.update { it.copy(steps24h = steps, sessions = sessions, loading = false) }
        } catch (t: Throwable) {
            _state.update { it.copy(loading = false, error = t.message ?: "Unknown error") }
        }
    }

    fun installIntent() = repo.installIntentIfNeeded()
}
