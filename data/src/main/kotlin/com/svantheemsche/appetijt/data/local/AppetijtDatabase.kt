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

package com.svantheemsche.appetijt.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.svantheemsche.appetijt.data.local.converter.DateConverters
import com.svantheemsche.appetijt.data.local.dao.MealEntryDao
import com.svantheemsche.appetijt.data.local.dao.RecipeDao
import com.svantheemsche.appetijt.data.local.entity.MealEntryEntity
import com.svantheemsche.appetijt.data.local.entity.RecipeEntity

@Database(
    entities = [RecipeEntity::class, MealEntryEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppetijtDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
    abstract fun mealEntryDao(): MealEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppetijtDatabase? = null

        fun getDatabase(context: android.content.Context): AppetijtDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppetijtDatabase::class.java,
                    "appetijt_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
