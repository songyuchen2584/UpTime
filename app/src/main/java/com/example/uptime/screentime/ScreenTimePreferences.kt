package com.example.uptime.screentime

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

private val Context.screenTimeDataStore by preferencesDataStore(name = "screen_time_prefs")

class ScreenTimePreferences(private val context: Context) {

    private object Keys {
        val SELECTED_PACKAGES = stringSetPreferencesKey("selected_packages")
        val PENDING_PACKAGES = stringSetPreferencesKey("pending_packages")
        val PENDING_DATE = stringPreferencesKey("pending_date")
        val FIRST_INSTALL_DATE = stringPreferencesKey("first_install_date")
    }

    val selectedPackagesFlow: Flow<Set<String>> =
        context.screenTimeDataStore.data.map { prefs ->
            prefs[Keys.SELECTED_PACKAGES] ?: emptySet()
        }

    val pendingPackagesFlow: Flow<Set<String>?> =
        context.screenTimeDataStore.data.map { prefs ->
            prefs[Keys.PENDING_PACKAGES]
        }

    val pendingDateFlow: Flow<String?> =
        context.screenTimeDataStore.data.map { prefs ->
            prefs[Keys.PENDING_DATE]
        }

    val firstInstallDateFlow: Flow<String?> =
        context.screenTimeDataStore.data.map { prefs ->
            prefs[Keys.FIRST_INSTALL_DATE]
        }

    suspend fun setSelectedPackages(packages: Set<String>) {
        context.screenTimeDataStore.edit { prefs ->
            prefs[Keys.SELECTED_PACKAGES] = packages
        }
    }

    suspend fun setPendingPackages(packages: Set<String>) {
        context.screenTimeDataStore.edit { prefs ->
            prefs[Keys.PENDING_PACKAGES] = packages
            prefs[Keys.PENDING_DATE] = todayString()
        }
    }

    suspend fun clearPendingPackages() {
        context.screenTimeDataStore.edit { prefs ->
            prefs.remove(Keys.PENDING_PACKAGES)
            prefs.remove(Keys.PENDING_DATE)
        }
    }

    suspend fun ensureFirstInstallDate() {
        context.screenTimeDataStore.edit { prefs ->
            if (prefs[Keys.FIRST_INSTALL_DATE] == null) {
                prefs[Keys.FIRST_INSTALL_DATE] = todayString()
            }
        }
    }

    suspend fun resolveEffectivePackages(): Set<String> {
        ensureFirstInstallDate()

        val prefs = context.screenTimeDataStore.data.first()

        val selected = prefs[Keys.SELECTED_PACKAGES] ?: emptySet()
        val pending = prefs[Keys.PENDING_PACKAGES]
        val pendingDate = prefs[Keys.PENDING_DATE]

        if (selected.isEmpty() && pending != null) {
            setSelectedPackages(pending)
            clearPendingPackages()
            return pending
        }

        if (pending != null && pendingDate != null) {
            val today = LocalDate.parse(todayString())
            val pendingCreatedDate = LocalDate.parse(pendingDate)

            if (today.isAfter(pendingCreatedDate)) {
                setSelectedPackages(pending)
                clearPendingPackages()
                return pending
            }
        }

        return selected
    }

    suspend fun updatePackagesWithNextDayRule(packages: Set<String>) {
        val currentSelected = selectedPackagesFlow.first()

        if (currentSelected.isEmpty()) {
            setSelectedPackages(packages)
            clearPendingPackages()
        } else {
            setPendingPackages(packages)
        }
    }

    private fun todayString(): String {
        return LocalDate.now().toString()
    }
}