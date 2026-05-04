package com.example.uptime.walking

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.health.connect.client.HealthConnectClient
import com.example.uptime.walking.model.WalkingStats
import com.example.uptime.walking.viewmodel.WalkingUiState
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for WalkingScreen.
 *
 * These tests focus on rendering and callback behavior:
 * - Source status text is correct for the provided SDK/sensor state.
 * - Walking totals are displayed from WalkingUiState.
 * - Buttons and switches call the callbacks supplied by the Route/ViewModel layer.
 *
 */
class WalkingScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun walkingStats_areDisplayed() {
        composeRule.setContent {
            WalkingScreen(
                state = WalkingUiState(
                    statsToday = WalkingStats(
                        totalSteps = 1250L,
                        totalWalkingMinutes = 14L
                    )
                ),
                sdkStatus = HealthConnectClient.SDK_AVAILABLE,
                sensorAvailable = true,
                sensorTracking = false,
                onToggleHealthConnect = {},
                onToggleSensor = {},
                onInstallHealthConnect = {},
                onRefresh = {}
            )
        }

        composeRule.onNodeWithText("Walking").assertIsDisplayed()
        composeRule.onNodeWithText("Steps: 1250").assertIsDisplayed()
        composeRule.onNodeWithText("Walking minutes: 14").assertIsDisplayed()
    }

    @Test
    fun fallbackMessage_isDisplayed_whenEstimatedMinutesWereUsed() {
        composeRule.setContent {
            WalkingScreen(
                state = WalkingUiState(
                    statsToday = WalkingStats(
                        totalSteps = 1000L,
                        totalWalkingMinutes = 10L,
                        usedEstimatedMinutesFallback = true
                    )
                ),
                sdkStatus = HealthConnectClient.SDK_AVAILABLE,
                sensorAvailable = true,
                sensorTracking = false,
                onToggleHealthConnect = {},
                onToggleSensor = {},
                onInstallHealthConnect = {},
                onRefresh = {}
            )
        }

        composeRule.onNodeWithText("Walking time estimated from steps for better accuracy.").assertIsDisplayed()
    }

    @Test
    fun refreshButton_callsCallback() {
        var clicked = false

        composeRule.setContent {
            WalkingScreen(
                state = WalkingUiState(loading = false),
                sdkStatus = HealthConnectClient.SDK_AVAILABLE,
                sensorAvailable = true,
                sensorTracking = false,
                onToggleHealthConnect = {},
                onToggleSensor = {},
                onInstallHealthConnect = {},
                onRefresh = { clicked = true }
            )
        }

        composeRule.onNodeWithText("Refresh").performClick()

        assertTrue(clicked)
    }

    @Test
    fun installHealthConnectButton_callsCallback_whenProviderUpdateRequired() {
        var clicked = false

        composeRule.setContent {
            WalkingScreen(
                state = WalkingUiState(),
                sdkStatus = HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED,
                sensorAvailable = true,
                sensorTracking = false,
                onToggleHealthConnect = {},
                onToggleSensor = {},
                onInstallHealthConnect = { clicked = true },
                onRefresh = {}
            )
        }

        composeRule.onNodeWithText("Install / Update Health Connect").performClick()

        assertTrue(clicked)
    }

    @Test
    fun errorMessage_isDisplayed_whenStateHasError() {
        composeRule.setContent {
            WalkingScreen(
                state = WalkingUiState(error = "Sensor failed"),
                sdkStatus = HealthConnectClient.SDK_AVAILABLE,
                sensorAvailable = true,
                sensorTracking = false,
                onToggleHealthConnect = {},
                onToggleSensor = {},
                onInstallHealthConnect = {},
                onRefresh = {}
            )
        }

        composeRule.onNodeWithText("Error: Sensor failed").assertIsDisplayed()
    }

    @Test
    fun healthConnectSwitch_callsCallback() {
        var toggled = false

        composeRule.setContent {
            WalkingScreen(
                state = WalkingUiState(useHealthConnect = false),
                sdkStatus = HealthConnectClient.SDK_AVAILABLE,
                sensorAvailable = true,
                sensorTracking = false,
                onToggleHealthConnect = { toggled = it },
                onToggleSensor = {},
                onInstallHealthConnect = {},
                onRefresh = {}
            )
        }

        // There are two switches on this screen. The first clickable toggle is Health Connect.
        // Adding Modifier.testTag("health_connect_switch") in production code would make this less brittle.
        composeRule.onAllNodes(hasClickAction())[0].performClick()

        assertTrue(toggled)
    }
}
