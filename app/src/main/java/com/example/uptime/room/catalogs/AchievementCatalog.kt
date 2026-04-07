package com.example.uptime.room.catalogs

import com.example.uptime.R
import com.example.uptime.room.Achievement
import com.example.uptime.room.AchievementSize

object AchievementCatalog {
    val allAchievements: List<Achievement> = listOf(
        Achievement(
            "start",
            "Start Your Journey",
            "Download the app and start walking",
            R.drawable.trophy_24px,
            AchievementSize.Small
        ),
        Achievement(
            "demo_sm",
            "Demo Medal",
            "Click place to display this in your trophy case",
            R.drawable.trophy_24px,
            AchievementSize.Small
        ),
        Achievement(
            "demo_med",
            "Demo Trophy",
            "Click place to display this in your trophy case",
            R.drawable.trophy_24px,
            AchievementSize.Medium
        ),
        Achievement(
            "demo_lg",
            "Demo Trophy (Large)",
            "Click place to display this in your trophy case",
            R.drawable.trophy_24px,
            AchievementSize.Large
        ),
        Achievement(
            "streak_7",
            "One Week Streak",
            "7 day streak",
            R.drawable.trophy_24px,
            AchievementSize.Small
        ),
        Achievement(
            "streak_14",
            "Two Week Streak",
            "14 day streak",
            R.drawable.trophy_24px,
            AchievementSize.Small
        ),
        Achievement(
            "streak_21",
            "Three Week Streak",
            "21 day streak",
            R.drawable.trophy_24px,
            AchievementSize.Small
        ),
        Achievement(
            "streak_28",
            "One Month Streak",
            "28 day streak",
            R.drawable.trophy_24px,
            AchievementSize.Medium
        ),
        Achievement(
            "streak_50",
            "50 Day Streak",
            "50 day streak",
            R.drawable.trophy_24px,
            AchievementSize.Medium
        ),
        Achievement(
            "streak_100",
            "Streak Master",
            "100 day streak",
            R.drawable.trophy_24px,
            AchievementSize.Large
        ),
        Achievement(
            "walk_60",
            "One Hour Walker",
            "Walk 60 total mins",
            R.drawable.trophy_24px,
            AchievementSize.Small
        ),
        Achievement(
            "walk_120",
            "Two Hour Walker",
            "Walk 120 total mins",
            R.drawable.trophy_24px,
            AchievementSize.Small
        ),
        Achievement(
            "walk_360",
            "Six Hour Walker",
            "Walk 360 total mins",
            R.drawable.trophy_24px,
            AchievementSize.Medium
        ),
        Achievement(
            "walk_600",
            "Ten Hour Walker",
            "Walk 600 total mins",
            R.drawable.trophy_24px,
            AchievementSize.Large
        ),
        Achievement(
            "walk_1000",
            "Walk 1000",
            "Walk 1000 total mins",
            R.drawable.trophy_24px,
            AchievementSize.Large
        ),
    )
}