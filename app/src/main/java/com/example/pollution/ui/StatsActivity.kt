package com.example.pollution.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.example.pollution.R
import com.example.pollution.response.WeatherService
import com.google.android.gms.maps.model.LatLng
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal
import java.math.RoundingMode

class StatsActivity : AppCompatActivity() {
    companion object {
        var perCityAQI : HashMap<Double, String> = hashMapOf()
        var count : Int = 1
        var nCities : Int = 0
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        // Sets theme
        if(getSharedPreferenceValue("theme")) setTheme(R.style.DarkTheme)
        else setTheme(R.style.AppTheme)
        // Sets layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        // Makes a client connection
        val client = Retrofit.Builder()
            .baseUrl("https://in2000-apiproxy.ifi.uio.no/weatherapi/")
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(WeatherService::class.java)
        // Loops through received list of countries and gets countryValues
        val cities : HashMap<String, LatLng> = intent.extras["hashMap"] as HashMap<String, LatLng>
        nCities = cities.size
        for ((key, value) in cities) getCityValue(client, key, value)
    }

    // TODO: Repetitive code
    private fun getSharedPreferenceValue(prefKey: String):Boolean {
        val sp = getSharedPreferences(MapsActivity.sharedPref, 0)
        return sp.getBoolean(prefKey, false)
    }

    private fun getCityValue(client : WeatherService, key : String, value : LatLng)  {
        doAsync {
            val weather = client.getWeather(value.latitude, value.longitude).execute().body()
            val aqi = weather?.data?.time?.get(0)?.variables?.aQI?.value
            uiThread {
                if (aqi != null) it.addCountryValue(key, aqi)
            }
        }
    }
    // Adds value to the second hashMap of countries
    private fun addCountryValue(city : String, aqiValue : Double)  {
        perCityAQI[aqiValue] = city
        if(count == nCities) setRanking(perCityAQI)
        else count++
    }
    private fun setRanking(citiesAndValues : HashMap<Double, String>) {
        // Sorts list
        val sortedMap = citiesAndValues.toSortedMap(compareByDescending { it })
        val statsTable : LinearLayout = findViewById(R.id.stats_list)
        var i = 0
        for ((key, value) in sortedMap) {
            i++
            val v =  statsTable.getChildAt(i)
            if(v is LinearLayout) {
                // Set country text in XML column
                val item0 : View = v.getChildAt(0)
                if(item0 is TextView) item0.text = value
                // Set AQI value text in XML column
                val item1 : View = v.getChildAt(1)
                if(item1 is TextView) {
                    val decimal = BigDecimal(key).setScale(2, RoundingMode.HALF_EVEN)
                    item1.text =  decimal.toString()
                }
            }
        }
    }
}
