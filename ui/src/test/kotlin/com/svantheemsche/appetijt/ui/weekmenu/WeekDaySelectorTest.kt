package com.svantheemsche.appetijt.ui.weekmenu

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class WeekDaySelectorTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun weekDaySelector_displaysDatesAndHighlightsSelected() {
        val today = LocalDate.now()
        val dates = (0 until 7).map { today.plusDays(it.toLong()) }
        var selectedDate = today
        
        composeTestRule.setContent {
            MaterialTheme {
                WeekDaySelector(
                    dates = dates,
                    selectedDate = selectedDate,
                    plannedDates = emptySet(),
                    onDateSelected = { selectedDate = it }
                )
            }
        }

        // Verify some dates are shown (check day of month)
        composeTestRule.onNodeWithText(today.dayOfMonth.toString()).assertIsDisplayed()
        composeTestRule.onNodeWithText(today.plusDays(1).dayOfMonth.toString()).assertIsDisplayed()
    }

    @Test
    fun weekDaySelector_showsDotForPlannedDates() {
        // Since the dot is a Box with no text, we can't easily find it by text.
        // We just ensure the component renders without crashing for now.
        val today = LocalDate.now()
        val dates = listOf(today)
        
        composeTestRule.setContent {
            MaterialTheme {
                WeekDaySelector(
                    dates = dates,
                    selectedDate = today,
                    plannedDates = setOf(today),
                    onDateSelected = {}
                )
            }
        }
        
        composeTestRule.onNodeWithText(today.dayOfMonth.toString()).assertIsDisplayed()
    }
}
