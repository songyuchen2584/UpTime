package com.example.uptime.onboarding

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.ui.graphics.vector.ImageVector

enum class OnboardingTask(
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    WALKING(
        title = "Set up walking tracker",
        description = "Choose how UpTime tracks your steps and walking progress.",
        icon = Icons.Default.DirectionsWalk
    ),

    SCREEN_TIME(
        title = "Set up screen time tracker",
        description = "Select the apps you want UpTime to monitor.",
        icon = Icons.Default.PhoneAndroid
    ),

    NOTIFICATIONS(
        title = "Set up notifications",
        description = "Enable reminders and warnings for your daily goals.",
        icon = Icons.Default.Notifications
    ),

    SIGN_IN(
        title = "Sign in",
        description = "Save your profile and personalize your experience.",
        icon = Icons.Default.Person
    )
}