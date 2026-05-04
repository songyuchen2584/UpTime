package com.example.uptime.screentime.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.uptime.screentime.ScreenTimePreferences
import com.example.uptime.screentime.models.ScreenTimeSnapshot
import com.example.uptime.screentime.models.ScreenTimeUiState
import com.example.uptime.screentime.repository.ScreenTimeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TAG = "ScreenTimeVM"

class ScreenTimeViewModel(
    application: Application,
    private val updateScreenTime: (ScreenTimeSnapshot) -> Unit
) : AndroidViewModel(application) {

    private val repository = ScreenTimeRepository(application)
    private val preferences = ScreenTimePreferences(application)

    private val _uiState = MutableStateFlow(ScreenTimeUiState())
    val uiState: StateFlow<ScreenTimeUiState> = _uiState

    init {
        loadInitialState()
    }

    private fun loadInitialState() {
        Log.d(TAG, "loadInitialState started")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val installedApps = repository.getInstalledApps()

                val activePackages = preferences.resolveEffectivePackages()
                val pendingPackages = preferences.pendingPackagesFlow.first()
                val displayPackages = pendingPackages ?: activePackages

                val hasAccess = repository.hasUsageAccess()

                Log.d(
                    TAG,
                    "Initial state inputs: installedApps=${installedApps.size}, " +
                            "activePackages=${activePackages.size}, " +
                            "pendingPackages=${pendingPackages?.size}, " +
                            "displayPackages=${displayPackages.size}, hasAccess=$hasAccess"
                )

                val usage = if (hasAccess) {
                    repository.getTodayUsageForSelectedApps(activePackages)
                } else {
                    Log.d(TAG, "Skipping initial usage load because usage access is missing")
                    emptyList()
                }

                _uiState.value = ScreenTimeUiState(
                    hasUsageAccess = hasAccess,
                    installedApps = installedApps,
                    selectedPackages = displayPackages,
                    todayUsage = usage,
                    totalTrackedTimeMs = usage.sumOf { it.totalTimeMs },
                    isLoading = false
                )

                Log.d(
                    TAG,
                    "Initial state loaded: usageCount=${usage.size}, totalMs=${usage.sumOf { it.totalTimeMs }}"
                )

                if (hasAccess) {
                    val snapshot = repository.buildTodaySnapshot(activePackages)
                    Log.d(
                        TAG,
                        "Sending initial snapshot to dashboard: totalMs=${snapshot.totalTrackedTimeMs}, " +
                                "trackedApps=${snapshot.trackedApps.size}"
                    )
                    updateScreenTime(snapshot)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load initial screen time state", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun refresh() {
        Log.d(TAG, "refresh started")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            try {
                val activePackages = preferences.resolveEffectivePackages()
                val pendingPackages = preferences.pendingPackagesFlow.first()
                val displayPackages = pendingPackages ?: activePackages

                val hasAccess = repository.hasUsageAccess()

                Log.d(
                    TAG,
                    "Refresh inputs: activePackages=${activePackages.size}, " +
                            "pendingPackages=${pendingPackages?.size}, " +
                            "displayPackages=${displayPackages.size}, hasAccess=$hasAccess"
                )

                val usage = if (hasAccess) {
                    repository.getTodayUsageForSelectedApps(activePackages)
                } else {
                    Log.d(TAG, "Skipping refresh usage load because usage access is missing")
                    emptyList()
                }

                _uiState.update {
                    it.copy(
                        hasUsageAccess = hasAccess,
                        selectedPackages = displayPackages,
                        todayUsage = usage,
                        totalTrackedTimeMs = usage.sumOf { app -> app.totalTimeMs },
                        isLoading = false
                    )
                }

                Log.d(
                    TAG,
                    "Refresh completed: usageCount=${usage.size}, totalMs=${usage.sumOf { it.totalTimeMs }}"
                )

                if (hasAccess) {
                    val snapshot = repository.buildTodaySnapshot(activePackages)
                    Log.d(
                        TAG,
                        "Sending refreshed snapshot to dashboard: totalMs=${snapshot.totalTrackedTimeMs}, " +
                                "trackedApps=${snapshot.trackedApps.size}"
                    )
                    updateScreenTime(snapshot)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to refresh screen time", e)
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun togglePackage(packageName: String, selected: Boolean) {
        Log.d(TAG, "togglePackage called: packageName=$packageName, selected=$selected")

        viewModelScope.launch {
            try {
                val activePackages = preferences.resolveEffectivePackages()
                val pendingPackages = preferences.pendingPackagesFlow.first()

                val currentDisplayPackages =
                    (pendingPackages ?: activePackages).toMutableSet()

                if (selected) {
                    currentDisplayPackages.add(packageName)
                } else {
                    currentDisplayPackages.remove(packageName)
                }

                Log.d(
                    TAG,
                    "Package selection before save: activeCount=${activePackages.size}, " +
                            "pendingCount=${pendingPackages?.size}, newDisplayCount=${currentDisplayPackages.size}"
                )

                preferences.updatePackagesWithNextDayRule(currentDisplayPackages)

                val newPendingPackages = preferences.pendingPackagesFlow.first()
                val displayPackages = newPendingPackages ?: currentDisplayPackages

                val hasAccess = repository.hasUsageAccess()

                Log.d(
                    TAG,
                    "Toggle inputs: activePackages=${activePackages.size}, " +
                            "oldPendingPackages=${pendingPackages?.size}, " +
                            "newPendingPackages=${newPendingPackages?.size}, " +
                            "displayPackages=${displayPackages.size}, hasAccess=$hasAccess"
                )

                val usage = if (hasAccess) {
                    repository.getTodayUsageForSelectedApps(activePackages)
                } else {
                    Log.d(TAG, "Skipping usage reload after package toggle because usage access is missing")
                    emptyList()
                }

                _uiState.update {
                    it.copy(
                        selectedPackages = displayPackages,
                        todayUsage = usage,
                        totalTrackedTimeMs = usage.sumOf { app -> app.totalTimeMs }
                    )
                }

                Log.d(
                    TAG,
                    "Package toggle completed: displayCount=${displayPackages.size}, " +
                            "usageCount=${usage.size}, totalMs=${usage.sumOf { it.totalTimeMs }}"
                )

                if (hasAccess) {
                    val snapshot = repository.buildTodaySnapshot(activePackages)
                    Log.d(
                        TAG,
                        "Sending post-toggle snapshot to dashboard: totalMs=${snapshot.totalTrackedTimeMs}, " +
                                "trackedApps=${snapshot.trackedApps.size}"
                    )
                    updateScreenTime(snapshot)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to toggle package selection", e)
            }
        }
    }

    fun onReturnedFromSettings() {
        Log.d(TAG, "Returned from settings; refreshing state")
        refresh()
    }
}
