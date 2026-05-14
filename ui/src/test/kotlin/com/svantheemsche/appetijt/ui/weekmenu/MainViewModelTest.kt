package com.svantheemsche.appetijt.ui.weekmenu

import com.svantheemsche.appetijt.domain.model.Recipe
import com.svantheemsche.appetijt.domain.repository.MealPlanRepository
import com.svantheemsche.appetijt.domain.usecase.AddSharedRecipeUseCase
import com.svantheemsche.appetijt.domain.usecase.GetRecipesForDateUseCase
import com.svantheemsche.appetijt.domain.model.WorkResult
import com.svantheemsche.appetijt.domain.model.ErrorCodes
import com.svantheemsche.appetijt.ui.test.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate
import com.svantheemsche.appetijt.domain.usecase.AddLibraryRecipeToDateUseCase
import com.svantheemsche.appetijt.domain.usecase.GetLibraryRecipesUseCase
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue

@RunWith(RobolectricTestRunner::class)
class MainViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockRepository: MealPlanRepository = mockk()
    private val mockGetRecipesUseCase: GetRecipesForDateUseCase = mockk()
    private val mockAddSharedUseCase: AddSharedRecipeUseCase = mockk()
    private val mockGetLibraryUseCase: GetLibraryRecipesUseCase = mockk()
    private val mockAddLibraryUseCase: AddLibraryRecipeToDateUseCase = mockk()
    
    private lateinit var viewModel: MainViewModel

    @Before
    fun setup() {
        every { mockGetRecipesUseCase(any()) } returns flowOf(emptyList())
        every { mockGetLibraryUseCase(any()) } returns flowOf(emptyList())

        viewModel = MainViewModel(
            mealPlanRepository = mockRepository,
            getRecipesForDateUseCase = mockGetRecipesUseCase,
            addSharedRecipeUseCase = mockAddSharedUseCase,
            getLibraryRecipesUseCase = mockGetLibraryUseCase,
            addLibraryRecipeToDateUseCase = mockAddLibraryUseCase
        )
    }

    @Test
    fun init_loadsAppetijtState() = runTest {
        verify(exactly = 1) { mockGetRecipesUseCase(match { it == LocalDate.now() }) }
    }

    @Test
    fun selectDay_updatesStateAndLoadsRecipes() = runTest {
        val testDate = LocalDate.now().plusDays(2)
        val recipes = listOf(Recipe(id = 1, url = "u", title = "T", imageUrl = null, sourceApp = "S"))
        every { mockGetRecipesUseCase(testDate) } returns flowOf(recipes)

        viewModel.selectDay(testDate)
        advanceUntilIdle()

        assertEquals(testDate, viewModel.state.value.selectedDate)
        assertEquals(recipes, viewModel.state.value.recipesForSelectedDay)
        assertNull(viewModel.state.value.errorMessage)
    }

    @Test
    fun shiftWeeks_updatesWeekDates() = runTest {
        val initialWeekDates = viewModel.state.value.weekDates
        
        viewModel.shiftWeeks(1)
        var newWeekDates = viewModel.state.value.weekDates
        assertEquals(7, newWeekDates.size)
        assertTrue(newWeekDates[0] == initialWeekDates[0].plusWeeks(1))

        viewModel.shiftWeeks(-2)
        newWeekDates = viewModel.state.value.weekDates
        assertTrue(newWeekDates[0] == initialWeekDates[0].minusWeeks(1))
    }

    @Test
    fun saveSharedRecipe_showsLoadingThenRefreshesOnSuccess() = runTest {
        val testDate = LocalDate.now()
        coEvery { mockRepository.addRecipeFromSharedText(any(), any(), any()) } returns WorkResult.Success(Unit)

        viewModel.saveSharedRecipe("https://e.com", "Title", testDate)
        advanceUntilIdle()
        
        coVerify { mockRepository.addRecipeFromSharedText("https://e.com", "Title", testDate) }
        verify { mockGetRecipesUseCase(testDate) }
    }

    @Test
    fun saveSharedRecipe_setsErrorMessageOnFailure() = runTest {
        val testDate = LocalDate.now()
        coEvery { mockRepository.addRecipeFromSharedText(any(), any(), any()) } returns WorkResult.Failure(ErrorCodes.NETWORK_ERROR, "Failed")

        viewModel.saveSharedRecipe("https://e.com", testDate)
        advanceUntilIdle()

        assertEquals("Failed", viewModel.state.value.errorMessage)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun deleteRecipe_callsRepositoryAndRefreshes() = runTest {
        val date = viewModel.state.value.selectedDate
        coEvery { mockRepository.deleteMealEntry(date, 10L) } returns WorkResult.Success(Unit)

        viewModel.deleteRecipe(10L, date)
        advanceUntilIdle()

        coVerify { mockRepository.deleteMealEntry(date, 10L) }
        verify { mockGetRecipesUseCase(date) }
    }

    @Test
    fun moveRecipe_callsRepositoryAndRefreshesSourceDate() = runTest {
        val from = LocalDate.now()
        val to = from.plusDays(1)
        coEvery { mockRepository.updateRecipeDate(1L, from, to) } returns WorkResult.Success(Unit)

        viewModel.moveRecipe(1L, from, to)
        advanceUntilIdle()

        coVerify { mockRepository.updateRecipeDate(1L, from, to) }
        verify { mockGetRecipesUseCase(from) }
    }

    @Test
    fun searchLibrary_updatesStateAndReloadsLibrary() = runTest {
        val query = "Pasta"
        val recipes = listOf(Recipe(id = 5, url = "u", title = "Pasta", imageUrl = null, sourceApp = "S"))
        every { mockGetLibraryUseCase(query) } returns flowOf(recipes)

        viewModel.searchLibrary(query)
        advanceUntilIdle()

        assertEquals(query, viewModel.state.value.librarySearchQuery)
        assertEquals(recipes, viewModel.state.value.libraryRecipes)
    }

    @Test
    fun addLibraryRecipeToDate_callsUseCaseAndRefreshesIfSelected() = runTest {
        val recipe = Recipe(id = 5, url = "u", title = "P", imageUrl = null, sourceApp = "S")
        val selectedDate = viewModel.state.value.selectedDate
        coEvery { mockAddLibraryUseCase(recipe, selectedDate) } returns WorkResult.Success(Unit)

        viewModel.addLibraryRecipeToDate(recipe, selectedDate)
        advanceUntilIdle()

        coVerify { mockAddLibraryUseCase(recipe, selectedDate) }
        verify { mockGetRecipesUseCase(selectedDate) }
    }

    @Test
    fun addLibraryRecipeToDate_setsErrorMessageOnFailure() = runTest {
        val recipe = Recipe(id = 5, url = "u", title = "P", imageUrl = null, sourceApp = "S")
        val date = LocalDate.now()
        coEvery { mockAddLibraryUseCase(recipe, date) } returns WorkResult.Failure(ErrorCodes.DATABASE_ERROR, "Fail")

        viewModel.addLibraryRecipeToDate(recipe, date)
        advanceUntilIdle()

        assertNotNull(viewModel.state.value.errorMessage)
        assertEquals("Fail", viewModel.state.value.errorMessage)
    }

    @Test
    fun getPlannedDatesFlow_emitsSetsOfDatesWithRecipes() = runTest {
        val dates = viewModel.state.value.weekDates
        every { mockRepository.hasRecipesForDate(any()) } returns flowOf(false)
        every { mockRepository.hasRecipesForDate(dates[0]) } returns flowOf(true)
        every { mockRepository.hasRecipesForDate(dates[2]) } returns flowOf(true)

        val plannedDates = viewModel.getPlannedDatesFlow().first()
        
        assertTrue(plannedDates.contains(dates[0]))
        assertTrue(plannedDates.contains(dates[2]))
        assertFalse(plannedDates.contains(dates[1]))
    }
}
