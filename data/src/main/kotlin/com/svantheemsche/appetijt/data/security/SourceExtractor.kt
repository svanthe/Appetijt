package com.svantheemsche.appetijt.data.security

import java.net.URI
import java.util.Locale

object SourceExtractor {
    private const val MAX_SOURCE_LENGTH = 30

    /**
     * Securely parses a URL to extract a clean source name.
     * Implements OWASP sanitization to prevent injection and UI overflows.
     */
    fun getCleanSourceName(url: String?): String {
        if (url.isNullOrBlank()) {
            return "Onbekend"
        }

        return try {
            val uri = URI(url.trim())
            val host = uri.host ?: return "Overig"
            val cleanHost = if (host.startsWith("www.")) host.substring(4) else host

            var cleanSource = when {
                cleanHost.contains("dagelijksekost") -> "Dagelijkse Kost"
                cleanHost.contains("colruyt") -> "Colruyt"
                cleanHost.contains("mijnxtra") || cleanHost.contains("bioplanet") -> "Xtra"
                cleanHost.contains("ah.") || cleanHost.contains("albertheijn") -> "Albert Heijn"
                cleanHost.contains("delhaize") -> "Delhaize"
                cleanHost.contains("njam") -> "Njam!"
                else -> {
                    val parts = cleanHost.split(".")
                    if (parts.isNotEmpty()) {
                        val rawName = parts[0]
                        rawName.substring(0, 1).uppercase(Locale.ROOT) + rawName.substring(1)
                    } else {
                        "Overig"
                    }
                }
            }

            // OWASP Sanitization: Remove everything that is not a letter, digit, or space
            cleanSource = cleanSource.replace(Regex("[^a-zA-Z0-9 ]"), "")

            // Bound-check against buffer/UI overflows
            if (cleanSource.length > MAX_SOURCE_LENGTH) {
                cleanSource = cleanSource.substring(0, MAX_SOURCE_LENGTH)
            }

            if (cleanSource.trim().isEmpty()) "Overig" else cleanSource.trim()
        } catch (e: Exception) {
            // Fail-safe: return "Overig" without logging the raw URL to prevent log-forging
            "Overig"
        }
    }
}
