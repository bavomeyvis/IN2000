
package com.example.pollution.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
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
import com.example.pollution.data.APIData
import com.example.pollution.data.Location
import com.example.pollution.response.WeatherService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.google.maps.android.data.geojson.GeoJsonLayer
import com.google.maps.android.data.geojson.GeoJsonPolygonStyle
import kotlinx.android.synthetic.main.activity_maps.*

// Async imports
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.runOnUiThread
import org.json.JSONException

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
    private val channel_id = "channel0"
    private val user_limit: Double = 0.0 // User-inputted value. If current location's air quality goes below user_limit, the app alerts the user.

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
        createNotificationChannel() // Create a notification channel for future use.
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

        val builder = LatLngBounds.Builder() // Set the boundaries for movement.
        builder.include(LatLng(62.740234, 9.858139))
        builder.include(LatLng(67.648627, 22.191212))

        val bounds = builder.build() // These are the coordinates of two corners.

        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = width * 0.2

        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding.toInt())) // Move the camera to the appropriate place.

        mMap.setLatLngBoundsForCameraTarget(bounds) // Setting the bounds. Unfortunately, the camera is restricted even when zoomed in. TODO

        mMap.setMinZoomPreference(mMap.cameraPosition.zoom) // Minimum zoom is where the camera currently is.
        mMap.setMaxZoomPreference(12.0f) // Maximum zoom.

        val oslo = LatLng(59.915780, 10.752913)
        mMap.addMarker(MarkerOptions().position(oslo).title(getString(R.string.marker_oslo)))

        try { // Colour surrounding countries in order to exert attention to Norway.
            var layer = GeoJsonLayer(mMap, R.raw.europe, applicationContext) // Use .geojson APIs to get the data on the countries' boundaries.
            var style = layer.defaultPolygonStyle
            style.fillColor = Color.rgb(170, 211, 241)
            style.strokeColor = Color.rgb(170, 211, 241)
            style.strokeWidth = 1F
            layer.addLayerToMap()
            layer = GeoJsonLayer(mMap, R.raw.russia, applicationContext) // Add Russia.
            style = layer.defaultPolygonStyle
            style.fillColor = Color.rgb(170, 211, 241)
            style.strokeColor = Color.rgb(170, 211, 241)
            style.strokeWidth = 1F
            layer.addLayerToMap()

        } catch (ioe: IOException) {
            Log.e("IOException", ioe.localizedMessage)
        } catch (jsone: JSONException) {
            Log.e("JSONException", jsone.localizedMessage)
        }
        setUpMap() // Set up for my location to work properly.
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

    private fun createNotificationChannel() { // Create the channel. All notifications will be sent through this channel, because we only ever use one alert.
        if (Build.VERSION.SDK_INT >= 26) { // This feature is not supported on earlier devices.
            val channel0 = NotificationChannel(
                channel_id,
                "Channel 0",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel0.description = getString(R.string.channel_desc)

            val manager: NotificationManager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel0)
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this, // Assure permission to access GPS is granted.
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        mMap.isMyLocationEnabled = true // This enables your location on the map; it will appear as a blue dot.

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) // Update last location and make the map zoom into current location.
                lastLocation = location
        }
        dangerAlert() // This is just for testing, the call should never be in this function. TODO
    }

    /* A dummy function that illustrate how an alert is sent.
    private fun check() {
        if (getData(lastLocation.latitude, lastLocation.longitude)?.data?.time?.get(Calendar.getInstance().getTime().hours)?.variables?.aQI?.value < user_limit)
            dangerAlert()
    }
    */

    private fun dangerAlert() { // Send the alert.
        val intent = Intent(this, MapsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder = NotificationCompat.Builder(this, channel_id) // The builder contains the notification attributes.
            .setSmallIcon(R.drawable.menu_item_alert)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_desc))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        Log.d("tag", "hello")

        with(NotificationManagerCompat.from(this)) {
            notify(0, builder.build()) // Send the notification with the builder defined above.
        }
    }
}