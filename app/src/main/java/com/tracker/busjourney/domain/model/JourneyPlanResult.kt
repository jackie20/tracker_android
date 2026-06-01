package com.tracker.busjourney.domain.model

sealed class JourneyPlanResult {
    data class Journeys(val journeys: List<Journey>) : JourneyPlanResult()
    data class NoResults(val message: String = "No bus routes found for this journey.") : JourneyPlanResult()
    // TfL returns HTTP 300 when a location string matches multiple stops.
    data class FromDisambiguation(val query: String, val options: List<StopPoint>) : JourneyPlanResult()
    data class ToDisambiguation(val query: String, val options: List<StopPoint>) : JourneyPlanResult()
    data class Error(val message: String) : JourneyPlanResult()
}
