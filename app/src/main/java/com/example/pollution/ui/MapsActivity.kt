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
import com.example.pollution.classes.Storby
import com.example.pollution.data.APIData
import com.example.pollution.response.WeatherService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*

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

    //list of Storby class objects containing name, coordinates and the marker for each large city
    public var storbyList = arrayListOf<Storby>()

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
                //var weather: APIData? = getData(point.latitude, point.longitude)
                //println(weather)

                //gets coordinates from touch event on map in point.lat and point.lon
                //println(" ")
                //println("CLICK LATIDUDE: " + point.latitude)
                //println("CLICK LONGITUDE: " + point.longitude)
                //gets air quality data from met using the lat lon from the touch event
                //send this to activity showing aqi
                //getData(point.latitude, point.longitude)

                //Thread.sleep(5000)

                //val aqi = weather?.data?.time?.get(0)?.variables?.aQI?.value
                //println(aqi)

                //println(getPositionData(point.latitude, point.longitude))

                //TEST
                for (storby in storbyList) {
                    println(storby.name)
                }
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

    //creates and adds markers for large cities on the googleMap
    //Does an api request for the LatLng of the marker and gives it an appropriate colour
    private fun addCityMarkers(mMap: GoogleMap) {

        //store all LatLng to large cities in Norway to add markers
        val oslo = LatLng(59.915780, 10.752913)
        val osloName: String = "Oslo"
        val bergen = LatLng(60.393975, 5.324937)
        val bergenName: String = "Bergen"
        val trondheim = LatLng(63.433465, 10.395516)
        val trondheimName: String = "Trondheim"
        val stavanger = LatLng(58.979964, 5.729269)
        val stavangerName: String = "Stavanger"
        val sandvika = LatLng(59.891695, 10.528088)
        val sandvikaName: String = "Sandvika"
        val kristiansand = LatLng(58.162897, 8.018848)
        val kristiansandName: String = "Kristiansand"
        val fredrikstad = LatLng(59.224392, 10.933630)
        val fredrikstadName: String = "Fredrikstad"
        val tromso = LatLng(69.653412, 18.953360)
        val tromsoName: String = "Tromsø"
        val drammen = LatLng(59.747642, 10.205377)
        val drammenName: String = "Drammen"
        val sandnes = LatLng(58.852107, 5.732697)
        val sandnesName: String = "Sandnes"
        val skien = LatLng(58.852107, 5.732697)
        val skienName: String = "Skien"
        val sarpsborg = LatLng(59.286260, 11.109056)
        val sarpsborgName: String = "Sarpsborg"
        val bodo = LatLng(67.282654, 14.404968)
        val bodoName: String = "Bodø"
        val larvik = LatLng(59.056636, 10.02887)
        val larvikName: String = "Larvik"
        val sandefjord = LatLng(59.056636, 10.028874)
        val sandefjordName: String = "Sandefjord"
        val lillestrom = LatLng(59.956639, 11.050240)
        val lillestromName: String = "Lillestrøm"
        val arendal = LatLng(58.463660, 8.772121)
        val arendalName: String = "Arendal"
        val alesund = LatLng(62.476929, 6.149429)
        val alesundName: String = "Ålesund"

        var cityCoordList = listOf(oslo, bergen, trondheim, stavanger, sandvika,
            kristiansand, fredrikstad, tromso, drammen, sandnes, skien, sarpsborg,
            bodo, larvik, sandefjord, lillestrom, arendal, alesund)

        var cityNameList = listOf(osloName, bergenName, trondheimName, stavangerName, sandvikaName,
            kristiansandName, fredrikstadName, tromsoName, drammenName, sandnesName, skienName, sarpsborgName,
            bodoName, larvikName, sandefjordName, lillestromName, arendalName, alesundName)

        //creating a client to fetch AQI data from api
        val client = Retrofit.Builder()
            .baseUrl("https://in2000-apiproxy.ifi.uio.no/weatherapi/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)

        //for each city's coordinates in list over large cities' coordinates
        //we check the air quality index for the coordinates, and add a marker coloured
        //with a colour indicating the returned index
        //All markers are added to the global list cityMarkers after creation

        for (i in cityCoordList.indices) {
        //for (city in cityCoordList) {
            doAsync {
                lateinit var marker: Marker
                val weather = client.getWeather(cityCoordList.get(i).latitude, cityCoordList.get(i).longitude).execute().body()
                val aqi = weather?.data?.time?.get(0)?.variables?.aQI?.value
                //println(aqi)
                runOnUiThread {
                    //checks if aqi returned value is null
                    if (aqi == null) {
                        marker = mMap
                            .addMarker(MarkerOptions()
                                .position(cityCoordList.get(i))
                                .title(cityNameList.get(i)))
                        val storby = Storby(cityNameList.get(i), cityCoordList.get(i), marker)
                        storbyList.add(storby)
                    } else if (aqi < 1.5) {
                        marker = mMap
                            .addMarker(MarkerOptions()
                                .position(cityCoordList.get(i))
                                .title(cityNameList.get(i))
                                .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory
                                        .HUE_GREEN)))
                        val storby = Storby(cityNameList.get(i), cityCoordList.get(i), marker)
                        storbyList.add(storby)
                    } else if (aqi < 2.0 && aqi > 1.5) {
                        marker = mMap
                            .addMarker(MarkerOptions()
                                .position(cityCoordList.get(i))
                                .title(cityNameList.get(i))
                                .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory
                                        .HUE_YELLOW)))
                        val storby = Storby(cityNameList.get(i), cityCoordList.get(i), marker)
                        storbyList.add(storby)
                    } else if (aqi < 2.5 && aqi > 2.0) {
                        marker = mMap
                            .addMarker(MarkerOptions()
                                .position(cityCoordList.get(i))
                                .title(cityNameList.get(i))
                                .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory
                                        .HUE_ORANGE)))
                        val storby = Storby(cityNameList.get(i), cityCoordList.get(i), marker)
                        storbyList.add(storby)
                    } else if (aqi > 2.5) {
                        marker = mMap
                            .addMarker(MarkerOptions()
                                .position(cityCoordList.get(i))
                                .title(cityNameList.get(i))
                                .icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory
                                        .HUE_RED)))
                        val storby = Storby(cityNameList.get(i), cityCoordList.get(i), marker)
                        storbyList.add(storby)
                    }
                }
            }
        }
    }
}
