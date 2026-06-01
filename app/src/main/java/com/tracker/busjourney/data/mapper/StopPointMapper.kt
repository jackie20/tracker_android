package com.tracker.busjourney.data.mapper

import com.tracker.busjourney.data.remote.dto.DisambiguationOptionDto
import com.tracker.busjourney.data.remote.dto.StopPointMatchDto
import com.tracker.busjourney.domain.model.StopPoint

class StopPointMapper {

    fun toDomain(dto: StopPointMatchDto): StopPoint = StopPoint(
        id = dto.id,
        name = dto.name,
        lat = dto.lat ?: 0.0,
        lon = dto.lon ?: 0.0,
    )

    fun toDomainList(dtos: List<StopPointMatchDto>): List<StopPoint> =
        dtos.map(::toDomain)

    fun disambiguationOptionToDomain(dto: DisambiguationOptionDto): StopPoint? {
        val id = dto.parameterValue ?: dto.place?.id ?: return null
        val name = dto.place?.commonName ?: return null
        return StopPoint(
            id = id,
            name = name,
            lat = dto.place.lat ?: 0.0,
            lon = dto.place.lon ?: 0.0,
        )
    }
}
