package com.svantheemsche.appetijt.data.share

import com.svantheemsche.appetijt.domain.model.WorkResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SharedTextUrlExtractorTest {

    private val extractor = SharedTextUrlExtractor()

    @Test
    fun `extractUrl handles plain URL`() {
        val text = "https://www.example.com/recipe"
        val result = extractor.extractUrl(text)
        
        assertTrue(result is WorkResult.Success)
        assertEquals("https://www.example.com/recipe", (result as WorkResult.Success).data)
    }

    @Test
    fun `extractUrl handles XTRA app share format`() {
        val text = "Stefan wil een recpet met je delen. Open deze link om het recept te bekijken https://www.xtra.be/recept/123"
        val result = extractor.extractUrl(text)
        
        assertTrue(result is WorkResult.Success)
        assertEquals("https://www.xtra.be/recept/123", (result as WorkResult.Success).data)
    }
    
    @Test
    fun `extractUrl cleans trailing punctuation`() {
        val text = "Check this out: https://www.example.com."
        val result = extractor.extractUrl(text)
        
        assertTrue(result is WorkResult.Success)
        assertEquals("https://www.example.com", (result as WorkResult.Success).data)
    }

    @Test
    fun `extractUrl handles null or blank input`() {
        assertTrue(extractor.extractUrl(null) is WorkResult.Failure)
        assertTrue(extractor.extractUrl("") is WorkResult.Failure)
        assertTrue(extractor.extractUrl("   ") is WorkResult.Failure)
    }

    @Test
    fun `extractUrl ensures protocol is present`() {
        val result = extractor.extractUrl("example.com/recipe")
        assertEquals("https://example.com/recipe", (result as WorkResult.Success).data)
    }

    @Test
    fun `extractUrl handles no URL found`() {
        val result = extractor.extractUrl("just some text")
        assertTrue(result is WorkResult.Failure)
        assertEquals("No valid URL found in shared text", (result as WorkResult.Failure).message)
    }

    @Test
    fun `extractTitleGuess cleans XTRA intro sentences`() {
        val text = "Stefan wil een recept met je delen. Open deze link om het recept te bekijken: Lasagne - https://xtra.be/1"
        val url = "https://xtra.be/1"
        val guess = extractor.extractTitleGuess(text, url)
        assertEquals("Lasagne", guess)
    }

    @Test
    fun `extractTitleGuess returns null for very short or blank remnants`() {
        val text = "https://e.com"
        val guess = extractor.extractTitleGuess(text, "https://e.com")
        assertNull(guess)
    }
}
