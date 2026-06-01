package com.tracker.busjourney.data.mapper

import com.tracker.busjourney.data.remote.dto.JourneyDto
import com.tracker.busjourney.data.remote.dto.LegDto
import com.tracker.busjourney.domain.model.Journey
import com.tracker.busjourney.domain.model.JourneyLeg

class JourneyMapper {

    fun toDomainList(dtos: List<JourneyDto>): List<Journey> =
        dtos.mapNotNull(::toDomain)

    private fun toDomain(dto: JourneyDto): Journey? {
        val busLegs = dto.legs
            ?.filter { it.mode?.id == "bus" }
            ?.mapNotNull(::legToDomain)
            ?: return null

        if (busLegs.isEmpty()) return null

        return Journey(
            duration = dto.duration ?: 0,
            legs = busLegs,
        )
    }

    private fun legToDomain(dto: LegDto): JourneyLeg? {
        val lineId = dto.routeOptions
            ?.firstOrNull()
            ?.lineIdentifier
            ?.id
            ?: return null

        return JourneyLeg(
            lineId = lineId,
            lineName = dto.routeOptions.firstOrNull()?.lineIdentifier?.name ?: lineId,
            fromName = dto.departurePoint?.commonName ?: "",
            toName = dto.arrivalPoint?.commonName ?: "",
            duration = dto.duration ?: 0,
            summary = dto.instruction?.summary ?: "Bus $lineId",
        )
    }
}
