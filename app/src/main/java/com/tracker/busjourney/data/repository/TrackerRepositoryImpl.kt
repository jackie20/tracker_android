package com.tracker.busjourney.data.repository

import com.tracker.busjourney.data.mapper.ArrivalMapper
import com.tracker.busjourney.data.mapper.RouteSequenceMapper
import com.tracker.busjourney.data.remote.api.TflApiService
import com.tracker.busjourney.domain.model.BusArrival
import com.tracker.busjourney.domain.model.RouteStop
import com.tracker.busjourney.domain.repository.TrackerRepository
import java.io.IOException

class TrackerRepositoryImpl(
    private val apiService: TflApiService,
    private val arrivalMapper: ArrivalMapper,
    private val routeSequenceMapper: RouteSequenceMapper,
) : TrackerRepository {

    override suspend fun getLiveArrivals(lineId: String): Result<List<BusArrival>> = runCatching {
        arrivalMapper.toDomainList(apiService.getArrivals(lineId))
    }

    override suspend fun getRouteSequence(lineId: String): Result<List<RouteStop>> = runCatching {
        routeSequenceMapper.toDomainList(apiService.getRouteSequence(lineId))
    }
}
