package com.tracker.busjourney.domain.model

data class BusArrival(
    val vehicleId: String,
    val naptanId: String,
    val stationName: String,
    val lineId: String,
    val timeToStation: Int,
    val towards: String,
    val direction: String,
)
