package com.example.uptime

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_logs")
data class DailyLog(
    @PrimaryKey val date: String, // "yyyy-MM-dd"
    val screenTimeMinutes: Int = 0,
    val walkingMinutes: Int = 0,
    val screenTimeGoal: Int = 30,
    val walkingGoal: Int = 30,
    val streakMaintained: Boolean = false
)