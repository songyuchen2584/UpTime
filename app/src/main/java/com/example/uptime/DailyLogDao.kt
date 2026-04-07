package com.example.uptime

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyLogDao {

    @Query("SELECT * FROM daily_logs WHERE date = :date LIMIT 1")
    fun observeLogForDate(date: String): Flow<DailyLog?>

    @Query("SELECT * FROM daily_logs WHERE date = :date LIMIT 1")
    suspend fun getLogForDate(date: String): DailyLog?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLog(log: DailyLog)

    @Query("SELECT * FROM daily_logs ORDER BY date DESC LIMIT :limit")
    suspend fun getRecentLogs(limit: Int = 30): List<DailyLog>

    @Query("SELECT * FROM daily_logs ORDER BY date DESC")
    fun observeAllLogs(): Flow<List<DailyLog>>

    @Query("SELECT * FROM daily_logs")
    suspend fun getAllLogs(): List<DailyLog>
}