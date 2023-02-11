package com.dapps.myweather.adapters

import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dapps.myweather.R
import com.dapps.myweather.databinding.LocationForecastCardviewRowBinding
import com.dapps.myweather.model.ForecastResponse
import com.dapps.myweather.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*


class ForecastsRecyclerViewAdapter :
    RecyclerView.Adapter<ForecastsRecyclerViewAdapter.ForecastViewHolder>() {

    private var forecastsList = mutableListOf<ForecastResponse>()
    private lateinit var clickListener: OnClickForecast

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ForecastViewHolder {
        val view = LocationForecastCardviewRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ForecastViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ForecastsRecyclerViewAdapter.ForecastViewHolder,
        position: Int
    ) {
        holder.bindForecastRow(forecastsList[position])
    }

    override fun getItemCount(): Int {
        return forecastsList.size
    }

    inner class ForecastViewHolder(private val rootView: LocationForecastCardviewRowBinding) :
        RecyclerView.ViewHolder(rootView.root) {
        fun bindForecastRow(forecastResponse: ForecastResponse) {

            val temperature =
                getTemperatureFormatted(rootView.root.context,forecastResponse.current_weather.temperature.toString())

            val windSpeed = getWindSpeedFormatted(
                rootView.root.context,
                forecastResponse.current_weather.windspeed.toString()
            )

            handleAddress(forecastResponse)
            rootView.forecastRowWeatherCodeTitle.text = getWeatherCodeImage(rootView.root.context,forecastResponse.current_weather.weathercode).first
            rootView.forecastRowWeatherCodeImageView.setImageResource(getWeatherCodeImage(rootView.root.context,forecastResponse.current_weather.weathercode).second)
            rootView.forecastRowCardBackgroundImageView.setImageResource(getTimeOfDayImage(forecastResponse.current_weather.time))
            rootView.forecastRowTimeTextView.text = getTimeAndDateFromLatLng(forecastResponse)
            rootView.forecastTempRowTextView.text = temperature
            rootView.forecastRowWindSpeedTextView.text = windSpeed

            //Transition Name
            rootView.forecastRowCardLayout.transitionName = "card_$adapterPosition"

        }

        private fun handleAddress(forecastResponse: ForecastResponse) {
            var address = rootView.root.context.getString(R.string.loading)

            CoroutineScope(Dispatchers.Main).launch {
                Geocoder(rootView.root.context, Locale.getDefault())
                    .getAddress(
                        forecastResponse.latitude,
                        forecastResponse.longitude
                    ) { addressHolder: android.location.Address? ->
                        if (addressHolder != null) {
                            if (addressHolder.locality != null) {
                                address = addressHolder.locality.toString()
                                setCityNameTextView(address)
                            } else if (addressHolder.subAdminArea != null) {
                                address = addressHolder.subAdminArea.toString()
                                setCityNameTextView(address)
                            } else if (addressHolder.adminArea != null) {
                                address = addressHolder.adminArea.toString()
                                setCityNameTextView(address)
                            } else {
                                address = rootView.root.context.getString(R.string.not_found)
                                setCityNameTextView(rootView.root.context.getString(R.string.not_found))
                            }
                        } else {
                            address = rootView.root.context.getString(R.string.not_found)
                            setCityNameTextView(rootView.root.context.getString(R.string.not_found))
                        }
                    }
            }

            rootView.forecastRowLayout.setOnClickListener {
                clickListener.setOnClickForecast(
                    forecastResponse,
                    address,
                    rootView.forecastRowCardLayout
                )
            }

            rootView.forecastRowLayout.setOnLongClickListener {
                clickListener.setOnLongClickForecast(forecastResponse, address, adapterPosition)
                true
            }

        }

        private fun setCityNameTextView(address: String) {
            rootView.forecastRowCityTextView.post {
                rootView.forecastRowCityTextView.isSelected = true
                rootView.forecastRowCityTextView.text = address
            }
        }

    }

    fun addSingleForecastToList(newForecast: ForecastResponse) {
        forecastsList.add(newForecast)
        notifyItemInserted(forecastsList.size)
    }

    interface OnClickForecast {
        fun setOnClickForecast(forecast: ForecastResponse, addressName: String?, view: View)
        fun setOnLongClickForecast(forecast: ForecastResponse, addressName: String?, position: Int)
    }

    fun setOnForecastClickListeners(listener: OnClickForecast) {
        clickListener = listener
    }

    fun setNewList(newList: List<ForecastResponse>) {
        forecastsList = newList as MutableList<ForecastResponse>
        notifyDataSetChanged()
    }

    fun removeAt(position: Int) {
        forecastsList.removeAt(position)
        notifyItemRemoved(position)
    }

    fun refreshList() {
//        val listHolder = forecastsList
//        forecastsList.clear()
//        forecastsList = listHolder
        notifyDataSetChanged()
    }
}