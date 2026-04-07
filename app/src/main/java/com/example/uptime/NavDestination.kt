package com.example.uptime

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
enum class NavDestination(
    val label: String,
    val icon: Int
) : NavKey {
    Dashboard("Dashboard", R.drawable.analytics_24px),
    Streak("Streak", R.drawable.lightning_stand_24px),
    Room("Room", R.drawable.door_sliding_24px),
    Walking("Walking", R.drawable.directions_walk_24px),
    ScreenTime("Screen Time", R.drawable.analytics_24px),
    Settings("Settings", R.drawable.settings_24px);

    companion object {
        val navBar = entries.filter { it != Settings && it != Walking && it != ScreenTime }
    }
}