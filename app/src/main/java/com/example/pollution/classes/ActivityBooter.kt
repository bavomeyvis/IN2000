package com.example.pollution.classes

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v4.content.ContextCompat.startActivity
import com.example.pollution.ui.*
import com.google.android.gms.maps.model.LatLng
import java.security.AccessController.getContext

class ActivityBooter(context: Context) {
    val context: Context = context

    fun runForecastActivity(lat: Double, lon: Double, title: String) {
        val forecastActivity = Intent(context, ForecastActivity::class.java).apply {
            putExtra("lat", lat)
            putExtra("lon", lon)
            putExtra("cityTitle", title)
        }
        context.startActivity(forecastActivity)
    }

    fun runGraphActivity(lat: Double, lon: Double, title: String) {
        val graphActivity = Intent(context, GraphActivity::class.java).apply {
            putExtra("lat", lat)
            putExtra("lon", lon)
            putExtra("cityTitle", title)
        }
        context.startActivity(graphActivity)
    }

    fun runStatsActivity(coordinates: HashMap<String, LatLng>) {
        val statsActivity = Intent(context, StatsActivity::class.java).putExtra("hashMap", coordinates)
        context.startActivity(statsActivity)
        //recreate()
    }


}