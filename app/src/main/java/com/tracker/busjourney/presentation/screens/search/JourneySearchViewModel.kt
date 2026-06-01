package com.tracker.busjourney.presentation.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tracker.busjourney.domain.model.JourneyPlanResult
import com.tracker.busjourney.domain.model.StopPoint
import com.tracker.busjourney.domain.usecase.PlanJourneyUseCase
import com.tracker.busjourney.domain.usecase.SearchStopPointsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JourneySearchViewModel @Inject constructor(
    private val planJourneyUseCase: PlanJourneyUseCase,
    private val searchStopPointsUseCase: SearchStopPointsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var fromSuggestionJob: Job? = null
    private var toSuggestionJob: Job? = null

    fun onFromQueryChanged(query: String) {
        _uiState.update { it.copy(fromQuery = query, resolvedFromId = null) }
        fromSuggestionJob?.cancel()
        if (query.trim().length < 2) {
            _uiState.update { it.copy(fromSuggestions = emptyList()) }
            return
        }
        fromSuggestionJob = viewModelScope.launch {
            delay(SUGGESTION_DEBOUNCE_MS)
            searchStopPointsUseCase(query)
                .onSuccess { stops ->
                    _uiState.update { it.copy(fromSuggestions = stops.take(MAX_SUGGESTIONS)) }
                }
        }
    }

    fun onFromSuggestionSelected(stop: StopPoint) {
        fromSuggestionJob?.cancel()
        _uiState.update {
            it.copy(
                fromQuery = stop.name,
                resolvedFromId = stop.id,
                fromSuggestions = emptyList(),
            )
        }
    }

    fun clearFromSuggestions() {
        fromSuggestionJob?.cancel()
        _uiState.update { it.copy(fromSuggestions = emptyList()) }
    }

    fun onToQueryChanged(query: String) {
        _uiState.update { it.copy(toQuery = query, resolvedToId = null) }
        toSuggestionJob?.cancel()
        if (query.trim().length < 2) {
            _uiState.update { it.copy(toSuggestions = emptyList()) }
            return
        }
        toSuggestionJob = viewModelScope.launch {
            delay(SUGGESTION_DEBOUNCE_MS)
            searchStopPointsUseCase(query)
                .onSuccess { stops ->
                    _uiState.update { it.copy(toSuggestions = stops.take(MAX_SUGGESTIONS)) }
                }
        }
    }

    fun onToSuggestionSelected(stop: StopPoint) {
        toSuggestionJob?.cancel()
        _uiState.update {
            it.copy(
                toQuery = stop.name,
                resolvedToId = stop.id,
                toSuggestions = emptyList(),
            )
        }
    }

    fun clearToSuggestions() {
        toSuggestionJob?.cancel()
        _uiState.update { it.copy(toSuggestions = emptyList()) }
    }

    fun search() {
        val state = _uiState.value
        val from = state.resolvedFromId ?: state.fromQuery.trim()
        val to = state.resolvedToId ?: state.toQuery.trim()

        if (from.isBlank() || to.isBlank()) {
            _uiState.update { it.copy(error = "Please enter an origin and destination.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    error = null,
                    disambiguation = null,
                    fromSuggestions = emptyList(),
                    toSuggestions = emptyList(),
                )
            }
            handlePlanResult(planJourneyUseCase(from, to))
        }
    }

    fun onDisambiguationOptionSelected(option: StopPoint) {
        val field = _uiState.value.disambiguation?.field ?: return

        _uiState.update { state ->
            when (field) {
                DisambiguationField.FROM -> state.copy(
                    resolvedFromId = option.id,
                    fromQuery = option.name,
                    disambiguation = null,
                )
                DisambiguationField.TO -> state.copy(
                    resolvedToId = option.id,
                    toQuery = option.name,
                    disambiguation = null,
                )
            }
        }
        search()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun handlePlanResult(result: JourneyPlanResult) {
        _uiState.update { state ->
            when (result) {
                is JourneyPlanResult.Journeys -> state.copy(
                    isLoading = false,
                    journeyResults = result.journeys,
                )
                is JourneyPlanResult.FromDisambiguation -> state.copy(
                    isLoading = false,
                    disambiguation = DisambiguationState(
                        field = DisambiguationField.FROM,
                        query = result.query,
                        options = result.options,
                    ),
                )
                is JourneyPlanResult.ToDisambiguation -> state.copy(
                    isLoading = false,
                    disambiguation = DisambiguationState(
                        field = DisambiguationField.TO,
                        query = result.query,
                        options = result.options,
                    ),
                )
                is JourneyPlanResult.NoResults -> state.copy(
                    isLoading = false,
                    error = result.message,
                )
                is JourneyPlanResult.Error -> state.copy(
                    isLoading = false,
                    error = result.message,
                )
            }
        }
    }

    companion object {
        private const val SUGGESTION_DEBOUNCE_MS = 300L
        private const val MAX_SUGGESTIONS = 5
    }
}
