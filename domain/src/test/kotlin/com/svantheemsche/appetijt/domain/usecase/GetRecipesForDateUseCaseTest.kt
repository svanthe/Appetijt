package com.svantheemsche.appetijt.domain.usecase

import com.svantheemsche.appetijt.domain.model.Recipe
import com.svantheemsche.appetijt.domain.repository.MealPlanRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class GetRecipesForDateUseCaseTest {

    private val repository: MealPlanRepository = mockk()
    private val useCase = GetRecipesForDateUseCase(repository)

    @Test
    fun `invoke should return flow of recipes from repository`() {
        // Given
        val date = LocalDate.now()
        val recipes = listOf(
            Recipe(1L, "url1", "title1", null, "source1"),
            Recipe(2L, "url2", "title2", null, "source2")
        )
        val expectedFlow = flowOf(recipes)
        
        every { repository.getRecipesForDate(date) } returns expectedFlow

        // When
        val result = useCase(date)

        // Then
        verify(exactly = 1) { repository.getRecipesForDate(date) }
        assertEquals(expectedFlow, result)
    }
}
