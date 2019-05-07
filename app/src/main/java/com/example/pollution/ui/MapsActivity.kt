package com.example.pollution.ui

// Our stuff
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Address
import android.location.Geocoder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatDelegate
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*

// Maps stuff
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment

// Packages' class imports
import com.example.pollution.R
import com.example.pollution.data.APIData
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
import java.util.*
import kotlin.collections.ArrayList

private const val TAG = "MapsActivity"

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, PopupMenu.OnMenuItemClickListener {
    companion object {
        var mapsActivity : MapsActivity? = null
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        val sharedPref = "settings"
    }

    //Google Maps
    private lateinit var mMap: GoogleMap
    private lateinit var lastLocation: android.location.Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //On create stuff
    override fun onCreate(savedInstanceState: Bundle?) {
        mapsActivity = this
        // Turns on dark mode
        darkMode()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment

        //getMapASYNC
        mapFragment.getMapAsync(this)
        val data : APIData? = getData(59.915780, 10.752913)
        init()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    /*
    override fun onResume() {
        super.onResume()
        recreate()
    }*/

    private fun darkMode() {
        if(getSharedPreferenceValue("theme")) {
            setTheme(R.style.DarkTheme)
            Toast.makeText(this, "Dark theme applied.", Toast.LENGTH_LONG).show()

        } else {
            setTheme(R.style.AppTheme)
            Toast.makeText(this, "Light theme applied.", Toast.LENGTH_LONG).show()
        }
    }
    // ???
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

    //???
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // https://stackoverflow.com/questions/6250325/hide-google-logo-from-mapview/6250405
        // Turns off the two buttons in the bottom right (directions and open maps)
        mMap.uiSettings.isMapToolbarEnabled = false
        mMap.uiSettings.isMyLocationButtonEnabled = true

        val boundOne = LatLng(57.696784, 3.601294)
        val boundTwo = LatLng(71.214304, 34.476990)

        val builder = LatLngBounds.Builder()
        builder.include(boundOne)
        builder.include(boundTwo)

        val bounds = builder.build()

        mMap.setLatLngBoundsForCameraTarget(bounds)
        // Add a marker in Oslo and move the camera
        val oslo = LatLng(59.915780, 10.752913)
        mMap.addMarker(MarkerOptions().position(oslo).title("Marker in Oslo"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(oslo, 8.0f))
        /*
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
        }*/
        if (getSharedPreferenceValue("theme")) {
            mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_dark))
        }
        else mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_normal))

        setUpMap()
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

    // ????
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

    // ???
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

    // the different menu items' actions
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        // this should be in item4...
        val settingsActivityIntent = Intent(this, SettingsActivity::class.java)
        startActivityForResult(settingsActivityIntent, 1)



        when(item?.itemId) {
            R.id.menu_1_home -> Toast.makeText(this, "darkTheme", Toast.LENGTH_LONG).show()
            R.id.menu_2_alert -> Toast.makeText(this, "saveInfo", Toast.LENGTH_LONG).show()
            R.id.menu_3_favorites -> Toast.makeText(this, "favorites", Toast.LENGTH_LONG).show()
            R.id.menu_4_settings -> {
                startActivityForResult(settingsActivityIntent, 1)
                recreate()
            }
        }
        return true
    }

    // shows popup as well as icons
    /*TODO: only popup.show() should be necessary*/
    fun showPopup(v:View) {
        val popup = PopupMenu(this, v)
        popup.setOnMenuItemClickListener(this)
        popup.inflate(R.menu.popup_menu)
        // Lots of bullshit due to PopupMenu not coming with icons
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
        } finally {
            popup.show()
        }
    }

    // ???
    private fun closeKeyboard() {
        val currentView: View? = this.currentFocus
        val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentView?.windowToken, 0)
    }

    // ???
    private fun setUpMap() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        mMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation.addOnSuccessListener(this) { location ->
            if (location != null) {
                lastLocation = location
                val currentLatLng = LatLng(location.latitude, location.longitude)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 12f))
            }
        }
    }

    // Returns value found at key in sharedPref
    private fun getSharedPreferenceValue(prefKey: String):Boolean {
        val sp = getSharedPreferences(sharedPref, 0)
        return sp.getBoolean(prefKey, false)
    }
}
