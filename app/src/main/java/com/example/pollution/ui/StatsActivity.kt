package com.example.pollution.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.pollution.R
import com.example.pollution.classes.City
import com.example.pollution.response.WeatherService
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import org.jetbrains.anko.custom.async
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.w3c.dom.Text
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.math.BigDecimal
import java.math.RoundingMode

class StatsActivity : AppCompatActivity() {
    companion object {
        var perCountryAQI : HashMap<Double, String> = hashMapOf()
        var count : Int = 0
    }
    var moreData = true
    override fun onCreate(savedInstanceState: Bundle?) {

        if(getSharedPreferenceValue("theme")) setTheme(R.style.DarkTheme)
        else setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        val client = Retrofit.Builder()
            .baseUrl("https://in2000-apiproxy.ifi.uio.no/weatherapi/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)

        val countries : HashMap<String, LatLng> = intent.extras["hashMap"] as HashMap<String, LatLng>

        for ((key, value) in countries) {
            getValue(client, key, value)
            // perCountryAQI[key] = getValue(client, key, value)
        }

    }
    private fun getValue(client : WeatherService, key : String, value : LatLng)  {
        doAsync {
            val weather = client.getWeather(value.latitude, value.longitude).execute().body()
            val aqi = weather?.data?.time?.get(0)?.variables?.aQI?.value

            uiThread {
                if (aqi != null) {
                    this@StatsActivity.setValue(key, aqi)


                }
                else Toast.makeText(this@StatsActivity, "Shit", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun setValue(country : String, aqiValue : Double)  {
        count++
        perCountryAQI[aqiValue] = country
        if(count == 18) setRanking(perCountryAQI)
    }


    // TODO: Repetitive code
    private fun getSharedPreferenceValue(prefKey: String):Boolean {
        val sp = getSharedPreferences(MapsActivity.sharedPref, 0)
        return sp.getBoolean(prefKey, false)
    }

    private fun setRanking(countriesAndValues : HashMap<Double, String>) {
        val sortedMap = countriesAndValues.toSortedMap(compareByDescending { it })
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
