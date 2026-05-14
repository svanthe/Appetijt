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

package com.svantheemsche.appetijt.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.svantheemsche.appetijt.data.local.entity.MealEntryEntity
import com.svantheemsche.appetijt.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MealEntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMealEntry(entry: MealEntryEntity)

    @Query("SELECT * FROM meal_plan WHERE date = :date")
    fun getEntriesForDate(date: LocalDate): Flow<List<MealEntryEntity>>

    @Query("""
        SELECT recipes.* FROM recipes 
        INNER JOIN meal_plan ON recipes.id = meal_plan.recipeId 
        WHERE meal_plan.date = :date
    """)
    fun getRecipesForDate(date: LocalDate): Flow<List<RecipeEntity>>

    @Delete
    suspend fun deleteMealEntry(entry: MealEntryEntity)
    
    @Query("DELETE FROM meal_plan WHERE date = :date AND recipeId = :recipeId")
    suspend fun deleteMealEntry(date: LocalDate, recipeId: Long)

    @Query("UPDATE meal_plan SET date = :toDate WHERE date = :fromDate AND recipeId = :recipeId")
    suspend fun updateMealEntryDate(recipeId: Long, fromDate: LocalDate, toDate: LocalDate): Int

    @Query("SELECT EXISTS(SELECT 1 FROM meal_plan WHERE date = :date)")
    fun hasEntriesForDate(date: LocalDate): Flow<Boolean>
}
