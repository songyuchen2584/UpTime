package com.example.uptime

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlin.collections.listOf

// Placeholder object for storing room state info for an instance
object RoomRepository {
    var state by mutableStateOf(getPlaceholderState())

    fun updateDisplayName(newName: String) {
        state = state.copy(displayName = newName)
    }

    fun selectTheme(themeId: String) {
        state = state.copy(selectedRoomThemeId = themeId)
    }

    fun placeAchievement(achievementId: String) {
        val achievement = state.achievements.find { it.id == achievementId } ?: return

        val targetSlot = when (achievement.size) {
            AchievementSize.Large -> state.shelfSlots
                .filter { it.acceptedSizes.contains(AchievementSize.Large) }
                .firstOrNull { slot ->
                    // Section must have no large already, and no mediums filled
                    val sectionSlots = state.shelfSlots.filter { it.section == slot.section }
                    sectionSlots.all { it.placedAchievementId == null }
                }
            AchievementSize.Medium -> state.shelfSlots
                .filter { it.acceptedSizes.contains(AchievementSize.Medium) && it.placedAchievementId == null }
                .firstOrNull { slot ->
                    // Section must have no large placed and not already have 2 mediums
                    val sectionSlots = state.shelfSlots.filter { it.section == slot.section }
                    val hasLarge = sectionSlots.any { it.acceptedSizes.contains(AchievementSize.Large) && it.placedAchievementId != null }
                    val mediumsFilled = sectionSlots.count { it.acceptedSizes.contains(AchievementSize.Medium) && it.placedAchievementId != null }
                    !hasLarge && mediumsFilled < 2
                }
            AchievementSize.Small -> state.shelfSlots
                .firstOrNull { it.acceptedSizes.contains(AchievementSize.Small) && it.placedAchievementId == null }
        } ?: return

        // Remove any previous placement of this achievement, then place it
        state = state.copy(
            shelfSlots = state.shelfSlots.map { slot ->
                when {
                    slot.placedAchievementId == achievementId -> slot.copy(placedAchievementId = null)
                    slot.id == targetSlot.id -> slot.copy(placedAchievementId = achievementId)
                    else -> slot
                }
            }
        )
    }

    fun removeAchievement(achievementId: String) {
        state = state.copy(
            shelfSlots = state.shelfSlots.map { slot ->
                if (slot.placedAchievementId == achievementId) slot.copy(placedAchievementId = null)
                else slot
            }
        )
    }

    fun hasShelfSpace(achievement: Achievement): Boolean {
        return when (achievement.size) {
            AchievementSize.Large -> state.shelfSlots
                .filter { it.acceptedSizes.contains(AchievementSize.Large) }
                .any { slot ->
                    state.shelfSlots
                        .filter { it.section == slot.section }
                        .all { it.placedAchievementId == null }
                }
            AchievementSize.Medium -> state.shelfSlots
                .filter { it.acceptedSizes.contains(AchievementSize.Medium) && it.placedAchievementId == null }
                .any { slot ->
                    val sectionSlots = state.shelfSlots.filter { it.section == slot.section }
                    val hasLarge = sectionSlots.any { it.acceptedSizes.contains(AchievementSize.Large) && it.placedAchievementId != null }
                    val mediumsFilled = sectionSlots.count { it.acceptedSizes.contains(AchievementSize.Medium) && it.placedAchievementId != null }
                    !hasLarge && mediumsFilled < 2
                }
            AchievementSize.Small -> state.shelfSlots
                .any { it.acceptedSizes.contains(AchievementSize.Small) && it.placedAchievementId == null }
        }
    }

