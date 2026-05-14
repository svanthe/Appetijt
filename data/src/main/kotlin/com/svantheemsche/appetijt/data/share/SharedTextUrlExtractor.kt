/*
 * Appetijt: A local-first meal planning Android application.
 * Copyright (C) 2026 Stefan Van Theemsche
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.svantheemsche.appetijt.data.share

import android.util.Patterns
import com.svantheemsche.appetijt.domain.model.ErrorCodes
import com.svantheemsche.appetijt.domain.model.WorkResult

class SharedTextUrlExtractor {

    fun extractUrl(text: String?): WorkResult<String> {
        if (text.isNullOrBlank()) {
            return WorkResult.Failure(
                code = ErrorCodes.INVALID_URL,
                message = "Shared text is empty"
            )
        }

        val matcher = Patterns.WEB_URL.matcher(text)
        return if (matcher.find()) {
            // Extract URL and remove ALL whitespace/newlines that might be hidden inside
            var url = matcher.group().replace("\\s".toRegex(), "")
            
            // Remove common trailing characters that aren't part of URLs but get caught by the regex
            val trailingChars = charArrayOf('.', ',', ')', ']', '!', '?', ';', '\'')
            while (url.isNotEmpty() && trailingChars.contains(url.last())) {
                url = url.substring(0, url.length - 1)
            }

            // Ensure protocol is present
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://$url"
            }
            WorkResult.Success(url)
        } else {
            WorkResult.Failure(
                code = ErrorCodes.INVALID_URL,
                message = "No valid URL found in shared text"
            )
        }
    }

    /**
     * Tries to extract a meaningful title from the shared text.
     * Often shared text looks like "Recipe Name - https://..."
     */
    fun extractTitleGuess(text: String, url: String): String? {
        // Remove the URL itself from the text
        var cleaned = text.replace(url, "").trim()
        
        // Remove common intro sentences from XTRA app
        val xtraIntros = listOf(
            "Stefan wil een recpet met je delen. Open deze link om het recept te bekijken",
            "Stefan wil een recept met je delen. Open deze link om het recept te bekijken"
        )
        for (intro in xtraIntros) {
            cleaned = cleaned.replace(intro, "").trim()
        }

        // Remove trailing/leading dashes, colons, or punctuation
        cleaned = cleaned.trim('-', ':', ' ', '|', '.', ',')
        
        return if (cleaned.isNotBlank() && cleaned.length > 3) cleaned else null
    }
}
