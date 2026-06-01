package com.tracker.busjourney.domain.usecase

import com.tracker.busjourney.domain.model.JourneyPlanResult
import com.tracker.busjourney.domain.repository.JourneyRepository
import javax.inject.Inject

class PlanJourneyUseCase @Inject constructor(
    private val repository: JourneyRepository,
) {
    suspend operator fun invoke(from: String, to: String): JourneyPlanResult {
        if (from.isBlank() || to.isBlank()) {
            return JourneyPlanResult.Error("Origin and destination cannot be empty.")
        }
        return repository.planJourney(from.trim(), to.trim())
    }
}
