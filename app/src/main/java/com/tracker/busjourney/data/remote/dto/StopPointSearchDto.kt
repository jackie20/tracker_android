package com.tracker.busjourney.data.remote.dto

import com.google.gson.annotations.SerializedName

data class StopPointSearchResponseDto(
    @SerializedName("matches") val matches: List<StopPointMatchDto>?,
    @SerializedName("total") val total: Int?,
)

data class StopPointMatchDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lon") val lon: Double?,
)
