package com.dapps.myweather.network

import com.dapps.myweather.model.ForecastFilters
import com.dapps.myweather.model.ForecastResponse
import com.dapps.myweather.model.LatLng
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiRequest {

    @GET("forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current_weather") boolean: Boolean = true,
        @Query("hourly") hourlyList: List<String> = listOf("temperature_2m","windspeed_10m","relativehumidity_2m","visibility","weathercode"),
        @Query("timezone") auto: String = "auto"
    ): Response<ForecastResponse>

}