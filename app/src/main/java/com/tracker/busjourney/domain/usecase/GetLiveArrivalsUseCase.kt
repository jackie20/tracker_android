package com.tracker.busjourney.domain.usecase

import com.tracker.busjourney.domain.model.BusArrival
import com.tracker.busjourney.domain.repository.TrackerRepository
import javax.inject.Inject

class GetLiveArrivalsUseCase @Inject constructor(
    private val repository: TrackerRepository,
) {
    suspend operator fun invoke(lineId: String): Result<List<BusArrival>> =
        repository.getLiveArrivals(lineId)
}
