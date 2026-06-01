package com.tracker.busjourney.presentation.tracker

import app.cash.turbine.test
import com.tracker.busjourney.domain.engine.VirtualBusPositionEngine
import com.tracker.busjourney.domain.model.BusArrival
import com.tracker.busjourney.domain.model.RouteStop
import com.tracker.busjourney.domain.usecase.GetLiveArrivalsUseCase
import com.tracker.busjourney.domain.usecase.GetRouteSequenceUseCase
import com.tracker.busjourney.presentation.screens.tracker.TrackerUiState
import com.tracker.busjourney.presentation.screens.tracker.TrackerViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class TrackerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var getArrivalsUseCase: GetLiveArrivalsUseCase
    private lateinit var getRouteSequenceUseCase: GetRouteSequenceUseCase
    private val positionEngine = VirtualBusPositionEngine()
    private lateinit var viewModel: TrackerViewModel

    private val stops = listOf(
        RouteStop("490001A", "Victoria Station", 51.496, -0.144, 1),
        RouteStop("490002B", "Pimlico", 51.489, -0.133, 2),
        RouteStop("490003C", "Euston", 51.528, -0.133, 3),
    )

    private val arrivals = listOf(
        BusArrival("LJ60AKJ", "490002B", "Pimlico", "24", 120, "Hampstead Heath", "outbound"),
        BusArrival("LJ61XYZ", "490003C", "Euston", "24", 300, "Hampstead Heath", "outbound"),
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getArrivalsUseCase = mock()
        getRouteSequenceUseCase = mock()
        viewModel = TrackerViewModel(getArrivalsUseCase, getRouteSequenceUseCase, positionEngine)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Loading`() {
        assertTrue(viewModel.uiState.value is TrackerUiState.Loading)
    }

    @Test
    fun `initialise transitions to LoadingArrivals after route sequence loads`() = runTest {
        whenever(getRouteSequenceUseCase("24")).thenReturn(Result.success(stops))

        viewModel.uiState.test {
            viewModel.initialise("24")
            advanceUntilIdle()

            val states = cancelAndConsumeRemainingEvents()
                .filterIsInstance<app.cash.turbine.Event.Item<TrackerUiState>>()
                .map { it.value }

            assertTrue(states.any { it is TrackerUiState.LoadingArrivals })
        }
    }

    @Test
    fun `Active state is emitted when arrivals are available`() = runTest {
        whenever(getRouteSequenceUseCase("24")).thenReturn(Result.success(stops))
        whenever(getArrivalsUseCase("24")).thenReturn(Result.success(arrivals))

        viewModel.initialise("24")
        advanceUntilIdle()
        viewModel.startPolling("24")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue("Expected Active but got $state", state is TrackerUiState.Active)
        val active = state as TrackerUiState.Active
        assertEquals(2, active.arrivals.size)
        assertEquals(2, active.busPositions.size)
    }

    @Test
    fun `Empty state is emitted when no arrivals exist`() = runTest {
        whenever(getRouteSequenceUseCase("24")).thenReturn(Result.success(stops))
        whenever(getArrivalsUseCase("24")).thenReturn(Result.success(emptyList()))

        viewModel.initialise("24")
        advanceUntilIdle()
        viewModel.startPolling("24")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is TrackerUiState.Empty)
    }

    @Test
    fun `Error state when route sequence fails`() = runTest {
        whenever(getRouteSequenceUseCase("24")).thenReturn(
            Result.failure(RuntimeException("timeout"))
        )

        viewModel.initialise("24")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is TrackerUiState.Error)
    }
}

// ----  VirtualBusPositionEngine unit tests  ----
// Moved here from TrackerViewModelTest after extraction to domain layer.
// The engine is now testable in isolation without any ViewModel scaffolding.

@ExperimentalCoroutinesApi
class VirtualBusPositionEngineTest {

    private val engine = VirtualBusPositionEngine()

    private val stops = listOf(
        RouteStop("490001A", "Victoria Station", 51.496, -0.144, 1),
        RouteStop("490002B", "Pimlico", 51.489, -0.133, 2),
        RouteStop("490003C", "Euston", 51.528, -0.133, 3),
    )

    private val arrivals = listOf(
        BusArrival("LJ60AKJ", "490002B", "Pimlico", "24", 120, "Hampstead Heath", "outbound"),
        BusArrival("LJ61XYZ", "490003C", "Euston", "24", 300, "Hampstead Heath", "outbound"),
    )

    @Test
    fun `derivePositions maps each arrival to the correct stop coordinates`() {
        val positions = engine.derivePositions(arrivals, stops)

        assertEquals(2, positions.size)

        val bus1 = positions.first { it.vehicleId == "LJ60AKJ" }
        assertEquals("490002B", bus1.nextNaptanId)
        assertEquals(51.489, bus1.lat, 0.0001)

        val bus2 = positions.first { it.vehicleId == "LJ61XYZ" }
        assertEquals("490003C", bus2.nextNaptanId)
        assertEquals(51.528, bus2.lat, 0.0001)
    }

    @Test
    fun `derivePositions returns empty list when stops are empty`() {
        val positions = engine.derivePositions(arrivals, emptyList())
        assertTrue(positions.isEmpty())
    }

    @Test
    fun `derivePositions skips arrivals whose naptanId is not in route sequence`() {
        val unknownArrival = BusArrival(
            vehicleId = "UNKNOWN",
            naptanId = "9999ZZZZZ",
            stationName = "Ghost Stop",
            lineId = "24",
            timeToStation = 60,
            towards = "Somewhere",
            direction = "outbound",
        )
        val positions = engine.derivePositions(listOf(unknownArrival), stops)
        assertTrue(positions.isEmpty())
    }

    @Test
    fun `derivePositions deduplicates by vehicleId keeping soonest arrival`() {
        val duplicateArrivals = listOf(
            BusArrival("LJ60AKJ", "490002B", "Pimlico", "24", 120, "", "outbound"),
            BusArrival("LJ60AKJ", "490003C", "Euston", "24", 300, "", "outbound"),
        )
        val positions = engine.derivePositions(duplicateArrivals, stops)

        assertEquals(1, positions.size)
        assertEquals(120, positions.first().timeToStation)
    }

    @Test
    fun `derivePositions returns empty list when arrivals are empty`() {
        val positions = engine.derivePositions(emptyList(), stops)
        assertTrue(positions.isEmpty())
    }
}
