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

package com.svantheemsche.appetijt.data.repository

import com.svantheemsche.appetijt.data.local.dao.MealEntryDao
import com.svantheemsche.appetijt.data.local.dao.RecipeDao
import com.svantheemsche.appetijt.data.local.entity.MealEntryEntity
import com.svantheemsche.appetijt.data.local.entity.RecipeEntity
import com.svantheemsche.appetijt.data.scraper.RecipeScraper
import com.svantheemsche.appetijt.data.share.SharedTextUrlExtractor
import com.svantheemsche.appetijt.data.security.SourceExtractor
import com.svantheemsche.appetijt.data.utils.HtmlSanitizer
import com.svantheemsche.appetijt.domain.model.ErrorCodes
import com.svantheemsche.appetijt.domain.model.Recipe
import com.svantheemsche.appetijt.domain.model.RecipeSource
import com.svantheemsche.appetijt.domain.model.WorkResult
import com.svantheemsche.appetijt.domain.repository.MealPlanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.net.URL
import java.time.LocalDate

class MealPlanRepositoryImpl(
    private val recipeDao: RecipeDao,
    private val mealEntryDao: MealEntryDao,
    private val scraper: RecipeScraper,
    private val extractor: SharedTextUrlExtractor
) : MealPlanRepository {

    override suspend fun addRecipeFromSharedText(text: String, date: LocalDate): WorkResult<Unit> {
        return addRecipeFromSharedText(text, null, date)
    }

    override suspend fun addRecipeFromSharedText(text: String, titleFallback: String?, date: LocalDate): WorkResult<Unit> {
        // 1. Extract URL
        val urlResult = extractor.extractUrl(text)
        if (urlResult is WorkResult.Failure) return urlResult
        val url = (urlResult as WorkResult.Success).data

        // 1b. Try to guess a title from the shared text if no fallback is provided
        val guessedTitle = titleFallback ?: extractor.extractTitleGuess(text, url)

        // 2. Scrape Metadata
        val scrapeResult = scraper.scrape(url)
        val metadata = if (scrapeResult is WorkResult.Success) {
            scrapeResult.data
        } else {
            // Fallback: Create a basic recipe entry if scraping fails so the user isn't blocked
            Timber.w("Scraping failed, using fallback for URL: $url")
            
            val sourceApp = SourceExtractor.getCleanSourceName(url)
            val displayTitle = when (sourceApp) {
                RecipeSource.XTRA -> "Xtra Recept"
                RecipeSource.COLRUYT -> "Colruyt Recept"
                else -> HtmlSanitizer.sanitize(guessedTitle ?: "Recept van $sourceApp")
            }
            Recipe(
                url = url,
                title = displayTitle,
                imageUrl = null,
                sourceApp = sourceApp
            )
        }

        // 3. Save to Database
        return try {
            Timber.d("Saving recipe to database: ${metadata.title}")
            val recipeId = recipeDao.insertRecipe(
                RecipeEntity(
                    url = metadata.url,
                    title = metadata.title,
                    imageUrl = metadata.imageUrl,
                    sourceApp = metadata.sourceApp
                )
            )
            
            mealEntryDao.insertMealEntry(
                MealEntryEntity(
                    date = date,
                    recipeId = recipeId
                )
            )
            Timber.d("Successfully saved recipe with ID: $recipeId for date: $date")
            WorkResult.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Database error while saving")
            WorkResult.Failure(
                code = ErrorCodes.DATABASE_ERROR,
                message = "Failed to save recipe: ${e.localizedMessage}",
                cause = e
            )
        }
    }

    override fun getRecipesForDate(date: LocalDate): Flow<List<Recipe>> {
        return mealEntryDao.getRecipesForDate(date).map { entities ->
            entities.map { entity ->
                Recipe(
                    id = entity.id,
                    url = entity.url,
                    title = entity.title,
                    imageUrl = entity.imageUrl,
                    sourceApp = entity.sourceApp
                )
            }
        }
    }

    override suspend fun deleteMealEntry(date: LocalDate, recipeId: Long): WorkResult<Unit> {
        return try {
            mealEntryDao.deleteMealEntry(date, recipeId)
            WorkResult.Success(Unit)
        } catch (e: Exception) {
            WorkResult.Failure(ErrorCodes.DATABASE_ERROR, "Could not delete recipe", e)
        }
    }

    override suspend fun updateRecipeDate(recipeId: Long, fromDate: LocalDate, toDate: LocalDate): WorkResult<Unit> {
        return try {
            val rows = mealEntryDao.updateMealEntryDate(recipeId, fromDate, toDate)
            if (rows > 0) WorkResult.Success(Unit) 
            else WorkResult.Failure(ErrorCodes.DATABASE_ERROR, "Recipe entry not found for the given date")
        } catch (e: Exception) {
            WorkResult.Failure(ErrorCodes.DATABASE_ERROR, "Error updating recipe date", e)
        }
    }

    override fun hasRecipesForDate(date: LocalDate): Flow<Boolean> {
        return mealEntryDao.hasEntriesForDate(date)
    }

    override fun getAllUniqueRecipes(): Flow<List<Recipe>> {
        return recipeDao.getAllUniqueRecipes().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun searchInLibrary(query: String): Flow<List<Recipe>> {
        return recipeDao.searchInLibrary("%$query%").map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun addRecipeToDate(recipe: Recipe, date: LocalDate): WorkResult<Unit> {
        return try {
            // We create a new recipe entry (cloning) as per instructions
            val recipeId = recipeDao.insertRecipe(
                RecipeEntity(
                    url = recipe.url,
                    title = recipe.title,
                    imageUrl = recipe.imageUrl,
                    sourceApp = recipe.sourceApp
                )
            )
            mealEntryDao.insertMealEntry(
                MealEntryEntity(
                    date = date,
                    recipeId = recipeId
                )
            )
            WorkResult.Success(Unit)
        } catch (e: Exception) {
            WorkResult.Failure(ErrorCodes.DATABASE_ERROR, "Failed to add recipe to date", e)
        }
    }

    private fun RecipeEntity.toDomain(): Recipe = Recipe(
        id = id,
        url = url,
        title = title,
        imageUrl = imageUrl,
        sourceApp = sourceApp
    )
}
