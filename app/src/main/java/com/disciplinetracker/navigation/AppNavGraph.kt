package com.disciplinetracker.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.disciplinetracker.data.AppDatabase
import com.disciplinetracker.data.repository.HabitRepository
import com.disciplinetracker.ui.screens.AddHabitScreen
import com.disciplinetracker.ui.screens.HistoryScreen
import com.disciplinetracker.ui.screens.HomeScreen
import com.disciplinetracker.ui.screens.SettingsScreen
import com.disciplinetracker.ui.screens.StatsScreen
import com.disciplinetracker.viewmodel.AddHabitViewModel

enum class AppDestination(val route: String, val label: String) {
    HOME("home", "Home"),
    ADD_HABIT("add_habit", "Add"),
    HISTORY("history", "History"),
    STATS("stats", "Stats"),
    SETTINGS("settings", "Settings")
}

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    val destinations = AppDestination.entries

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination

            NavigationBar {
                destinations.forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = { Text(destination.label) },
                        icon = {}
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.HOME.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppDestination.HOME.route) { HomeScreen() }
            composable(AppDestination.ADD_HABIT.route) {
                val context = LocalContext.current
                val repository = HabitRepository(AppDatabase.getInstance(context).habitDao())
                val viewModel: AddHabitViewModel = viewModel(factory = AddHabitViewModel.Factory(repository))
                AddHabitScreen(viewModel = viewModel)
            }
            composable(AppDestination.HISTORY.route) { HistoryScreen() }
            composable(AppDestination.STATS.route) { StatsScreen() }
            composable(AppDestination.SETTINGS.route) { SettingsScreen() }
        }
    }
}
