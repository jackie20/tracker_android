package com.tracker.busjourney.presentation.screens.tracker

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tracker.busjourney.domain.engine.VirtualBusPositionEngine
import com.tracker.busjourney.domain.model.BusArrival
import com.tracker.busjourney.domain.model.BusPosition
import com.tracker.busjourney.domain.model.RouteStop
import com.tracker.busjourney.domain.usecase.GetLiveArrivalsUseCase
import com.tracker.busjourney.domain.usecase.GetRouteSequenceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrackerViewModel @Inject constructor(
    private val getLiveArrivalsUseCase: GetLiveArrivalsUseCase,
    private val getRouteSequenceUseCase: GetRouteSequenceUseCase,
    private val positionEngine: VirtualBusPositionEngine,
) : ViewModel() {

    private val _uiState = MutableStateFlow<TrackerUiState>(TrackerUiState.Loading)
    val uiState: StateFlow<TrackerUiState> = _uiState.asStateFlow()

    private var cachedRouteStops: List<RouteStop> = emptyList()
    private var initialisedLineId: String? = null
    private var pollingJob: Job? = null

    fun initialise(lineId: String) {
        if (initialisedLineId == lineId) return
        initialisedLineId = lineId

        viewModelScope.launch {
            _uiState.value = TrackerUiState.Loading
            getRouteSequenceUseCase(lineId)
                .onSuccess { stops ->
                    cachedRouteStops = stops
                    _uiState.value = TrackerUiState.LoadingArrivals(stops)
                }
                .onFailure { e ->
                    _uiState.value = TrackerUiState.Error(
                        "Failed to load route: ${e.message}"
                    )
                }
        }
    }

    fun startPolling(lineId: String) {
        pollingJob?.cancel()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                fetchArrivals(lineId)
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
    }

    private suspend fun fetchArrivals(lineId: String) {
        getLiveArrivalsUseCase(lineId)
            .onSuccess { arrivals ->
                val outbound = filterToOutbound(arrivals)
                val positions = positionEngine.derivePositions(outbound, cachedRouteStops)

                _uiState.value = if (outbound.isEmpty()) {
                    TrackerUiState.Empty(lineId, cachedRouteStops)
                } else {
                    TrackerUiState.Active(
                        lineId = lineId,
                        routeStops = cachedRouteStops,
                        arrivals = outbound,
                        busPositions = positions,
                    )
                }
            }
            .onFailure { e ->
                if (_uiState.value is TrackerUiState.Loading ||
                    _uiState.value is TrackerUiState.LoadingArrivals
                ) {
                    _uiState.value = TrackerUiState.Error("Failed to fetch arrivals: ${e.message}")
                }
            }
    }

    private fun filterToOutbound(arrivals: List<BusArrival>): List<BusArrival> {
        if (cachedRouteStops.isEmpty()) return arrivals
        val outboundIds = cachedRouteStops.map { it.id }.toSet()
        return arrivals.filter { it.naptanId in outboundIds }
    }

    override fun onCleared() {
        super.onCleared()
        pollingJob?.cancel()
    }

    companion object {
        internal const val POLL_INTERVAL_MS = 30_000L
    }
}
