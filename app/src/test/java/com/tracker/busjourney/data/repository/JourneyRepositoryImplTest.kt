package com.tracker.busjourney.data.repository

import com.google.gson.Gson
import com.tracker.busjourney.data.mapper.JourneyMapper
import com.tracker.busjourney.data.mapper.StopPointMapper
import com.tracker.busjourney.data.remote.api.TflApiService
import com.tracker.busjourney.data.remote.dto.DisambiguationDto
import com.tracker.busjourney.data.remote.dto.DisambiguationOptionDto
import com.tracker.busjourney.data.remote.dto.DisambiguationPlaceDto
import com.tracker.busjourney.data.remote.dto.JourneyDto
import com.tracker.busjourney.data.remote.dto.JourneyResultDto
import com.tracker.busjourney.data.remote.dto.LegDto
import com.tracker.busjourney.data.remote.dto.LineIdentifierDto
import com.tracker.busjourney.data.remote.dto.ModeDto
import com.tracker.busjourney.data.remote.dto.RouteOptionDto
import com.tracker.busjourney.domain.model.JourneyPlanResult
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response

class JourneyRepositoryImplTest {

    private lateinit var apiService: TflApiService
    private lateinit var repository: JourneyRepositoryImpl
    private val gson = Gson()

    @Before
    fun setUp() {
        apiService = mock()
        repository = JourneyRepositoryImpl(
            apiService = apiService,
            gson = gson,
            stopPointMapper = StopPointMapper(),
            journeyMapper = JourneyMapper(),
        )
    }

    @Test
    fun `planJourney returns Journeys on HTTP 200 with bus legs`() = runTest {
        val dto = JourneyResultDto(
            journeys = listOf(
                JourneyDto(
                    duration = 25,
                    legs = listOf(
                        LegDto(
                            duration = 25,
                            instruction = null,
                            departurePoint = null,
                            arrivalPoint = null,
                            mode = ModeDto(id = "bus", name = "bus"),
                            routeOptions = listOf(
                                RouteOptionDto(
                                    name = "24",
                                    lineIdentifier = LineIdentifierDto(id = "24", name = "24")
                                )
                            )
                        )
                    )
                )
            ),
            fromDisambiguation = null,
            toDisambiguation = null,
        )
        whenever(apiService.planJourney("Victoria", "Euston"))
            .thenReturn(Response.success(dto))

        val result = repository.planJourney("Victoria", "Euston")

        assertTrue("Expected Journeys but got $result", result is JourneyPlanResult.Journeys)
        assertTrue((result as JourneyPlanResult.Journeys).journeys.isNotEmpty())
    }

    @Test
    fun `planJourney returns NoResults when journey list is empty`() = runTest {
        val dto = JourneyResultDto(
            journeys = emptyList(),
            fromDisambiguation = null,
            toDisambiguation = null,
        )
        whenever(apiService.planJourney("Victoria", "Euston"))
            .thenReturn(Response.success(dto))

        val result = repository.planJourney("Victoria", "Euston")

        assertTrue(result is JourneyPlanResult.NoResults)
    }

    @Test
    fun `planJourney returns FromDisambiguation on HTTP 300 with from options`() = runTest {
        val disambiguationDto = JourneyResultDto(
            journeys = null,
            fromDisambiguation = DisambiguationDto(
                matchStatus = "ambiguous",
                options = listOf(
                    DisambiguationOptionDto(
                        parameterValue = "490001A",
                        place = DisambiguationPlaceDto(
                            id = "490001A",
                            commonName = "Victoria Station",
                            lat = 51.496,
                            lon = -0.144,
                        )
                    )
                )
            ),
            toDisambiguation = null,
        )
        val errorJson = gson.toJson(disambiguationDto)

        // Retrofit's Response.error() rejects HTTP codes < 400, and Response.success()
        // rejects codes outside 200-299. Mocking the Response directly is the only way
        // to simulate a legitimate HTTP 300 response in unit tests.
        val mockResponse = mock<Response<JourneyResultDto>>()
        whenever(mockResponse.isSuccessful).thenReturn(false)
        whenever(mockResponse.code()).thenReturn(300)
        whenever(mockResponse.errorBody()).thenReturn(
            errorJson.toResponseBody("application/json".toMediaType())
        )

        whenever(apiService.planJourney("Victoria", "Euston"))
            .thenReturn(mockResponse)

        val result = repository.planJourney("Victoria", "Euston")

        assertTrue(result is JourneyPlanResult.FromDisambiguation)
        assertEquals(1, (result as JourneyPlanResult.FromDisambiguation).options.size)
    }

    @Test
    fun `planJourney returns Error on HTTP 500`() = runTest {
        val errorBody = "Internal server error"
            .toResponseBody("text/plain".toMediaType())

        whenever(apiService.planJourney("Victoria", "Euston"))
            .thenReturn(Response.error(500, errorBody))

        val result = repository.planJourney("Victoria", "Euston")

        assertTrue(result is JourneyPlanResult.Error)
    }
}
