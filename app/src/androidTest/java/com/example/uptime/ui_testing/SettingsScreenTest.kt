package com.example.uptime.ui_testing

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.uptime.AboutRow
import com.example.uptime.SettingsRow
import com.example.uptime.SignedInCard
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for settings UI components.
 *
 * SettingsScreen depends on AuthViewModel and Firebase-backed auth state, so these tests
 * target the smaller pure Composables used by SettingsScreen: SignedInCard, SettingsRow,
 * and AboutRow.
 *
 */
class SettingsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun signedInCard_displaysEmailAndCallsSignOut() {
        var signOutClicked = false

        composeRule.setContent {
            SignedInCard(
                email = "student@example.com",
                onSignOut = { signOutClicked = true }
            )
        }

        composeRule.onNodeWithText("Account").assertIsDisplayed()
        composeRule.onNodeWithText("student@example.com").assertIsDisplayed()
        composeRule.onNodeWithText("Sign Out").performClick()

        assertTrue(signOutClicked)
    }

    @Test
    fun settingsRow_displaysTitleSubtitleAndCallsClick() {
        var clicked = false

        composeRule.setContent {
            SettingsRow(
                title = "Notifications",
                subtitle = "Customize screen time warnings and walking reminders",
                onClick = { clicked = true }
            )
        }

        composeRule.onNodeWithText("Notifications").assertIsDisplayed()
        composeRule.onNodeWithText("Customize screen time warnings and walking reminders").assertIsDisplayed()
        composeRule.onNodeWithText("Notifications").performClick()

        assertTrue(clicked)
    }

    @Test
    fun aboutRow_displaysLabelAndValue() {
        composeRule.setContent {
            AboutRow(label = "App", value = "UpTime")
        }

        composeRule.onNodeWithText("App").assertIsDisplayed()
        composeRule.onNodeWithText("UpTime").assertIsDisplayed()
    }
}
