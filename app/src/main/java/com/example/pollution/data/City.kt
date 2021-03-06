package com.example.pollution.data

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

data class City(
    val cityName: String,
    val coordinates: LatLng,
    val cityMarker: Marker
)