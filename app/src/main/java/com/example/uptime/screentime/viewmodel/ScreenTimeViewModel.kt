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
            val selectedPackages = preferences.selectedPackagesFlow.first()
            val hasAccess = repository.hasUsageAccess()
            val usage = if (hasAccess) {
                repository.getTodayUsageForSelectedApps(selectedPackages)
            } else {
                emptyList()
            }

            _uiState.value = ScreenTimeUiState(
                hasUsageAccess = hasAccess,
                installedApps = installedApps,
                selectedPackages = selectedPackages,
                todayUsage = usage,
                totalTrackedTimeMs = usage.sumOf { it.totalTimeMs },
                isLoading = false
            )

            if (hasAccess) {
                val snapshot = repository.buildTodaySnapshot(selectedPackages)
                updateScreenTime(snapshot)
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val selectedPackages = preferences.selectedPackagesFlow.first()
            val hasAccess = repository.hasUsageAccess()
            val usage = if (hasAccess) {
                repository.getTodayUsageForSelectedApps(selectedPackages)
            } else {
                emptyList()
            }

            _uiState.update {
                it.copy(
                    hasUsageAccess = hasAccess,
                    selectedPackages = selectedPackages,
                    todayUsage = usage,
                    totalTrackedTimeMs = usage.sumOf { app -> app.totalTimeMs },
                    isLoading = false
                )
            }

            if (hasAccess) {
                val snapshot = repository.buildTodaySnapshot(selectedPackages)
                updateScreenTime(snapshot)
            }
        }
    }

    fun togglePackage(packageName: String, selected: Boolean) {
        viewModelScope.launch {
            val current = preferences.selectedPackagesFlow.first().toMutableSet()
            if (selected) current.add(packageName) else current.remove(packageName)
            preferences.setSelectedPackages(current)

            val hasAccess = repository.hasUsageAccess()
            val usage = if (hasAccess) {
                repository.getTodayUsageForSelectedApps(current)
            } else {
                emptyList()
            }

            _uiState.update {
                it.copy(
                    selectedPackages = current,
                    todayUsage = usage,
                    totalTrackedTimeMs = usage.sumOf { app -> app.totalTimeMs }
                )
            }

            if (hasAccess) {
                val snapshot = repository.buildTodaySnapshot(current)
                updateScreenTime(snapshot)
            }
        }
    }

    fun onReturnedFromSettings() {
        refresh()
    }
}