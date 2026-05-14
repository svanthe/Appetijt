package com.svantheemsche.appetijt.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.svantheemsche.appetijt.data.local.AppetijtDatabase
import com.svantheemsche.appetijt.data.local.entity.RecipeEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RecipeDaoTest {

    private lateinit var database: AppetijtDatabase
    private lateinit var recipeDao: RecipeDao

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppetijtDatabase::class.java
        ).allowMainThreadQueries().build()
        recipeDao = database.recipeDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun recipeDao_insertAndGetById_returnsPersistedEntity() = runBlocking {
        // Given
        val recipe = RecipeEntity(
            id = 1L,
            url = "https://example.com/recipe",
            title = "Test Recipe",
            imageUrl = "https://example.com/image.jpg",
            sourceApp = "TestApp"
        )

        // When
        recipeDao.insertRecipe(recipe)
        val loaded = recipeDao.getRecipeById(1L)

        // Then
        assertEquals(recipe, loaded)
    }

    @Test
    fun recipeDao_deleteById_removesRecipe() = runBlocking {
        // Given
        val recipe = RecipeEntity(
            id = 1L,
            url = "https://example.com/recipe",
            title = "Test Recipe",
            imageUrl = null,
            sourceApp = null
        )
        recipeDao.insertRecipe(recipe)

        // When
        recipeDao.deleteById(1L)
        val loaded = recipeDao.getRecipeById(1L)

        // Then
        assertNull(loaded)
    }
}
