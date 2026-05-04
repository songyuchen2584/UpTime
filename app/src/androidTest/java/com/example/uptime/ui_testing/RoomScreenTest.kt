package com.example.uptime.ui_testing

import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.uptime.room.AchievementsPanel
import com.example.uptime.room.CustomizePanel
import com.example.uptime.room.ExchangePanel
import com.example.uptime.room.RoomLoadingScreen
import com.example.uptime.room.RoomMode
import com.example.uptime.room.RoomScreen
import com.example.uptime.room.VisitPanel
import org.junit.Assert.assertTrue
import androidx.compose.ui.test.onAllNodesWithText
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for room UI components.
 *
 * RoomScreen depends on RoomViewModel, Firebase user state, catalogs, and animated room
 * drawing. These tests focus on the smaller Composables that are stable and verifiable
 * with Compose UI testing.
 *
 */
class RoomScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun roomLoadingScreen_displaysLoadingMessage() {
        composeRule.setContent {
            RoomLoadingScreen()
        }

        composeRule.onNodeWithText("Loading the room...").assertIsDisplayed()
    }

    @Test
    fun visitPanel_displaysLabelAndCallsClick() {
        var clicked = false

        composeRule.setContent {
            VisitPanel(onClick = { clicked = true })
        }

        composeRule.onNodeWithText("Visit").assertIsDisplayed()
        composeRule.onNodeWithText("Visit").performClick()

        assertTrue(clicked)
    }

    @Test
    fun achievementsPanel_displaysLabelAndCallsClick() {
        var clicked = false

        composeRule.setContent {
            AchievementsPanel(onClick = { clicked = true })
        }

        composeRule.onNodeWithText("Achievements").assertIsDisplayed()
        composeRule.onNodeWithText("Achievements").performClick()

        assertTrue(clicked)
    }

    @Test
    fun customizePanel_displaysLabelAndCallsClick() {
        var clicked = false

        composeRule.setContent {
            CustomizePanel(isActive = false, onClick = { clicked = true })
        }

        composeRule.onNodeWithText("Customize").assertIsDisplayed()
        composeRule.onNodeWithText("Customize").performClick()

        assertTrue(clicked)
    }

}
