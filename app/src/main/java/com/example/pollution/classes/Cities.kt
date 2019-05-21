package com.example.pollution.classes

import android.content.Context
import android.location.Geocoder
import com.example.pollution.data.City
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import java.io.IOException
import java.util.*
import kotlin.collections.HashMap

class Cities(context: Context) {

    val context: Context = context
    var cities = arrayListOf<City>()
    val coordinates : HashMap<String, LatLng> = hashMapOf(
        "Oslo" to LatLng(59.915780, 10.752913), "Bergen" to LatLng(60.393975, 5.324937),
        "Trondheim" to LatLng(63.433465, 10.395516), "Stavanger" to LatLng(58.9486344, 5.6102977),
        "Sandvika" to LatLng(59.891695, 10.528088), "Kristiansand" to LatLng(58.162897, 8.018848),
        "Fredrikstad" to LatLng(59.224392, 10.933630), "Tromsø" to LatLng(69.653412, 18.953360),
        "Drammen" to LatLng(59.747642, 10.205377), "Sandnes" to LatLng(58.852107, 5.732697),
        "Skien" to LatLng(58.852107, 5.732697), "Sarpsborg" to LatLng(59.286260, 11.109056),
        "Bodø" to LatLng(67.282654, 14.404968), "Larvik" to LatLng(59.056636, 10.02887),
        "Sandefjord" to LatLng(59.056636, 10.028874), "Lillestrøm" to LatLng(59.956639, 11.050240),
        "Arendal" to LatLng(58.463660, 8.772121), "Ålesund" to LatLng(62.476929, 6.149429)
    )

    fun addCity(city: City) {
        cities.add(city)
    }

    fun getCity(marker: Marker): City? {
        var returnCity: City? = null
        for (city in cities) {
            if (city.cityName.equals(marker.title)) {
                returnCity = city
            }
        }
        return returnCity
    }

    fun getCity(name: String): City? {
        var returnCity: City? = null
        for (city in cities) {
            if (city.cityName.equals(name)) {
                returnCity = city
            }
        }
        return returnCity
    }

    fun getCity(latlng: LatLng): City? {
        var returnCity: City? = null
        for (city in cities) {
            if (city.coordinates.latitude == latlng.latitude && city.coordinates.longitude == latlng.longitude) {
                returnCity = city
            }
        }
        return returnCity
    }

    fun getPositionData(lat: Double, lon: Double): String {
        var returnInfo: String?
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            returnInfo = addresses.get(0).getAddressLine(0)
        } catch (e: IOException) {
            return ""
        }
        return returnInfo
    }
}