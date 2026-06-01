package com.tracker.busjourney.presentation.screens.search

import com.tracker.busjourney.domain.model.Journey
import com.tracker.busjourney.domain.model.StopPoint

enum class DisambiguationField { FROM, TO }

data class SearchUiState(
    val fromQuery: String = "",
    val toQuery: String = "",
    val resolvedFromId: String? = null,
    val resolvedToId: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val disambiguation: DisambiguationState? = null,
    val journeyResults: List<Journey>? = null,
    val fromSuggestions: List<StopPoint> = emptyList(),
    val toSuggestions: List<StopPoint> = emptyList(),
)

data class DisambiguationState(
    val field: DisambiguationField,
    val query: String,
    val options: List<StopPoint>,
)
