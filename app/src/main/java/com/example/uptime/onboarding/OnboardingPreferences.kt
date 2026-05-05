package com.example.uptime.onboarding

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.onboardingDataStore by preferencesDataStore(
    name = "onboarding_preferences"
)

class OnboardingPreferences(
    private val context: Context
) {
    private val completedTasksKey = stringSetPreferencesKey("completed_onboarding_tasks")

    val completedTasks: Flow<Set<String>> =
        context.onboardingDataStore.data.map { preferences ->
            preferences[completedTasksKey] ?: emptySet()
        }

    suspend fun markTaskCompleted(task: OnboardingTask) {
        context.onboardingDataStore.edit { preferences ->
            val current = preferences[completedTasksKey] ?: emptySet()
            preferences[completedTasksKey] = current + task.name
        }
    }

    suspend fun resetOnboarding() {
        context.onboardingDataStore.edit { preferences ->
            preferences.remove(completedTasksKey)
        }
    }
}