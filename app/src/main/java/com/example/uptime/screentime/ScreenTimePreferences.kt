package com.example.uptime.screentime

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.screenTimeDataStore by preferencesDataStore(name = "screen_time_prefs")

class ScreenTimePreferences(private val context: Context) {

    private object Keys {
        val SELECTED_PACKAGES = stringSetPreferencesKey("selected_packages")
    }

    val selectedPackagesFlow: Flow<Set<String>> =
        context.screenTimeDataStore.data.map { prefs ->
            prefs[Keys.SELECTED_PACKAGES] ?: emptySet()
        }

    suspend fun setSelectedPackages(packages: Set<String>) {
        context.screenTimeDataStore.edit { prefs ->
            prefs[Keys.SELECTED_PACKAGES] = packages
        }
    }
}