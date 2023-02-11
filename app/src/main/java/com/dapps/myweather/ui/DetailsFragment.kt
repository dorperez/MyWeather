package com.dapps.myweather.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.TransitionInflater
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.dapps.myweather.R
import com.dapps.myweather.adapters.HourlyDetailsRecyclerViewAdapter
import com.dapps.myweather.databinding.FragmentDetailsBinding
import com.dapps.myweather.model.ForecastResponse
import com.dapps.myweather.model.LatLng
import com.dapps.myweather.utils.*
import com.dapps.myweather.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailsFragment : Fragment() {

    //Base
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var rootView: FragmentDetailsBinding

    //Hourly recyclerView adapter
    private lateinit var hourlyDetailsRecyclerViewAdapter: HourlyDetailsRecyclerViewAdapter

    //Update every 10min handler
    private val updateDetailsHandler = Handler(Looper.getMainLooper())
    private lateinit var updateDetailsRunnable: Runnable

    //Place Details
    private var placeDetails: ForecastResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(requireContext()).inflateTransition(android.R.transition.move)
    }

    override fun onResume() {
        super.onResume()
        if (!isInternetOn()){
            toastSnack(rootView.fragmentDetailsLayout,getString(R.string.please_check_your_internet_connection))
        }
    }
    override fun onDetach() {
        super.onDetach()
        stopUpdateDetailsHandler()
    }

    private fun stopUpdateDetailsHandler() {
        updateDetailsHandler.removeCallbacks(updateDetailsRunnable)
        updateDetailsHandler.removeCallbacksAndMessages(null)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = FragmentDetailsBinding.inflate(layoutInflater)
        getDataFromArguments()
        return rootView.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHourlyDetailsRecyclerView()
        setObservers()
        setClickListener()

    }

    private fun setUpdateAfterTenMinutes(placeLatLng: LatLng?) {

        updateDetailsRunnable = object : Runnable {
            override fun run() {
                if (placeLatLng != null) {
                    if (isInternetOn()){
                        sharedViewModel.getSingleLocationToDetails(placeLatLng)
                        return
                    }
                    updateDetailsHandler.postDelayed(this, ConstUtil.TimeToUpdate.UPDATE_TIME)
                }
            }
        }

        updateDetailsHandler.postDelayed(updateDetailsRunnable,ConstUtil.TimeToUpdate.UPDATE_TIME)
    }

    private fun setClickListener() {
        //Google Maps Listener
        rootView.firstcastDetailsGoogleMapImageView.setOnClickListener {
           val placeDetails = sharedViewModel.getPlaceDetails()
            val placeUri = "http://maps.google.com/maps?q=loc:${placeDetails?.latitude},${placeDetails?.longitude}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(placeUri))
            startActivity(intent)
        }
    }

    private fun setObservers() {
        lifecycleScope.launchWhenCreated {
            repeatOnLifecycle(Lifecycle.State.CREATED) {

                //Observe temp unit change
                sharedViewModel.tempUnitChanged.observe(viewLifecycleOwner) {
                    rootView.forecastTempTextView.text = placeDetails?.current_weather?.temperature?.let { it1 ->
                        getTemperatureFormatted(requireActivity(), it1.toString())
                    }
                }

                //Location updated details
                sharedViewModel.locationDetails.observe(viewLifecycleOwner, Observer {
                    when (it) {
                        is MyResponse.ERROR -> {
                            toastSnack(
                                rootView.fragmentDetailsLayout,
                                "Error: ${it.errorCode}, ${it.errorMessage}"
                            )
                        }
                        is MyResponse.EXCEPTION -> {
                            toastSnack(
                                rootView.fragmentDetailsLayout,
                                "Error: ${it.exceptionMessage}"
                            )
                        }
                        MyResponse.LOADING -> {

                        }
                        is MyResponse.SUCCESS -> {
                            setPlaceDetailsOnViews(it.data)
                            sharedViewModel.setPlaceDetailsDetails(it.data)
                            hourlyDetailsRecyclerViewAdapter.setNewList(sharedViewModel.formatToMyHourly(it.data.hourly))
                        }
                    }
                })
            }
        }
    }

    private fun setHourlyDetailsRecyclerView() {
        hourlyDetailsRecyclerViewAdapter = HourlyDetailsRecyclerViewAdapter()
        rootView.forecastDetailsHourlyRecyclerView.apply {
            //layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
            layoutManager = GridLayoutManager(requireActivity(),2)
            adapter = hourlyDetailsRecyclerViewAdapter
        }
    }

    private fun getDataFromArguments() {
        //Getting Data
        placeDetails = objectFromGson(arguments?.getString("placeDetails"), ForecastResponse::class.java)
        val placeLatLng = objectFromGson(arguments?.getString("placeLatLng"), LatLng::class.java)
        val placeAddress = arguments?.getString("placeAddress")

        //Setting city name
        rootView.forecastCityTextView.text = placeAddress

        if (placeLatLng!= null){
            if (isInternetOn()){
                sharedViewModel.getSingleLocationToDetails(placeLatLng)
            }
            rootView.forecastLatLngTextView.text = getLatLngFormatted(placeLatLng)
        }else{
            rootView.forecastLatLngTextView.text = getString(R.string.not_found)
        }

        setPlaceDetailsOnViews(placeDetails)
        setUpdateAfterTenMinutes(placeLatLng)
    }

    private fun setPlaceDetailsOnViews(placeDetails: ForecastResponse?) {

        if (placeDetails != null) {

            //Visibility
            rootView.forecastDetailsVisibilityTextView.text = getWindSpeedFormatted(requireActivity(),
                getMetersToKilometers(
                    placeDetails.hourly.visibility[0].toString().toDouble()
                ).toString()
            )

            //Temperature
            rootView.forecastTempTextView.text =
                getTemperatureFormatted(requireActivity(),placeDetails.current_weather.temperature.toString())

            //Wind Speed
            rootView.forecastDetailsWindSpeed.text =
                getWindSpeedFormatted(requireActivity(),placeDetails.current_weather.windspeed.toString())

            //Humidity
            rootView.forecastDetailsHumidityTextView.text =
                getHumidityFormatted(placeDetails.hourly.relativehumidity_2m[1].toString())

            //Day image
            rootView.forecastCardBackgroundImageView.setImageResource(getTimeOfDayImage(placeDetails.current_weather.time))

            //Time
            rootView.forecastTimeTextView.text = getTimeAndDateFromLatLng(placeDetails)

            //Focusing City textview for auto scroll if name is to long
            rootView.forecastCityTextView.isSelected = true

            //Weather image code handler
            rootView.forecastWeatherCodeImageView.setImageResource(getWeatherCodeImage(rootView.root.context,placeDetails.current_weather.weathercode).second)
            rootView.forecastWeatherCodeTitle.text = getWeatherCodeImage(rootView.root.context,placeDetails.current_weather.weathercode).first

        } else {
            //Set as not found
            rootView.forecastDetailsVisibilityTextView.text = getString(R.string.not_found)
            rootView.forecastTempTextView.text = getString(R.string.not_found)
            rootView.forecastDetailsWindSpeed.text = getString(R.string.not_found)
            rootView.forecastDetailsHumidityTextView.text = getString(R.string.not_found)
            rootView.forecastTimeTextView.text = getString(R.string.not_found)
            rootView.forecastCardBackgroundImageView.setImageResource(R.drawable.morning_image)
        }
    }


}