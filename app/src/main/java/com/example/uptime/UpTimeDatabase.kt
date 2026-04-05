package com.example.uptime

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DailyLog::class], version = 1)
abstract class UpTimeDatabase : RoomDatabase() {
    abstract fun dailyLogDao(): DailyLogDao
}