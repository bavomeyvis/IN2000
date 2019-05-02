package com.example.pollution.ui

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

// Packages' class imports
import com.example.pollution.R
import com.example.pollution.response.WeatherService
import kotlinx.android.synthetic.main.activity_maps.*

// Async imports
import org.jetbrains.anko.doAsync

// Retrofit imports
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

private const val TAG = "MapsActivity"

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, PopupMenu.OnMenuItemClickListener {

    private lateinit var mMap: GoogleMap

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
    }

    private fun init() {
        search_input.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE
                || actionId == EditorInfo.IME_ACTION_SEARCH
                || event.action == KeyEvent.ACTION_DOWN
                || event.action == KeyEvent.KEYCODE_ENTER) {
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
        // Add a marker in Oslo and move the camera
        val oslo = LatLng(59.915780, 10.752913)
        mMap.addMarker(MarkerOptions().position(oslo).title("Marker in Oslo"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(oslo, 8.0f))
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

    // find location
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
            mMap.addMarker(MarkerOptions().position(addressLatLng).title(address.getAddressLine(0)))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(addressLatLng, 15F))
        }
    }

    // the different menu items' actions
    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when(item?.itemId) {
            R.id.item1 -> Toast.makeText(this, "Item 1 clicked", Toast.LENGTH_SHORT).show()
            R.id.item2 -> Toast.makeText(this, "Item 2 clicked", Toast.LENGTH_SHORT).show()
            R.id.item3 -> Toast.makeText(this, "Item 3 clicked", Toast.LENGTH_SHORT).show()
            R.id.item4 -> Toast.makeText(this, "Item 4 clicked", Toast.LENGTH_SHORT).show()
        }
        return true
    }

    // https://www.youtube.com/watch?v=ncHjCsoj0Ws
    // https://developer.android.com/guide/topics/ui/menus
    // https://stackoverflow.com/questions/36876720/android-studio-2-0-not-showing-menu-icons-in-preview
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

    private fun closeKeyboard() {
        val currentView : View? = this.currentFocus
        val imm : InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentView?.windowToken, 0)
    }
}
