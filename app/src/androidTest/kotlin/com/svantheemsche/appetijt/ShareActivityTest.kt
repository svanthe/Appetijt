package com.svantheemsche.appetijt

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.svantheemsche.appetijt.domain.model.ErrorCodes
import com.svantheemsche.appetijt.domain.model.WorkResult
import com.svantheemsche.appetijt.domain.repository.MealPlanRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class ShareActivityTest {

    private lateinit var mockRepository: MealPlanRepository
    private val testIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "https://example.com/recipe-1")
    }

    @Before
    fun setup() {
        mockRepository = mockk<MealPlanRepository>()
    }

    @Test
    fun testShareIntent_Success() = runBlocking {
        coEvery { mockRepository.addRecipeFromSharedText(any(), any()) } returns WorkResult.Success(Unit)

        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            activity.repository = mockRepository
            activity.handleShareIntent(testIntent)
        }

        coVerify(exactly = 1) { mockRepository.addRecipeFromSharedText(any(), any()) }
    }

    @Test
    fun testShareIntent_Failure() = runBlocking {
        val failureResult = WorkResult.Failure(ErrorCodes.NETWORK_ERROR, "Network failure")
        coEvery { mockRepository.addRecipeFromSharedText(any(), any()) } returns failureResult

        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            activity.repository = mockRepository
            activity.handleShareIntent(testIntent)
        }

        coVerify(exactly = 1) { mockRepository.addRecipeFromSharedText(any(), any()) }
    }
}
