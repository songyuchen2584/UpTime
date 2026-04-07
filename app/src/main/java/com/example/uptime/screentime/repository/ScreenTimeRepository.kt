package com.example.uptime.screentime.repository

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import com.example.uptime.screentime.ScreenTimePermission
import com.example.uptime.screentime.models.AppScreenTime
import com.example.uptime.screentime.models.InstalledAppInfo
import com.example.uptime.screentime.models.ScreenTimeSnapshot
import java.util.Calendar

class ScreenTimeRepository(
    private val context: Context
) {
    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    fun hasUsageAccess(): Boolean {
        return ScreenTimePermission.hasUsageAccess(context)
    }

    fun getInstalledApps(): List<InstalledAppInfo> {
        val pm = context.packageManager

        val installedApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledApplications(0)
        }

        return installedApps
            .asSequence()
            .filter { appInfo -> shouldIncludeApp(appInfo, pm) }
            .map { appInfo ->
                val label = try {
                    pm.getApplicationLabel(appInfo).toString()
                } catch (_: Exception) {
                    appInfo.packageName
                }

                InstalledAppInfo(
                    packageName = appInfo.packageName,
                    appLabel = if (label.isBlank()) appInfo.packageName else label
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.appLabel.lowercase() }
            .toList()
    }

    private fun shouldIncludeApp(
        appInfo: ApplicationInfo,
        pm: PackageManager
    ): Boolean {
        if (appInfo.packageName == context.packageName) return false

        val isSystemApp =
            (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0 &&
                    (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0

        val hasLaunchIntent = pm.getLaunchIntentForPackage(appInfo.packageName) != null

        return hasLaunchIntent || !isSystemApp
    }

    fun getTodayUsageForSelectedApps(selectedPackages: Set<String>): List<AppScreenTime> {
        if (!hasUsageAccess() || selectedPackages.isEmpty()) return emptyList()

        val now = System.currentTimeMillis()
        val startOfDay = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val appLabels = getInstalledApps().associate { it.packageName to it.appLabel }

        val aggregatedStats = usageStatsManager.queryAndAggregateUsageStats(startOfDay, now)

        val maxPossibleTodayMs = now - startOfDay

        return aggregatedStats
            .asSequence()
            .filter { (packageName, _) -> packageName in selectedPackages }
            .map { (packageName, stats) ->
                AppScreenTime(
                    packageName = packageName,
                    appLabel = appLabels[packageName] ?: packageName,
                    totalTimeMs = stats.totalTimeInForeground
                        .coerceAtLeast(0L)
                        .coerceAtMost(maxPossibleTodayMs)
                )
            }
            .filter { it.totalTimeMs > 0L }
            .sortedByDescending { it.totalTimeMs }
            .toList()
    }

    fun buildTodaySnapshot(selectedPackages: Set<String>): ScreenTimeSnapshot {
        val usage = getTodayUsageForSelectedApps(selectedPackages)
        return ScreenTimeSnapshot(
            trackedApps = usage,
            totalTrackedTimeMs = usage.sumOf { it.totalTimeMs },
            generatedAtMillis = System.currentTimeMillis()
        )
    }
}