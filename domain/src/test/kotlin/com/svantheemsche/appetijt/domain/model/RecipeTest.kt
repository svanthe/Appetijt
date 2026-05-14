package com.svantheemsche.appetijt.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class RecipeTest {

    @Test
    fun `Recipe properties are correctly initialized`() {
        val recipe = Recipe(
            id = 1L,
            url = "https://example.com",
            title = "Test Recipe",
            imageUrl = "https://example.com/img.jpg",
            sourceApp = "App"
        )

        assertEquals(1L, recipe.id)
        assertEquals("https://example.com", recipe.url)
        assertEquals("Test Recipe", recipe.title)
        assertEquals("https://example.com/img.jpg", recipe.imageUrl)
        assertEquals("App", recipe.sourceApp)
    }

    @Test
    fun `Recipe copy works correctly`() {
        val recipe = Recipe(id = 1L, url = "u", title = "T", imageUrl = null, sourceApp = "S")
        val copied = recipe.copy(title = "New T")
        
        assertEquals(1L, copied.id)
        assertEquals("New T", copied.title)
    }
}
