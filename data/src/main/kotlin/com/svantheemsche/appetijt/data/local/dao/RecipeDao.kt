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
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.svantheemsche.appetijt.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {
    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeById(id: Long): RecipeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Query("SELECT * FROM recipes")
    fun getAllRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes GROUP BY title ORDER BY title ASC")
    fun getAllUniqueRecipes(): Flow<List<RecipeEntity>>

    @Query("SELECT * FROM recipes WHERE title LIKE :searchQuery GROUP BY title ORDER BY title ASC")
    fun searchInLibrary(searchQuery: String): Flow<List<RecipeEntity>>

    @Query("DELETE FROM recipes WHERE id = :id")
    suspend fun deleteById(id: Long)
}
