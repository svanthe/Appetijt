package com.svantheemsche.appetijt.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.svantheemsche.appetijt.data.local.AppetijtDatabase
import com.svantheemsche.appetijt.data.local.entity.MealEntryEntity
import com.svantheemsche.appetijt.data.local.entity.RecipeEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class MealEntryDaoTest {

    private lateinit var database: AppetijtDatabase
    private lateinit var recipeDao: RecipeDao
    private lateinit var mealEntryDao: MealEntryDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppetijtDatabase::class.java
        ).allowMainThreadQueries().build()
        recipeDao = database.recipeDao()
        mealEntryDao = database.mealEntryDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun mealEntryDao_getByDate_returnsMultipleEntriesForSameDay() = runBlocking {
        // Given
        val date = LocalDate.now()
        val recipe1 = RecipeEntity(1L, "url1", "Title 1", null, null)
        val recipe2 = RecipeEntity(2L, "url2", "Title 2", null, null)
        recipeDao.insertRecipe(recipe1)
        recipeDao.insertRecipe(recipe2)

        val entry1 = MealEntryEntity(date = date, recipeId = 1L)
        val entry2 = MealEntryEntity(date = date, recipeId = 2L)

        // When
        mealEntryDao.insertMealEntry(entry1)
        mealEntryDao.insertMealEntry(entry2)

        // Then
        mealEntryDao.getEntriesForDate(date).test {
            val entries = awaitItem()
            assertEquals(2, entries.size)
            assertTrue(entries.any { it.recipeId == 1L })
            assertTrue(entries.any { it.recipeId == 2L })
        }
    }

    @Test
    fun mealEntryDao_getRecipesForDate_returnsExpectedList() = runBlocking {
        // Given
        val date = LocalDate.now()
        val recipe1 = RecipeEntity(1L, "url1", "Title 1", null, null)
        val recipe2 = RecipeEntity(2L, "url2", "Title 2", null, null)
        recipeDao.insertRecipe(recipe1)
        recipeDao.insertRecipe(recipe2)

        mealEntryDao.insertMealEntry(MealEntryEntity(date = date, recipeId = 1L))
        mealEntryDao.insertMealEntry(MealEntryEntity(date = date, recipeId = 2L))

        // When & Then
        mealEntryDao.getRecipesForDate(date).test {
            val recipes = awaitItem()
            assertEquals(2, recipes.size)
            assertTrue(recipes.any { it.title == "Title 1" })
            assertTrue(recipes.any { it.title == "Title 2" })
        }
    }

    @Test
    fun mealEntryDao_insertDuplicate_replacesEntry() = runBlocking {
        // Given
        val date = LocalDate.now()
        val recipe = RecipeEntity(1L, "url", "Title", null, null)
        recipeDao.insertRecipe(recipe)

        val entry1 = MealEntryEntity(date = date, recipeId = 1L)
        val entry2 = MealEntryEntity(date = date, recipeId = 1L)

        // When
        mealEntryDao.insertMealEntry(entry1)
        mealEntryDao.insertMealEntry(entry2) // Should succeed due to OnConflictStrategy.REPLACE

        // Then
        mealEntryDao.getEntriesForDate(date).test {
            val entries = awaitItem()
            assertEquals(1, entries.size)
            assertEquals(1L, entries.first().recipeId)
        }
    }

    @Test
    fun deleteRecipe_updatesMealPlanConsistency_viaCascade() = runBlocking {
        // Given
        val date = LocalDate.now()
        val recipe = RecipeEntity(1L, "url", "Title", null, null)
        recipeDao.insertRecipe(recipe)
        mealEntryDao.insertMealEntry(MealEntryEntity(date = date, recipeId = 1L))

        // When
        recipeDao.deleteById(1L)

        // Then
        mealEntryDao.getEntriesForDate(date).test {
            val entries = awaitItem()
            assertTrue(entries.isEmpty())
        }
    }

    @Test
    fun deleteMealEntry_removesSpecificEntry() = runBlocking {
        val date = LocalDate.now()
        recipeDao.insertRecipe(RecipeEntity(1L, "u", "T", null, null))
        mealEntryDao.insertMealEntry(MealEntryEntity(date = date, recipeId = 1L))

        mealEntryDao.deleteMealEntry(date, 1L)

        mealEntryDao.getEntriesForDate(date).test {
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun updateMealEntryDate_movesEntryToNewDate() = runBlocking {
        val date1 = LocalDate.of(2024, 1, 1)
        val date2 = LocalDate.of(2024, 1, 2)
        recipeDao.insertRecipe(RecipeEntity(1L, "u", "T", null, null))
        mealEntryDao.insertMealEntry(MealEntryEntity(date = date1, recipeId = 1L))

        val rows = mealEntryDao.updateMealEntryDate(1L, date1, date2)

        assertEquals(1, rows)
        mealEntryDao.getEntriesForDate(date1).test { assertTrue(awaitItem().isEmpty()) }
        mealEntryDao.getEntriesForDate(date2).test { assertEquals(1, awaitItem().size) }
    }
}
