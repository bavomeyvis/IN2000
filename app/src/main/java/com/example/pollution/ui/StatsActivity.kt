package com.example.pollution.ui

import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.*
import com.example.pollution.R
import com.example.pollution.response.Client
import com.example.pollution.response.WeatherService
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_settings.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.logging.Logger

class StatsActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    companion object {
        var perCityAQI : HashMap<Double, String> = hashMapOf()
        var count : Int = 1
        var nCities : Int = 0
        lateinit var cities: HashMap<String, LatLng>
    }
    var check = 0
    var selectedUnit = "aQI"

    override fun onNothingSelected(parent: AdapterView<*>) {
        print("nothing happened")

    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        if(++check > 1) {
            selectedUnit = parent.selectedItem.toString()
            Log.d("DEBUG", "SELECTED UNIT: $selectedUnit")
            getStats()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Sets theme
        if(getSharedPreferenceValue("theme")) setTheme(R.style.DarkTheme)
        else setTheme(R.style.AppTheme)
        // Sets layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        // Loops through received list of countries and gets countryValues
        cities = intent.extras["hashMap"] as HashMap<String, LatLng>
        nCities = cities.size
        // TODO: Insert upgrade

        //getStats()

        val spinner: Spinner = findViewById(R.id.statsUnit)
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter.createFromResource(
            this,
            R.array.units_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner
            spinner.adapter = adapter
        }

        spinner.onItemSelectedListener = this

    }

    private fun getStats() {
        val unitTitle : TextView = findViewById(R.id.statsCountryName)
        for ((key, value) in cities) {
            getCityValue(key, value, selectedUnit)
        }
    }

    // TODO: Repetitive code
    private fun getSharedPreferenceValue(prefKey: String):Boolean {
        val sp = getSharedPreferences(MapsActivity.sharedPref, 0)
        return sp.getBoolean(prefKey, false)
    }

    private fun getCityValue(key : String, pos : LatLng, pollutionUnit : String)  {
        val client = Client.client
        doAsync {
            val weather = client.getWeather(pos.latitude, pos.longitude).execute().body()
            val aqi = when (pollutionUnit) {
                "no2" -> weather?.data?.time?.get(0)?.variables?.aQINo2?.value
                "o3" -> weather?.data?.time?.get(0)?.variables?.aQIO3?.value
                "pm10" -> weather?.data?.time?.get(0)?.variables?.aQIPm10?.value
                "pm25" -> weather?.data?.time?.get(0)?.variables?.aQIPm25?.value
                else -> weather?.data?.time?.get(0)?.variables?.aQI?.value
            }
            uiThread {
                if (aqi != null) {
                    Log.d("DEBUG","Fuz debugger aint working... $aqi")
                    it.addCountryValue(key, aqi)
                }
            }
        }
    }

    // Adds value to the second hashMap of countries
    private fun addCountryValue(city : String, aqiValue : Double)  {
        perCityAQI[aqiValue] = city
        if(count == nCities) setRanking(perCityAQI)
        else count++
    }

    // Ranks and sorts values and cities.
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
