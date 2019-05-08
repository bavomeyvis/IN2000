package com.example.pollution.classes

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class Storby(cityName: String, coordinates: LatLng, cityMarker: Marker) {
    val name = cityName
    val coordinates = coordinates
    val marker = cityMarker
}