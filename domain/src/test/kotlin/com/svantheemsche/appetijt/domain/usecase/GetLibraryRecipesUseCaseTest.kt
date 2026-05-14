package com.svantheemsche.appetijt.domain.usecase

import com.svantheemsche.appetijt.domain.model.Recipe
import com.svantheemsche.appetijt.domain.repository.MealPlanRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Test

class GetLibraryRecipesUseCaseTest {

    private val repository: MealPlanRepository = mockk()
    private val useCase = GetLibraryRecipesUseCase(repository)

    @Test
    fun `invoke with null query calls getAllUniqueRecipes`() {
        // Given
        every { repository.getAllUniqueRecipes() } returns flowOf(emptyList())

        // When
        useCase(null)

        // Then
        verify(exactly = 1) { repository.getAllUniqueRecipes() }
        verify(exactly = 0) { repository.searchInLibrary(any()) }
    }

    @Test
    fun `invoke with blank query calls getAllUniqueRecipes`() {
        // Given
        every { repository.getAllUniqueRecipes() } returns flowOf(emptyList())

        // When
        useCase("  ")

        // Then
        verify(exactly = 1) { repository.getAllUniqueRecipes() }
    }

    @Test
    fun `invoke with query calls searchInLibrary`() {
        // Given
        val query = "Pasta"
        every { repository.searchInLibrary(query) } returns flowOf(emptyList())

        // When
        useCase(query)

        // Then
        verify(exactly = 1) { repository.searchInLibrary(query) }
        verify(exactly = 0) { repository.getAllUniqueRecipes() }
    }
}
