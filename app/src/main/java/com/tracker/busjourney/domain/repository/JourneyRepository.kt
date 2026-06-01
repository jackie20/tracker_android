package com.tracker.busjourney.domain.repository

import com.tracker.busjourney.domain.model.JourneyPlanResult
import com.tracker.busjourney.domain.model.StopPoint

interface JourneyRepository {

    /**
     * Search for bus stop points matching [query].
     * Returns an empty list when nothing is found — never throws on empty results.
     */
    suspend fun searchStopPoints(query: String): Result<List<StopPoint>>

    /**
     * Plan a journey between two location strings (free text or resolved NAPTAN/NaPTAN IDs).
     *
     * Returns a [JourneyPlanResult] which may be:
     * - [JourneyPlanResult.Journeys]           — one or more route options
     * - [JourneyPlanResult.FromDisambiguation] — the 'from' location needs user clarification
     * - [JourneyPlanResult.ToDisambiguation]   — the 'to' location needs user clarification
     * - [JourneyPlanResult.NoResults]          — valid request but no bus routes exist
     * - [JourneyPlanResult.Error]              — network or unexpected API failure
     */
    suspend fun planJourney(from: String, to: String): JourneyPlanResult
}
