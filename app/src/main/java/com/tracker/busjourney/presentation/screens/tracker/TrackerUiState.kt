package com.tracker.busjourney.presentation.screens.tracker

import com.tracker.busjourney.domain.model.BusArrival
import com.tracker.busjourney.domain.model.BusPosition
import com.tracker.busjourney.domain.model.RouteStop

sealed class TrackerUiState {

    data object Loading : TrackerUiState()

    data class LoadingArrivals(val routeStops: List<RouteStop>) : TrackerUiState()

    data class Active(
        val lineId: String,
        val routeStops: List<RouteStop>,
        val arrivals: List<BusArrival>,
        val busPositions: List<BusPosition>,
    ) : TrackerUiState()

    data class Empty(val lineId: String, val routeStops: List<RouteStop>) : TrackerUiState()

    data class Error(val message: String) : TrackerUiState()
}
