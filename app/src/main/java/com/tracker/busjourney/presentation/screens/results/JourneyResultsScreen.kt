package com.tracker.busjourney.presentation.screens.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tracker.busjourney.domain.model.JourneyLeg
import com.tracker.busjourney.presentation.components.EmptyState
import com.tracker.busjourney.presentation.components.JourneyLegCard
import com.tracker.busjourney.presentation.screens.search.JourneySearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JourneyResultsScreen(
    viewModel: JourneySearchViewModel,
    onLegSelected: (lineId: String) -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val journeys = state.journeyResults.orEmpty()

    val fromLabel = state.fromQuery.ifBlank { "Origin" }
    val toLabel = state.toQuery.ifBlank { "Destination" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "$fromLabel → $toLabel",
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${journeys.size} route${if (journeys.size != 1) "s" else ""} found",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        modifier = modifier,
    ) { innerPadding ->
        if (journeys.isEmpty()) {
            EmptyState(
                title = "No routes found",
                subtitle = "There are no bus routes between these locations.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(journeys) { journey ->
                    JourneyLegCard(
                        journey = journey,
                        onSelectLeg = { leg: JourneyLeg ->
                            onLegSelected(leg.lineId)
                        },
                    )
                }
            }
        }
    }
}
