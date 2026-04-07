package com.example.uptime.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RoomSettingsDao {

    @Query("SELECT * FROM room_settings WHERE id = 0")
    fun observeRoomSettings(): Flow<RoomSettings?>

    @Query("SELECT * FROM room_settings WHERE id = 0")
    suspend fun getSettings(): RoomSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRoomSettings(settings: RoomSettings)
}