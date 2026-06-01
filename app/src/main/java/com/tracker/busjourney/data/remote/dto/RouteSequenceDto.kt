package com.tracker.busjourney.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RouteSequenceDto(
    @SerializedName("lineId") val lineId: String?,
    @SerializedName("direction") val direction: String?,
    @SerializedName("stopPointSequences") val stopPointSequences: List<StopPointSequenceDto>?,
)

data class StopPointSequenceDto(
    @SerializedName("branchId") val branchId: Int?,
    @SerializedName("direction") val direction: String?,
    @SerializedName("stopPoint") val stopPoint: List<RouteStopDto>?,
)

data class RouteStopDto(
    @SerializedName("id") val id: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lon") val lon: Double?,
    /** Position within this branch's sequence (1-based). */
    @SerializedName("sequenceNumber") val sequenceNumber: Int?,
)
