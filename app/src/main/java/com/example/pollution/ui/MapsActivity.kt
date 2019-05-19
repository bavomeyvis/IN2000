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
import com.example.pollution.classes.CheckAlertConditions
import com.example.pollution.classes.City
import com.example.pollution.response.WeatherService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.google.maps.android.data.geojson.GeoJsonLayer
import kotlinx.android.synthetic.main.activity_maps.*

// Async imports
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.startActivityForResult
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
        val TITLE = "com.example.pollution.ui.TITLE"
    }

    //Google Maps
    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
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

    //Todo: Move starting of GraphActivity
    /*graphActivityIntent.putExtra(LAT, testLat)
    graphActivityIntent.putExtra(LON, testLon)*/
    // Test lats
    val testLat = 59.915780
    val testLon = 10.752913
    // TODO: ???
    //list of City class objects containing name, coordinates and the marker for each large city
    var cities = arrayListOf<City>()
    private lateinit var lastLocation: android.location.Location

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
        createNotificationChannel("channel0")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
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
        search_input.isCursorVisible = true

        // Set the boundaries for movement.
        val builder = LatLngBounds.Builder()
        builder.include(LatLng(60.443184, 8.052995))
        builder.include(LatLng(70.012997, 24.316675))
        val bounds = builder.build() // These are the coordinates of two corners.
        // ???
        val width = resources.displayMetrics.widthPixels
        val height = resources.displayMetrics.heightPixels
        val padding = width * 0.2
        // Move the camera to the appropriate place.
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding.toInt()))
        //mMap.setLatLngBoundsForCameraTarget(bounds) // Setting the bounds. Unfortunately, the camera is restricted even when zoomed in.
        //mMap.setMinZoomPreference(mMap.cameraPosition.zoom) // Minimum zoom is where the camera currently is.
        //mMap.setMaxZoomPreference(12.0f) // Maximum zoom.
         //get latlong for corners for specified city

        darkenSurroundings(getSharedPreferenceValue("theme"))
        // Assures location is set
        setMyLocation()

        // TODO: This is only if we wantevery place
        /*
        mMap.setOnMapClickListener { point ->
            //map is clicked latlng can be accessed from
            //point.Latitude & point.Longitude
            runForecastActivity(point.latitude, point.longitude, getPositionData(point.latitude, point.longitude))
        }*/
        addCityMarkers(mMap)

        //marker is clicked and we find the marker's corresponding City class object
        mMap.setOnMarkerClickListener { marker ->
            val city: City? = getCity(marker)
            runForecastActivity(marker.position.latitude, marker.position.longitude, city!!.cityName)
            false
        }

        /*
        // Execute task implemented in CheckAlertConditions.kt.
        val asyncTask = CheckAlertConditions()
        TODO("Properly convert the string to AQI on form of integer.")
        asyncTask.execute(getPositionData(lastLocation.latitude, lastLocation.longitude).toInt())
        */
    }

    //Takes a city marker as argument and returns the corresponding City object
    fun getCity(marker: Marker): City? {
        var returnCity: City? = null
        for (city in cities) {
            if (city.cityName.equals(marker.title)) {
                returnCity = city
            }
        }
        return returnCity
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

    // TODO: Consider migrating the methods below into an object
    private fun getPositionData(lat: Double, lon: Double): String {
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

            //TODO: Hvis man klikker på markeren som lages her krasjer appen
            //addMarkerColoured(address)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(addressLatLng, 15F))
            //Open ForecastActivity when searched
            runForecastActivity(address.latitude, address.longitude, address.getAddressLine(0))
        }
    }

    // The menu items' listener
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.menu_home -> recreate()
            R.id.menu_alert -> runAlertActivity()
            R.id.menu_favorites -> Toast.makeText(this, "favorites", Toast.LENGTH_SHORT).show()
            R.id.menu_graph -> runGraphActivity(testLat, testLon)
            R.id.menu_stats -> runStatsActivity()
            R.id.menu_settings -> runSettingsActivity()
        }
        return true
    }

    //Method that runs GraphActivity with extra parameters
    private fun runGraphActivity(lat: Double, lon: Double) {
        val graphActivityIntent = Intent(this, GraphActivity::class.java)
        graphActivityIntent.putExtra("lat", lat)
        graphActivityIntent.putExtra("lon", lon)
        startActivity(graphActivityIntent)
    }

    // Runs activity "Statistics"
    private fun runStatsActivity() {
        val statsActivity = Intent(this, StatsActivity::class.java).putExtra("hashMap", coordinates)
        startActivity(statsActivity)
        recreate()

    }

    // Runs ForecastActivity with extra parameters
    private fun runForecastActivity(lat: Double, lon: Double, title:String) {
        val forecastActivityIntent = Intent(this, ForecastActivity::class.java) //< --- Change this
        forecastActivityIntent.putExtra("lat", lat)
        forecastActivityIntent.putExtra("lon", lon)
        forecastActivityIntent.putExtra("cityTitle", title)
        startActivity(forecastActivityIntent)
    }

    // Runs settingsActivity
    private fun runSettingsActivity() {
        val settingsActivityIntent = Intent(this, SettingsActivity::class.java)
        startActivityForResult(settingsActivityIntent, 1)
        recreate()
    }

    // Returns value found at key in sharedPref
    private fun getSharedPreferenceValue(prefKey: String):Boolean {
        val sp = getSharedPreferences(sharedPref, 0)
        return sp.getBoolean(prefKey, false)
    }

    fun runAlertActivity() {
        val alertActivityIntent = Intent(this, AlertActivity::class.java)
        startActivityForResult(alertActivityIntent, 1)
        recreate()
    }

    // shows popup as well as icons
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

    private fun createNotificationChannel(channel_id: String) { // Create the channel. All notifications will be sent through this channel, because we only ever use one alert.
        if (Build.VERSION.SDK_INT >= 26) { // This feature is not supported on earlier devices.
            val channel0 = NotificationChannel(channel_id, "Channel 0", NotificationManager.IMPORTANCE_HIGH)
            channel0.description = getString(R.string.alert_title) // Set the description for the channel, can be arbitrary.
            val manager: NotificationManager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel0) // Create the channel used for dangerAlert.
        }
    }

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

    // Sets up a listener for the enter button on the keyboard.
    private fun setKeyboardFinishedListener() {
        search_input.setOnEditorActionListener { _, actionId, event ->
            search_input.isCursorVisible = true

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

    // Closes the keyboard properly
    private fun closeKeyboard() {
        val currentView: View? = this.currentFocus
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentView?.windowToken, 0)
    }

    //Colors surrounding area of Norway as water
    private fun darkenSurroundings(dark : Boolean) {
        try {
            // If you want to improve: http://geojson.io.
            val layer = GeoJsonLayer(mMap, R.raw.camo, applicationContext) //.geojson APIs for data on countries' boundaries.
            val style = layer.defaultPolygonStyle
            style.strokeWidth = 40F
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