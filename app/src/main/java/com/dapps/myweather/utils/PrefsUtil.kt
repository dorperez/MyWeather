package com.dapps.myweather.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dapps.myweather.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

private val startingLocationsLatLng = listOf(
    LatLng(32.08, 34.78),
    LatLng(40.71, -74.00),
    LatLng( 25.20, 55.27),
    LatLng(48.85, 2.35),
)

//--------- Fragment --------

fun Fragment.isUserFirstLaunch(): Boolean {
    val prefs = requireActivity().getSharedPreferences(ConstUtil.Prefs.USER,MODE_PRIVATE)
    return prefs.getBoolean(ConstUtil.Prefs.IS_FIRST_LAUNCH,true)
}

fun Fragment.setUserFirstValues(){
    val prefs = requireActivity().getSharedPreferences(ConstUtil.Prefs.USER,MODE_PRIVATE).edit()
    prefs.putBoolean(ConstUtil.Prefs.IS_FIRST_LAUNCH,false)
    prefs.putString(ConstUtil.Prefs.TEMPERATURE_TYPE,ConstUtil.Temperature.CELSIUS)
    prefs.putString(ConstUtil.Prefs.LOCATION_LIST, objectToGson(startingLocationsLatLng))
    prefs.apply()
}

fun getUserTemperaturePref(context: Context): String? {
    val prefs = context.getSharedPreferences(ConstUtil.Prefs.USER,MODE_PRIVATE)
    return prefs.getString(ConstUtil.Prefs.TEMPERATURE_TYPE, ConstUtil.Temperature.CELSIUS)
}


fun Fragment.getUserLocationList(): MutableList<LatLng> {
    val prefs = requireActivity().getSharedPreferences(ConstUtil.Prefs.USER,MODE_PRIVATE)
    val locationsGson = prefs.getString(ConstUtil.Prefs.LOCATION_LIST,"")
    return Gson().fromJson(locationsGson, object : TypeToken<List<LatLng>>() {}.type)
}

fun Fragment.removeUserLocation(locationToRemove: Int) {
    val list = getUserLocationList()
    list.removeAt(locationToRemove)
    saveNewLocationList(list)
}

fun Fragment.addLocationToList(locationLatLng: LatLng){
    val latLngListFromPrefs: MutableList<LatLng> = getUserLocationList()
    latLngListFromPrefs.add(LatLng(locationLatLng.latitude,locationLatLng.longitude))
    saveNewLocationList(latLngListFromPrefs)
}

fun Fragment.saveNewLocationList(newLocationList: MutableList<LatLng>) {
    val prefs = requireActivity().getSharedPreferences(ConstUtil.Prefs.USER,MODE_PRIVATE).edit()
    val locationsGson = Gson().toJson(newLocationList)
    prefs.putString(ConstUtil.Prefs.LOCATION_LIST,locationsGson)
    prefs.apply()
}

//--------- AppCompatActivity --------

fun AppCompatActivity.setUserTemperaturePref(newTempType: String) {
    val prefs = getSharedPreferences(ConstUtil.Prefs.USER,MODE_PRIVATE).edit()
    prefs.putString(ConstUtil.Prefs.TEMPERATURE_TYPE, newTempType).apply()
}

fun AppCompatActivity.getUserLocationList(): MutableList<LatLng> {
    val prefs = getSharedPreferences(ConstUtil.Prefs.USER,MODE_PRIVATE)
    val locationsGson = prefs.getString(ConstUtil.Prefs.LOCATION_LIST,"NONE")
    return Gson().fromJson(locationsGson, object : TypeToken<List<LatLng>>() {}.type)
}
