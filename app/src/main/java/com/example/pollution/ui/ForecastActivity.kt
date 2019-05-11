package com.example.pollution.ui

import android.content.Intent
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import com.example.pollution.R
import com.example.pollution.response.WeatherService
import kotlinx.android.synthetic.main.activity_forecast.*
import kotlinx.android.synthetic.main.activity_forecast_units.*
import kotlinx.android.synthetic.main.activity_maps.*
import org.jetbrains.anko.doAsync
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*

private const val TAG = "ForecastActivity"

class ForecastActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {

    private val arraySizes = 49

    private val timeValues = arrayOfNulls<String>(arraySizes)
    private val aqiValues = arrayOfNulls<Double>(arraySizes)
    private val pm25Values = arrayOfNulls<Double>(arraySizes)
    private val pm10Values = arrayOfNulls<Double>(arraySizes)
    private val no2Values = arrayOfNulls<Double>(arraySizes)
    private val o3Values = arrayOfNulls<Double>(arraySizes)

    private lateinit var timeTextView: TextView
    private lateinit var aqiRectangle: View
    private lateinit var pm25Rectangle: View
    private lateinit var pm10Rectangle: View
    private lateinit var no2Rectangle: View
    private lateinit var o3Rectangle: View

    private lateinit var aqiTextView: TextView
    private lateinit var pm25TextView: TextView
    private lateinit var pm10TextView: TextView
    private lateinit var no2TextView: TextView
    private lateinit var o3TextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        // Sets UI theme
        if(getSharedPreferenceValue("theme")) setTheme(R.style.DarkTheme)
        else setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast)

        //Receives data from MapsActivity
        val intent = intent
        val inputLat = intent.getDoubleExtra(MapsActivity.LAT, 0.0)
        val inputLon = intent.getDoubleExtra(MapsActivity.LON, 0.0)
        val inputTitle = intent.getStringExtra(MapsActivity.TITLE)
        btnStartGraph.setOnClickListener {
            runGraphActivity(inputLat, inputLon, inputTitle)
        }
        //Makes an address object out of the title received from MapsActivity
        lateinit var address: Address
        val geocoder = Geocoder(this)
        var addressList = arrayListOf<Address>()
        try {
            addressList = geocoder.getFromLocationName(inputTitle, 1) as ArrayList<Address>
        } catch (e: IOException) {
            Log.e(TAG, "searchLocation: IOException: " + e.message)
        }
        if (addressList.size > 0) {
            address = addressList[0]
        }


        this.forecast_time_scroller!!.setOnSeekBarChangeListener(this)

        timeTextView = findViewById<TextView>(R.id.forecast_card2_time)
        aqiRectangle = findViewById<View>(R.id.card2_unit1)
        pm25Rectangle = findViewById<View>(R.id.card2_unit2)
        pm10Rectangle = findViewById<View>(R.id.card2_unit3)
        no2Rectangle = findViewById<View>(R.id.card2_unit4)
        o3Rectangle = findViewById<View>(R.id.card2_unit5)


        aqiTextView = findViewById<View>(R.id.forecast_card2_units).findViewById<TextView>(R.id.textView3)
        pm25TextView = findViewById<View>(R.id.forecast_card2_units).findViewById<TextView>(R.id.textView4)
        pm10TextView = findViewById<View>(R.id.forecast_card2_units).findViewById<TextView>(R.id.textView5)
        no2TextView = findViewById<View>(R.id.forecast_card2_units).findViewById<TextView>(R.id.textView6)
        o3TextView = findViewById<View>(R.id.forecast_card2_units).findViewById<TextView>(R.id.textView7)

        aqiTextView.text = "AQI"
        pm25TextView.text = "PM2.5"
        pm10TextView.text = "PM10"
        no2TextView.text = "NO2"
        o3TextView.text = "O3"

//        forecast_time_scroller = findViewById<SeekBar>(R.id.forecast_time_scroller)

//        aqiRectangle2.alpha = (0.5).toFloat()

        val placeNameTextView = findViewById<TextView>(R.id.textView2)
        placeNameTextView.text = address.getAddressLine(0)

        println(address)



        val lat = address.latitude
        val lon = address.longitude

        val client = Retrofit.Builder()
            .baseUrl("https://in2000-apiproxy.ifi.uio.no/weatherapi/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)


        doAsync {
            val weather = client.getWeather(lat, lon).execute().body()

            println("timelength: " + weather?.data?.time?.size)

            for (i in aqiValues.indices + 1) {
                aqiValues[i] = weather?.data?.time?.get(i)?.variables?.aQI?.value
                pm25Values[i] = weather?.data?.time?.get(i)?.variables?.pm25Concentration?.value
                pm10Values[i] = weather?.data?.time?.get(i)?.variables?.pm10Concentration?.value
                no2Values[i] = weather?.data?.time?.get(i)?.variables?.no2Concentration?.value
                o3Values[i] = weather?.data?.time?.get(i)?.variables?.o3Concentration?.value

                println(pm25Values[i])
                timeValues[i] = weather?.data?.time?.get(i)?.from

                println("timevalues:" + timeValues[i] + "   " + i)
            }


            val rightNow = Calendar.getInstance()
            val currentHourIn24Format = rightNow.get(Calendar.HOUR_OF_DAY) - 1

            updateColorViews(currentHourIn24Format)
            forecast_time_scroller.progress = currentHourIn24Format
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        updateColorViews(progress)
        println("progress: " + progress)
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
    }

// TODO: This should be the specified values
    fun updateColorViews2(value: Double?, view: View) {
        if (value != null) {
            if (value >= 4) {
                view.setBackgroundColor(Color.parseColor("#4900AC"))
            } else if (value >= 3) {
                view.setBackgroundColor(Color.parseColor("#C13500"))
            } else if (value >= 2) {
                view.setBackgroundColor(Color.parseColor("#FFCB00"))
            } else {
                view.setBackgroundColor(Color.parseColor("#3F9F41"))
            }
        }
    }

    fun updateColorViews(progress: Int) {
        timeTextView.text = timeValues[progress]?.substring(11, 16)
        println("timeprogress: " + timeValues[progress])

        val aqi = aqiValues[progress]
        //  val aqi2 = aqiValues[progress + 1] caused error. Bravo
        val aqi2 = aqiValues[progress]
        val pm25 = pm25Values[progress]
        val pm10 = pm10Values[progress]
        val no2 = no2Values[progress]
        val o3 = o3Values[progress]

        updateColorViews2(aqi, aqiRectangle)
        updateColorViews2(pm25, pm25Rectangle)
        updateColorViews2(pm10, pm10Rectangle)
        updateColorViews2(no2, no2Rectangle)
        updateColorViews2(o3, o3Rectangle)

    }
    private fun getSharedPreferenceValue(prefKey: String):Boolean {
        val sp = getSharedPreferences(MapsActivity.sharedPref, 0)
        return sp.getBoolean(prefKey, false)
    }

    //Method that runs GraphActivity with extra parameters
    private fun runGraphActivity(lat: Double, lon: Double, title: String) {
        val graphActivityIntent = Intent(this, GraphActivity::class.java)
        graphActivityIntent.putExtra(MapsActivity.LAT, lat)
        graphActivityIntent.putExtra(MapsActivity.LON, lon)
        graphActivityIntent.putExtra(MapsActivity.TITLE, title)
        startActivity(graphActivityIntent)
    }
}
