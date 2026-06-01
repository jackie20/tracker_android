package com.tracker.busjourney.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Top-level response from GET /Journey/JourneyResults/{from}/to/{to}.
 *
 * The API returns HTTP 200 with journeys populated for a successful plan,
 * or HTTP 300 with disambiguation blocks when the location strings are ambiguous.
 * Both are parsed using this DTO — the repository checks which fields are present.
 */
data class JourneyResultDto(
    @SerializedName("journeys") val journeys: List<JourneyDto>?,
    @SerializedName("fromLocationDisambiguation") val fromDisambiguation: DisambiguationDto?,
    @SerializedName("toLocationDisambiguation") val toDisambiguation: DisambiguationDto?,
)

data class JourneyDto(
    @SerializedName("duration") val duration: Int?,
    @SerializedName("legs") val legs: List<LegDto>?,
)

data class LegDto(
    @SerializedName("duration") val duration: Int?,
    @SerializedName("instruction") val instruction: InstructionDto?,
    @SerializedName("departurePoint") val departurePoint: PointDto?,
    @SerializedName("arrivalPoint") val arrivalPoint: PointDto?,
    @SerializedName("mode") val mode: ModeDto?,
    @SerializedName("routeOptions") val routeOptions: List<RouteOptionDto>?,
)

data class InstructionDto(
    @SerializedName("summary") val summary: String?,
)

data class PointDto(
    @SerializedName("commonName") val commonName: String?,
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lon") val lon: Double?,
)

data class ModeDto(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
)

data class RouteOptionDto(
    @SerializedName("name") val name: String?,
    @SerializedName("lineIdentifier") val lineIdentifier: LineIdentifierDto?,
)

data class LineIdentifierDto(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
)

data class DisambiguationDto(
    @SerializedName("matchStatus") val matchStatus: String?,
    @SerializedName("disambiguationOptions") val options: List<DisambiguationOptionDto>?,
)

data class DisambiguationOptionDto(
    @SerializedName("parameterValue") val parameterValue: String?,
    @SerializedName("place") val place: DisambiguationPlaceDto?,
)

data class DisambiguationPlaceDto(
    @SerializedName("id") val id: String?,
    @SerializedName("commonName") val commonName: String?,
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lon") val lon: Double?,
)
