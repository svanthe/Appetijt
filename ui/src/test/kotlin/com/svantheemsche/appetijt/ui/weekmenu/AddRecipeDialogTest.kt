package com.svantheemsche.appetijt.ui.weekmenu

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AddRecipeDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun addRecipeDialog_displaysTitleAndInput() {
        composeTestRule.setContent {
            MaterialTheme {
                AddRecipeDialog(onDismiss = {}, onConfirm = {})
            }
        }

        composeTestRule.onNodeWithText("Add Recipe URL").assertIsDisplayed()
        composeTestRule.onNodeWithText("Recipe URL").assertIsDisplayed()
    }

    @Test
    fun addRecipeDialog_callsOnConfirmWithUrl() {
        var confirmedUrl = ""
        composeTestRule.setContent {
            MaterialTheme {
                AddRecipeDialog(
                    onDismiss = {},
                    onConfirm = { confirmedUrl = it }
                )
            }
        }

        val url = "https://pasta.com"
        composeTestRule.onNode(hasSetTextAction()).performTextInput(url)
        composeTestRule.onNodeWithText("Add").performClick()

        assert(confirmedUrl == url)
    }
}
