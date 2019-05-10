package com.example.pollution.ui

// Our stuff
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import android.widget.Toast

// Maps stuff
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

// Packages' class imports
import com.example.pollution.R
import com.example.pollution.classes.City
import com.example.pollution.response.WeatherService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.google.maps.android.data.geojson.GeoJsonLayer
import kotlinx.android.synthetic.main.activity_maps.*

// Async imports
import org.jetbrains.anko.doAsync
import org.json.JSONException

// Retrofit imports
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

private const val TAG = "MapsActivity"

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, PopupMenu.OnMenuItemClickListener {
    companion object {
        var mapsActivity : MapsActivity? = null
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        val sharedPref = "settings"
        val LAT = "com.example.pollution.ui.LAT"
        val LON = "com.example.pollution.ui.LON"
    }

    //Google Maps
    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: android.location.Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //Todo: Move starting of GraphActivity
    /*graphActivityIntent.putExtra(LAT, testLat)
    graphActivityIntent.putExtra(LON, testLon)*/
    // Test lats
    val testLat = 59.915780
    val testLon = 10.752913

    private val channel_id = "channel0"
    // User-inputted value. If current location's air quality goes below user_limit, the app alerts the user.
    private val user_limit: Double = 0.0

    //list of City class objects containing name, coordinates and the marker for each large city
    var cities = arrayListOf<City>()

    //On create stuff
    override fun onCreate(savedInstanceState: Bundle?) {
        // When object is created. Static variable mapsActivity is set
        mapsActivity = this
        // Sets UI theme
        if(getSharedPreferenceValue("theme")) setTheme(R.style.DarkTheme)
        else setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        //getMapASYNC
        mapFragment.getMapAsync(this)
        // TODO: Explain what this does
        setKeyboardFinishedListener()
        // Create a notification channel for future use.
        createNotificationChannel()

        // TODO: ???
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    // TODO: Consider migrating into object
    // Sets up a listener for the enter button on the keyboard.
    // TODO: Isn't this an app for mobiles? What is KEYCODE_ENTER?
    private fun setKeyboardFinishedListener() {
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
    // TODO: Consider migrating into object
    // Closes the keyboard properly
    private fun closeKeyboard() {
        val currentView: View? = this.currentFocus
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentView?.windowToken, 0)
    }

    // Recreates when startActivityForResult gets OK_Signal (.e.g from settings)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1) { if(resultCode == Activity.RESULT_OK) recreate() }
    }

    // Sets Map preferences (e.g. theme, boundaries)
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Turns off most of Google Maps widgets
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.uiSettings.isMyLocationButtonEnabled = false
        mMap.uiSettings.isCompassEnabled = false
        mMap.uiSettings.isZoomControlsEnabled = false
        // Sets map theme and surroundings
        if (getSharedPreferenceValue("theme")){
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark))
            darkenSurroundings(true)
        }
        else {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_normal))
            darkenSurroundings(false)
        }


        // Set the boundaries for movement.
        val builder = LatLngBounds.Builder()
        builder.include(LatLng(62.740234, 9.858139))
        builder.include(LatLng(67.648627, 22.191212))
        val bounds = builder.build() // These are the coordinates of two corners.
        // ???
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = width * 0.2
        // Move the camera to the appropriate place.
        //mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding.toInt()))
        //mMap.setLatLngBoundsForCameraTarget(bounds) // Setting the bounds. Unfortunately, the camera is restricted even when zoomed in.
        // TODO Ideally, panning freely should be allowed, provided it takes place within the predefined boundaries.
        //mMap.setMinZoomPreference(mMap.cameraPosition.zoom) // Minimum zoom is where the camera currently is.
        //mMap.setMaxZoomPreference(12.0f) // Maximum zoom.
        // TODO: camo_light() and darkenSurroundings() not working.
        darkenSurroundings(false)
        // Assures location is set
        setMyLocation()

        mMap.setOnMapClickListener(object: GoogleMap.OnMapClickListener {
            override fun onMapClick(point:LatLng) {
                //map is clicked latlng can be accessed from
                //point.Latitude & point.Longitude
            }
        })

        addCityMarkers(mMap)
    }

    // TODO: Consider migrating into object
    // TODO: Jørgen's code (make private?)
    fun getPositionData(lat: Double, lon: Double): String {
        lateinit var returnInfo: String
        try {
            val geocoder = Geocoder(this@MapsActivity, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            returnInfo = addresses.get(0).getAddressLine(0)
        } catch (e: IOException) {
            return ""
        }
        return returnInfo
    }

    // TODO: Consider migrating into object
    // Find location
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
            // TODO: Remove print (J)
            println(aqi)

            var markerColor = BitmapDescriptorFactory.HUE_RED

            if (aqi != null && aqi < 1.75) markerColor = BitmapDescriptorFactory.HUE_GREEN

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

    // TODO: Consider migrating into object
    // Function that searches for a location
    private fun searchLocation() {
        val searchAddress: String = search_input.text.toString()
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
            //Open ForecastActivity when searched
            val intent = Intent(this, ForecastActivity::class.java)
            intent.putExtra("address", address)
            startActivity(intent)
        }
    }

    // The menu items' listener
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.menu_home -> recreate()
            R.id.menu_alert -> Toast.makeText(this, "alerts", Toast.LENGTH_SHORT).show()
            R.id.menu_favorites -> Toast.makeText(this, "favorites", Toast.LENGTH_SHORT).show()
            R.id.menu_graph -> runGraphActivity(testLat, testLon)
            R.id.menu_stats -> Toast.makeText(this, "stats", Toast.LENGTH_SHORT).show()
            R.id.menu_settings -> runSettingsActivity()
        }
        return true
    }

    //TODO: Move to Bjørn's activity
    //Method that runs GraphActivity with extra parameters
    fun runGraphActivity(lat: Double, lon: Double) {
        val graphActivityIntent = Intent(this, GraphActivity::class.java)
        graphActivityIntent.putExtra(LAT, lat)
        graphActivityIntent.putExtra(LON, lon)
        startActivity(graphActivityIntent)
    }

    //TODO: Change the name of class Bjørn (vet ikke hva den heter)
    //Method that runs ForecastActivity with extra parameters
    fun runForecastActivity(lat: Double, lon: Double) {
        val forecastActivityIntent = Intent(this, GraphActivity::class.java) //<--- Change this
        forecastActivityIntent.putExtra(LAT, lat)
        forecastActivityIntent.putExtra(LON, lon)
        startActivity(forecastActivityIntent)
    }

    fun runSettingsActivity() {
        val settingsActivityIntent = Intent(this, SettingsActivity::class.java)
        startActivityForResult(settingsActivityIntent, 1)
        recreate()
    }
    // shows popup as well as icons
    /*TODO: only popup.show() should be necessary*/
    fun showPopup(v:View) {
        val popup = PopupMenu(this, v)
        popup.setOnMenuItemClickListener(this)
        popup.inflate(R.menu.popup_menu)
        // TODO: Lots of bullshit due to PopupMenu not coming with icons
        // Consider using your own defined menu
        try {
            val fieldMPopup = PopupMenu::class.java.getDeclaredField("mPopup")
            fieldMPopup.isAccessible = true
            val mPopup = fieldMPopup.get(popup)
            mPopup.javaClass
                .getDeclaredMethod("setForceShowIcon", Boolean::class.java)
                .invoke(mPopup, true)
        } catch (e: Exception){
            Log.e("Main", "Error showing menu icons.", e)
        } finally { popup.show() }
    }

    // TODO: comment please ???
    private fun createNotificationChannel() { // Create the channel. All notifications will be sent through this channel, because we only ever use one alert.
        if (Build.VERSION.SDK_INT >= 26) { // This feature is not supported on earlier devices.
            val channel0 = NotificationChannel(channel_id, "Channel 0", NotificationManager.IMPORTANCE_HIGH)
            channel0.description = getString(R.string.channel_desc)
            val manager: NotificationManager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel0)
        }
    }

    // TODO: Consider migrating into object
    // Assure permission to access GPS is granted.
    private fun setMyLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }
        mMap.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) lastLocation = location
        }
    }

    // TODO: Consider migrating into object
    private fun dangerAlert() { // Send the alert.
        // Do not proceed if user has turned off alerts in settings.
        if (!getSharedPreferenceValue("alert")) return
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

        with(NotificationManagerCompat.from(this)) {
            notify(0, builder.build()) // Send the notification with the builder defined above.
        }
    }

    // Returns value found at key in sharedPref
    private fun getSharedPreferenceValue(prefKey: String):Boolean {
        val sp = getSharedPreferences(sharedPref, 0)
        return sp.getBoolean(prefKey, false)
    }

    // Adds (colored, depending on AQI value, ) markers to "cityMarkers" using API request (with LatLng)
    private fun addCityMarkers(mMap: GoogleMap) {
        val coordinates : HashMap<String, LatLng> = hashMapOf(
            "oslo" to LatLng(59.915780, 10.752913), "bergen" to LatLng(60.393975, 5.324937),
            "trondheim" to LatLng(63.433465, 10.395516), "stavanger" to LatLng(63.433465, 10.395516),
            "sandvika" to LatLng(59.891695, 10.528088), "kristiansand" to LatLng(58.162897, 8.018848),
            "fredrikstad" to LatLng(59.224392, 10.933630), "tromsø" to LatLng(69.653412, 18.953360),
            "drammen" to LatLng(59.747642, 10.205377), "sandnes" to LatLng(58.852107, 5.732697),
            "skien" to LatLng(58.852107, 5.732697), "sarpsborg" to LatLng(59.286260, 11.109056),
            "bodø" to LatLng(67.282654, 14.404968), "larvik" to LatLng(59.056636, 10.02887),
            "sandefjord" to LatLng(59.056636, 10.028874), "lillestrøm" to LatLng(59.956639, 11.050240),
            "arendal" to LatLng(58.463660, 8.772121), "ålesund" to LatLng(62.476929, 6.149429))
        //creating a client to fetch AQI data from api
        val client = Retrofit.Builder()
            .baseUrl("https://in2000-apiproxy.ifi.uio.no/weatherapi/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)
        // Add a colored marker according to checked AQ index (if any)
        for ((key, value) in coordinates) {
            doAsync {
                lateinit var marker: Marker
                val weather = client.getWeather(value.latitude, value.longitude).execute().body()
                val aqi = weather?.data?.time?.get(0)?.variables?.aQI?.value
                runOnUiThread {
                    marker = mMap
                        .addMarker(MarkerOptions()
                            .position(value)
                            .title(key))
                    if(aqi != null) {
                        when {
                            aqi < 1.5 -> marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                            aqi < 2.0 && aqi > 1.5 -> marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                            aqi < 2.5 && aqi > 2.0 -> marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                            aqi > 2.5 ->  marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        }
                    } // Marker added to "cityMarkers"
                    val city = City(key, value, marker)
                    cities.add(city)
                }
            }
        }
    }

    // TODO: Bravo integrate this
    //Colors surrounding area of Norway as water, TODO: appropriate to current theme.
    private fun darkenSurroundings(dark : Boolean) {
        try {
            val layer = GeoJsonLayer(mMap, R.raw.camo, applicationContext) //.geojson APIs for data on countries' boundaries.
            val style = layer.defaultPolygonStyle
            style.strokeWidth = 50F
            if(dark) {
                style.fillColor = Color.rgb(0, 0, 0)
                style.strokeColor = Color.rgb(0, 0, 0)
            } else {
                style.fillColor = Color.rgb(201, 201, 201)
                style.strokeColor = Color.rgb(201, 201, 201)
            }
            layer.addLayerToMap()
        } catch (ioe: IOException) {
            Log.e("IOException", ioe.localizedMessage)
        } catch (jsone: JSONException) {
            Log.e("JSONException", jsone.localizedMessage)
        }
    }
}