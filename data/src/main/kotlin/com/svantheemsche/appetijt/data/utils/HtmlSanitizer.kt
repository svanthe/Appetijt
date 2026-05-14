package com.svantheemsche.appetijt.data.utils

import org.jsoup.Jsoup

/**
 * Utility class for sanitizing HTML content scraped from external URLs.
 * This prevents XSS attacks by stripping dangerous tags and attributes.
 */
object HtmlSanitizer {
    /**
     * Cleans and sanitizes a given raw HTML/text string, removing all tags
     * and decoding HTML entities (e.g., &amp; becomes &).
     * @param rawHtml The input string potentially containing HTML.
     * @return The sanitized plain text string.
     */
    fun sanitize(rawHtml: String): String {
        if (rawHtml.isBlank()) return ""

        // Using Jsoup to extract plain text handles both tag removal and entity decoding
        return Jsoup.parse(rawHtml).text()
    }
}
