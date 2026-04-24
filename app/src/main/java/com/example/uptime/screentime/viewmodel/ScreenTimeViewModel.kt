package com.example.uptime.screentime.viewmodel

import android.app.Application
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
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val installedApps = repository.getInstalledApps()

            val activePackages = preferences.resolveEffectivePackages()
            val pendingPackages = preferences.pendingPackagesFlow.first()
            val displayPackages = pendingPackages ?: activePackages

            val hasAccess = repository.hasUsageAccess()

            val usage = if (hasAccess) {
                repository.getTodayUsageForSelectedApps(activePackages)
            } else {
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

            if (hasAccess) {
                val snapshot = repository.buildTodaySnapshot(activePackages)
                updateScreenTime(snapshot)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val activePackages = preferences.resolveEffectivePackages()
            val pendingPackages = preferences.pendingPackagesFlow.first()
            val displayPackages = pendingPackages ?: activePackages

            val hasAccess = repository.hasUsageAccess()

            val usage = if (hasAccess) {
                repository.getTodayUsageForSelectedApps(activePackages)
            } else {
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

            if (hasAccess) {
                val snapshot = repository.buildTodaySnapshot(activePackages)
                updateScreenTime(snapshot)
            }
        }
    }

    fun togglePackage(packageName: String, selected: Boolean) {
        viewModelScope.launch {
            val activePackages = preferences.resolveEffectivePackages()
            val pendingPackages = preferences.pendingPackagesFlow.first()

            val currentDisplayPackages =
                (pendingPackages ?: activePackages).toMutableSet()

            if (selected) {
                currentDisplayPackages.add(packageName)
            } else {
                currentDisplayPackages.remove(packageName)
            }

            preferences.updatePackagesWithNextDayRule(currentDisplayPackages)

            val newPendingPackages = preferences.pendingPackagesFlow.first()
            val displayPackages = newPendingPackages ?: currentDisplayPackages

            val hasAccess = repository.hasUsageAccess()

            val usage = if (hasAccess) {
                repository.getTodayUsageForSelectedApps(activePackages)
            } else {
                emptyList()
            }

            _uiState.update {
                it.copy(
                    selectedPackages = displayPackages,
                    todayUsage = usage,
                    totalTrackedTimeMs = usage.sumOf { app -> app.totalTimeMs }
                )
            }

            if (hasAccess) {
                val snapshot = repository.buildTodaySnapshot(activePackages)
                updateScreenTime(snapshot)
            }
        }
    }

    fun onReturnedFromSettings() {
        refresh()
    }
}