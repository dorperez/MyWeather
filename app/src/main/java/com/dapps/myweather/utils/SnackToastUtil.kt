package com.dapps.myweather.utils

import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.dapps.myweather.R
import com.google.android.material.snackbar.Snackbar

fun Snackbar.snackToast(): Snackbar{

    // SnackBar Style
    view.setBackgroundColor(context.getColor(R.color.main_color))
    val snackBarTextView = view.findViewById(com.google.android.material.R.id.snackbar_text) as TextView
    snackBarTextView.gravity = Gravity.CENTER_HORIZONTAL
    snackBarTextView.textAlignment = View.TEXT_ALIGNMENT_CENTER
    snackBarTextView.textSize = 14f
    snackBarTextView.maxLines = 3
    snackBarTextView.setTextColor(context.getColor(R.color.on_main_color))

    val viewParams = view.layoutParams as FrameLayout.LayoutParams
    viewParams.setMargins(50,0,50,50)
    view.layoutParams = viewParams

    return this
}

fun toastSnack(mainLayout: View, text:String) {
    return Snackbar.make(mainLayout, text, Snackbar.LENGTH_LONG).snackToast().show()
}