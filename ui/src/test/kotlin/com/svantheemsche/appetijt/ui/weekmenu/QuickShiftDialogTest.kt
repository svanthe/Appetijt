package com.svantheemsche.appetijt.ui.weekmenu

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.svantheemsche.appetijt.domain.model.Recipe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class QuickShiftDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun quickShiftDialog_displaysRecipeTitleAndAvailableDates() {
        val recipe = Recipe(id = 1, url = "u", title = "Spaghetti", imageUrl = null, sourceApp = "S")
        val today = LocalDate.now()
        val availableDates = listOf(today, today.plusDays(1))

        composeTestRule.setContent {
            MaterialTheme {
                QuickShiftDialog(
                    recipe = recipe,
                    availableDates = availableDates,
                    onDismiss = {},
                    onConfirm = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Move Recipe").assertIsDisplayed()
        composeTestRule.onNodeWithText("Move Spaghetti to another day this week:").assertIsDisplayed()
    }
}
