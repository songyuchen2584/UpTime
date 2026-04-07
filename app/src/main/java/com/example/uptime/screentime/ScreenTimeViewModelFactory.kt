package com.example.uptime.screentime

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.uptime.screentime.models.ScreenTimeSnapshot
import com.example.uptime.screentime.viewmodel.ScreenTimeViewModel

class ScreenTimeViewModelFactory(
    private val application: Application,
    private val updateScreenTime: (ScreenTimeSnapshot) -> Unit
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScreenTimeViewModel::class.java)) {
            return ScreenTimeViewModel(application, updateScreenTime) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}