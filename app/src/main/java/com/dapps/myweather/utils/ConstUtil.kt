package com.dapps.myweather.utils

class ConstUtil {

    object Forecast{
        const val FORECAST_TIME_FORMAT = "yyyy-MM-dd' 'HH:mm"
    }

    object TimeToUpdate{
        const val UPDATE_TIME: Long = 10000 * 10
    }

    object Temperature{
        const val FAHRENHEIT = "F"
        const val CELSIUS = "C"
    }

    object Fragments{
        const val LIST_FRAGMENT = "list_fragment"
        const val DETAILS_FRAGMENT = "details_fragment"
    }

    object Prefs{
        const val USER = "USER"
        const val IS_FIRST_LAUNCH = "isFirstLaunch"
        const val TEMPERATURE_TYPE = "temperatureType"
        const val LOCATION_LIST = "locationList"
    }

    object ListToDetailsBundle{
        const val PLACE_DETAILS = "placeDetails"
        const val PLACE_LAT_LNG = "placeLatLng"
        const val PLACE_ADDRESS = "placeAddress"
        const val DETAILS_TRANSITION_NAME = "big"
    }
}