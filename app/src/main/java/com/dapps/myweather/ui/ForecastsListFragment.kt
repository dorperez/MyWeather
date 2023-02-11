package com.dapps.myweather.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.dapps.myweather.R
import com.dapps.myweather.adapters.ForecastsRecyclerViewAdapter
import com.dapps.myweather.databinding.FragmentForecastsListBinding
import com.dapps.myweather.model.ForecastResponse
import com.dapps.myweather.model.LatLng
import com.dapps.myweather.utils.*
import com.dapps.myweather.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ForecastsListFragment : Fragment() {

    //Base
    private lateinit var rootView: FragmentForecastsListBinding
    private val sharedViewModel: SharedViewModel by activityViewModels()

    //Adapter
    private lateinit var forecastListRecyclerViewAdapter: ForecastsRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Handle First Launch
        handleFirstLaunchPrefs()
        observeAddLocation()
    }

    override fun onResume() {
        super.onResume()
        if (!isInternetOn()){
            toastSnack(rootView.forecastListFragmentLayout,getString(R.string.please_check_your_internet_connection))
        }else{
            sharedViewModel.getForecastFromLocationList(getUserLocationList())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = FragmentForecastsListBinding.inflate(layoutInflater)

        //Settings Recycler And Click Listeners
        setForecastsRecyclerView()

        return rootView.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Observe Locations List Response
        observeListResponse()
    }

    private fun handleFirstLaunchPrefs() {
        if (isUserFirstLaunch()) {
            setUserFirstValues()
        }
    }

    private fun observeAddLocation() {
        lifecycleScope.launch(Dispatchers.Main) {
            //Add New Location LiveData
            sharedViewModel.newForecastToAdd.collect {
                when (it) {
                    is MyResponse.ERROR -> {
                        toastSnack(
                            rootView.forecastListFragmentLayout,
                            "Error: ${it.errorCode}, ${it.errorMessage}"
                        )
                    }
                    is MyResponse.EXCEPTION -> {
                        toastSnack(
                            rootView.forecastListFragmentLayout,
                            "Error: ${it.exceptionMessage}"
                        )
                    }
                    MyResponse.LOADING -> {

                    }
                    is MyResponse.SUCCESS -> {
                        toastSnack(
                            rootView.forecastListFragmentLayout,
                            "The location added successfully"
                        )
                        forecastListRecyclerViewAdapter.addSingleForecastToList(it.data)
                        addLocationToList(LatLng(it.data.latitude, it.data.longitude))
                    }
                }
            }
        }
    }

    private fun observeListResponse() {
        lifecycleScope.launchWhenCreated {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                //Location Forecast List LiveData
                sharedViewModel.forecastListResponse.observe(viewLifecycleOwner) {
                    if (it != null && it.isNotEmpty()) {
                        forecastListRecyclerViewAdapter.setNewList(it)
                        if (!sharedViewModel.getIsSlidingAnimationShowed()) {
                            startRecyclerViewAnimation()
                            sharedViewModel.hideLoadingAnimation()
                        }
                    }
                }
                //Observe Temp Unit Changed livedata
                sharedViewModel.tempUnitChanged.observe(viewLifecycleOwner) {
                    forecastListRecyclerViewAdapter.refreshList()
                }
            }
        }
    }

    private fun startRecyclerViewAnimation() {

        sharedViewModel.setIsSlidingAnimationShowed(true)

        val controller = LayoutAnimationController(
            AnimationUtils.loadAnimation(context, R.anim.recycler_view_slide_in_animation)
        )
        controller.order = LayoutAnimationController.ORDER_NORMAL
        controller.delay = 0.5f

        rootView.forecastsRecyclerView.layoutAnimation = controller
    }

    private fun setForecastsRecyclerView() {
        forecastListRecyclerViewAdapter = ForecastsRecyclerViewAdapter()
        sharedViewModel.showLoadingAnimation()

        rootView.forecastsRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false)
            adapter = forecastListRecyclerViewAdapter

        }

        //RecyclerView Click Listeners
        forecastListRecyclerViewAdapter.setOnForecastClickListeners(object :
            ForecastsRecyclerViewAdapter.OnClickForecast {
            override fun setOnClickForecast(
                forecast: ForecastResponse,
                addressName: String?,
                view: View
            ) {
                moveToLocationDetails(
                    view,
                    forecast,
                    LatLng(forecast.latitude, forecast.longitude),
                    addressName
                )
            }

            override fun setOnLongClickForecast(
                forecast: ForecastResponse,
                addressName: String?,
                position: Int
            ) {
                //Options Dialog
                showForecastOptionDialog(position, addressName)
            }
        })
    }

    private fun showForecastOptionDialog(
        position: Int,
        addressName: String?
    ) {

        val removeForecastDialog = Dialog(requireActivity())
        removeForecastDialog.setContentView(R.layout.remove_forecast_dialog_layout)
        removeForecastDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val deleteButton = removeForecastDialog.findViewById<Button>(R.id.deleteDialogDeleteButton)
        val cancelButton = removeForecastDialog.findViewById<Button>(R.id.deleteDialogCancelButton)
        val locationNameTextView =
            removeForecastDialog.findViewById<TextView>(R.id.deleteDialogLocationNameTextView)

        locationNameTextView.text = addressName

        deleteButton.setOnClickListener {
            removeForecastDialog.dismiss()
            removeUserLocation(position)
            forecastListRecyclerViewAdapter.removeAt(position)
        }

        cancelButton.setOnClickListener {
            removeForecastDialog.dismiss()
        }

        removeForecastDialog.show()

    }

    private fun moveToLocationDetails(
        view: View,
        forecast: ForecastResponse,
        placeLatLng: LatLng,
        addressName: String?
    ) {

        val bundle = Bundle().apply {
            putString(ConstUtil.ListToDetailsBundle.PLACE_DETAILS, objectToGson(forecast))
            putString(ConstUtil.ListToDetailsBundle.PLACE_LAT_LNG, objectToGson(placeLatLng))
            putString(ConstUtil.ListToDetailsBundle.PLACE_ADDRESS, addressName)
        }

        val extras = FragmentNavigatorExtras(view to ConstUtil.ListToDetailsBundle.DETAILS_TRANSITION_NAME)

        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build()

        if (addressName != null) {
            toastSnack(rootView.forecastListFragmentLayout,addressName)
        }

        findNavController().navigate(
            R.id.action_forecastsFragment_to_detailsFragment,
            bundle,
            navOptions,
            extras
        )

    }

}