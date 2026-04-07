package com.example.uptime.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserInventoryDao {

    @Query("SELECT * FROM user_inventory WHERE id = 0")
    fun observeUserInventory(): Flow<UserInventory?>

    @Query("SELECT * FROM user_inventory WHERE id = 0")
    suspend fun getInventory(): UserInventory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInventory(inventory: UserInventory)
}