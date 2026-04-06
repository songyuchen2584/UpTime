package com.example.uptime

enum class NavDestination(
    val label: String,
    val icon: Int
) {
    Dashboard("Dashboard", R.drawable.analytics_24px),
    Streak("Streak", R.drawable.lightning_stand_24px),
    Room("Room", R.drawable.door_sliding_24px),

    Walking("Walking", R.drawable.directions_walk_24px),
    Settings("Settings", R.drawable.settings_24px);


    companion object {
        val all = entries.filter { it != Settings }
    }
}