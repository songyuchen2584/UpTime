package com.example.uptime.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserInventoryDao {

    @Query("SELECT * FROM user_inventory WHERE userId = :userId")
    fun observeUserInventory(userId: String): Flow<UserInventory?>

    @Query("SELECT * FROM user_inventory WHERE userId = :userId")
    suspend fun getInventory(userId: String): UserInventory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertInventory(inventory: UserInventory)
}