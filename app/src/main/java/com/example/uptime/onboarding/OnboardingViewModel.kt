package com.example.uptime.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val preferences: OnboardingPreferences
) : ViewModel() {

    val visibleTasks = preferences.completedTasks
        .map { completed ->
            OnboardingTask.entries.filter { task ->
                task.name !in completed
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            OnboardingTask.entries
        )

    fun completeTask(task: OnboardingTask) {
        viewModelScope.launch {
            preferences.markTaskCompleted(task)
        }
    }

    fun dismissTask(task: OnboardingTask) {
        completeTask(task)
    }
}