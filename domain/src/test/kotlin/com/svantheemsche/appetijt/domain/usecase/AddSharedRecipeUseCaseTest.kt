package com.svantheemsche.appetijt.domain.usecase

import com.svantheemsche.appetijt.domain.model.WorkResult
import com.svantheemsche.appetijt.domain.repository.MealPlanRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class AddSharedRecipeUseCaseTest {

    private val repository: MealPlanRepository = mockk()
    private val useCase = AddSharedRecipeUseCase(repository)

    @Test
    fun `invoke should call repository with correct parameters`() = runTest {
        // Given
        val text = "Check this recipe: https://example.com"
        val date = LocalDate.now()
        val expectedResult = WorkResult.Success(Unit)
        
        coEvery { repository.addRecipeFromSharedText(text, null, date) } returns expectedResult

        // When
        val result = useCase(text, date = date)

        // Then
        coVerify(exactly = 1) { repository.addRecipeFromSharedText(text, null, date) }
        assertEquals(expectedResult, result)
    }

    @Test
    fun `invoke with title should call repository with correct parameters`() = runTest {
        // Given
        val text = "Check this recipe: https://example.com"
        val title = "My Recipe"
        val date = LocalDate.now()
        val expectedResult = WorkResult.Success(Unit)
        
        coEvery { repository.addRecipeFromSharedText(text, title, date) } returns expectedResult

        // When
        val result = useCase(text, title, date)

        // Then
        coVerify(exactly = 1) { repository.addRecipeFromSharedText(text, title, date) }
        assertEquals(expectedResult, result)
    }
}
