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

package com.svantheemsche.appetijt.domain.usecase

import com.svantheemsche.appetijt.domain.model.Recipe
import com.svantheemsche.appetijt.domain.model.WorkResult
import com.svantheemsche.appetijt.domain.repository.MealPlanRepository
import java.time.LocalDate

class AddLibraryRecipeToDateUseCase(
    private val repository: MealPlanRepository
) {
    suspend operator fun invoke(recipe: Recipe, date: LocalDate): WorkResult<Unit> {
        return repository.addRecipeToDate(recipe, date)
    }
}
