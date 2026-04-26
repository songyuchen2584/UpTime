package com.example.uptime.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class RoomConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromMap(map: Map<String, String>): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun toMap(json: String): Map<String, String> {
        return gson.fromJson(json, object: TypeToken<Map<String, String>>() {}.type)
    }

    @TypeConverter
    fun fromStringSet(set: Set<String>): String {
        return gson.toJson(set)
    }

    @TypeConverter
    fun toStringSet(json: String): Set<String> {
        return gson.fromJson(json, object: TypeToken<Set<String>>() {}.type)
    }
}

data class UserInventoryConverted(
    val userId: String = "",
    val unlockedRoomThemeIds: List<String> = listOf("default"),
    val unlockedWoodThemeIds: List<String> = listOf("oak"),
    val unlockedAchievementIds: List<String> = listOf("start"),
    val unlockedRoomItemIds: List<String> = emptyList(),
    val unlockedRoomLayoutIds: List<String> = listOf("default"),
    val currentPoints: Int = 0,
    val totalPointsSpent: Int = 0,
    val screenTimeFailCount: Int = 0,
    val consecutiveScreenTimeSuccess: Int = 0,
)

data class RoomSettingsConverted(
    val userId: String = "",
    val selectedRoomLayoutId: String = "default",
    val displayName: String = "My Room",
    val selectedRoomThemeId: String = "default",
    val selectedWoodThemeId: String = "oak",
    val placedAchievements: Map<String, String> = emptyMap(),
    val placedRoomItems: Map<String, String> = emptyMap()
)

fun UserInventory.convert() = UserInventoryConverted(
    userId = userId,
    unlockedRoomThemeIds = unlockedRoomThemeIds.toList(),
    unlockedWoodThemeIds = unlockedWoodThemeIds.toList(),
    unlockedAchievementIds = unlockedAchievementIds.toList(),
    unlockedRoomItemIds = unlockedRoomItemIds.toList(),
    unlockedRoomLayoutIds = unlockedRoomLayoutIds.toList(),
    currentPoints = currentPoints,
    totalPointsSpent = totalPointsSpent,
    screenTimeFailCount = screenTimeFailCount,
    consecutiveScreenTimeSuccess = consecutiveScreenTimeSuccess
)

fun RoomSettings.convert() = RoomSettingsConverted(
    userId = userId,
    selectedRoomLayoutId = selectedRoomLayoutId,
    displayName = displayName,
    selectedRoomThemeId = selectedRoomThemeId,
    selectedWoodThemeId = selectedWoodThemeId,
    placedAchievements = placedAchievements,
    placedRoomItems = placedRoomItems
)

fun UserInventoryConverted.unconvert() = UserInventory(
    userId = userId,
    unlockedRoomThemeIds = unlockedRoomThemeIds.toSet(),
    unlockedWoodThemeIds = unlockedWoodThemeIds.toSet(),
    unlockedAchievementIds = unlockedAchievementIds.toSet(),
    unlockedRoomItemIds = unlockedRoomItemIds.toSet(),
    unlockedRoomLayoutIds = unlockedRoomLayoutIds.toSet(),
    currentPoints = currentPoints,
    totalPointsSpent = totalPointsSpent,
    screenTimeFailCount = screenTimeFailCount,
    consecutiveScreenTimeSuccess = consecutiveScreenTimeSuccess
)

fun RoomSettingsConverted.unconvert() = RoomSettings(
    userId = userId,
    selectedRoomLayoutId = selectedRoomLayoutId,
    displayName = displayName,
    selectedRoomThemeId = selectedRoomThemeId,
    selectedWoodThemeId = selectedWoodThemeId,
    placedAchievements = placedAchievements,
    placedRoomItems = placedRoomItems
)