package com.svantheemsche.appetijt.domain.usecase

import com.svantheemsche.appetijt.domain.model.Recipe
import com.svantheemsche.appetijt.domain.model.WorkResult
import com.svantheemsche.appetijt.domain.repository.MealPlanRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.time.LocalDate

class AddLibraryRecipeToDateUseCaseTest {

    private val repository: MealPlanRepository = mockk()
    private val useCase = AddLibraryRecipeToDateUseCase(repository)

    @Test
    fun `invoke calls repository addRecipeToDate`() = runTest {
        // Given
        val recipe = Recipe(url = "https://e.com", title = "T", imageUrl = null, sourceApp = "S")
        val date = LocalDate.now()
        coEvery { repository.addRecipeToDate(recipe, date) } returns WorkResult.Success(Unit)

        // When
        useCase(recipe, date)

        // Then
        coVerify(exactly = 1) { repository.addRecipeToDate(recipe, date) }
    }
}
