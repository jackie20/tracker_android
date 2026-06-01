package com.tracker.busjourney.presentation.navigation

/**
 * Typed navigation destinations.
 * Using sealed class + object pattern avoids stringly-typed route strings
 * scattered across the codebase.
 */
sealed class Screen(val route: String) {

    /** Home screen — shows a map + recent journey history. */
    data object Home : Screen("home")

    /**
     * Journey search + disambiguation flow, grouped in a nested nav graph.
     * Screens within this graph share a single [JourneySearchViewModel] scoped
     * to the graph's back-stack entry.
     */
    data object JourneyFlow : Screen("journey_flow")

    /** Search form (start destination of the journey_flow graph). */
    data object Search : Screen("search")

    /** Flat journey results list after a successful plan. */
    data object JourneyResults : Screen("journey_results")

    /** Live bus tracking screen for a specific line. */
    data object Tracker : Screen("tracker/{lineId}") {
        fun createRoute(lineId: String) = "tracker/$lineId"
        const val ARG_LINE_ID = "lineId"
    }
}
