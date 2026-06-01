package com.tracker.busjourney.domain.engine

import com.tracker.busjourney.domain.model.BusArrival
import com.tracker.busjourney.domain.model.BusPosition
import com.tracker.busjourney.domain.model.RouteStop
import javax.inject.Inject

/**
 * Domain service that infers approximate vehicle GPS positions by joining
 * live arrival telemetry with known route stop coordinates.
 *
 * The TfL API exposes [BusArrival.naptanId] (the approaching stop) and
 * [BusArrival.timeToStation] for each vehicle, but no raw latitude/longitude.
 * This engine maps each vehicle to the coordinates of its next stop — a
 * reasonable approximation documented in the assignment brief.
 *
 * A more precise implementation could linearly interpolate between the
 * previous stop and the next stop using [BusArrival.timeToStation] as a
 * progress fraction, but this is left as an optional extension.
 *
 * Deduplication: when a vehicle appears multiple times in [arrivals] (it may
 * be scheduled at several stops on the same line), only the soonest arrival
 * is used to produce a single map pin per vehicle.
 */
class VirtualBusPositionEngine @Inject constructor() {

    /**
     * @param arrivals Live arrival records returned from the arrivals endpoint.
     * @param routeStops Ordered stop sequence from the route geometry endpoint.
     * @return One [BusPosition] per unique vehicle, placed at its next stop.
     *         Returns an empty list when [routeStops] is empty (no geometry
     *         to join against).
     */
    fun derivePositions(
        arrivals: List<BusArrival>,
        routeStops: List<RouteStop>,
    ): List<BusPosition> {
        if (routeStops.isEmpty()) return emptyList()

        val stopById = routeStops.associateBy { it.id }

        return arrivals
            .groupBy { it.vehicleId }
            .mapNotNull { (vehicleId, vehicleArrivals) ->
                val soonest = vehicleArrivals.minByOrNull { it.timeToStation }
                    ?: return@mapNotNull null
                val stop = stopById[soonest.naptanId] ?: return@mapNotNull null
                BusPosition(
                    vehicleId = vehicleId,
                    lat = stop.lat,
                    lon = stop.lon,
                    timeToStation = soonest.timeToStation,
                    nextStopName = stop.name,
                    nextNaptanId = stop.id,
                )
            }
    }
}
