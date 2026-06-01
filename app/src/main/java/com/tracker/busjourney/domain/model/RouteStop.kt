package com.tracker.busjourney.domain.model

data class RouteStop(
    /** NAPTAN stop ID — used as join key against BusArrival.naptanId. */
    val id: String,
    val name: String,
    val lat: Double,
    val lon: Double,
    val sequenceNumber: Int,
)
