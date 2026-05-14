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

package com.svantheemsche.appetijt.ui.weekmenu

import com.svantheemsche.appetijt.domain.model.Recipe
import java.time.LocalDate

/**
 * Represents the entire state of the Appetijt screen.
 * This state is immutable and should be used by the UI layer.
 */
data class AppetijtState(
    /** The list of dates representing the 7 days of the week (starting from today). */
    val weekDates: List<LocalDate> = emptyList(),
    /** The currently selected date for viewing the meal plan. */
    val selectedDate: LocalDate = LocalDate.now(),
    /** The list of recipes for the currently selected date. */
    val recipesForSelectedDay: List<Recipe> = emptyList(),
    /** Loading state indicator. True if any background operation is running. */
    val isLoading: Boolean = true,
    /** Error message if a critical operation fails. Null otherwise. */
    val errorMessage: String? = null,
    /** The list of unique recipes in the cookbook/library. */
    val libraryRecipes: List<Recipe> = emptyList(),
    /** The current search query for the library. */
    val librarySearchQuery: String = ""
)
