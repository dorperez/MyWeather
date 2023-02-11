package com.dapps.myweather.model

data class Hourly(
    val temperature_2m: List<Any>,
    val relativehumidity_2m: List<Any>,
    val time: List<String>,
    val visibility: List<Any>,
    val windspeed_10m: List<Any>,
    val weathercode: List<Int>
)