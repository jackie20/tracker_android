package com.tracker.busjourney.domain.model

data class Journey(
    val duration: Int,
    val legs: List<JourneyLeg>,
)

data class JourneyLeg(
    val lineId: String,
    val lineName: String,
    val fromName: String,
    val toName: String,
    val duration: Int,
    val summary: String,
)
