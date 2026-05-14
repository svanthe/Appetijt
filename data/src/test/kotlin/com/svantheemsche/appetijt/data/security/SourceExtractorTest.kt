package com.svantheemsche.appetijt.data.security

import org.junit.Assert.assertEquals
import org.junit.Test

class SourceExtractorTest {

    @Test
    fun `getCleanSourceName returns Onbekend for null or blank`() {
        assertEquals("Onbekend", SourceExtractor.getCleanSourceName(null))
        assertEquals("Onbekend", SourceExtractor.getCleanSourceName(""))
        assertEquals("Onbekend", SourceExtractor.getCleanSourceName("  "))
    }

    @Test
    fun `getCleanSourceName extracts known sources`() {
        assertEquals("Dagelijkse Kost", SourceExtractor.getCleanSourceName("https://dagelijksekost.vrt.be/recepten/pasta"))
        assertEquals("Colruyt", SourceExtractor.getCleanSourceName("https://www.colruyt.be/nl/recepten/lasagne"))
        assertEquals("Xtra", SourceExtractor.getCleanSourceName("https://mijnxtra.be/recept/123"))
        assertEquals("Albert Heijn", SourceExtractor.getCleanSourceName("https://www.ah.nl/allerhande/recept/R-R1195652"))
        assertEquals("Njam", SourceExtractor.getCleanSourceName("https://njam.tv/recepten/soep"))
    }

    @Test
    fun `getCleanSourceName handles unknown domains by capitalizing first part`() {
        assertEquals("Example", SourceExtractor.getCleanSourceName("https://example.com/some-recipe"))
        assertEquals("Myrecipes", SourceExtractor.getCleanSourceName("https://myrecipes.net/chicken"))
    }

    @Test
    fun `getCleanSourceName sanitizes special characters`() {
        assertEquals("Examplesite", SourceExtractor.getCleanSourceName("https://example-site.com/recipe"))
        assertEquals("Special", SourceExtractor.getCleanSourceName("https://special.site.com/recipe"))
    }

    @Test
    fun `getCleanSourceName limits length`() {
        val longUrl = "https://thisisaveryveryveryveryveryveryverylongdomainname.com/recipe"
        val result = SourceExtractor.getCleanSourceName(longUrl)
        assert(result.length <= 30)
    }

    @Test
    fun `getCleanSourceName returns Overig on malformed URL`() {
        assertEquals("Overig", SourceExtractor.getCleanSourceName("not a url"))
    }
}
