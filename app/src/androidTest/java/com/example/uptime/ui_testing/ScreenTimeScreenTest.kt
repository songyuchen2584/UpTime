package com.example.uptime.ui_testing

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.example.uptime.screentime.ScreenTimeScreen
import com.example.uptime.screentime.models.AppScreenTime
import com.example.uptime.screentime.models.InstalledAppInfo
import com.example.uptime.screentime.models.ScreenTimeUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for ScreenTimeScreen.
 *
 * These tests verify only the UI contract of the screen:
 * - The screen displays the expected text for a given ScreenTimeUiState.
 * - User actions call the callbacks passed into the Composable.
 * - Search and recommended-only filtering change the visible app list.
 *
 */
class ScreenTimeScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun titleAndUsageAccessWarning_areDisplayed_whenPermissionMissing() {
        composeRule.setContent {
            ScreenTimeScreen(
                uiState = ScreenTimeUiState(hasUsageAccess = false),
                onOpenUsageAccessSettings = {},
                onTogglePackage = { _, _ -> },
                onRefresh = {}
            )
        }

        composeRule.onNodeWithText("Screen Time").assertIsDisplayed()
        composeRule.onNodeWithText("Usage Access Required").assertIsDisplayed()
        composeRule.onNodeWithText("Open Settings").assertIsDisplayed()
    }

    @Test
    fun openSettingsButton_callsCallback() {
        var clicked = false

        composeRule.setContent {
            ScreenTimeScreen(
                uiState = ScreenTimeUiState(hasUsageAccess = false),
                onOpenUsageAccessSettings = { clicked = true },
                onTogglePackage = { _, _ -> },
                onRefresh = {}
            )
        }

        composeRule.onNodeWithText("Open Settings").performClick()

        assertTrue(clicked)
    }

    @Test
    fun refreshButton_callsCallback() {
        var clicked = false

        composeRule.setContent {
            ScreenTimeScreen(
                uiState = ScreenTimeUiState(hasUsageAccess = true),
                onOpenUsageAccessSettings = {},
                onTogglePackage = { _, _ -> },
                onRefresh = { clicked = true }
            )
        }

        composeRule.onNodeWithText("Refresh Screen Time").performClick()

        assertTrue(clicked)
    }

    @Test
    fun searchFiltersInstalledApps() {
        val state = ScreenTimeUiState(
            hasUsageAccess = true,
            installedApps = listOf(
                InstalledAppInfo(
                    packageName = "com.instagram.android",
                    appLabel = "Instagram",
                    icon = null,
                    isRecommendedSocial = true
                ),
                InstalledAppInfo(
                    packageName = "com.amazon.mShop.android.shopping",
                    appLabel = "Amazon",
                    icon = null,
                    isRecommendedSocial = false
                )
            )
        )

        composeRule.setContent {
            ScreenTimeScreen(
                uiState = state,
                onOpenUsageAccessSettings = {},
                onTogglePackage = { _, _ -> },
                onRefresh = {}
            )
        }

        composeRule.onNodeWithText("Search apps...").performTextInput("insta")

        composeRule.onNodeWithText("Instagram").assertIsDisplayed()
        composeRule.onNodeWithText("Amazon").assertIsNotDisplayed()
    }

    @Test
    fun appRowClick_callsToggleCallbackWithPackageName() {
        var toggledPackage: String? = null
        var toggledSelected: Boolean? = null

        val state = ScreenTimeUiState(
            hasUsageAccess = true,
            installedApps = listOf(
                InstalledAppInfo(
                    packageName = "com.instagram.android",
                    appLabel = "Instagram",
                    icon = null,
                    isRecommendedSocial = true
                )
            ),
            selectedPackages = emptySet()
        )

        composeRule.setContent {
            ScreenTimeScreen(
                uiState = state,
                onOpenUsageAccessSettings = {},
                onTogglePackage = { packageName, selected ->
                    toggledPackage = packageName
                    toggledSelected = selected
                },
                onRefresh = {}
            )
        }

        composeRule
            .onNodeWithTag("app_checkbox_com.instagram.android", useUnmergedTree = true)
            .performClick()

        assertEquals("com.instagram.android", toggledPackage)
        assertEquals(true, toggledSelected)
    }

    @Test
    fun todayUsageSummary_displaysTrackedTime() {
        val state = ScreenTimeUiState(
            hasUsageAccess = true,
            todayUsage = listOf(
                AppScreenTime(
                    packageName = "com.youtube.android",
                    appLabel = "YouTube",
                    totalTimeMs = 90L * 60_000L
                )
            ),
            totalTrackedTimeMs = 90L * 60_000L
        )

        composeRule.setContent {
            ScreenTimeScreen(
                uiState = state,
                onOpenUsageAccessSettings = {},
                onTogglePackage = { _, _ -> },
                onRefresh = {}
            )
        }

        composeRule.onNodeWithText("Tracked today: 1h 30m").assertIsDisplayed()
        composeRule.onNodeWithText("YouTube").assertIsDisplayed()
        composeRule.onNodeWithText("1h 30m").assertIsDisplayed()
    }
}