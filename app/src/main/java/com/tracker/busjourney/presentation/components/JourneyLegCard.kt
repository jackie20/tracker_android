package com.tracker.busjourney.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.tracker.busjourney.domain.model.Journey
import com.tracker.busjourney.domain.model.JourneyLeg

@Composable
fun JourneyLegCard(
    journey: Journey,
    onSelectLeg: (JourneyLeg) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 2.dp,
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${journey.duration} min journey",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(12.dp))

            journey.legs.forEachIndexed { index, leg ->
                if (index > 0) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
                BusLegRow(
                    leg = leg,
                    onSelect = { onSelectLeg(leg) },
                )
            }
        }
    }
}

@Composable
private fun BusLegRow(
    leg: JourneyLeg,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val desc = "Route ${leg.lineName} from ${leg.fromName} to ${leg.toName}, ${leg.duration} minutes"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = desc },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.weight(1f),
        ) {
            RouteBadge(lineName = leg.lineName)

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Text(
                    text = "${leg.fromName} → ${leg.toName}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (leg.summary.isNotBlank()) {
                    Text(
                        text = leg.summary,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = "${leg.duration} min",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Button(
            onClick = onSelect,
            modifier = Modifier.padding(start = 8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsBus,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Track")
        }
    }
}

@Composable
private fun RouteBadge(
    lineName: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.primary,
    ) {
        Text(
            text = lineName,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}
