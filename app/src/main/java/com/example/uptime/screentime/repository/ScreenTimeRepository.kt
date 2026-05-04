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

private const val TAG = "ScreenTimeRepo"

class ScreenTimeRepository(
    private val context: Context
) {
    private val usageStatsManager =
        context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

    private val socialKeywords = listOf(
        "instagram", "facebook", "messenger", "snapchat",
        "tiktok", "twitter", "x", "reddit",
        "discord", "whatsapp", "telegram", "youtube"
    )

    private fun isSocialApp(packageName: String, label: String): Boolean {
        val combined = "$packageName $label".lowercase()
        return socialKeywords.any { combined.contains(it) }
    }

    fun hasUsageAccess(): Boolean {
        val hasAccess = ScreenTimePermission.hasUsageAccess(context)
        Log.d(TAG, "Usage access check result: hasAccess=$hasAccess")
        return hasAccess
    }

    fun getInstalledApps(): List<InstalledAppInfo> {
        Log.d(TAG, "Loading installed apps")
        val pm = context.packageManager

        val installedApps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledApplications(0)
        }

        val result = installedApps
            .asSequence()
            .filter { appInfo -> shouldIncludeApp(appInfo, pm) }
            .map { appInfo ->
                val label = try {
                    pm.getApplicationLabel(appInfo).toString()
                } catch (_: Exception) {
                    appInfo.packageName
                }

                val safeLabel = if (label.isBlank()) appInfo.packageName else label

                InstalledAppInfo(
                    packageName = appInfo.packageName,
                    appLabel = safeLabel,
                    icon = try { pm.getApplicationIcon(appInfo.packageName) } catch (_: Exception) { null },
                    isRecommendedSocial = isSocialApp(appInfo.packageName, safeLabel)
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.appLabel.lowercase() }
            .toList()

        Log.d(TAG, "Installed apps loaded: rawCount=${installedApps.size}, filteredCount=${result.size}")
        return result
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
        Log.d(TAG, "Computing usage: start=$start, end=$end, selectedCount=${selectedPackages.size}")
        val usageEvents = usageStatsManager.queryEvents(start, end)
        val event = UsageEvents.Event()

        val totalTime = mutableMapOf<String, Long>()

        var foregroundPackage: String? = null
        var foregroundStartTime: Long = 0L

        fun closeForegroundSession(closeTime: Long) {
            val pkg = foregroundPackage ?: return

            if (pkg in selectedPackages) {
                val duration = closeTime - foregroundStartTime
                if (duration > 0) {
                    totalTime[pkg] = totalTime.getOrDefault(pkg, 0L) + duration
                }
            }

            foregroundPackage = null
            foregroundStartTime = 0L
        }

        var eventCount = 0
        var resumedCount = 0
        var pausedCount = 0

        while (usageEvents.hasNextEvent()) {
            eventCount++
            usageEvents.getNextEvent(event)

            val pkg = event.packageName ?: continue
            val time = event.timeStamp.coerceIn(start, end)

            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    resumedCount++
                    // If another app was already marked foreground, close it first.
                    closeForegroundSession(time)

                    if (pkg in selectedPackages) {
                        foregroundPackage = pkg
                        foregroundStartTime = time
                    }
                }

                UsageEvents.Event.ACTIVITY_PAUSED -> {
                    pausedCount++
                    // Only close if this pause belongs to the currently active app.
                    if (foregroundPackage == pkg) {
                        closeForegroundSession(time)
                    }
                }
            }
        }

        // If selected app is still open at query end, count until now/end.
        closeForegroundSession(end)

        Log.d(TAG, "Usage computed: eventCount=$eventCount, resumedCount=$resumedCount, pausedCount=$pausedCount, resultCount=${totalTime.size}, totalMs=${totalTime.values.sum()}")
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
        Log.d(TAG, "getTodayUsageForSelectedApps called: selectedCount=${selectedPackages.size}")
        if (!hasUsageAccess() || selectedPackages.isEmpty()) {
            Log.d(TAG, "Returning empty today usage: hasAccess=${hasUsageAccess()}, selectedEmpty=${selectedPackages.isEmpty()}")
            return emptyList()
        }

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

        val result = mapToAppScreenTime(usageMap, appLabels)
        Log.d(TAG, "Today usage result: appCount=${result.size}, totalMs=${result.sumOf { it.totalTimeMs }}")
        return result
    }

    fun buildTodaySnapshot(selectedPackages: Set<String>): ScreenTimeSnapshot {
        Log.d(TAG, "Building today snapshot: selectedCount=${selectedPackages.size}")
        val usage = getTodayUsageForSelectedApps(selectedPackages)
        Log.d(TAG, "Today snapshot built: trackedApps=${usage.size}, totalMs=${usage.sumOf { it.totalTimeMs }}")
        return ScreenTimeSnapshot(
            trackedApps = usage,
            totalTrackedTimeMs = usage.sumOf { it.totalTimeMs },
            generatedAtMillis = System.currentTimeMillis()
        )
    }

    fun buildYesterdaySnapshot(selectedPackages: Set<String>): ScreenTimeSnapshot {
        Log.d(TAG, "Building yesterday snapshot: selectedCount=${selectedPackages.size}")
        if (!hasUsageAccess() || selectedPackages.isEmpty()) {
            Log.d(TAG, "Returning empty yesterday snapshot: hasAccess=${hasUsageAccess()}, selectedEmpty=${selectedPackages.isEmpty()}")
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

        Log.d(TAG, "Yesterday snapshot built: trackedApps=${usageList.size}, totalMs=${usageList.sumOf { it.totalTimeMs }}")

        return ScreenTimeSnapshot(
            trackedApps = usageList,
            totalTrackedTimeMs = usageList.sumOf { it.totalTimeMs },
            generatedAtMillis = now
        )
    }
}