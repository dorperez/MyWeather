package com.dapps.myweather.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dapps.myweather.model.ForecastResponse
import com.dapps.myweather.model.Hourly
import com.dapps.myweather.model.LatLng
import com.dapps.myweather.network.ForecastRepositoryImp
import com.dapps.myweather.utils.MyResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SharedViewModel @Inject constructor(private val foreCastRepositoryImp: ForecastRepositoryImp) :
    ViewModel() {

    //Forecast list liveData
    private var _forecastListResponse = MutableLiveData<List<ForecastResponse>>()
    val forecastListResponse: LiveData<List<ForecastResponse>> = _forecastListResponse

    //Single forecast livedata
    //Add new location
    private var _newForecastToAdd = MutableStateFlow<MyResponse<ForecastResponse>>(MyResponse.LOADING)
    val newForecastToAdd: StateFlow<MyResponse<ForecastResponse>> = _newForecastToAdd.asStateFlow()

    //Get location details liveData
    private var _locationDetails = MutableLiveData<MyResponse<ForecastResponse>>()
    val locationDetails: LiveData<MyResponse<ForecastResponse>> = _locationDetails

    //Forecast temp unit changed liveData
    private var _tempUnitChanged = MutableLiveData<Boolean>()
    val tempUnitChanged: LiveData<Boolean> = _tempUnitChanged

    //Forecast temp unit changed liveData
    private var _forecastListLoadingAnimation = MutableLiveData<Boolean>()
    val forecastListLoadingAnimation: LiveData<Boolean> = _forecastListLoadingAnimation
    private var isLoaded = false

    //RecyclerView Slide In Animation
    private var isSlidingAnimationShowed = false

    //LocationDetails
    private var placeDetails: ForecastResponse? = null


    //Sliding Recycler Animation
    fun setIsSlidingAnimationShowed(value: Boolean) {
        isSlidingAnimationShowed = value
    }
    fun getIsSlidingAnimationShowed(): Boolean {
        return isSlidingAnimationShowed
    }

    //Refresh RecyclerView
    fun refreshRecyclerView() {
        _tempUnitChanged.postValue(true)
    }

    //Loading Animation
    fun showLoadingAnimation() {
        if (!isLoaded){
            _forecastListLoadingAnimation.postValue(true)
            isLoaded = true
        }
    }

    fun hideLoadingAnimation() {
        _forecastListLoadingAnimation.postValue(false)
    }

    //Place Details
    fun setPlaceDetailsDetails(placeDetailsHolder: ForecastResponse?) {
        placeDetails = placeDetailsHolder
    }

    fun getPlaceDetails(): ForecastResponse? {
        return placeDetails
    }

    //Get Forecasts From Location List
    fun getForecastFromLocationList(locationList: List<LatLng>) {
        try {
        viewModelScope.launch(Dispatchers.IO) {
            val deferredResponses = locationList.map {
                async {
                    foreCastRepositoryImp.getForecastForLocation(
                        it.latitude,
                        it.longitude
                    )
                }
            }

            val responses = deferredResponses.map { it.await() }
            val successResponses = responses.filterIsInstance<MyResponse.SUCCESS<ForecastResponse>>()
            //val errorResponses = responses.filterIsInstance<MyResponse.ERROR>()
            //val exceptionResponses = responses.filterIsInstance<MyResponse.EXCEPTION>()

            val forecastResponses = successResponses.map { it.data }

            _forecastListResponse.postValue(forecastResponses)
        }
        }catch (e: IOException){
            e.printStackTrace()
        }
    }

    //Get Single Location Forecast
    fun getSingleLocationToAdd(locationLatLng: LatLng) {
        viewModelScope.launch(Dispatchers.IO) {
            val response = foreCastRepositoryImp.getForecastForLocation(
                locationLatLng.latitude,
                locationLatLng.longitude
            )

            when (response) {
                is MyResponse.ERROR -> {
                    _newForecastToAdd.emit(
                        MyResponse.ERROR(
                            response.errorCode,
                            response.errorMessage
                        )
                    )
                }
                is MyResponse.EXCEPTION -> {
                    _newForecastToAdd.emit(MyResponse.EXCEPTION(response.exceptionMessage))
                }
                is MyResponse.SUCCESS -> {
                    _newForecastToAdd.emit(MyResponse.SUCCESS(response.data))
                }
                else -> {}
            }
        }
    }

    fun getSingleLocationToDetails(locationLatLng: LatLng) {

        viewModelScope.launch(Dispatchers.IO) {
            val response = foreCastRepositoryImp.getForecastForLocation(
                locationLatLng.latitude,
                locationLatLng.longitude
            )

            when (response) {
                is MyResponse.ERROR -> {

                    _locationDetails.postValue(
                        MyResponse.ERROR(
                            response.errorCode,
                            response.errorMessage
                        )
                    )

                }
                is MyResponse.EXCEPTION -> {
                    _locationDetails.postValue(MyResponse.EXCEPTION(response.exceptionMessage))

                }
                is MyResponse.SUCCESS -> {
                    _locationDetails.postValue(MyResponse.SUCCESS(response.data))
                }
                else -> {}
            }
        }
    }

    fun isValidLatLng(lat: Double, lng: Double): Boolean {
        if (lat < -90 || lat > 90) {
            return false
        } else if (lng < -180 || lng > 180) {
            return false
        }
        return true
    }

    fun formatToMyHourly(hourly: Hourly): MutableList<Map<Int, Any>> {
        val mapList = hourly.time.mapIndexed { index, time ->
            mapOf(0 to time,
                1 to hourly.visibility[index],
                2 to hourly.temperature_2m[index],
                3 to hourly.relativehumidity_2m[index],
                4 to hourly.windspeed_10m[index],
                5 to hourly.weathercode[index])
        }.toMutableList()

        return mapList
    }

}