package com.tracker.busjourney.data.remote.api

import com.tracker.busjourney.data.remote.dto.ArrivalDto
import com.tracker.busjourney.data.remote.dto.JourneyResultDto
import com.tracker.busjourney.data.remote.dto.RouteSequenceDto
import com.tracker.busjourney.data.remote.dto.StopPointSearchResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TflApiService {

    @GET("StopPoint/Search/{query}")
    suspend fun searchStopPoints(
        @Path("query") query: String,
        @Query("modes") modes: String = "bus",
    ): StopPointSearchResponseDto

    // Returns Response so the repository can inspect the HTTP status code.
    // HTTP 300 means disambiguation is required; the body still carries option lists.
    @GET("Journey/JourneyResults/{from}/to/{to}")
    suspend fun planJourney(
        @Path("from", encoded = false) from: String,
        @Path("to", encoded = false) to: String,
        @Query("mode") mode: String = "bus",
    ): Response<JourneyResultDto>

    @GET("Line/{lineId}/Arrivals")
    suspend fun getArrivals(
        @Path("lineId") lineId: String,
    ): List<ArrivalDto>

    @GET("Line/{lineId}/Route/Sequence/outbound")
    suspend fun getRouteSequence(
        @Path("lineId") lineId: String,
    ): RouteSequenceDto
}
