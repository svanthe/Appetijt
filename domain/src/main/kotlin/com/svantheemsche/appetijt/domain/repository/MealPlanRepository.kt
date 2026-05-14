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

package com.svantheemsche.appetijt.domain.repository

import com.svantheemsche.appetijt.domain.model.Recipe
import com.svantheemsche.appetijt.domain.model.WorkResult
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface MealPlanRepository {
    suspend fun addRecipeFromSharedText(text: String, date: LocalDate): WorkResult<Unit>
    suspend fun addRecipeFromSharedText(text: String, titleFallback: String?, date: LocalDate): WorkResult<Unit>
    fun getRecipesForDate(date: LocalDate): Flow<List<Recipe>>
    
    suspend fun deleteMealEntry(date: LocalDate, recipeId: Long): WorkResult<Unit>
    suspend fun updateRecipeDate(recipeId: Long, fromDate: LocalDate, toDate: LocalDate): WorkResult<Unit>
    fun hasRecipesForDate(date: LocalDate): Flow<Boolean>

    // Library
    fun getAllUniqueRecipes(): Flow<List<Recipe>>
    fun searchInLibrary(query: String): Flow<List<Recipe>>
    suspend fun addRecipeToDate(recipe: Recipe, date: LocalDate): WorkResult<Unit>
}