    fun getPlaceholderState() = RoomState(
        layout = RoomLayout.Default,
        currentPoints = 320,
        displayName = "Cody's Room",
        selectedRoomThemeId = "default",
        selectedWoodThemeId = "mahogany",
        achievements = listOf(
            Achievement(
                "streak_7",
                "One Week Streak",
                "7 day streak",
                R.drawable.trophy_24px,
                AchievementSize.Small,
                isUnlocked = true),
            Achievement(
                "streak_14",
                "Two Week Streak",
                "14 day streak",
                R.drawable.trophy_24px,
                AchievementSize.Small,
            isUnlocked = false),
            Achievement(
                "streak_21",
                "Three Week Streak",
                "21 day streak",
                R.drawable.trophy_24px,
                AchievementSize.Small,
                isUnlocked = false),
            Achievement(
                "streak_28",
                "One Month Streak",
                "28 day streak",
                R.drawable.trophy_24px,
                AchievementSize.Medium,
                isUnlocked = false),
            Achievement(
                "streak_50",
                "50 Day Streak",
                "50 day streak",
                R.drawable.trophy_24px,
                AchievementSize.Medium,
                isUnlocked = false),
            Achievement(
                "streak_100",
                "Streak Master",
                "100 day streak",
                R.drawable.trophy_24px,
                AchievementSize.Large,
                isUnlocked = false),
            Achievement(
                "walk_60",
                "One Hour Walker",
                "Walk 60 total mins",
                R.drawable.trophy_24px,
                AchievementSize.Small,
                isUnlocked = true),
            Achievement(
                "walk_120",
                "Two Hour Walker",
                "Walk 120 total mins",
                R.drawable.trophy_24px,
                AchievementSize.Small,
                isUnlocked = true),
            Achievement(
                "walk_360",
                "Six Hour Walker",
                "Walk 360 total mins",
                R.drawable.trophy_24px,
                AchievementSize.Medium,
                isUnlocked = true),
            Achievement(
                "walk_600",
                "Ten Hour Walker",
                "Walk 600 total mins",
                R.drawable.trophy_24px,
                AchievementSize.Large,
                isUnlocked = true),
            Achievement(
                "walk_1000",
                "Walk 1000",
                "Walk 1000 total mins",
                R.drawable.trophy_24px,
                AchievementSize.Large,
                isUnlocked = false),
        ),
        availableItems = listOf(),
        roomThemeOptions = listOf(
            RoomThemeOption(
                "default",
                "Default",
                RoomTheme(Color(0xFFDCCDC5), Color(0xFFCB8B50), Color(0xFFE0C4B3)),
                isUnlocked = true),
            RoomThemeOption(
                "moody",
                "Moody",
                RoomTheme(Color(0xFF606791), Color(0xFF403E4B), Color(0xFF6374A1)),
                isUnlocked = true),
            RoomThemeOption(
                "warm",
                "Warm",
                RoomTheme(Color(0xFFAF6D50), Color(0xFF6B4226), Color(0xFFE0A882)),
                isUnlocked = true),
            RoomThemeOption(
                "icy",
                "Icy",
                RoomTheme(Color(0xFF79B9C4), Color(0xFF4B719A), Color(0xFF93C5D2)),
                isUnlocked = true),
            RoomThemeOption(
                "forest",
                "Forest",
                RoomTheme(Color(0xFF4A7C59), Color(0xFF2D4A35), Color(0xFF7AAF8A)),
                isUnlocked = false),
            RoomThemeOption(
                "ocean",
                "Ocean",
                RoomTheme(Color(0xFF3A6B8A), Color(0xFF1A3A4A), Color(0xFF5B9BBF)),
                isUnlocked = false),
            RoomThemeOption(
                "midnight",
                "Midnight",
                RoomTheme(Color(0xFF2C2C54), Color(0xFF1A1A2E), Color(0xFF4A4A8A)),
                isUnlocked = false),
            RoomThemeOption(
                "opulent",
                "Opulent",
                RoomTheme(Color(0xFF542970), Color(0xFFD99D43), Color(0xFFE0AE65)),
                isUnlocked = false),
        ),
        shelfSlots = defaultShelfSlots(),
        woodThemeOptions = listOf(
            WoodThemeOption(
                id = "oak",
                name = "Oak",
                theme = WoodTheme(woodFront = Color(0xFF8B5E3C), woodTop = Color(0xFFA0714F), woodSide = Color(0xFF6B4226), woodDark = Color(0xFF4E2E14)),
                isUnlocked = true
            ),
            WoodThemeOption(
                id = "mahogany",
                name = "Mahogany",
                theme = WoodTheme(woodFront = Color(0xFF7B2D2D), woodTop = Color(0xFF9E4040), woodSide = Color(0xFF5C1E1E), woodDark = Color(0xFF3A0E0E)
                ),
                isUnlocked = true
            ),
            WoodThemeOption(
                id = "cherry",
                name = "Cherry",
                theme = WoodTheme(woodFront = Color(0xFFA1473B), woodTop = Color(0xFFB7584B), woodSide = Color(0xFF883B2D), woodDark = Color(0xFF692019)),
                isUnlocked = true
            ),
            WoodThemeOption(
                id = "walnut",
                name = "Walnut",
                theme = WoodTheme(woodFront = Color(0xFF4A3728), woodTop = Color(0xFF5E4A38), woodSide = Color(0xFF342418), woodDark = Color(0xFF1E1208)
                ),
                isUnlocked = true
            ),
            )
    )
}