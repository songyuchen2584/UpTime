package com.example.uptime.ui_testing

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.uptime.dashboard.DailyStatusCard
import com.example.uptime.dashboard.DashboardState
import com.example.uptime.dashboard.GoalsCard
import com.example.uptime.dashboard.ProgressRing
import com.example.uptime.dashboard.StreakCard
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Compose UI tests for dashboard UI components.
 *
 * DashboardScreen itself creates real ViewModels and observes lifecycle events, so these
 * tests focus on the reusable dashboard Composables that are pure UI: StreakCard,
 * ProgressRing, DailyStatusCard, and GoalsCard.
 *
 */
class DashboardScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun streakCard_displaysCurrentStreakAndMonthlyReport() {
        var clicked = false

        composeRule.setContent {
            StreakCard(
                currentStreak = 5,
                bothGoalsMet = true,
                onClick = { clicked = true }
            )
        }

        composeRule.onNodeWithText("5 day streak").assertIsDisplayed()
        composeRule.onNodeWithText("Monthly").assertIsDisplayed()
        composeRule.onNodeWithText("Report").assertIsDisplayed()
        composeRule.onNodeWithText("5 day streak").performClick()

        assertTrue(clicked)
    }

    @Test
    fun progressRing_displaysLabelValueUnitAndSubtitle() {
        var clicked = false

        composeRule.setContent {
            MaterialTheme {
                ProgressRing(
                    label = "Walking",
                    value = "20",
                    unit = "min",
                    subtitle = "10 min to go",
                    progress = 0.67f,
                    ringColor = Color.Green,
                    trackColor = Color.LightGray,
                    onClick = { clicked = true }
                )
            }
        }

        composeRule.onNodeWithText("Walking").assertIsDisplayed()
        composeRule.onNodeWithText("20").assertIsDisplayed()
        composeRule.onNodeWithText("min").assertIsDisplayed()
        composeRule.onNodeWithText("10 min to go").assertIsDisplayed()
        composeRule.onNodeWithText("Walking").performClick()

        assertTrue(clicked)
    }

    @Test
    fun dailyStatusCard_displaysBothProgressRows() {
        composeRule.setContent {
            DailyStatusCard(
                state = DashboardState(
                    screenTimeUsed = 20,
                    screenTimeGoal = 30,
                    walkingDone = 10,
                    walkingGoal = 30
                ),
                onClickWalking = {},
                onClickScreenTime = {}
            )
        }

        composeRule.onNodeWithText("Today's Progress").assertIsDisplayed()
        composeRule.onNodeWithText("20 / 30 min").assertIsDisplayed()
        composeRule.onNodeWithText("10 / 30 min").assertIsDisplayed()
    }

    @Test
    fun goalsCard_displaysGoalRequirementsAndSuccessMessage() {
        composeRule.setContent {
            GoalsCard(
                state = DashboardState(
                    screenTimeUsed = 20,
                    screenTimeGoal = 30,
                    walkingDone = 30,
                    walkingGoal = 30
                )
            )
        }

        composeRule.onNodeWithText("Daily Goals").assertIsDisplayed()
        composeRule.onNodeWithText("Stay under 30 min of screen time").assertIsDisplayed()
        composeRule.onNodeWithText("Walk at least 30 min").assertIsDisplayed()
        composeRule.onNodeWithText("Make it to midnight to increase your streak!").assertIsDisplayed()
    }
}
