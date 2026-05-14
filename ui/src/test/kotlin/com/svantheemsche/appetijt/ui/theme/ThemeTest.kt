package com.svantheemsche.appetijt.ui.theme

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ThemeTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun theme_loadsCorrectly() {
        composeTestRule.setContent {
            AppetijtTheme(darkTheme = false) {
                Text("Light Theme Test")
            }
        }
    }

    @Test
    fun darkTheme_loadsCorrectly() {
        composeTestRule.setContent {
            AppetijtTheme(darkTheme = true) {
                Text("Dark Theme Test")
            }
        }
    }

    @Test
    fun dynamicTheme_loadsCorrectly() {
        composeTestRule.setContent {
            AppetijtTheme(dynamicColor = true) {
                Text("Dynamic Theme Test")
            }
        }
    }
}
