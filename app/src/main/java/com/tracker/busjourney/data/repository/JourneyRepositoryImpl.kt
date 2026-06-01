package com.tracker.busjourney.data.repository

import com.google.gson.Gson
import com.tracker.busjourney.data.mapper.JourneyMapper
import com.tracker.busjourney.data.mapper.StopPointMapper
import com.tracker.busjourney.data.remote.api.TflApiService
import com.tracker.busjourney.data.remote.dto.JourneyResultDto
import com.tracker.busjourney.domain.model.JourneyPlanResult
import com.tracker.busjourney.domain.model.StopPoint
import com.tracker.busjourney.domain.repository.JourneyRepository
import java.io.IOException

class JourneyRepositoryImpl(
    private val apiService: TflApiService,
    private val gson: Gson,
    private val stopPointMapper: StopPointMapper,
    private val journeyMapper: JourneyMapper,
) : JourneyRepository {

    override suspend fun searchStopPoints(query: String): Result<List<StopPoint>> = runCatching {
        val response = apiService.searchStopPoints(query)
        response.matches?.let(stopPointMapper::toDomainList) ?: emptyList()
    }

    override suspend fun planJourney(from: String, to: String): JourneyPlanResult {
        return try {
            val response = apiService.planJourney(from, to)

            when {
                response.isSuccessful -> parseSuccessBody(response.body(), from, to)

                // TfL uses HTTP 300 to signal that the location is ambiguous.
                // The error body contains disambiguation options.
                response.code() == 300 -> {
                    val disambiguationDto = parseErrorBody(response.errorBody()?.string())
                    parseDisambiguationDto(disambiguationDto, from, to)
                }

                else -> JourneyPlanResult.Error(
                    "API error ${response.code()}: ${response.message()}"
                )
            }
        } catch (e: IOException) {
            JourneyPlanResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            JourneyPlanResult.Error("Unexpected error: ${e.message}")
        }
    }

    private fun parseSuccessBody(body: JourneyResultDto?, from: String, to: String): JourneyPlanResult {
        if (body == null) return JourneyPlanResult.Error("Empty response body")

        if (body.journeys.isNullOrEmpty()) {
            val disambiguation = parseDisambiguationDto(body, from, to)
            if (disambiguation !is JourneyPlanResult.Error) return disambiguation
            return JourneyPlanResult.NoResults()
        }

        val journeys = journeyMapper.toDomainList(body.journeys)
        return if (journeys.isEmpty()) JourneyPlanResult.NoResults()
        else JourneyPlanResult.Journeys(journeys)
    }

    private fun parseDisambiguationDto(dto: JourneyResultDto?, from: String, to: String): JourneyPlanResult {
        if (dto == null) return JourneyPlanResult.Error("Could not parse disambiguation response")

        val fromOptions = dto.fromDisambiguation
            ?.options
            ?.mapNotNull(stopPointMapper::disambiguationOptionToDomain)

        if (!fromOptions.isNullOrEmpty()) {
            return JourneyPlanResult.FromDisambiguation(query = from, options = fromOptions)
        }

        val toOptions = dto.toDisambiguation
            ?.options
            ?.mapNotNull(stopPointMapper::disambiguationOptionToDomain)

        if (!toOptions.isNullOrEmpty()) {
            return JourneyPlanResult.ToDisambiguation(query = to, options = toOptions)
        }

        return JourneyPlanResult.Error("Disambiguation response contained no options")
    }

    private fun parseErrorBody(json: String?): JourneyResultDto? {
        if (json.isNullOrBlank()) return null
        return try {
            gson.fromJson(json, JourneyResultDto::class.java)
        } catch (_: Exception) {
            null
        }
    }
}
