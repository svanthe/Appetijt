package com.svantheemsche.appetijt.data.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class HtmlSanitizerTest {

    @Test
    fun `sanitize removes script tags`() {
        val input = "Hello <script>alert('XSS')</script>World"
        val expected = "Hello World"
        val actual = HtmlSanitizer.sanitize(input)
        assertEquals(expected, actual)
    }

    @Test
    fun `sanitize removes html tags but keeps text`() {
        val input = "<b>Bold</b> <i>Italic</i>"
        val expected = "Bold Italic"
        val actual = HtmlSanitizer.sanitize(input)
        assertEquals(expected, actual)
    }

    @Test
    fun `sanitize handles empty input`() {
        assertEquals("", HtmlSanitizer.sanitize(""))
        assertEquals("", HtmlSanitizer.sanitize("   "))
    }

    @Test
    fun `sanitize decodes html entities`() {
        val input = "Tom &amp; Jerry"
        // Jsoup.parse().text() decodes entities into plain text
        val expected = "Tom & Jerry"
        val actual = HtmlSanitizer.sanitize(input)
        assertEquals(expected, actual)
    }
}
