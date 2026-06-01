package com.tracker.busjourney.di

import com.google.gson.Gson
import com.tracker.busjourney.data.mapper.ArrivalMapper
import com.tracker.busjourney.data.mapper.JourneyMapper
import com.tracker.busjourney.data.mapper.RouteSequenceMapper
import com.tracker.busjourney.data.mapper.StopPointMapper
import com.tracker.busjourney.data.remote.api.TflApiService
import com.tracker.busjourney.data.repository.JourneyRepositoryImpl
import com.tracker.busjourney.data.repository.TrackerRepositoryImpl
import com.tracker.busjourney.domain.repository.JourneyRepository
import com.tracker.busjourney.domain.repository.TrackerRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideStopPointMapper(): StopPointMapper = StopPointMapper()

    @Provides
    @Singleton
    fun provideJourneyMapper(): JourneyMapper = JourneyMapper()

    @Provides
    @Singleton
    fun provideArrivalMapper(): ArrivalMapper = ArrivalMapper()

    @Provides
    @Singleton
    fun provideRouteSequenceMapper(): RouteSequenceMapper = RouteSequenceMapper()

    @Provides
    @Singleton
    fun provideJourneyRepository(
        apiService: TflApiService,
        gson: Gson,
        stopPointMapper: StopPointMapper,
        journeyMapper: JourneyMapper,
    ): JourneyRepository = JourneyRepositoryImpl(apiService, gson, stopPointMapper, journeyMapper)

    @Provides
    @Singleton
    fun provideTrackerRepository(
        apiService: TflApiService,
        arrivalMapper: ArrivalMapper,
        routeSequenceMapper: RouteSequenceMapper,
    ): TrackerRepository = TrackerRepositoryImpl(apiService, arrivalMapper, routeSequenceMapper)
}
