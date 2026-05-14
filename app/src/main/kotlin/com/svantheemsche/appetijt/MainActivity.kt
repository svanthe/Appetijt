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

package com.svantheemsche.appetijt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.lifecycleScope
import com.svantheemsche.appetijt.data.local.AppetijtDatabase
import com.svantheemsche.appetijt.data.repository.MealPlanRepositoryImpl
import com.svantheemsche.appetijt.data.scraper.RecipeScraper
import com.svantheemsche.appetijt.data.share.SharedTextUrlExtractor
import com.svantheemsche.appetijt.domain.model.ErrorCodes
import com.svantheemsche.appetijt.domain.model.WorkResult
import com.svantheemsche.appetijt.domain.repository.MealPlanRepository
import com.svantheemsche.appetijt.domain.usecase.AddSharedRecipeUseCase
import com.svantheemsche.appetijt.domain.usecase.GetRecipesForDateUseCase
import com.svantheemsche.appetijt.domain.usecase.GetLibraryRecipesUseCase
import com.svantheemsche.appetijt.domain.usecase.AddLibraryRecipeToDateUseCase
import com.svantheemsche.appetijt.ui.theme.AppetijtTheme
import com.svantheemsche.appetijt.ui.weekmenu.MainViewModel
import com.svantheemsche.appetijt.ui.weekmenu.WeekMenuScreen
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : ComponentActivity() {

    lateinit var repository: MealPlanRepository
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate called")
        
        // Theme initialization
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val savedTheme = prefs.getInt("theme_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setDefaultNightMode(savedTheme)

        initializeDependencies()

        mainViewModel = ViewModelProvider(this, object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MainViewModel(
                    mealPlanRepository = repository,
                    getRecipesForDateUseCase = GetRecipesForDateUseCase(repository),
                    addSharedRecipeUseCase = AddSharedRecipeUseCase(repository),
                    getLibraryRecipesUseCase = GetLibraryRecipesUseCase(repository),
                    addLibraryRecipeToDateUseCase = AddLibraryRecipeToDateUseCase(repository)
                ) as T
            }
        })[MainViewModel::class.java]
        
        setContent {
            AppetijtTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WeekMenuScreen(viewModel = mainViewModel)
                }
            }
        }

        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            handleShareIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("MainActivity", "onNewIntent called - Reusing existing instance")
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            handleShareIntent(intent)
        }
    }

    private fun initializeDependencies() {
        if (!::repository.isInitialized) {
            val database = AppetijtDatabase.getDatabase(this)
            repository = MealPlanRepositoryImpl(
                recipeDao = database.recipeDao(),
                mealEntryDao = database.mealEntryDao(),
                scraper = RecipeScraper(),
                extractor = SharedTextUrlExtractor()
            )
        }
    }

    fun handleShareIntent(intent: Intent) {
        val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
        val sharedSubject = intent.getStringExtra(Intent.EXTRA_SUBJECT)
        
        Log.d("MainActivity", "Shared text received: $sharedText")
        Log.d("MainActivity", "Shared subject received: $sharedSubject")
        
        if (sharedText != null) {
            val date = mainViewModel.state.value.selectedDate
            mainViewModel.saveSharedRecipe(sharedText, sharedSubject, date)
        } else {
            Toast.makeText(this, "No text content found in the shared link.", Toast.LENGTH_SHORT).show()
        }
    }
}
