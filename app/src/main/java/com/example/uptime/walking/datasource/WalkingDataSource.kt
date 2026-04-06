package com.example.uptime.walking.datasource

import com.example.uptime.walking.model.WalkingSessionInterval


interface WalkingDataSource {
    suspend fun getStepIntervals(startMillis: Long, endMillis: Long): List<WalkingSessionInterval>
}