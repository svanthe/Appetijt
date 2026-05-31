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

import android.R
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.svantheemsche.appetijt.domain.model.Recipe
import com.svantheemsche.appetijt.ui.components.RecipeCard
import com.svantheemsche.appetijt.ui.components.RecipeLibraryDialog
import timber.log.Timber
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun WeekMenuScreen(
    viewModel: MainViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val plannedDates by viewModel.getPlannedDatesFlow().collectAsState(initial = emptySet())
    var showAddDialog by remember { mutableStateOf(false) }
    var showLibraryDialog by remember { mutableStateOf(false) }
    var recipeToMove by remember { mutableStateOf<Recipe?>(null) }
    val context = LocalContext.current

    val monthYearFormatter = remember { 
        DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault()) 
    }
    val currentMonthLabel = remember(state.weekDates) {
        state.weekDates.firstOrNull()?.format(monthYearFormatter) ?: ""
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Appetijt",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = { showLibraryDialog = true },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "'t Archief",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currentMonthLabel,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { viewModel.shiftWeeks(-1) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, 
                            contentDescription = "Previous Week"
                        )
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        WeekDaySelector(
                            dates = state.weekDates,
                            selectedDate = state.selectedDate,
                            plannedDates = plannedDates,
                            onDateSelected = { viewModel.selectDay(it)
                            }
                        )
                    }

                    IconButton(onClick = { viewModel.shiftWeeks(1) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, 
                            contentDescription = "Next Week"
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Recipe")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                RecipeList(
                    recipes = state.recipesForSelectedDay,
                    errorMessage = state.errorMessage,
                    onRecipeDelete = { recipeId -> viewModel.deleteRecipe(recipeId, state.selectedDate) },
                    onRecipeMove = { recipe -> recipeToMove = recipe },
                    onRecipeClick = { recipe ->
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(recipe.url)).apply {
                                addCategory(Intent.CATEGORY_BROWSABLE)
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Timber.e(e, "Failed to open recipe URL")
                        }
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddRecipeDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { url ->
                viewModel.saveSharedRecipe(url, state.selectedDate)
                showAddDialog = false
            }
        )
    }

    if (recipeToMove != null) {
        QuickShiftDialog(
            recipe = recipeToMove!!,
            availableDates = state.weekDates,
            onDismiss = { recipeToMove = null },
            onConfirm = { newDate ->
                viewModel.moveRecipe(recipeToMove!!.id, state.selectedDate, newDate)
                recipeToMove = null
            }
        )
    }

    if (showLibraryDialog) {
        RecipeLibraryDialog(
            recipes = state.libraryRecipes,
            searchQuery = state.librarySearchQuery,
            onSearchQueryChange = { viewModel.searchLibrary(it) },
            onRecipeSelected = { recipe ->
                viewModel.addLibraryRecipeToDate(recipe, state.selectedDate)
                showLibraryDialog = false
            },
            onDismiss = { showLibraryDialog = false }
        )
    }
}

@Composable
fun QuickShiftDialog(
    recipe: com.svantheemsche.appetijt.domain.model.Recipe,
    availableDates: List<LocalDate>,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Move Recipe") },
        text = {
            Column {
                Text("Move ${recipe.title} to another day this week:")
                Spacer(modifier = Modifier.padding(vertical = 8.dp))
                LazyRow(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
                    items(availableDates) { date ->
                        Button(onClick = { onConfirm(date) }) {
                            Text(date.format(DateTimeFormatter.ofPattern("EEE dd")))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddRecipeDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var url by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Recipe URL") },
        text = {
            Column {
                Text("Paste a recipe link to add it to the selected day.")
                Spacer(modifier = Modifier.padding(vertical = 4.dp))
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("Recipe URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(url) },
                enabled = url.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun WeekDaySelector(
    dates: List<LocalDate>,
    selectedDate: LocalDate,
    plannedDates: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit
) {
    val dayFormatter = DateTimeFormatter.ofPattern("E")
    val dateFormatter = DateTimeFormatter.ofPattern("d")

    LazyRow(
        contentPadding = PaddingValues(vertical = 8.dp),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.Center
    ) {
        items(dates) { date ->
            val isSelected = date == selectedDate
            val isPlanned = plannedDates.contains(date)
            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 8.dp)
                    .clickable { onDateSelected(date) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = date.format(dayFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
                )
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                    border = if (isSelected) BorderStroke(1.5.dp, MaterialTheme.colorScheme.onBackground) else null
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = date.format(dateFormatter),
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onBackground
                        )
                        if (isPlanned) {
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .background(MaterialTheme.colorScheme.primary, androidx.compose.foundation.shape.CircleShape)
                                    .padding(bottom = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeList(
    recipes: List<com.svantheemsche.appetijt.domain.model.Recipe>,
    errorMessage: String?,
    onRecipeDelete: (Long) -> Unit,
    onRecipeMove: (com.svantheemsche.appetijt.domain.model.Recipe) -> Unit,
    onRecipeClick: (com.svantheemsche.appetijt.domain.model.Recipe) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        if (errorMessage != null) {
            item {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }

        if (recipes.isEmpty()) {
            item {
                Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "No recipes planned for this day.")
                }
            }
        } else {
            items(recipes) { recipe ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = {
                        if (it == SwipeToDismissBoxValue.EndToStart) {
                            onRecipeDelete(recipe.id)
                            true
                        } else false
                    }
                )

                SwipeToDismissBox(
                    state = dismissState,
                    modifier = Modifier.padding(vertical = 8.dp),
                    backgroundContent = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(16.dp))
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                ) {
                    RecipeCard(
                        recipe = recipe,
                        onClick = { onRecipeClick(recipe) },
                        onLongClick = { onRecipeMove(recipe) }
                    )
                }
            }
        }
    }
}
