package com.dapps.myweather.network

import com.dapps.myweather.model.ForecastResponse
import com.dapps.myweather.utils.MyResponse
import retrofit2.Response

interface ForecastRepository {

    suspend fun getForecastForLocation(latitude: Double, longitude: Double): MyResponse<ForecastResponse>
}