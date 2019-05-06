package com.example.pollution.ui

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Address
import android.location.Geocoder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

// Packages' class imports
import com.example.pollution.R
import com.example.pollution.data.APIData
import com.example.pollution.response.WeatherService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.coroutines.delay

// Async imports
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.doAsyncResult

// Retrofit imports
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "MapsActivity"

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: android.location.Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        //getMapASYNC
        mapFragment.getMapAsync(this)
        getData(59.915780, 10.752913)
        init()
        //fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun init() {
        search_input.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE
                || actionId == EditorInfo.IME_ACTION_SEARCH
                || event.action == KeyEvent.ACTION_DOWN
                || event.action == KeyEvent.KEYCODE_ENTER
            ) {
                searchLocation()
                search_input.setText("")
                closeKeyboard()
                return@setOnEditorActionListener true
            }
            false
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val boundOne = LatLng(57.696784, 3.601294)
        val boundTwo = LatLng(71.214304, 34.476990)

        val builder = LatLngBounds.Builder()
        builder.include(boundOne)
        builder.include(boundTwo)

        val bounds = builder.build()

        mMap.setLatLngBoundsForCameraTarget(bounds)
        // Add a marker in Oslo and move the camera
        val oslo = LatLng(59.915780, 10.752913)
        addCityMarkers(mMap)

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(oslo, 8.0f))
        setUpMap()

        try
        {
            val success = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_normal))
            if (!success)
            {
                println("FAILURE")
            }
        }
        catch (e: Resources.NotFoundException) {
            println("EXCEPTION")
        }

        //mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.mapStyle))

        mMap.setOnMapClickListener(object: GoogleMap.OnMapClickListener {
            override fun onMapClick(point:LatLng) {
                //gets coordinates from touch event on map in point.lat and point.lon
                println(" ")
                println("CLICK LATIDUDE: " + point.latitude)
                println("CLICK LONGITUDE: " + point.longitude)
                //gets air quality data from met using the lat lon from the touch event
                //send this to activity showing aqi
                //getData(point.latitude, point.longitude)

                var weather: APIData? = getData(point.latitude, point.longitude)
                //Thread.sleep(5000)




                val aqi = weather?.data?.time?.get(0)?.variables?.aQI?.value
                println(aqi)

                println(weather)

                println(getPositionData(point.latitude, point.longitude))
            }
        })

        
    }
    fun getPositionData(lat: Double, lon: Double): String {
        lateinit var returnInfo: String
        try {
            var geocoder = Geocoder(this@MapsActivity, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            returnInfo = addresses.get(0).getAddressLine(0)
        } catch (e: IOException) {
            return ""
        }
        return returnInfo
    }

    fun getData(lat: Double, lon: Double): APIData? {
        var weather: APIData? = null
        doAsync {
            val client = Retrofit.Builder()
                .baseUrl("https://in2000-apiproxy.ifi.uio.no/weatherapi/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherService::class.java)

            weather = client.getWeather(lat, lon).execute().body()
            //println(weather)
        }
        return weather
    }

    private fun addMarkerColoured(address: Address) {
        val lat = address.latitude
        val lon = address.longitude

        val client = Retrofit.Builder()
            .baseUrl("https://in2000-apiproxy.ifi.uio.no/weatherapi/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)

        doAsync {
            val weather = client.getWeather(lat, lon).execute().body()
            val aqi = weather?.data?.time?.get(0)?.variables?.aQI?.value
            println(aqi)

            var markerColor = BitmapDescriptorFactory.HUE_RED

            if (aqi != null && aqi < 1.75) {
                markerColor = BitmapDescriptorFactory.HUE_GREEN
            }

            runOnUiThread {
                mMap.addMarker(
                    MarkerOptions()
                        .position(LatLng(lat, lon))
                        .title(address.getAddressLine(0))
                        .icon(BitmapDescriptorFactory.defaultMarker(markerColor))
                )
            }
        }
    }

    fun searchLocation() {
        val searchAddress = search_input.text.toString()
        val geocoder = Geocoder(this)
        var addressList = arrayListOf<Address>()
        try {
            addressList = geocoder.getFromLocationName(searchAddress, 1) as ArrayList<Address>
        } catch (e: IOException) {
            Log.e(TAG, "searchLocation: IOException: " + e.message)
        }
        if (addressList.size > 0) {
            val address = addressList[0]
            val addressLatLng = LatLng(address.latitude, address.longitude)

            addMarkerColoured(address)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(addressLatLng, 15F))
        }
    }

    private fun closeKeyboard() {
        val currentView: View? = this.currentFocus
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentView?.windowToken, 0)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        mMap.isMyLocationEnabled = true

        /* Kanskje dette bare ikke funker på emulator?
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }
        */
    }

    private fun addCityMarkers(mMap: GoogleMap) {
        val oslo = LatLng(59.915780, 10.752913)
        val bergen = LatLng(60.393975, 5.324937)
        val trondheim = LatLng(63.433465, 10.395516)
        val stavanger = LatLng(58.979964, 5.729269)
        val sandvika = LatLng(59.891695, 10.528088)
        val kristiansand = LatLng(58.162897, 8.018848)
        val fredrikstad = LatLng(59.224392, 10.933630)
        val tromso = LatLng(69.653412, 18.953360)
        val drammen = LatLng(59.747642, 10.205377)
        val sandnes = LatLng(58.852107, 5.732697)
        val skien = LatLng(58.852107, 5.732697)
        val sarpsborg = LatLng(59.286260, 11.109056)
        val bodo = LatLng(67.282654, 14.404968)
        val larvik = LatLng(59.056636, 10.028874)
        val sandefjord = LatLng(59.056636, 10.028874)
        val lillestrom = LatLng(59.956639, 11.050240)
        val arendal = LatLng(58.463660, 8.772121)
        val alesund = LatLng(62.476929, 6.149429)

        mMap.addMarker(MarkerOptions().position(oslo).title("OSLO"))
        mMap.addMarker(MarkerOptions().position(bergen).title("BERGEN"))
        mMap.addMarker(MarkerOptions().position(trondheim).title("TRONDHEIM"))
        mMap.addMarker(MarkerOptions().position(stavanger).title("STAVANGER"))
        mMap.addMarker(MarkerOptions().position(sandvika).title("SANDVIKA"))
        mMap.addMarker(MarkerOptions().position(kristiansand).title("KRISTIANSAND"))
        mMap.addMarker(MarkerOptions().position(fredrikstad).title("FREDRIKSTAD"))
        mMap.addMarker(MarkerOptions().position(tromso).title("TROMSØ"))
        mMap.addMarker(MarkerOptions().position(drammen).title("DRAMMEN"))
        mMap.addMarker(MarkerOptions().position(sandnes).title("SANDNES"))
        mMap.addMarker(MarkerOptions().position(skien).title("SKIEN"))
        mMap.addMarker(MarkerOptions().position(sarpsborg).title("SARPSBORG"))
        mMap.addMarker(MarkerOptions().position(bodo).title("BODØ"))
        mMap.addMarker(MarkerOptions().position(larvik).title("LARVIK"))
        mMap.addMarker(MarkerOptions().position(sandefjord).title("SANDEFJORD"))
        mMap.addMarker(MarkerOptions().position(lillestrom).title("LILLESTRØM"))
        mMap.addMarker(MarkerOptions().position(arendal).title("ARENDAL"))
        mMap.addMarker(MarkerOptions().position(alesund).title("ÅLESUND"))
    }
}
