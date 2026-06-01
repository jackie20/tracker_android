package com.tracker.busjourney.domain.usecase

import com.tracker.busjourney.domain.model.Journey
import com.tracker.busjourney.domain.model.JourneyLeg
import com.tracker.busjourney.domain.model.JourneyPlanResult
import com.tracker.busjourney.domain.model.StopPoint
import com.tracker.busjourney.domain.repository.JourneyRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Unit tests for [PlanJourneyUseCase].
 *
 * Demonstrates testing a public use case API from outside its package —
 * the test imports from `com.tracker.busjourney.domain.usecase` while
 * residing in the test source set.
 */
class PlanJourneyUseCaseTest {

    private lateinit var repository: JourneyRepository
    private lateinit var useCase: PlanJourneyUseCase

    @Before
    fun setUp() {
        repository = mock()
        useCase = PlanJourneyUseCase(repository)
    }

    @Test
    fun `returns journeys when repository returns successful plan`() = runTest {
        val journeys = listOf(
            Journey(
                duration = 25,
                legs = listOf(
                    JourneyLeg(
                        lineId = "24",
                        lineName = "24",
                        fromName = "Victoria",
                        toName = "Euston",
                        duration = 25,
                        summary = "Take bus 24 towards Hampstead Heath",
                    )
                )
            )
        )
        whenever(repository.planJourney("Victoria", "Euston"))
            .thenReturn(JourneyPlanResult.Journeys(journeys))

        val result = useCase("Victoria", "Euston")

        assertTrue(result is JourneyPlanResult.Journeys)
        assertEquals(journeys, (result as JourneyPlanResult.Journeys).journeys)
    }

    @Test
    fun `returns FromDisambiguation when origin is ambiguous`() = runTest {
        val options = listOf(
            StopPoint("490014869K", "Victoria Station", 51.496, -0.144),
            StopPoint("490G00004444", "Victoria Bus Station", 51.495, -0.143),
        )
        whenever(repository.planJourney("Victoria", "Euston"))
            .thenReturn(JourneyPlanResult.FromDisambiguation("Victoria", options))

        val result = useCase("Victoria", "Euston")

        assertTrue(result is JourneyPlanResult.FromDisambiguation)
        assertEquals(2, (result as JourneyPlanResult.FromDisambiguation).options.size)
    }

    @Test
    fun `returns error when from query is blank`() = runTest {
        val result = useCase("", "Euston")

        assertTrue(result is JourneyPlanResult.Error)
        // Repository should NOT be called for invalid input
        verify(repository, org.mockito.kotlin.never()).planJourney(
            org.mockito.kotlin.any(),
            org.mockito.kotlin.any()
        )
    }

    @Test
    fun `returns error when to query is blank`() = runTest {
        val result = useCase("Victoria", "   ")

        assertTrue(result is JourneyPlanResult.Error)
    }

    @Test
    fun `trims whitespace before delegating to repository`() = runTest {
        whenever(repository.planJourney("Victoria", "Euston"))
            .thenReturn(JourneyPlanResult.NoResults())

        useCase("  Victoria  ", "  Euston  ")

        verify(repository).planJourney("Victoria", "Euston")
    }

    @Test
    fun `propagates repository error`() = runTest {
        whenever(repository.planJourney("Victoria", "Euston"))
            .thenReturn(JourneyPlanResult.Error("Network error"))

        val result = useCase("Victoria", "Euston")

        assertTrue(result is JourneyPlanResult.Error)
        assertEquals("Network error", (result as JourneyPlanResult.Error).message)
    }
}
