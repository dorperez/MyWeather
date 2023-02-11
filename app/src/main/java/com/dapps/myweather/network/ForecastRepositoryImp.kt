package com.dapps.myweather.network

import com.dapps.myweather.model.ForecastResponse
import com.dapps.myweather.utils.MyResponse
import com.dapps.myweather.utils.handleResponse
import retrofit2.Response
import javax.inject.Inject

class ForecastRepositoryImp @Inject constructor(private val apiClient: ApiRequest): ForecastRepository {

    override suspend fun getForecastForLocation(latitude: Double, longitude: Double): MyResponse<ForecastResponse> {
        return handleResponse { apiClient.getForecast(latitude,longitude) }
    }

}