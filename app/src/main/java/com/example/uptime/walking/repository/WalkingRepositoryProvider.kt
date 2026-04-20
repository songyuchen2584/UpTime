package com.example.uptime.walking.repository

import android.content.Context
import com.example.uptime.walking.TrackingPreferences
import com.example.uptime.walking.WalkingRepository
import com.example.uptime.walking.datasource.DeviceSensorStepsDataSource
import com.example.uptime.walking.datasource.HealthConnectStepsDataSource

object WalkingRepositoryProvider {
    private var instance: WalkingRepository? = null

    fun get(context: Context): WalkingRepository {
        if (instance == null) {
            val prefs = TrackingPreferences(context)
            instance = WalkingRepository(
                HealthConnectStepsDataSource(context),
                DeviceSensorStepsDataSource.getInstance(context),
                prefs
            )
        }
        return instance!!
    }
}