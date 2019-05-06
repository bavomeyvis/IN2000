
package com.example.pollution.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NotificationBuilderWithBuilderAccessor
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
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
import com.example.pollution.data.Location
import com.example.pollution.response.WeatherService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import kotlinx.android.synthetic.main.activity_maps.*

// Async imports
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread

// Retrofit imports
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

private const val TAG = "MapsActivity"

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: android.location.Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val user_limit: Double? = null // User-inputted value. If current location's air quality goes below user_limit, the app alerts the user.

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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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
        mMap.addMarker(MarkerOptions().position(oslo).title(getString(R.string.marker_oslo)))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(oslo, 8.0f))
        setUpMap()
    }

    fun getData(lat: Double, lon: Double) {
        doAsync {
            val client = Retrofit.Builder()
                .baseUrl("https://in2000-apiproxy.ifi.uio.no/weatherapi/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherService::class.java)

            val weather = client.getWeather(lat, lon).execute().body()
            println(weather)
        }
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

    private fun setUpMap() { // The purpose of this function is to start the app zoomed in on current location.
        getLocation()
        val currentLatLng = LatLng(lastLocation.latitude, lastLocation.longitude)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
    }

    private fun getLocation(): Location? {
        if (ActivityCompat.checkSelfPermission(this, // First, assure permission is granted.
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return null // Location could not be fetched; access not granted.
        }

        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null)
                lastLocation = location // Update lastLocation.
        }
        return null // Location could not be fetched; something went wrong.
    }

    private fun getLatLon(): DoubleArray { // Return current location's latitude and longitude, in the form of an array with two indexes.
        getLocation() // Call to make sure the location is updated.
        if (lastLocation == null)
            return doubleArrayOf(0.0, 0.0) // Location could not be fetched, return latitude and longitude of 0.0 0.0.
        return doubleArrayOf(lastLocation.latitude, lastLocation.longitude)
    }

    /* A dummy function that illustrate how an alert is sent.
    private fun check() {
        if (getData(getLatLon()[0], getLatLon()[1]) < user_limit)
            dangerAlert()
    }
    */

    // Send the alert.
    private fun dangerAlert() {
        val intent = Intent(this, MapsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder = NotificationCompat.Builder(this, "0") // The builder contains the notification attributes.
            .setSmallIcon(R.drawable.menu_item_alert)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_desc))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(this)) {
            notify(0, builder.build()) // Send the notification with the builder defined above.
        }
    }
}