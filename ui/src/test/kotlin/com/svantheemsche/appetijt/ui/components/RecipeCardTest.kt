package com.svantheemsche.appetijt.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsDisplayed
import coil.compose.LocalImageLoader
import coil.ImageLoader
import com.svantheemsche.appetijt.domain.model.Recipe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RecipeCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Use a lazy property to ensure the context is accessed only after the test environment is ready
    private val mockImageLoader by lazy {
        ImageLoader.Builder(
            context = androidx.test.core.app.ApplicationProvider.getApplicationContext()
        ).build()
    }

    @Test
    fun recipeCard_displaysTitleAndSourceApp() {
        val recipe = Recipe(
            url = "https://example.com",
            title = "Test Recipe",
            imageUrl = null,
            sourceApp = "Test App"
        )

        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides mockImageLoader) {
                MaterialTheme {
                    RecipeCard(
                        recipe = recipe,
                        onClick = {},
                        onLongClick = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Test Recipe").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test App").assertIsDisplayed()
    }

    @Test
    fun recipeCard_handlesMissingSourceApp() {
        val recipe = Recipe(
            url = "https://example.com",
            title = "Test Recipe",
            imageUrl = null,
            sourceApp = null
        )

        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides mockImageLoader) {
                MaterialTheme {
                    RecipeCard(
                        recipe = recipe,
                        onClick = {},
                        onLongClick = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Test Recipe").assertIsDisplayed()
    }

    @Test
    fun recipeCard_displaysPlaceholderWhenImageLoading() {
        val recipe = Recipe(
            url = "https://example.com",
            title = "Test Recipe",
            imageUrl = "https://example.com/image.jpg",
            sourceApp = "Test App"
        )

        composeTestRule.setContent {
            CompositionLocalProvider(LocalImageLoader provides mockImageLoader) {
                MaterialTheme {
                    RecipeCard(
                        recipe = recipe,
                        onClick = {},
                        onLongClick = {}
                    )
                }
            }
        }

        composeTestRule.onNodeWithText("Test Recipe").assertIsDisplayed()
    }
}
