package com.dapps.myweather.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.dapps.myweather.R
import com.dapps.myweather.databinding.ActivityMainBinding
import com.dapps.myweather.model.LatLng
import com.dapps.myweather.utils.*
import com.dapps.myweather.viewmodel.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    //Base
    private lateinit var rootView: ActivityMainBinding
    private val sharedViewModel: SharedViewModel by viewModels()

    //Nav controller
    private lateinit var navController: NavController

    //Delay Handler for update list after x minutes
    private val delayHandler = Handler(Looper.getMainLooper())

    //currentFragment For ToolBar
    private var currentFragment = ConstUtil.Fragments.LIST_FRAGMENT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootView = ActivityMainBinding.inflate(layoutInflater)
        setContentView(rootView.root)

        updateListEveryTenMinutes()
        setNavController()
        setToolBar()
        setupTempSwitcher()
        setObservers()
    }

    override fun onPause() {
        super.onPause()
        delayHandler.removeCallbacks(updateListRunnable)
        delayHandler.removeCallbacksAndMessages(null)
    }

    private val updateListRunnable = object : Runnable {
        override fun run() {
            sharedViewModel.getForecastFromLocationList(getUserLocationList())
            delayHandler.postDelayed(this, ConstUtil.TimeToUpdate.UPDATE_TIME)
        }
    }

    private fun updateListEveryTenMinutes() {
        delayHandler.postDelayed(updateListRunnable,ConstUtil.TimeToUpdate.UPDATE_TIME)
    }

    private fun setNavController() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.forecastNavigationContainer) as NavHostFragment
        navController = navHostFragment.navController
    }

    private fun setObservers() {

        //Launch Animation Observer
        sharedViewModel.forecastListLoadingAnimation.observe(this, Observer {
            if(it){
                rootView.forecastListLottiLoadingAnimationLayout.visibility = View.VISIBLE
                rootView.forecastListToolbar.visibility = View.GONE
            }else{
                rootView.forecastListLottiLoadingAnimationLayout.visibility = View.GONE
                rootView.forecastListToolbar.visibility = View.VISIBLE
            }
        })
    }

    private fun setToolBar() {

        //Destination change listener
        navController.addOnDestinationChangedListener { controller, destination, arguments ->
            currentFragment = destination.label.toString()
            when (currentFragment) {
                ConstUtil.Fragments.LIST_FRAGMENT -> {
                    rootView.toolBarAddAndReturnButton.setImageResource(R.drawable.add_button_icon)
                }
                ConstUtil.Fragments.DETAILS_FRAGMENT -> {
                    rootView.toolBarAddAndReturnButton.setImageResource(R.drawable.arrow_back_icon)
                }
            }
        }

        //Tool bar add / return button listener
        rootView.toolBarAddAndReturnButton.setOnClickListener {
            when(currentFragment){
                ConstUtil.Fragments.LIST_FRAGMENT -> {
                    showAddForecastDialog()
                }
                ConstUtil.Fragments.DETAILS_FRAGMENT -> {
                    navController.popBackStack()
                }
            }
        }
    }

    private fun setupTempSwitcher() {

        val currentTempType = getUserTemperaturePref(this)
        if (currentTempType == ConstUtil.Temperature.FAHRENHEIT){
            rootView.temperatureSwitcher.isChecked = true
        }

        rootView.temperatureSwitcher.setOnCheckedChangeListener { buttonView, isChecked ->
            //Handle temp change
            if (isChecked){
                setUserTemperaturePref(ConstUtil.Temperature.FAHRENHEIT)
            }else{
                setUserTemperaturePref(ConstUtil.Temperature.CELSIUS)
            }
            sharedViewModel.refreshRecyclerView()
        }

    }

    private fun showAddForecastDialog() {

        if (!isInternetOn()){
            toastSnack(rootView.mainActivityLayout,getString(R.string.please_check_your_internet_connection))
            return
        }

        //Add Forecast Dialog
        val addForecastDialog = Dialog(this)
        addForecastDialog.setContentView(R.layout.add_forecast_dialog_layout)
        addForecastDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        addForecastDialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_PANEL)
        addForecastDialog.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)

        //Dialog EditTexts
        val latitudeEditText =
            addForecastDialog.findViewById<EditText>(R.id.addForecastDialogLatitudeEditText)
        val longitudeEditText =
            addForecastDialog.findViewById<EditText>(R.id.addForecastDialogLongitudeEditText)

        //Dialog Buttons
        val addButton = addForecastDialog.findViewById<Button>(R.id.addForecastDialogAddButton)
        val cancelButton =
            addForecastDialog.findViewById<Button>(R.id.addForecastDialogCancelButton)

        //Dialog Add button
        addButton.setOnClickListener {

            if (latitudeEditText.text.toString().isEmpty()) {
                Toast.makeText(
                    this,
                    getString(R.string.latitude_cant_be_empty),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (longitudeEditText.text.toString().isEmpty()) {
                Toast.makeText(
                    this,
                    getString(R.string.longitude_cant_be_empty),
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val latitude = latitudeEditText.text.toString().toDouble()
            val longitude = longitudeEditText.text.toString().toDouble()

            if (sharedViewModel.isValidLatLng(latitude, longitude)) {
                addForecastDialog.dismiss()
                sharedViewModel.getSingleLocationToAdd(LatLng(latitude, longitude))
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.please_make_sure_you_entered_the_right_lat_lng),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        //Dialog Cancel Button
        cancelButton.setOnClickListener {
            addForecastDialog.dismiss()
        }

        addForecastDialog.show()
    }

}