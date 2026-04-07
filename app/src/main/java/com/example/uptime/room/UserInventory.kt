package com.example.uptime.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_inventory")
data class UserInventory (
    @PrimaryKey val id: Int = 0,
    val unlockedRoomThemeIds: Set<String> = setOf("default"),
    val unlockedWoodThemeIds: Set<String> = setOf("oak"),
    val unlockedAchievementIds: Set<String> = setOf("start"),
    val unlockedRoomItemIds: Set<String> = emptySet(),
    val unlockedRoomLayoutIds: Set<String> = setOf("default"),
)