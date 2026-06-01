package com.tracker.busjourney.presentation.screens.home

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class RecentSearch(
    val fromQuery: String,
    val toQuery: String,
)

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {

    private val _recentSearches = MutableStateFlow<List<RecentSearch>>(emptyList())
    val recentSearches: StateFlow<List<RecentSearch>> = _recentSearches.asStateFlow()

    fun recordSearch(from: String, to: String) {
        val entry = RecentSearch(from, to)
        val updated = (_recentSearches.value + entry)
            .distinctBy { it.fromQuery to it.toQuery }
            .takeLast(MAX_RECENT)
        _recentSearches.value = updated
    }

    companion object {
        private const val MAX_RECENT = 5
    }
}
