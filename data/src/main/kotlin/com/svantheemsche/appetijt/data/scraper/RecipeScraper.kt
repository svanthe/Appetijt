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

package com.svantheemsche.appetijt.data.scraper

import android.util.Log
import com.svantheemsche.appetijt.data.security.SourceExtractor
import com.svantheemsche.appetijt.data.utils.HtmlSanitizer
import com.svantheemsche.appetijt.domain.model.ErrorCodes
import com.svantheemsche.appetijt.domain.model.Recipe
import com.svantheemsche.appetijt.domain.model.WorkResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.TlsVersion
import okhttp3.logging.HttpLoggingInterceptor
import org.jsoup.Jsoup
import java.io.IOException
import java.net.URL
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class RecipeScraper {

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("RecipeScraperNetwork", message)
    }.apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    // Enforce modern TLS versions for security (HIGH Finding Mitigation)
    private val connectionSpec = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
        .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_3)
        .build()

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectionSpecs(listOf(connectionSpec, ConnectionSpec.CLEARTEXT))
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    // High-quality Android Mobile User Agent
    private val userAgent = "Mozilla/5.0 (Linux; Android 13; SM-S901B) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/112.0.0.0 Mobile Safari/537.36"

    suspend fun scrape(url: String): WorkResult<Recipe> = withContext(Dispatchers.IO) {
        Log.d("RecipeScraper", "Starting scrape for URL: $url")
        try {
            if (!url.startsWith("http")) {
                Log.e("RecipeScraper", "Invalid URL format: $url")
                return@withContext WorkResult.Failure(
                    code = ErrorCodes.INVALID_URL,
                    message = "Invalid link format. Please provide a valid website URL."
                )
            }

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", userAgent)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Accept-Language", "nl-BE,nl;q=0.9,en-US;q=0.8,en;q=0.7")
                .header("Referer", "https://www.google.com/")
                .header("Cache-Control", "no-cache")
                .header("Pragma", "no-cache")
                .build()

            val response = executeRequest(request)
            
            response.use { resp ->
                Log.d("RecipeScraper", "Response received: ${resp.code} ${resp.message}")
                
                val html = resp.body?.string() ?: ""
                Log.d("RecipeScraper", "HTML received, length: ${html.length}")
                if (html.length > 500) {
                    Log.d("RecipeScraper", "Snippet: ${html.substring(0, 500)}")
                }

                if (resp.isSuccessful) {
                    val doc = Jsoup.parse(html, url)
                    val recipe = parseDocument(url, doc)
                    return@withContext WorkResult.Success(recipe)
                } else {
                    // Even if unsuccessful (404/403), try to parse the HTML for Meta tags
                    // as some sites (like XTRA) return metadata in the body of an error page
                    if (html.isNotBlank()) {
                        val doc = Jsoup.parse(html, url)
                        val recipe = parseDocument(url, doc)
                        
                        // If we actually found a real title (not generic error text), treat as success
                        val isGenericErrorTitle = listOf("404", "not found", "error", "forbidden", "access denied")
                            .any { recipe.title.lowercase().contains(it) }

                        if (!isGenericErrorTitle && recipe.title != URL(url).host) {
                            Log.d("RecipeScraper", "Recovered metadata from error page: ${recipe.title}")
                            return@withContext WorkResult.Success(recipe)
                        }
                    }

                    val msg = when (resp.code) {
                        403 -> "Access denied (403). This website blocks automated access."
                        404 -> "Recipe not found (404) at: $url"
                        else -> "The website returned an error (${resp.code}) for: $url"
                    }
                    Log.e("RecipeScraper", "Request failed: $msg")
                    return@withContext WorkResult.Failure(
                        code = ErrorCodes.NETWORK_ERROR,
                        message = msg
                    )
                }
            }
        } catch (e: UnknownHostException) {
            Log.e("RecipeScraper", "Host not found: ${e.message}")
            WorkResult.Failure(
                code = ErrorCodes.NETWORK_ERROR,
                message = "Could not find the website. Please check your internet connection.",
                cause = e
            )
        } catch (e: java.net.SocketTimeoutException) {
            Log.e("RecipeScraper", "Timeout: ${e.message}")
            WorkResult.Failure(
                code = ErrorCodes.NETWORK_ERROR,
                message = "The website took too long to respond. It might be down or very slow.",
                cause = e
            )
        } catch (e: IOException) {
            Log.e("RecipeScraper", "Network error: ${e.message}")
            WorkResult.Failure(
                code = ErrorCodes.NETWORK_ERROR,
                message = "Network error while connecting to the website.",
                cause = e
            )
        } catch (e: Exception) {
            Log.e("RecipeScraper", "Unexpected error: ${e.message}", e)
            WorkResult.Failure(
                code = ErrorCodes.UNKNOWN_ERROR,
                message = "Connection error: ${e.localizedMessage ?: "Check the link and try again."}",
                cause = e
            )
        }
    }

    private suspend fun executeRequest(request: Request): Response = suspendCancellableCoroutine { continuation ->
        val call = client.newCall(request)
        continuation.invokeOnCancellation {
            call.cancel()
        }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (!continuation.isCancelled) {
                    continuation.resumeWith(Result.failure(e))
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (!continuation.isCancelled) {
                    continuation.resume(response)
                } else {
                    response.close()
                }
            }
        })
    }

    internal fun parseDocument(url: String, doc: org.jsoup.nodes.Document): Recipe {
        val sourceApp = SourceExtractor.getCleanSourceName(url)
        
        // 1. Title extraction order: og:title -> h1 -> <title> -> host/domain
        var title = doc.select("meta[property=og:title]").attr("content").ifBlank {
            doc.select("h1").first()?.text()?.ifBlank {
                doc.title().ifBlank {
                    URL(url).host
                }
            } ?: doc.title().ifBlank { URL(url).host }
        }

        // Clean up common "404" or "Not Found" generic titles
        val errorKeywords = listOf("404", "not found", "niet beschikbaar", "niet gevonden", "onbeschikbaar", "error")
        if (errorKeywords.any { title.lowercase().contains(it) }) {
            title = if (sourceApp == "Xtra") "Xtra Recept" else if (sourceApp == "Colruyt") "Colruyt Recept" else URL(url).host
        }

        // 2. Image extraction order: og:image -> twitter:image -> null
        val imageUrl = doc.select("meta[property=og:image]").attr("content").ifBlank {
            doc.select("meta[name=twitter:image]").attr("content").ifBlank {
                null
            }
        }

        return Recipe(
            url = url,
            title = HtmlSanitizer.sanitize(title),
            imageUrl = imageUrl,
            sourceApp = sourceApp
        )
    }
}
