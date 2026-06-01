package com.tracker.busjourney.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ArrivalDto(
    @SerializedName("vehicleId") val vehicleId: String?,
    @SerializedName("naptanId") val naptanId: String?,
    @SerializedName("stationName") val stationName: String?,
    @SerializedName("lineId") val lineId: String?,
    @SerializedName("platformName") val platformName: String?,
    @SerializedName("direction") val direction: String?,
    @SerializedName("destinationName") val destinationName: String?,
    @SerializedName("timeToStation") val timeToStation: Int?,
    @SerializedName("towards") val towards: String?,
)
