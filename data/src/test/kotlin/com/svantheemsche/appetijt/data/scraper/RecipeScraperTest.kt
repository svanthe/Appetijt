package com.svantheemsche.appetijt.data.scraper

import com.svantheemsche.appetijt.domain.model.ErrorCodes
import com.svantheemsche.appetijt.domain.model.WorkResult
import kotlinx.coroutines.runBlocking
import okhttp3.ConnectionSpec
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.jsoup.Jsoup
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class RecipeScraperTest {

    private val scraper = RecipeScraper(listOf(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT))
    private val testUrl = "https://example.com/recipe-1"
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setup() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
    }

    @After
    fun teardown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `scrape returns success when server returns 200 with valid html`() = runBlocking {
        val html = "<html><head><meta property=\"og:title\" content=\"Pasta\"></head><body></body></html>"
        mockWebServer.enqueue(MockResponse().setResponseCode(200).setBody(html))

        val url = mockWebServer.url("/test").toString()
        val result = scraper.scrape(url)

        assertTrue(result is WorkResult.Success)
        assertEquals("Pasta", (result as WorkResult.Success).data.title)
    }

    @Test
    fun `scrape recovers metadata from 404 page if html is present`() = runBlocking {
        val html = "<html><head><meta property=\"og:title\" content=\"Xtra Recipe\"></head><body></body></html>"
        mockWebServer.enqueue(MockResponse().setResponseCode(404).setBody(html))

        val url = mockWebServer.url("/fail").toString()
        val result = scraper.scrape(url)

        // It should recover because Xtra often sends metadata even on 404s
        assertTrue(result is WorkResult.Success)
        assertEquals("Xtra Recipe", (result as WorkResult.Success).data.title)
    }

    @Test
    fun `scrape returns failure for 403 error without recovery metadata`() = runBlocking {
        mockWebServer.enqueue(MockResponse().setResponseCode(403).setBody("Forbidden"))

        val url = mockWebServer.url("/blocked").toString()
        val result = scraper.scrape(url)

        assertTrue(result is WorkResult.Failure)
        assertEquals(ErrorCodes.NETWORK_ERROR, (result as WorkResult.Failure).code)
    }

    @Test
    fun `scrape handles network exceptions`() = runBlocking {
        mockWebServer.shutdown() // Force connection error

        val result = scraper.scrape("http://localhost:12345/fail")

        assertTrue(result is WorkResult.Failure)
        assertEquals(ErrorCodes.NETWORK_ERROR, (result as WorkResult.Failure).code)
    }

    @Test
    fun `scrape returns failure on unexpected exception`() = runBlocking {
        // This is tricky to force, but we can try a malformed URL that passes the http check
        val result = scraper.scrape("http://[::1]:999999/") // Invalid port

        assertTrue(result is WorkResult.Failure)
        // Should catch as UNKNOWN_ERROR or NETWORK_ERROR depending on how it fails
    }

    @Test
    fun `parseDocument handles missing image and title tags`() {
        val doc = Jsoup.parse("<html><body></body></html>")
        val result = scraper.parseDocument("https://test.com", doc)
        
        assertEquals("test.com", result.title)
        assertNull(result.imageUrl)
    }

    @Test
    fun `parseDocument recovers title from h1 if og title missing`() {
        val html = "<html><body><h1>H1 Title</h1></body></html>"
        val doc = Jsoup.parse(html)
        val result = scraper.parseDocument("https://test.com", doc)
        assertEquals("H1 Title", result.title)
    }

    @Test
    fun `parseDocument uses Colruyt fallback for error pages`() {
        val doc = Jsoup.parse("<html><title>Error 404</title></html>")
        val result = scraper.parseDocument("https://www.colruyt.be/recept", doc)
        assertEquals("Colruyt Recept", result.title)
    }

    @Test
    fun `parseDocument uses Xtra fallback for error pages`() {
        val doc = Jsoup.parse("<html><title>Page Not Found</title></html>")
        val result = scraper.parseDocument("https://mijnxtra.be/recept", doc)
        assertEquals("Xtra Recept", result.title)
    }

    @Test
    fun `scrape returns INVALID_URL for non-http urls`() = runBlocking {
        val result = scraper.scrape("ftp://files.com")
        assertTrue(result is WorkResult.Failure)
        assertEquals(ErrorCodes.INVALID_URL, (result as WorkResult.Failure).code)
    }
    
    // Existing parseDocument tests below...

    @Test
    fun `parseDocument returns title and image when OG tags present`() {
        val html = """
            <html>
            <head>
                <meta property="og:title" content="OG Title">
                <meta property="og:image" content="https://example.com/image.jpg">
                <title>HTML Title</title>
            </head>
            </html>
        """.trimIndent()
        val doc = Jsoup.parse(html)
        
        val result = scraper.parseDocument(testUrl, doc)
        
        assertEquals("OG Title", result.title)
        assertEquals("https://example.com/image.jpg", result.imageUrl)
    }

    @Test
    fun `parseDocument returns fallback title when og title missing`() {
        val html = """
            <html>
            <head>
                <title>HTML Fallback Title</title>
            </head>
            </html>
        """.trimIndent()
        val doc = Jsoup.parse(html)
        
        val result = scraper.parseDocument(testUrl, doc)
        
        assertEquals("HTML Fallback Title", result.title)
    }

    @Test
    fun `parseDocument returns host fallback when no title tags found`() {
        val html = "<html><body></body></html>"
        val doc = Jsoup.parse(html)
        
        val result = scraper.parseDocument(testUrl, doc)
        
        assertEquals("example.com", result.title)
    }

    @Test
    fun `parseDocument returns twitter image when og image missing`() {
        val html = """
            <html>
            <head>
                <meta name="twitter:image" content="https://example.com/twitter.jpg">
            </head>
            </html>
        """.trimIndent()
        val doc = Jsoup.parse(html)
        
        val result = scraper.parseDocument(testUrl, doc)
        
        assertEquals("https://example.com/twitter.jpg", result.imageUrl)
    }

    @Test
    fun `parseDocument returns null image when no image tags found`() {
        val html = "<html><head><title>Title</title></head></html>"
        val doc = Jsoup.parse(html)
        
        val result = scraper.parseDocument(testUrl, doc)
        
        assertNull(result.imageUrl)
    }

    @Test
    fun `parseDocument sanitizes title to prevent XSS`() {
        val html = """
            <html>
            <head>
                <meta property="og:title" content="Recept <script>alert('xss')</script> met Pasta">
            </head>
            </html>
        """.trimIndent()
        val doc = Jsoup.parse(html)
        
        val result = scraper.parseDocument(testUrl, doc)
        
        // Jsoup.parse().text() removes the <script> tag and its content.
        // We normalize whitespace to avoid failures on double spaces.
        val normalizedResult = result.title.replace(Regex("\\s+"), " ").trim()
        assertEquals("Recept met Pasta", normalizedResult)
    }

    @Test
    fun `parseDocument sanitizes sourceApp`() {
        val maliciousUrl = "https://example.com/<script>alert(1)</script>"
        val doc = Jsoup.parse("<html><body></body></html>")
        
        val result = scraper.parseDocument(maliciousUrl, doc)
        
        // Host should be extracted and sanitized
        assertTrue(!result.sourceApp!!.contains("<script>"))
    }
}
