package com.dapps.myweather.utils

import android.content.Context
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.dapps.myweather.R
import com.dapps.myweather.model.ForecastResponse
import com.dapps.myweather.model.LatLng
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.*

fun objectToGson(anyObject: Any): String? {
    return Gson().toJson(anyObject)
}

fun <T> objectFromGson(anyObject: String?, objectClass: Class<T>): T? {
    return Gson().fromJson(anyObject, objectClass)
}

fun getWindSpeedFormatted(context: Context, string: String): String {
    return "$string " + context.getString(
        R.string.km
    )

}

fun getHumidityFormatted(string: String): CharSequence {
    return "$string%"
}

fun getTemperatureFormatted(context:Context,tempInC: String): String {
    var currentTemp = tempInC.toDouble()
    when(getUserTemperaturePref(context)){
        ConstUtil.Temperature.FAHRENHEIT -> {
            currentTemp = celsiusToFahrenheit(currentTemp)
        }
    }

    val roundedNumber = "%.2f".format(currentTemp)

    return if (roundedNumber.startsWith("-")) {
        roundedNumber.reversed()
    } else {
        "$roundedNumber°"
    }
}


fun celsiusToFahrenheit(celsius: Double): Double {
    return (celsius * 9 / 5) + 32
}

fun getTimeOnlyFromDate(context: Context,string: String): String {
    val parts = string.split(" ")
    val time = parts[1]
    val amPm = getAmPm(context,time)
    return "$time $amPm"
}

fun getMetersToKilometers(meters: Double): Double {
    return meters / 1000
}

fun getLatLngFormatted(latLng: LatLng): String {
    return "${latLng.latitude} / ${latLng.longitude}"
}

fun replaceTinDateTime(forecastTimeHolder: String): String {
    return forecastTimeHolder.replace('T', ' ')
}

fun getWeatherCodeImage(context:Context,number: Int): Pair<String, Int> {
    val text: String
    val image: Int
    when (number) {
        0 -> {
            text = context.getString(R.string.clear_weather)
            image = R.drawable.clear_icon
        }
        in 1..3 -> {
            text = context.getString(R.string.partly_clouded)
            image = R.drawable.cloudy_icon
        }
        in 45.. 48 -> {
            text = context.getString(R.string.fog)
            image = R.drawable.foggy_icon
        }
        in 51..55 -> {
            text = context.getString(R.string.light_drizzle)
            image = R.drawable.rainy_icon
        }
        in 56..57 -> {
            text = context.getString(R.string.freezing_drizzle)
            image = R.drawable.rainy_icon
        }
        in 61..65 -> {
            text = context.getString(R.string.slightly_rain)
            image = R.drawable.rainy_icon
        }
        in 66..67 -> {
            text = context.getString(R.string.freezing_rain)
            image = R.drawable.rainy_icon
        }
        in 71..77 -> {
            text = context.getString(R.string.snow)
            image = R.drawable.snow_icon
        }
        in 80..82 -> {
            text = context.getString(R.string.rain_showers)
            image = R.drawable.rainy_icon
        }
        in 85..86 -> {
            text = context.getString(R.string.snow_showers)
            image = R.drawable.snow_icon
        }
        in 95..99 -> {
            text = context.getString(R.string.thunderstorm)
            image = R.drawable.storm_icon
        }
        else -> {
            text = context.getString(R.string.not_found)
            image = R.drawable.clear_icon
        }
    }
    return Pair(text, image)
}

fun getAmPm(context: Context, input: String): String {
    val parts = input.split(":")

    return when (parts[0].toInt()) {
        in 5..11 -> {
            context.getString(R.string.morning)
        }
        in 12..16 -> {
            context.getString(R.string.noon)
        }
        in 17..20 -> {
            context.getString(R.string.evening)
        }
        else -> {
            context.getString(R.string.night)
        }
    }

}

fun Fragment.isInternetOn() : Boolean {

    val connectivityManager = requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities =  connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

    capabilities.also {
        if (it != null){
            if (it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                return true
            else if (it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)){
                return true
            }
        }
    }
    return false
}

fun AppCompatActivity.isInternetOn() : Boolean {

    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities =  connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

    capabilities.also {
        if (it != null){
            if (it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI))
                return true
            else if (it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)){
                return true
            }
        }
    }
    return false
}


@Suppress("DEPRECATION")
fun Geocoder.getAddress(
    latitude: Double,
    longitude: Double,
    address: (android.location.Address?) -> Unit
) {

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getFromLocation(latitude, longitude, 1) { address(it.firstOrNull()) }
        return
    }

    try {
        address(getFromLocation(latitude, longitude, 1)?.firstOrNull())
    } catch (e: Exception) {
        address(null)
    }
}

fun getTimeAndDateFromLatLng(placeTimeDetails: ForecastResponse): String {

    val longitudeAsTimeZone = (placeTimeDetails.longitude / 15).toInt()
    val timeZone = TimeZone.getTimeZone(placeTimeDetails.timezone_abbreviation)
    val offset = placeTimeDetails.utc_offset_seconds / (1000 * 60) + longitudeAsTimeZone * 60

    timeZone.rawOffset = (offset * 60 * 1000)

    val calendar = Calendar.getInstance(timeZone)

    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)
    val time = "$hour:%02d".format(minute)

    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val month = calendar.get(Calendar.MONTH) + 1
    val year = calendar.get(Calendar.YEAR)
    val date = "$day/$month/$year"

    return "$date $time"
}

fun getTimeOfDayImage(forecastTimeHolder: String): Int {

    val forecastDataTime = replaceTinDateTime(forecastTimeHolder)

    val simpleDataFormat =
        SimpleDateFormat(ConstUtil.Forecast.FORECAST_TIME_FORMAT, Locale.getDefault())
    val date: Date? = simpleDataFormat.parse(forecastDataTime)

    val c = Calendar.getInstance()
    if (date != null) {
        c.time = date

        return when (c[Calendar.HOUR_OF_DAY]) {
            in 5..16 -> {
                R.drawable.morning_image
            }
            in 17..20 -> {
                R.drawable.sunset_image
            }
            else -> {
                R.drawable.night_image
            }
        }
    }
    return R.drawable.morning_image
}