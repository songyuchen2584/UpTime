package com.example.uptime

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.uptime.room.RoomConverters
import com.example.uptime.room.RoomSettings
import com.example.uptime.room.RoomSettingsDao
import com.example.uptime.room.UserInventory
import com.example.uptime.room.UserInventoryDao

@Database(entities = [DailyLog::class, RoomSettings::class, UserInventory::class], version = 3)
@TypeConverters(RoomConverters::class)
abstract class UpTimeDatabase : RoomDatabase() {
    abstract fun dailyLogDao(): DailyLogDao
    abstract fun roomSettingsDao(): RoomSettingsDao
    abstract fun userInventoryDao(): UserInventoryDao

    companion object {
        @Volatile
        private var INSTANCE: UpTimeDatabase? = null

        fun getDatabase(context: Context): UpTimeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    UpTimeDatabase::class.java,
                    "uptime_db"
                ).fallbackToDestructiveMigration(false).build()
                INSTANCE = instance
                instance
            }
        }
    }
}