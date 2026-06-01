package com.tracker.busjourney.domain.usecase

import com.tracker.busjourney.domain.model.StopPoint
import com.tracker.busjourney.domain.repository.JourneyRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for [SearchStopPointsUseCase].
 *
 * These tests access the use case from the test package — demonstrating
 * cross-package public API testing as required by the assignment.
 */
class SearchStopPointsUseCaseTest {

    private lateinit var repository: JourneyRepository
    private lateinit var useCase: SearchStopPointsUseCase

    @Before
    fun setUp() {
        repository = mock()
        useCase = SearchStopPointsUseCase(repository)
    }

    @Test
    fun `returns stop points when query matches`() = runTest {
        val expected = listOf(
            StopPoint("490014869K", "Victoria Station", 51.496, -0.144),
        )
        whenever(repository.searchStopPoints("Victoria"))
            .thenReturn(Result.success(expected))

        val result = useCase("Victoria")

        assertTrue(result.isSuccess)
        assertEquals(expected, result.getOrNull())
    }

    @Test
    fun `returns empty list for blank query without calling repository`() = runTest {
        val result = useCase("   ")

        assertTrue(result.isSuccess)
        assertEquals(emptyList<StopPoint>(), result.getOrNull())
        verify(repository, never()).searchStopPoints(org.mockito.kotlin.any())
    }

    @Test
    fun `trims query whitespace before calling repository`() = runTest {
        whenever(repository.searchStopPoints("Victoria"))
            .thenReturn(Result.success(emptyList()))

        useCase("  Victoria  ")

        verify(repository).searchStopPoints("Victoria")
    }

    @Test
    fun `propagates repository failure`() = runTest {
        val exception = RuntimeException("Network unavailable")
        whenever(repository.searchStopPoints("Victoria"))
            .thenReturn(Result.failure(exception))

        val result = useCase("Victoria")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
