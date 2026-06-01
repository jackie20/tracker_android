package com.tracker.busjourney.presentation.screens.tracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.tracker.busjourney.domain.model.BusArrival
import com.tracker.busjourney.presentation.components.BusArrivalItem
import com.tracker.busjourney.presentation.components.BusTrackerMap
import com.tracker.busjourney.presentation.components.EmptyState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(
    viewModel: TrackerViewModel,
    lineId: String,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lineId) {
        viewModel.initialise(lineId)
    }

    LaunchedEffect(lineId) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.startPolling(lineId)
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.stopPolling() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Route $lineId")
                        Text(
                            text = "Live",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
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
        when (val state = uiState) {
            is TrackerUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is TrackerUiState.LoadingArrivals -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    BusTrackerMap(
                        routeStops = state.routeStops,
                        busPositions = emptyList(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.6f),
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.4f),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            is TrackerUiState.Active -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    BusTrackerMap(
                        routeStops = state.routeStops,
                        busPositions = state.busPositions,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.55f),
                    )
                    BusList(
                        lineId = state.lineId,
                        arrivals = state.arrivals,
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.45f),
                    )
                }
            }

            is TrackerUiState.Empty -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    BusTrackerMap(
                        routeStops = state.routeStops,
                        busPositions = emptyList(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.55f),
                    )
                    EmptyState(
                        title = "No Buses Running",
                        subtitle = "No active buses on Route ${state.lineId} right now.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.45f),
                    )
                }
            }

            is TrackerUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun BusList(
    lineId: String,
    arrivals: List<BusArrival>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                ) {
                    Text(
                        text = lineId,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
                Text(
                    text = "Buses",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                items = arrivals.sortedBy { it.timeToStation },
                key = { it.vehicleId },
            ) { arrival ->
                BusArrivalItem(arrival = arrival)
            }
        }
    }
}
