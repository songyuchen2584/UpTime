package com.example.uptime.ui_testing

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.uptime.notification.NotificationSettings
import com.example.uptime.notification.NotificationSettingsScreen
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.collections.get

/**
 * Compose UI tests for NotificationSettingsScreen.
 *
 * These tests verify the screen's UI contract:
 * - Current notification settings are displayed.
 * - Buttons call the expected callbacks with updated values.
 * - The time-picker dialog can be opened and dismissed.
 *
 */
class NotificationSettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun notificationSettings_areDisplayed() {
        composeRule.setContent {
            NotificationSettingsScreen(
                settings = NotificationSettings(
                    screenWarningEnabled = false,
                    screenWarningThresholdMinutes = 10,
                    walkingReminderEnabled = false,
                    walkingReminderHour = 18,
                    walkingReminderMinute = 30
                ),
                onScreenWarningToggle = {},
                onWalkingReminderToggle = {},
                onWalkingTimeChange = { _, _ -> },
                onThresholdChange = {}
            )
        }

        composeRule.onNodeWithText("Notifications").assertIsDisplayed()
        composeRule.onNodeWithText("Screen time warning").assertIsDisplayed()
        composeRule.onNodeWithText("10 min").assertIsDisplayed()
        composeRule.onNodeWithText("Walking reminder").assertIsDisplayed()
        composeRule.onNodeWithText("Send walking progress reminder at 6:30 PM.").assertIsDisplayed()
    }

    @Test
    fun increaseThresholdButton_callsCallbackWithIncrementedValue() {
        var newThreshold: Int? = null

        composeRule.setContent {
            NotificationSettingsScreen(
                settings = NotificationSettings(screenWarningThresholdMinutes = 10),
                onScreenWarningToggle = {},
                onWalkingReminderToggle = {},
                onWalkingTimeChange = { _, _ -> },
                onThresholdChange = { newThreshold = it }
            )
        }

        composeRule.onNodeWithText("+1").performClick()

        assertEquals(11, newThreshold)
    }

    @Test
    fun decreaseThresholdButton_callsCallbackWithDecrementedValue() {
        var newThreshold: Int? = null

        composeRule.setContent {
            NotificationSettingsScreen(
                settings = NotificationSettings(screenWarningThresholdMinutes = 10),
                onScreenWarningToggle = {},
                onWalkingReminderToggle = {},
                onWalkingTimeChange = { _, _ -> },
                onThresholdChange = { newThreshold = it }
            )
        }

        composeRule.onNodeWithText("-1").performClick()

        assertEquals(9, newThreshold)
    }

    @Test
    fun timePickerDialog_opensAndCancels() {
        composeRule.setContent {
            NotificationSettingsScreen(
                settings = NotificationSettings(),
                onScreenWarningToggle = {},
                onWalkingReminderToggle = {},
                onWalkingTimeChange = { _, _ -> },
                onThresholdChange = {}
            )
        }

        composeRule.onNodeWithText("Change reminder time").performClick()
        composeRule.onNodeWithText("Choose walking reminder time").assertIsDisplayed()

        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.onNodeWithText("Choose walking reminder time").assertDoesNotExist()
    }

    @Test
    fun screenWarningSwitch_callsCallback() {
        var enabled = false

        composeRule.setContent {
            NotificationSettingsScreen(
                settings = NotificationSettings(screenWarningEnabled = false),
                onScreenWarningToggle = { enabled = it },
                onWalkingReminderToggle = {},
                onWalkingTimeChange = { _, _ -> },
                onThresholdChange = {}
            )
        }

        // First clickable toggle is the screen-warning switch.
        // Prefer Modifier.testTag("screen_warning_switch") in production code for a stronger selector.
        composeRule.onAllNodes(hasClickAction())[0].performClick()

        assertTrue(enabled)
    }
}
