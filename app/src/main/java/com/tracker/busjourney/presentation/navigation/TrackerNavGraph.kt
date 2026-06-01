package com.tracker.busjourney.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tracker.busjourney.presentation.screens.home.HomeScreen
import com.tracker.busjourney.presentation.screens.home.HomeViewModel
import com.tracker.busjourney.presentation.screens.results.JourneyResultsScreen
import com.tracker.busjourney.presentation.screens.search.JourneySearchViewModel
import com.tracker.busjourney.presentation.screens.search.SearchScreen
import com.tracker.busjourney.presentation.screens.tracker.TrackerScreen
import com.tracker.busjourney.presentation.screens.tracker.TrackerViewModel

@Composable
fun TrackerNavGraph() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
    ) {

        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onStartSearch = { navController.navigate(Screen.JourneyFlow.route) },
            )
        }

        // Nested nav graph — SearchScreen and JourneyResultsScreen share one ViewModel
        // scoped to this graph's back-stack entry.
        navigation(
            startDestination = Screen.Search.route,
            route = Screen.JourneyFlow.route,
        ) {
            composable(Screen.Search.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.JourneyFlow.route)
                }
                val viewModel: JourneySearchViewModel = hiltViewModel(parentEntry)
                SearchScreen(
                    viewModel = viewModel,
                    onNavigateToResults = { navController.navigate(Screen.JourneyResults.route) },
                    onNavigateUp = { navController.popBackStack() },
                )
            }

            composable(Screen.JourneyResults.route) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Screen.JourneyFlow.route)
                }
                val viewModel: JourneySearchViewModel = hiltViewModel(parentEntry)
                JourneyResultsScreen(
                    viewModel = viewModel,
                    onLegSelected = { lineId ->
                        navController.navigate(Screen.Tracker.createRoute(lineId))
                    },
                    onNavigateUp = { navController.popBackStack() },
                )
            }
        }

        composable(
            route = Screen.Tracker.route,
            arguments = listOf(navArgument(Screen.Tracker.ARG_LINE_ID) { type = NavType.StringType }),
        ) { backStackEntry ->
            val lineId = backStackEntry.arguments?.getString(Screen.Tracker.ARG_LINE_ID) ?: return@composable
            val viewModel: TrackerViewModel = hiltViewModel()
            TrackerScreen(
                viewModel = viewModel,
                lineId = lineId,
                onNavigateUp = { navController.popBackStack() },
            )
        }
    }
}
