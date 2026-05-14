package com.svantheemsche.appetijt.data.repository

import com.svantheemsche.appetijt.data.local.dao.MealEntryDao
import com.svantheemsche.appetijt.data.local.dao.RecipeDao
import com.svantheemsche.appetijt.data.scraper.RecipeScraper
import com.svantheemsche.appetijt.data.share.SharedTextUrlExtractor
import com.svantheemsche.appetijt.domain.model.ErrorCodes
import com.svantheemsche.appetijt.domain.model.Recipe
import com.svantheemsche.appetijt.domain.model.WorkResult
import com.svantheemsche.appetijt.data.local.entity.MealEntryEntity
import com.svantheemsche.appetijt.data.local.entity.RecipeEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class MealPlanRepositoryImplTest {

    private val recipeDao = mockk<RecipeDao>()
    private val mealEntryDao = mockk<MealEntryDao>()
    private val scraper = mockk<RecipeScraper>()
    private val extractor = mockk<SharedTextUrlExtractor>()

    private val repository = MealPlanRepositoryImpl(
        recipeDao, mealEntryDao, scraper, extractor
    )

    @Test
    fun `addRecipeFromSharedText returns success when everything succeeds`() = runBlocking {
        // Given
        val text = "Check this: https://example.com"
        val url = "https://example.com"
        val date = LocalDate.now()
        val metadata = Recipe(url = url, title = "Title", imageUrl = "image", sourceApp = "App")

        every { extractor.extractUrl(text) } returns WorkResult.Success(url)
        every { extractor.extractTitleGuess(any(), any()) } returns null
        coEvery { scraper.scrape(url) } returns WorkResult.Success(metadata)
        coEvery { recipeDao.insertRecipe(any()) } returns 1L
        coEvery { mealEntryDao.insertMealEntry(any()) } returns Unit

        // When
        val result = repository.addRecipeFromSharedText(text, date)

        // Then
        assertTrue(result is WorkResult.Success)
    }

    @Test
    fun `addRecipeFromSharedText returns INVALID_URL when extraction fails`() = runBlocking {
        // Given
        val text = "no url"
        every { extractor.extractUrl(text) } returns WorkResult.Failure(ErrorCodes.INVALID_URL, "Error")

        // When
        val result = repository.addRecipeFromSharedText(text, LocalDate.now())

        // Then
        assertTrue(result is WorkResult.Failure)
        assertEquals(ErrorCodes.INVALID_URL, (result as WorkResult.Failure).code)
    }

    @Test
    fun `addRecipeFromSharedText uses fallback when scraping fails`() = runBlocking {
        // Given
        val text = "https://example.com"
        val url = "https://example.com"
        val date = LocalDate.now()
        
        every { extractor.extractUrl(text) } returns WorkResult.Success(url)
        every { extractor.extractTitleGuess(any(), any()) } returns "Guessed Title"
        coEvery { scraper.scrape(url) } returns WorkResult.Failure(ErrorCodes.NETWORK_ERROR, "Timeout")
        coEvery { recipeDao.insertRecipe(any()) } returns 1L
        coEvery { mealEntryDao.insertMealEntry(any()) } returns Unit

        // When
        val result = repository.addRecipeFromSharedText(text, date)

        // Then
        assertTrue(result is WorkResult.Success)
        // Verify that it was saved anyway
    }

    @Test
    fun `addRecipeFromSharedText returns DATABASE_ERROR when saving fails`() = runBlocking {
        // Given
        val text = "https://example.com"
        val url = "https://example.com"
        val date = LocalDate.now()
        val metadata = Recipe(url = url, title = "Title", imageUrl = "image", sourceApp = "App")

        every { extractor.extractUrl(text) } returns WorkResult.Success(url)
        every { extractor.extractTitleGuess(any(), any()) } returns null
        coEvery { scraper.scrape(url) } returns WorkResult.Success(metadata)
        coEvery { recipeDao.insertRecipe(any()) } throws Exception("DB Error")

        // When
        val result = repository.addRecipeFromSharedText(text, date)

        // Then
        assertTrue(result is WorkResult.Failure)
        assertEquals(ErrorCodes.DATABASE_ERROR, (result as WorkResult.Failure).code)
    }

    @Test
    fun `getRecipesForDate returns mapped domain recipes`() = runBlocking {
        // Given
        val date = LocalDate.now()
        val entities = listOf(
            RecipeEntity(id = 1L, url = "u1", title = "T1", imageUrl = "i1", sourceApp = "S1")
        )
        every { mealEntryDao.getRecipesForDate(date) } returns flowOf(entities)

        // When
        val result = repository.getRecipesForDate(date).first()

        // Then
        assertEquals(1, result.size)
        assertEquals("T1", result[0].title)
    }

    @Test
    fun `deleteMealEntry returns success when dao succeeds`() = runBlocking {
        // Given
        val date = LocalDate.now()
        coEvery { mealEntryDao.deleteMealEntry(date, 1L) } returns Unit

        // When
        val result = repository.deleteMealEntry(date, 1L)

        // Then
        assertTrue(result is WorkResult.Success)
    }

    @Test
    fun `deleteMealEntry returns failure when dao throws`() = runBlocking {
        // Given
        coEvery { mealEntryDao.deleteMealEntry(any(), any()) } throws Exception("Fail")

        // When
        val result = repository.deleteMealEntry(LocalDate.now(), 1L)

        // Then
        assertTrue(result is WorkResult.Failure)
    }

    @Test
    fun `updateRecipeDate returns success when rows updated`() = runBlocking {
        // Given
        val from = LocalDate.now()
        val to = from.plusDays(1)
        coEvery { mealEntryDao.updateMealEntryDate(1L, from, to) } returns 1

        // When
        val result = repository.updateRecipeDate(1L, from, to)

        // Then
        assertTrue(result is WorkResult.Success)
    }

    @Test
    fun `updateRecipeDate returns failure when no rows updated`() = runBlocking {
        // Given
        coEvery { mealEntryDao.updateMealEntryDate(any(), any(), any()) } returns 0

        // When
        val result = repository.updateRecipeDate(1L, LocalDate.now(), LocalDate.now())

        // Then
        assertTrue(result is WorkResult.Failure)
    }

    @Test
    fun `hasRecipesForDate returns dao flow`() = runBlocking {
        // Given
        every { mealEntryDao.hasEntriesForDate(any()) } returns flowOf(true)

        // When
        val result = repository.hasRecipesForDate(LocalDate.now()).first()

        // Then
        assertTrue(result)
    }

    @Test
    fun `getAllUniqueRecipes returns unique recipes`() = runBlocking {
        // Given
        val entities = listOf(RecipeEntity(id = 1, url = "u", title = "T", imageUrl = null, sourceApp = "S"))
        every { recipeDao.getAllUniqueRecipes() } returns flowOf(entities)

        // When
        val result = repository.getAllUniqueRecipes().first()

        // Then
        assertEquals(1, result.size)
        assertEquals("T", result[0].title)
    }

    @Test
    fun `searchInLibrary returns filtered recipes`() = runBlocking {
        // Given
        val query = "Pasta"
        val entities = listOf(RecipeEntity(id = 1, url = "u", title = "Pasta", imageUrl = null, sourceApp = "S"))
        every { recipeDao.searchInLibrary("%$query%") } returns flowOf(entities)

        // When
        val result = repository.searchInLibrary(query).first()

        // Then
        assertEquals(1, result.size)
        assertEquals("Pasta", result[0].title)
    }

    @Test
    fun `addRecipeToDate clones recipe and saves entry`() = runBlocking {
        // Given
        val recipe = Recipe(url = "u", title = "T", imageUrl = null, sourceApp = "S")
        val date = LocalDate.now()
        coEvery { recipeDao.insertRecipe(any()) } returns 10L
        coEvery { mealEntryDao.insertMealEntry(any()) } returns Unit

        // When
        val result = repository.addRecipeToDate(recipe, date)

        // Then
        assertTrue(result is WorkResult.Success)
        coVerify { recipeDao.insertRecipe(match { it.title == "T" }) }
        coVerify { mealEntryDao.insertMealEntry(match { it.recipeId == 10L && it.date == date }) }
    }
}
