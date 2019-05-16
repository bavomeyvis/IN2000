package com.example.pollution.classes

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

data class Location(
    var cityName: String?,
    var timeValues : ArrayList<String?>?,
    var aqiValues: ArrayList<Double?>?,
    var pm25Values : ArrayList<Double?>?,
    var pm10Values : ArrayList<Double?>?,
    var no2Values : ArrayList<Double?>?,
    var o3Values : ArrayList<Double?>?
)
// I give up.