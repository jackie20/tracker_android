package com.tracker.busjourney.domain.usecase

import com.tracker.busjourney.domain.model.RouteStop
import com.tracker.busjourney.domain.repository.TrackerRepository
import javax.inject.Inject

class GetRouteSequenceUseCase @Inject constructor(
    private val repository: TrackerRepository,
) {
    suspend operator fun invoke(lineId: String): Result<List<RouteStop>> =
        repository.getRouteSequence(lineId)
}
