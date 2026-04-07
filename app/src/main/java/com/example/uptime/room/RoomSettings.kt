package com.example.uptime.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "room_settings")
data class RoomSettings (
    @PrimaryKey val id: Int = 0,
    val selectedRoomLayoutId: String = "default",
    val displayName: String = "My Room",
    val selectedRoomThemeId: String = "default",
    val selectedWoodThemeId: String = "oak",
    val placedAchievements: Map<String, String> = emptyMap(), // Slot id, achievementID
    val placedRoomItems: Map<String, String> = emptyMap()
)