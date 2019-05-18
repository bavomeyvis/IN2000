package com.example.pollution.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.example.pollution.R
import com.example.pollution.response.Client
import com.google.android.gms.maps.model.LatLng
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.math.BigDecimal
import java.math.RoundingMode

class StatsActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    companion object {
        var count : Int = 1
        var nCities : Int = 0

        val client = Client.client
    }

    lateinit var cities: HashMap<String, LatLng>
    var perCityAQI : HashMap<Double, String> = HashMap()

    override fun onNothingSelected(parent: AdapterView<*>) { print("nothing happened") }
    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) { getCitiesStats(parent.selectedItem.toString()) }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Sets theme
        if(getSharedPreferenceValue("theme")) setTheme(R.style.DarkTheme)
        else setTheme(R.style.AppTheme)
        // Sets layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
    }

    override fun onStart() {
        super.onStart()
        // Loops through received list of countries and gets countryValues
        cities = intent.extras["hashMap"] as HashMap<String, LatLng>
        nCities = cities.size

        val spinner: Spinner = findViewById(R.id.statsUnit)
        // Set adapter of spinner
        ArrayAdapter.createFromResource(this, R.array.units_array, android.R.layout.simple_spinner_item)
            .also { adapter ->
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner
                spinner.adapter = adapter
            }
        spinner.onItemSelectedListener = this
    }

    // Receives spinner item name. Iterate cities and call getCityValue to find aqi
    private fun getCitiesStats(selectedUnit : String) {
        for ((key, value) in cities) {
            getCityValue(key, value, selectedUnit)
        }
    }
    private fun getCityValue(key : String, pos : LatLng, pollutionUnit : String)  {

        doAsync {
            val weather = client.getWeather(pos.latitude, pos.longitude).execute().body()
            val aqi = when (pollutionUnit) {
                "o2" -> weather?.data?.time?.get(0)?.variables?.aQINo2?.value
                "o3" -> weather?.data?.time?.get(0)?.variables?.aQIO3?.value
                "pm10" -> weather?.data?.time?.get(0)?.variables?.aQIPm10?.value
                "pm25" -> weather?.data?.time?.get(0)?.variables?.aQIPm25?.value
                else -> weather?.data?.time?.get(0)?.variables?.aQI?.value
            }
            uiThread {
                if (aqi != null) it.addCountryValue(key, aqi)
            }
        }
    }

    // Adds value to the second hashMap of countries
    private fun addCountryValue(city : String, aqiValue : Double)  {

        // clear list
        // aqivalue : By
        perCityAQI[aqiValue] = city
        if(count == nCities) setRanking(perCityAQI)
        else count++
    }

    // Ranks and sorts values and cities based on hashmap received from api
    private fun setRanking(citiesAndValues : HashMap<Double, String>) {
        // Sorts list
        citiesAndValues.toSortedMap()

        val statsTable : LinearLayout = findViewById(R.id.stats_list)
        var i = 0
        count = 0
        for ((key, value) in citiesAndValues) {
            val v =  statsTable.getChildAt(++i)
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
        citiesAndValues.clear()
    }

    // TODO: Repetitive code
    private fun getSharedPreferenceValue(prefKey: String):Boolean {
        val sp = getSharedPreferences(MapsActivity.sharedPref, 0)
        return sp.getBoolean(prefKey, false)
    }
}
