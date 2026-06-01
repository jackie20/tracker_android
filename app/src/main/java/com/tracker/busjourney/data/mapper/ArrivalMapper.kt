package com.tracker.busjourney.data.mapper

import com.tracker.busjourney.data.remote.dto.ArrivalDto
import com.tracker.busjourney.domain.model.BusArrival

class ArrivalMapper {

    fun toDomain(dto: ArrivalDto): BusArrival? {
        val vehicleId = dto.vehicleId?.takeIf { it.isNotBlank() } ?: return null
        val naptanId = dto.naptanId?.takeIf { it.isNotBlank() } ?: return null
        return BusArrival(
            vehicleId = vehicleId,
            naptanId = naptanId,
            stationName = dto.stationName ?: "",
            lineId = dto.lineId ?: "",
            timeToStation = dto.timeToStation ?: 0,
            towards = dto.towards ?: dto.destinationName ?: "",
            direction = dto.direction ?: "",
        )
    }

    fun toDomainList(dtos: List<ArrivalDto>): List<BusArrival> =
        dtos.mapNotNull(::toDomain)
}
