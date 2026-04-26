package com.example.uptime.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomSettingsDao {

    @Query("SELECT * FROM room_settings WHERE userId = :userId")
    fun observeRoomSettings(userId: String): Flow<RoomSettings?>

    @Query("SELECT * FROM room_settings WHERE userId = :userId")
    suspend fun getSettings(userId: String): RoomSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRoomSettings(settings: RoomSettings)
}