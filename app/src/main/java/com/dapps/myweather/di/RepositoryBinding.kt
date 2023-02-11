package com.dapps.myweather.di

import com.dapps.myweather.network.ForecastRepositoryImp
import com.dapps.myweather.network.ForecastRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryBinding {


    @Binds
    abstract fun bindRepository(repositoryImp: ForecastRepositoryImp): ForecastRepository
}