package com.svantheemsche.appetijt.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.svantheemsche.appetijt.domain.model.Recipe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RecipeLibraryDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockRecipes = listOf(
        Recipe(id = 1L, url = "https://r1.com", title = "Pasta Carbonara", imageUrl = null, sourceApp = "App"),
        Recipe(id = 2L, url = "https://r2.com", title = "Chicken Curry", imageUrl = null, sourceApp = "App")
    )

    @Test
    fun recipeLibraryDialog_displaysRecipes() {
        composeTestRule.setContent {
            MaterialTheme {
                RecipeLibraryDialog(
                    recipes = mockRecipes,
                    searchQuery = "",
                    onSearchQueryChange = {},
                    onRecipeSelected = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithText("Culinair archief").assertIsDisplayed()
        composeTestRule.onNodeWithText("Pasta Carbonara").assertIsDisplayed()
        composeTestRule.onNodeWithText("Chicken Curry").assertIsDisplayed()
    }

    @Test
    fun recipeLibraryDialog_displaysEmptyMessage() {
        composeTestRule.setContent {
            MaterialTheme {
                RecipeLibraryDialog(
                    recipes = emptyList(),
                    searchQuery = "",
                    onSearchQueryChange = {},
                    onRecipeSelected = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithText("No recipes found.").assertIsDisplayed()
    }

    @Test
    fun recipeLibraryDialog_callsOnSearchQueryChange() {
        var capturedQuery = ""
        composeTestRule.setContent {
            MaterialTheme {
                RecipeLibraryDialog(
                    recipes = mockRecipes,
                    searchQuery = "",
                    onSearchQueryChange = { capturedQuery = it },
                    onRecipeSelected = {},
                    onDismiss = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("library_search_field").performTextInput("Pasta")
        
        assert(capturedQuery == "Pasta")
    }
}
