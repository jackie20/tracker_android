package com.tracker.busjourney.domain.model

/**
 * Derived model produced by joining a [BusArrival] with a [RouteStop].
 *
 * The TfL API does not expose GPS coordinates for vehicles.  Position is
 * inferred by matching [BusArrival.naptanId] to a [RouteStop] in the route
 * sequence, placing the bus at that stop's coordinates (simple approximation).
 *
 * A more precise implementation would linearly interpolate between the previous
 * stop and this stop using [timeToStation] as a progress hint.
 */
data class BusPosition(
    val vehicleId: String,
    val lat: Double,
    val lon: Double,
    /** Seconds until this vehicle reaches its next stop. */
    val timeToStation: Int,
    val nextStopName: String,
    val nextNaptanId: String,
)
