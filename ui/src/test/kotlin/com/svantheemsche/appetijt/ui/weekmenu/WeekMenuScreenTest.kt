package com.svantheemsche.appetijt.ui.weekmenu

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.svantheemsche.appetijt.domain.model.Recipe
import com.svantheemsche.appetijt.domain.repository.MealPlanRepository
import com.svantheemsche.appetijt.domain.usecase.GetRecipesForDateUseCase
import com.svantheemsche.appetijt.domain.usecase.AddSharedRecipeUseCase
import com.svantheemsche.appetijt.domain.usecase.GetLibraryRecipesUseCase
import com.svantheemsche.appetijt.domain.usecase.AddLibraryRecipeToDateUseCase
import com.svantheemsche.appetijt.ui.test.MainDispatcherRule
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class WeekMenuScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockRepository: MealPlanRepository = mockk()
    private val mockGetRecipesUseCase: GetRecipesForDateUseCase = mockk()
    private val mockAddSharedUseCase: AddSharedRecipeUseCase = mockk()
    private val mockGetLibraryUseCase: GetLibraryRecipesUseCase = mockk(relaxed = true)
    private val mockAddLibraryUseCase: AddLibraryRecipeToDateUseCase = mockk(relaxed = true)
    
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        every { mockGetRecipesUseCase(any()) } returns flowOf(emptyList())
        every { mockRepository.hasRecipesForDate(any()) } returns flowOf(false)

        // Initialize the ViewModel with mocked dependencies
        viewModel = MainViewModel(
            mealPlanRepository = mockRepository,
            getRecipesForDateUseCase = mockGetRecipesUseCase,
            addSharedRecipeUseCase = mockAddSharedUseCase,
            getLibraryRecipesUseCase = mockGetLibraryUseCase,
            addLibraryRecipeToDateUseCase = mockAddLibraryUseCase
        )
    }

    @Test
    fun weekMenuScreen_displaysTitleAndFab() {
        composeTestRule.setContent {
            MaterialTheme {
                WeekMenuScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Appetijt").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Add Recipe").assertIsDisplayed()
    }

    @Test
    fun weekMenuScreen_displaysRecipesForSelectedDay() {
        val today = LocalDate.now()
        val recipes = listOf(
            Recipe(id = 1, url = "u1", title = "Recipe 1", imageUrl = null, sourceApp = "App")
        )
        every { mockGetRecipesUseCase(today) } returns flowOf(recipes)
        
        viewModel.selectDay(today)

        composeTestRule.setContent {
            MaterialTheme {
                WeekMenuScreen(viewModel = viewModel)
            }
        }

        composeTestRule.onNodeWithText("Recipe 1").assertIsDisplayed()
    }
}
