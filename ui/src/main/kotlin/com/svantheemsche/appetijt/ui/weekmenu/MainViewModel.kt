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

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svantheemsche.appetijt.domain.model.Recipe
import com.svantheemsche.appetijt.domain.model.WorkResult
import com.svantheemsche.appetijt.domain.repository.MealPlanRepository
import com.svantheemsche.appetijt.domain.usecase.AddSharedRecipeUseCase
import com.svantheemsche.appetijt.domain.usecase.GetRecipesForDateUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * ViewModel responsible for managing the UI state of the meal planner screen.
 * It handles loading the week, tracking the selected day, and executing recipe saving actions.
 */
class MainViewModel(
    private val mealPlanRepository: MealPlanRepository,
    private val getRecipesForDateUseCase: GetRecipesForDateUseCase,
    private val addSharedRecipeUseCase: AddSharedRecipeUseCase,
    private val getLibraryRecipesUseCase: com.svantheemsche.appetijt.domain.usecase.GetLibraryRecipesUseCase,
    private val addLibraryRecipeToDateUseCase: com.svantheemsche.appetijt.domain.usecase.AddLibraryRecipeToDateUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(AppetijtState())
    val state: StateFlow<AppetijtState> = _state

    private var displayedWeekStart: LocalDate = LocalDate.now()

    init {
        loadWeekState()
        loadLibrary()
    }

    private fun loadWeekState() {
        val today = LocalDate.now()
        displayedWeekStart = today
        val weekDates = (0 until 7).map { today.plusDays(it.toLong()) }

        _state.update { currentState ->
            currentState.copy(
                weekDates = weekDates,
                selectedDate = today,
                isLoading = true,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            getRecipesForDateUseCase(today).collect { recipes ->
                _state.update { it.copy(recipesForSelectedDay = recipes, isLoading = false) }
            }
        }
    }

    fun shiftWeeks(weeks: Long) {
        displayedWeekStart = displayedWeekStart.plusWeeks(weeks)
        val newWeekDates = (0 until 7).map { displayedWeekStart.plusDays(it.toLong()) }
        
        _state.update { it.copy(weekDates = newWeekDates) }
        
        val selectedDate = _state.value.selectedDate
        selectDay(selectedDate)
    }

    fun selectDay(date: LocalDate) {
        _state.update { currentState ->
            currentState.copy(
                selectedDate = date,
                recipesForSelectedDay = emptyList(),
                errorMessage = null
            )
        }

        viewModelScope.launch {
            getRecipesForDateUseCase(date).collect { recipes ->
                _state.update { it.copy(recipesForSelectedDay = recipes, isLoading = false) }
            }
        }
    }

    fun saveSharedRecipe(sharedText: String, title: String?, date: LocalDate) {
        _state.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            val result = mealPlanRepository.addRecipeFromSharedText(sharedText, title, date)

            if (result is WorkResult.Success) {
                selectDay(date)
            } else if (result is WorkResult.Failure) {
                Log.e("MainViewModel", "Failed to save recipe: ${result.message}", result.cause)
                _state.update { it.copy(isLoading = false, errorMessage = result.message) }
            }
        }
    }

    fun saveSharedRecipe(sharedText: String, date: LocalDate) {
        saveSharedRecipe(sharedText, null, date)
    }

    fun deleteRecipe(recipeId: Long, date: LocalDate) {
        viewModelScope.launch {
            val result = mealPlanRepository.deleteMealEntry(date, recipeId)
            if (result is WorkResult.Success) {
                selectDay(date)
            }
        }
    }

    fun moveRecipe(recipeId: Long, fromDate: LocalDate, toDate: LocalDate) {
        viewModelScope.launch {
            val result = mealPlanRepository.updateRecipeDate(recipeId, fromDate, toDate)
            if (result is WorkResult.Success) {
                selectDay(fromDate) // Refresh current view
            }
        }
    }

    fun searchLibrary(query: String) {
        _state.update { it.copy(librarySearchQuery = query) }
        loadLibrary()
    }

    private fun loadLibrary() {
        val query = _state.value.librarySearchQuery
        viewModelScope.launch {
            getLibraryRecipesUseCase(query).collect { recipes ->
                _state.update { it.copy(libraryRecipes = recipes) }
            }
        }
    }

    fun addLibraryRecipeToDate(recipe: Recipe, date: LocalDate) {
        viewModelScope.launch {
            val result = addLibraryRecipeToDateUseCase(recipe, date)
            if (result is WorkResult.Success) {
                if (date == _state.value.selectedDate) {
                    selectDay(date)
                }
            } else if (result is WorkResult.Failure) {
                _state.update { it.copy(errorMessage = result.message) }
            }
        }
    }

    /**
     * Returns a flow of dates in the current week that have planned recipes.
     */
    fun getPlannedDatesFlow(): Flow<Set<LocalDate>> {
        return flow {
            while (true) {
                val currentDates = _state.value.weekDates
                if (currentDates.isEmpty()) {
                    emit(emptySet())
                    delay(1000)
                    continue
                }

                val planned = currentDates.filter { date ->
                    mealPlanRepository.hasRecipesForDate(date).first() 
                }.toSet()
                
                emit(planned)
                delay(2000)
            }
        }
    }
}
