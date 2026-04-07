package com.example.uptime.screentime.models

data class InstalledAppInfo(
    val packageName: String,
    val appLabel: String
)

data class AppScreenTime(
    val packageName: String,
    val appLabel: String,
    val totalTimeMs: Long
)

data class ScreenTimeSnapshot(
    val trackedApps: List<AppScreenTime>,
    val totalTrackedTimeMs: Long,
    val generatedAtMillis: Long
)

data class ScreenTimeUiState(
    val hasUsageAccess: Boolean = false,
    val installedApps: List<InstalledAppInfo> = emptyList(),
    val selectedPackages: Set<String> = emptySet(),
    val todayUsage: List<AppScreenTime> = emptyList(),
    val totalTrackedTimeMs: Long = 0L,
    val isLoading: Boolean = false
)