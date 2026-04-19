package com.example.uptime.screentime.repository

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.example.uptime.screentime.ScreenTimePermission
import com.example.uptime.screentime.models.AppScreenTime
import com.example.uptime.screentime.models.InstalledAppInfo
import com.example.uptime.screentime.models.ScreenTimeSnapshot
import java.util.Calendar
import java.util.Date

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

    fun computeUsageBetween(
        start: Long,
        end: Long,
        selectedPackages: Set<String>
    ): Map<String, Long> {

        val usageEvents = usageStatsManager.queryEvents(start, end)
        val event = UsageEvents.Event()

        val totalTime = mutableMapOf<String, Long>()
        val activeSessions = mutableMapOf<String, Long>()

        while (usageEvents.hasNextEvent()) {
            usageEvents.getNextEvent(event)

            val pkg = event.packageName
            if (pkg !in selectedPackages) continue

            when (event.eventType) {

                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    if (!activeSessions.containsKey(pkg)) {
                        activeSessions[pkg] = event.timeStamp
                    }
                }

                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    val startTime = activeSessions[pkg]

                    if (startTime != null) {
                        val duration = event.timeStamp - startTime
                        if (duration > 0) {
                            totalTime[pkg] = totalTime.getOrDefault(pkg, 0L) + duration
                        }
                        activeSessions.remove(pkg)
                    } else {
                        // Session started before start of day
                        val duration = event.timeStamp - start
                        if (duration > 0) {
                            totalTime[pkg] = totalTime.getOrDefault(pkg, 0L) + duration
                        }
                    }
                }
            }
        }

        // Close sessions still active at end of day
        activeSessions.forEach { (pkg, startTime) ->
            val duration = end - startTime
            if (duration > 0) {
                totalTime[pkg] = totalTime.getOrDefault(pkg, 0L) + duration
            }
        }

        return totalTime
    }

    fun mapToAppScreenTime(
        usageMap: Map<String, Long>,
        appLabels: Map<String, String>
    ): List<AppScreenTime> {
        return usageMap
            .map { (pkg, time) ->
                AppScreenTime(
                    packageName = pkg,
                    appLabel = appLabels[pkg] ?: pkg,
                    totalTimeMs = time
                )
            }
            .filter { it.totalTimeMs > 0 }
            .sortedByDescending { it.totalTimeMs }
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

        val usageMap = computeUsageBetween(startOfDay, now, selectedPackages)

        return mapToAppScreenTime(usageMap, appLabels)
    }

    fun buildTodaySnapshot(selectedPackages: Set<String>): ScreenTimeSnapshot {
        val usage = getTodayUsageForSelectedApps(selectedPackages)
        return ScreenTimeSnapshot(
            trackedApps = usage,
            totalTrackedTimeMs = usage.sumOf { it.totalTimeMs },
            generatedAtMillis = System.currentTimeMillis()
        )
    }

    fun buildYesterdaySnapshot(selectedPackages: Set<String>): ScreenTimeSnapshot {
        if (!hasUsageAccess() || selectedPackages.isEmpty()) {
            return ScreenTimeSnapshot(emptyList(), 0L, System.currentTimeMillis())
        }

        val now = System.currentTimeMillis()

        val yesterdayStart = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val yesterdayEnd = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val appLabels = getInstalledApps().associate { it.packageName to it.appLabel }

        val usageMap = computeUsageBetween(yesterdayStart, yesterdayEnd, selectedPackages)
        val usageList = mapToAppScreenTime(usageMap, appLabels)

        return ScreenTimeSnapshot(
            trackedApps = usageList,
            totalTrackedTimeMs = usageList.sumOf { it.totalTimeMs },
            generatedAtMillis = now
        )
    }
}