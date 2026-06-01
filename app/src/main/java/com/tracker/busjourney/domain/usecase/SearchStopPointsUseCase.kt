package com.tracker.busjourney.domain.usecase

import com.tracker.busjourney.domain.model.StopPoint
import com.tracker.busjourney.domain.repository.JourneyRepository
import javax.inject.Inject

class SearchStopPointsUseCase @Inject constructor(
    private val repository: JourneyRepository,
) {
    suspend operator fun invoke(query: String): Result<List<StopPoint>> {
        if (query.isBlank()) return Result.success(emptyList())
        return repository.searchStopPoints(query.trim())
    }
}
