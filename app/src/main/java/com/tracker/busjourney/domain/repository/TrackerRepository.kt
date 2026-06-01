package com.tracker.busjourney.domain.repository

import com.tracker.busjourney.domain.model.BusArrival
import com.tracker.busjourney.domain.model.RouteStop

interface TrackerRepository {

    /** Fetch the current live arrivals for a given bus line. */
    suspend fun getLiveArrivals(lineId: String): Result<List<BusArrival>>

    /**
     * Fetch the outbound stop sequence for a given bus line.
     * Callers are responsible for caching this — it changes very rarely.
     */
    suspend fun getRouteSequence(lineId: String): Result<List<RouteStop>>
}
