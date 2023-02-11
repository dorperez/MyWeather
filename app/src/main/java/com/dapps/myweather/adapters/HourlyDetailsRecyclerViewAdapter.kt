package com.dapps.myweather.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dapps.myweather.R
import com.dapps.myweather.databinding.HourlyRecyclerViewRowBinding
import com.dapps.myweather.utils.*

class HourlyDetailsRecyclerViewAdapter: RecyclerView.Adapter<HourlyDetailsRecyclerViewAdapter.HourlyDetailsRecyclerViewHolder>() {

    private var hourlyDataList = mutableListOf<Map<Int, Any>>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): HourlyDetailsRecyclerViewHolder {
        val root = HourlyRecyclerViewRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return HourlyDetailsRecyclerViewHolder(root)
    }

    override fun onBindViewHolder(
        holder: HourlyDetailsRecyclerViewAdapter.HourlyDetailsRecyclerViewHolder,
        position: Int
    ) {
        holder.bindForecastRow(hourlyDataList[position])
    }

    override fun getItemCount(): Int {
        return hourlyDataList.size
    }

    inner class HourlyDetailsRecyclerViewHolder(private val rootView: HourlyRecyclerViewRowBinding): RecyclerView.ViewHolder(rootView.root){

        fun bindForecastRow(hourlyData: Map<Int, Any>){

            val dateTime = replaceTinDateTime(hourlyData.getOrDefault(0,rootView.root.context.getString(R.string.not_found)).toString())
            val visibility = getMetersToKilometers(hourlyData.getOrDefault(1,rootView.root.context.getString(R.string.not_found)).toString().toDouble()).toString()
            val temperature = getTemperatureFormatted(rootView.root.context,hourlyData.getOrDefault(2,rootView.root.context.getString(R.string.not_found)).toString())
            val humidity = getHumidityFormatted(hourlyData.getOrDefault(3,rootView.root.context.getString(R.string.not_found)).toString())
            val windSpeed = getWindSpeedFormatted(rootView.root.context,hourlyData.getOrDefault(4,rootView.root.context.getString(R.string.not_found)).toString())
            val weatherCode = hourlyData.getOrDefault(5,rootView.root.context.getString(R.string.not_found)).toString().toInt()

            rootView.hourlyWeatherCodeTitle.text = getWeatherCodeImage(rootView.root.context,weatherCode).first
            rootView.hourlyWeatherCodeImageView.setImageResource(getWeatherCodeImage(rootView.root.context,weatherCode).second)

            rootView.hourlyDateTimeTextView.text = getTimeOnlyFromDate(rootView.root.context,dateTime)
            rootView.hourlyVisibilityTextView.text = visibility
            rootView.hourlyTempTextView.text = temperature
            rootView.hourlyHumidityTextView.text = humidity
            rootView.hourlyWindSpeedTextView.text = windSpeed

        }
    }

    fun setNewList(newList: MutableList<Map<Int, Any>>) {
        hourlyDataList = newList
        notifyDataSetChanged()
    }
}
