package com.tracker.busjourney.data.mapper

import com.tracker.busjourney.data.remote.dto.RouteSequenceDto
import com.tracker.busjourney.data.remote.dto.RouteStopDto
import com.tracker.busjourney.domain.model.RouteStop

class RouteSequenceMapper {

    /**
     * Flattens all branch sequences into a single ordered stop list.
     *
     * Most London bus routes are linear (one branch), but some split.
     * We take the first (main) branch only — adequate for the outbound journey
     * and avoids conflating two different route arms into one polyline.
     */
    fun toDomainList(dto: RouteSequenceDto): List<RouteStop> {
        val mainSequence = dto.stopPointSequences
            ?.firstOrNull()
            ?.stopPoint
            ?: return emptyList()

        return mainSequence
            .mapNotNull(::toDomain)
            .sortedBy { it.sequenceNumber }
    }

    private fun toDomain(dto: RouteStopDto): RouteStop? {
        val id = dto.id?.takeIf { it.isNotBlank() } ?: return null
        return RouteStop(
            id = id,
            name = dto.name ?: "",
            lat = dto.lat ?: 0.0,
            lon = dto.lon ?: 0.0,
            sequenceNumber = dto.sequenceNumber ?: 0,
        )
    }
}
