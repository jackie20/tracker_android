package com.tracker.busjourney.presentation.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tracker.busjourney.domain.model.StopPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: JourneySearchViewModel,
    onNavigateToResults: () -> Unit,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val fromFocusRequester = remember { FocusRequester() }

    LaunchedEffect(state.journeyResults) {
        if (state.journeyResults != null) onNavigateToResults()
    }

    LaunchedEffect(state.error) {
        state.error?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    LaunchedEffect(Unit) {
        fromFocusRequester.requestFocus()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Plan journey") },
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
        modifier = modifier.imePadding(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LocationField(
                value = state.fromQuery,
                onValueChange = viewModel::onFromQueryChanged,
                label = "From",
                suggestions = state.fromSuggestions,
                onSuggestionSelected = viewModel::onFromSuggestionSelected,
                onSuggestionsDismissed = viewModel::clearFromSuggestions,
                focusRequester = fromFocusRequester,
                leadingIconTint = MaterialTheme.colorScheme.primary,
            )

            LocationField(
                value = state.toQuery,
                onValueChange = viewModel::onToQueryChanged,
                label = "To",
                suggestions = state.toSuggestions,
                onSuggestionSelected = viewModel::onToSuggestionSelected,
                onSuggestionsDismissed = viewModel::clearToSuggestions,
                leadingIconTint = MaterialTheme.colorScheme.error,
            )

            Button(
                onClick = viewModel::search,
                enabled = !state.isLoading &&
                    state.fromQuery.isNotBlank() &&
                    state.toQuery.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                } else {
                    Text("Search")
                }
            }
        }
    }

    state.disambiguation?.let { disambiguation ->
        DisambiguationSheet(
            state = disambiguation,
            onOptionSelected = viewModel::onDisambiguationOptionSelected,
            onDismiss = { },
        )
    }
}

@Composable
private fun LocationField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    suggestions: List<StopPoint>,
    onSuggestionSelected: (StopPoint) -> Unit,
    onSuggestionsDismissed: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    leadingIconTint: Color = MaterialTheme.colorScheme.primary,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (focusRequester != null) Modifier.focusRequester(focusRequester)
                    else Modifier,
                ),
            leadingIcon = {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = leadingIconTint,
                )
            },
            trailingIcon = {
                if (value.isNotEmpty()) {
                    IconButton(onClick = { onValueChange("") }) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear $label",
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            },
        )

        DropdownMenu(
            expanded = suggestions.isNotEmpty(),
            onDismissRequest = onSuggestionsDismissed,
            modifier = Modifier.fillMaxWidth(),
        ) {
            suggestions.forEach { stop ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = stop.name,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                    onClick = { onSuggestionSelected(stop) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DisambiguationSheet(
    state: DisambiguationState,
    onOptionSelected: (StopPoint) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()
    val fieldLabel = if (state.field == DisambiguationField.FROM) "From" else "To"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .systemBarsPadding(),
        ) {
            Text(
                text = "Select Location",
                style = MaterialTheme.typography.titleLarge,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Multiple matches for \"${state.query}\" ($fieldLabel)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn {
                items(state.options) { option ->
                    DisambiguationOptionRow(
                        option = option,
                        onSelect = { onOptionSelected(option) },
                    )
                    HorizontalDivider()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DisambiguationOptionRow(
    option: StopPoint,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 14.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = option.name,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
