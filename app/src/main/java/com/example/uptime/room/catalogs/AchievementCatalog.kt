package com.example.uptime.room.catalogs

import com.example.uptime.room.Achievement
import com.example.uptime.room.AchievementCategory
import com.example.uptime.room.AchievementSize
import com.example.uptime.room.AchievementTier

object AchievementCatalog {
    val all: List<Achievement> = listOf(
        Achievement(
            "start",
            "Start Your Journey",
            "Download the app and start walking",
            AchievementTier.Bronze,
            AchievementCategory.Special,
            AchievementSize.Small
        ),
        Achievement(
            "demo_sm",
            "Demo Medal",
            "Click place to display this in your trophy case",
            AchievementTier.Silver,
            AchievementCategory.Special,
            AchievementSize.Small
        ),
        Achievement(
            "demo_sm_alt",
            "Demo Medal",
            "Click place to display this in your trophy case",
            AchievementTier.Diamond,
            AchievementCategory.Special,
            AchievementSize.Small
        ),
        Achievement(
            "demo_med",
            "Demo Trophy",
            "Click place to display this in your trophy case",
            AchievementTier.Bronze,
            AchievementCategory.Special,
            AchievementSize.Medium
        ),
        Achievement(
            "demo_lg",
            "Demo Trophy (Large)",
            "Click place to display this in your trophy case",
            AchievementTier.Bronze,
            AchievementCategory.Special,
            AchievementSize.Large
        ),
        Achievement(
            "streak_1",
            "First Steps",
            "1 day streak",
            AchievementTier.Bronze,
            AchievementCategory.Streak,
            AchievementSize.Small
        ),
        Achievement(
            "streak_2",
            "A Solid Start",
            "2 day streak",
            AchievementTier.Bronze,
            AchievementCategory.Streak,
            AchievementSize.Small
        ),
        Achievement(
            "streak_3",
            "Consistency",
            "3 day streak",
            AchievementTier.Bronze,
            AchievementCategory.Streak,
            AchievementSize.Small
        ),
        Achievement(
            "streak_7",
            "First Week",
            "7 day streak",
            AchievementTier.Silver,
            AchievementCategory.Streak,
            AchievementSize.Small
        ),
        Achievement(
            "streak_14",
            "Two Week Streak",
            "14 day streak",
            AchievementTier.Silver,
            AchievementCategory.Streak,
            AchievementSize.Small
        ),
        Achievement(
            "streak_21",
            "Three Week Streak",
            "21 day streak",
            AchievementTier.Gold,
            AchievementCategory.Streak,
            AchievementSize.Small
        ),
        Achievement(
            "streak_28",
            "One Month Streak",
            "28 day streak",
             AchievementTier.Gold,
            AchievementCategory.Streak,
            AchievementSize.Small
        ),
        Achievement(
            "streak_50",
            "50 Days Later",
            "50 day streak",
             AchievementTier.Bronze,
            AchievementCategory.Streak,
            AchievementSize.Medium
        ),
        Achievement(
            "streak_100",
            "Streak Master",
            "100 day streak",
             AchievementTier.Silver,
            AchievementCategory.Streak,
            AchievementSize.Medium
        ),
        Achievement(
            "streak_150",
            "Still Going",
            "100 day streak",
            AchievementTier.Gold,
            AchievementCategory.Streak,
            AchievementSize.Medium
        ),
        Achievement(
            "streak_365",
            "Unstoppable",
            "300 day streak",
            AchievementTier.Bronze,
            AchievementCategory.Streak,
            AchievementSize.Large
        ),
        Achievement(
            "streak_500",
            "Juggernaut",
            "500 day streak",
            AchievementTier.Silver,
            AchievementCategory.Streak,
            AchievementSize.Large
        ),
        Achievement(
            "streak_1000",
            "Ascended",
            "1000 day streak",
            AchievementTier.Gold,
            AchievementCategory.Streak,
            AchievementSize.Large
        ),
        Achievement(
            "streak_1825",
            "True Divinity",
            "Five year streak",
            AchievementTier.Diamond,
            AchievementCategory.Streak,
            AchievementSize.Large
        ),
        Achievement(
            "walk_60",
            "One Hour Walker",
            "Walk 60 total mins",
             AchievementTier.Bronze,
            AchievementCategory.WalkingTime,
            AchievementSize.Small
        ),
        Achievement(
            "walk_120",
            "Two Hour Walker",
            "Walk 120 total mins",
             AchievementTier.Bronze,
            AchievementCategory.WalkingTime,
            AchievementSize.Small
        ),
        Achievement(
            "walk_360",
            "Six Hour Walker",
            "Walk 360 total mins",
             AchievementTier.Silver,
            AchievementCategory.WalkingTime,
            AchievementSize.Small
        ),
        Achievement(
            "walk_600",
            "Ten Hour Walker",
            "Walk 600 total mins",
             AchievementTier.Gold,
            AchievementCategory.WalkingTime,
            AchievementSize.Small
        ),
        Achievement(
            "walk_1000",
            "Walk 1000",
            "Walk 1000 total mins",
             AchievementTier.Bronze,
            AchievementCategory.WalkingTime,
            AchievementSize.Medium
        ),
        Achievement(
            "walk_2000",
            "Ruined Shoes",
            "Walk 2000 total mins",
            AchievementTier.Silver,
            AchievementCategory.WalkingTime,
            AchievementSize.Medium
        ),
        Achievement(
            "walk_5000",
            "Running in Circles",
            "Walk 5000 total mins",
            AchievementTier.Bronze,
            AchievementCategory.WalkingTime,
            AchievementSize.Large
        ),
        Achievement(
            "walk_10000",
            "Over 9000",
            "Walk 10000 total mins",
            AchievementTier.Silver,
            AchievementCategory.WalkingTime,
            AchievementSize.Large
        ),
        Achievement(
            "walk_20000",
            "There and Back Again",
            "Walk 20000 total mins",
            AchievementTier.Gold,
            AchievementCategory.WalkingTime,
            AchievementSize.Large
        ),
        Achievement(
            "walk_50000",
            "A Quick Jog",
            "Walk 50000 total mins",
            AchievementTier.Diamond,
            AchievementCategory.WalkingTime,
            AchievementSize.Large
        ),
        Achievement(
            "screen_fail",
            "A little too much",
            "Go over the screen time limit",
            AchievementTier.Bronze,
            AchievementCategory.ScreenTime,
            AchievementSize.Small
        ),
        Achievement(
            "screen_7",
            "No Temptations",
            "Stay under your screen time limit for 1 week",
            AchievementTier.Bronze,
            AchievementCategory.ScreenTime,
            AchievementSize.Small
        ),
        Achievement(
            "screen_14",
            "Screen King",
            "Stay under your screen time limit for 2 weeks",
            AchievementTier.Silver,
            AchievementCategory.ScreenTime,
            AchievementSize.Small
        ),
        Achievement(
            "screen_inv_7",
            "Addicted",
            "Fail your screen time goal 7 times",
            AchievementTier.Gold,
            AchievementCategory.ScreenTime,
            AchievementSize.Small
        ),
        Achievement(
            "screen_31",
            "So Close...",
            "Fail your screen time goal by a minute or less",
            AchievementTier.Gold,
            AchievementCategory.Secret,
            AchievementSize.Small
        ),
        Achievement(
            "walk_29",
            "So Close...",
            "Fail your walking goal by a minute or less",
            AchievementTier.Gold,
            AchievementCategory.Secret,
            AchievementSize.Small
        ),
    )

    val streakAchievements: List<Achievement> = all.filter { it.category == AchievementCategory.Streak }
    val walkingAchievements: List<Achievement> = all.filter { it.category == AchievementCategory.WalkingTime }
    val screenAchievements: List<Achievement> = all.filter { it.category == AchievementCategory.ScreenTime }
    val specialAchievements: List<Achievement> = all.filter { it.category == AchievementCategory.Special }
    val secretAchievements: List<Achievement> = all.filter { it.category == AchievementCategory.Secret }

    val bronzeAchievements: List<Achievement> = all.filter { it.tier == AchievementTier.Bronze }
    val silverAchievements: List<Achievement> = all.filter { it.tier == AchievementTier.Silver }
    val goldAchievements: List<Achievement> = all.filter { it.tier == AchievementTier.Gold }
    val diamondAchievements: List<Achievement> = all.filter { it.tier == AchievementTier.Diamond }

}