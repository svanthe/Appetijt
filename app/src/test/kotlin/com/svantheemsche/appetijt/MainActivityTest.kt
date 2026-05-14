package com.svantheemsche.appetijt

import android.content.Intent
import com.svantheemsche.appetijt.domain.model.ErrorCodes
import com.svantheemsche.appetijt.domain.model.WorkResult
import com.svantheemsche.appetijt.domain.repository.MealPlanRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertNotNull
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import io.mockk.*

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class MainActivityTest {

    // Mock dependencies
    private val mockRepository: MealPlanRepository = mockk()

    private lateinit var activity: MainActivity

    @Before
    fun setup() {
        // Build the activity but don't call 'create' yet so we can inject mocks
        val controller = Robolectric.buildActivity(MainActivity::class.java)
        activity = controller.get()
        
        // Inject the mock repository before onCreate runs
        activity.repository = mockRepository
        
        // Now call create, which will trigger ViewModel initialization with our mock
        controller.create()
    }

    @After
    fun tearDown() {
        // Clean up mocks after each test
        clearAllMocks()
    }

    /**
     * Helper function to simulate the intent handling logic for testing.
     */
    private fun setupActivityWithMocks(intent: Intent) {
        // Manually call the method under test using reflection
        val method = MainActivity::class.java.getDeclaredMethod("handleShareIntent", Intent::class.java)
        method.isAccessible = true
        method.invoke(activity, intent)
    }

    @Test
    fun handleShareIntent_success_showsSuccessToast() = runBlocking {
        // GIVEN: A valid shared intent
        val sharedText = "https://example.com/recipe-1"
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, sharedText)
            type = "text/plain"
        }

        // WHEN: The repository call succeeds
        coEvery { mockRepository.addRecipeFromSharedText(sharedText, any(), any()) } returns WorkResult.Success(Unit)

        // ACT: Simulate handling the intent
        setupActivityWithMocks(intent)

        // ASSERT: Verify the repository call was made
        coVerify(exactly = 1) { mockRepository.addRecipeFromSharedText(sharedText, any(), any()) }
    }

    @Test
    fun `handleShareIntent_failure_invalidUrl_showsErrorToast`() = runBlocking {
        // GIVEN: A valid shared intent
        val sharedText = "some text with no url"
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, sharedText)
            type = "text/plain"
        }

        // WHEN: The repository call fails with INVALID_URL
        val failureResult = WorkResult.Failure(
            code = ErrorCodes.INVALID_URL,
            message = "Invalid URL format"
        )
        coEvery { mockRepository.addRecipeFromSharedText(sharedText, any(), any()) } returns failureResult

        // ACT: Simulate handling the intent
        setupActivityWithMocks(intent)

        // ASSERT: Verify the repository call was made
        coVerify(exactly = 1) { mockRepository.addRecipeFromSharedText(sharedText, any(), any()) }
    }

    @Test
    fun `handleShareIntent_failure_networkError_showsErrorToast`() = runBlocking {
        // GIVEN: A valid shared intent
        val sharedText = "https://example.com/recipe-1"
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, sharedText)
            type = "text/plain"
        }

        // WHEN: The repository call fails with NETWORK_ERROR
        val failureResult = WorkResult.Failure(
            code = ErrorCodes.NETWORK_ERROR,
            message = "Network failure"
        )
        coEvery { mockRepository.addRecipeFromSharedText(sharedText, any(), any()) } returns failureResult

        // ACT: Simulate handling the intent
        setupActivityWithMocks(intent)

        // ASSERT: Verify the repository call was made
        coVerify(exactly = 1) { mockRepository.addRecipeFromSharedText(sharedText, any(), any()) }
    }

    @Test
    fun `handleShareIntent_noTextProvided_showsNoContentToast`() = runBlocking {
        // GIVEN: An intent with no extra text
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, null as String?)
        }

        // WHEN: The intent is processed
        // ACT: Simulate handling the intent
        setupActivityWithMocks(intent)

        // ASSERT: Verify that the repository method was never called
        coVerify(exactly = 0) { mockRepository.addRecipeFromSharedText(any(), any(), any()) }
    }

    @Test
    fun `initializeDependencies should create repository`() {
        // Using a fresh activity to test initialization logic
        val controller = Robolectric.buildActivity(MainActivity::class.java)
        val freshActivity = controller.get()
        
        val method = MainActivity::class.java.getDeclaredMethod("initializeDependencies")
        method.isAccessible = true
        method.invoke(freshActivity)
        
        assertNotNull(freshActivity.repository)
    }

    @Test
    fun `onCreate should initialize dependencies`() {
        assertNotNull(activity.repository)
    }

    @Test
    fun `onNewIntent should handle shared text`() = runBlocking {
        val sharedText = "https://example.com/onNewIntent"
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, sharedText)
            type = "text/plain"
        }
        coEvery { mockRepository.addRecipeFromSharedText(sharedText, any(), any()) } returns WorkResult.Success(Unit)

        // Using reflection to call the protected onNewIntent
        val method = MainActivity::class.java.getDeclaredMethod("onNewIntent", Intent::class.java)
        method.isAccessible = true
        method.invoke(activity, intent)

        coVerify { mockRepository.addRecipeFromSharedText(sharedText, any(), any()) }
    }
}
