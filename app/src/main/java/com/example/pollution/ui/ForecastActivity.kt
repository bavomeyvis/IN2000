package com.example.pollution.ui

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.example.pollution.R
import com.example.pollution.classes.ActivityBooter
import com.example.pollution.response.Client
import kotlinx.android.synthetic.main.activity_forecast.*
import org.jetbrains.anko.doAsync
import java.util.*

private const val TAG = "ForecastActivity"

class ForecastActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {
    // TODO: Make a data class of this.
    companion object {

    }
    private val booter = ActivityBooter(this@ForecastActivity)

    private var dataReceived: Boolean = false
    // Size of arrays
    private val arraySizes = 49

    //  C A R D 1
    // Title
    private lateinit var card1Title: TextView
    // Shows the time
    private lateinit var timeTextView: TextView
    // Rectangles
    private lateinit var aqiRectangle: View
    private lateinit var pm25Rectangle: View
    private lateinit var pm10Rectangle: View
    private lateinit var no2Rectangle: View
    private lateinit var o3Rectangle: View
    // values
    private val timeValues = arrayOfNulls<String>(arraySizes)
    private val aqiValues = arrayOfNulls<Double>(arraySizes)
    private val pm25Values = arrayOfNulls<Double>(arraySizes)
    private val pm10Values = arrayOfNulls<Double>(arraySizes)
    private val no2Values = arrayOfNulls<Double>(arraySizes)
    private val o3Values = arrayOfNulls<Double>(arraySizes)

    //  C A R D 2
    // Title
    private lateinit var card2Title: TextView
    // Shows time
    private lateinit var card2timeTextView: TextView
    // Rectangles
    private lateinit var card2aqiRectangle: View
    private lateinit var card2pm25Rectangle: View
    private lateinit var card2pm10Rectangle: View
    private lateinit var card2no2Rectangle: View
    private lateinit var card2o3Rectangle: View
    // Values
    private val card2timeValues = arrayOfNulls<String>(arraySizes)
    private val card2aqiValues = arrayOfNulls<Double>(arraySizes)
    private val card2pm25Values = arrayOfNulls<Double>(arraySizes)
    private val card2pm10Values = arrayOfNulls<Double>(arraySizes)
    private val card2no2Values = arrayOfNulls<Double>(arraySizes)
    private val card2o3Values = arrayOfNulls<Double>(arraySizes)

    override fun onCreate(savedInstanceState: Bundle?) {
        // Sets UI theme
        if (getSharedPreferenceValue("theme")) setTheme(R.style.DarkTheme)
        else setTheme(R.style.AppTheme)
        // Inflate layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast)
        // Receives data from MapsActivity
        val inputLat = intent.getDoubleExtra("lat", 0.0)
        val inputLon = intent.getDoubleExtra("lon", 0.0)
        val inputTitle = intent.getStringExtra("cityTitle")
        //FindViews
        iniCardsUnits()
        // Set listeners
        // Graph button
        var forecastGraphBtn = findViewById<ImageView>(R.id.forecast_card1_graph)
        var forecastMapsBtn = findViewById<ImageView>(R.id.forecast_card1_replace)
        forecastGraphBtn.setOnClickListener{booter.runGraphActivity(inputLat, inputLon, inputTitle) }
        forecastMapsBtn.setOnClickListener{ runMapsActivity() }

        // Make object based on cards
        /*
        if(data1.cityName != null) setCardFocus(2, inputTitle, inputLat, inputLat)
        else setCardFocus(1, inputTitle, inputLat, inputLon)
        */
        // Set seek bar
        setCardTitle(1, inputTitle, inputLat, inputLon)
        this.forecast_time_scroller!!.setOnSeekBarChangeListener(this)
        // Sets graph on button click
        forecast_card1_graph.setOnClickListener { booter.runGraphActivity(inputLat, inputLon, inputTitle) }

        doAsync {
            val weather = Client.client.getWeather(inputLat, inputLon).execute().body()
            // TODO: afterwards add this into the Location class
            for (i in aqiValues.indices + 1) {
                aqiValues[i] = weather?.data?.time?.get(i)?.variables?.aQI?.value
                pm25Values[i] = weather?.data?.time?.get(i)?.variables?.pm25Concentration?.value
                pm10Values[i] = weather?.data?.time?.get(i)?.variables?.pm10Concentration?.value
                no2Values[i] = weather?.data?.time?.get(i)?.variables?.no2Concentration?.value
                o3Values[i] = weather?.data?.time?.get(i)?.variables?.o3Concentration?.value
                timeValues[i] = weather?.data?.time?.get(i)?.from
            }

            val time = Calendar.getInstance()
            val currentHourIn24Format = time.get(Calendar.HOUR_OF_DAY)

            //updateColorViews(currentHourIn24Format)
            forecast_time_scroller.progress = currentHourIn24Format
            dataReceived = true
        }

    }

    private fun iniCardsUnits() {
        card1Title = findViewById(R.id.forecast_card1_title)
        timeTextView = findViewById(R.id.forecast_card1_time)
        aqiRectangle = findViewById<View>(R.id.card1_unit1)
        pm25Rectangle = findViewById<View>(R.id.card1_unit2)
        pm10Rectangle = findViewById<View>(R.id.card1_unit3)
        no2Rectangle = findViewById<View>(R.id.card1_unit4)
        o3Rectangle = findViewById<View>(R.id.card1_unit5)

        card2Title = findViewById(R.id.forecast_card2_title)
        card2timeTextView = findViewById(R.id.forecast_card2_time)
        card2aqiRectangle = findViewById<View>(R.id.card2_unit1)
        card2pm25Rectangle = findViewById<View>(R.id.card2_unit2)
        card2pm10Rectangle = findViewById<View>(R.id.card2_unit3)
        card2no2Rectangle = findViewById<View>(R.id.card2_unit4)
        card2o3Rectangle = findViewById<View>(R.id.card2_unit5)
    }

    private fun setCardTitle(card : Int, title: String, lat: Double, lon: Double) {
        // Finds all relevant rectangles and textViews
        when(card) {
            1 -> {
                // Sets title of card1
                card1Title.text = title
            }
            2 -> {
                card2Title.text = title
            }
            else -> print("Something weird happened")
        }
    }
/*
    fun updateCompareTexts(card1Value: Double?, card2Value: Double?, cardTextView: TextView?) {
        val card1aqiDifference = card1Value!! - card2Value!!
        var card1aqiDifferenceString = card1aqiDifference.toString()

        if (card1aqiDifference < 0) {
            card1aqiDifferenceString = card1aqiDifferenceString.substring(0, 4)
            cardTextView?.setTextColor(Color.parseColor("#C13500"))
        } else {
            card1aqiDifferenceString = "+" + card1aqiDifferenceString.substring(0, 3)
            cardTextView?.setTextColor(Color.parseColor("#3F9F41"))
        }
        cardTextView?.text = card1aqiDifferenceString
    }
*/
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//        if (dataReceived) {
        updateColorViews(progress)
/*
        updateCompareTexts(card1aqiValues[progress], card2aqiValues[progress], card1CompareTexts[0])
        updateCompareTexts(card2aqiValues[progress], card1aqiValues[progress], card2CompareTexts[0])

        updateCompareTexts(card1pm25Values[progress], card2pm25Values[progress], card1CompareTexts[1])
        updateCompareTexts(card2pm25Values[progress], card1pm25Values[progress], card2CompareTexts[1])

        updateCompareTexts(card1pm10Values[progress], card2pm10Values[progress], card1CompareTexts[2])
        updateCompareTexts(card2pm10Values[progress], card1pm10Values[progress], card2CompareTexts[2])

        updateCompareTexts(card1no2Values[progress], card2no2Values[progress], card1CompareTexts[3])
        updateCompareTexts(card2no2Values[progress], card1no2Values[progress], card2CompareTexts[3])

        updateCompareTexts(card1o3Values[progress], card2o3Values[progress], card1CompareTexts[4])
        updateCompareTexts(card2o3Values[progress], card1o3Values[progress], card2CompareTexts[4])
//
*/
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
    override fun onStopTrackingTouch(seekBar: SeekBar?) {

    }

    fun updateColorViews(value: Double?, view: View) {
        if (value != null) {
            if (value >= 4) view.setBackgroundColor(Color.parseColor("#4900AC"))
            else if (value >= 3) view.setBackgroundColor(Color.parseColor("#C13500"))
            else if (value >= 2) view.setBackgroundColor(Color.parseColor("#FFCB00"))
            else view.setBackgroundColor(Color.parseColor("#3F9F41"))
        }
    }

    private fun updateColorViews(progress: Int) {
        timeTextView.text = timeValues[progress]?.substring(11, 16)

        val aqi = aqiValues[progress]
        //  val aqi2 = aqiValues[progress + 1] caused error. Bravo
        val aqi2 = aqiValues[progress]
        val pm25 = pm25Values[progress]
        val pm10 = pm10Values[progress]
        val no2 = no2Values[progress]
        val o3 = o3Values[progress]

        updateColorViews(aqi, aqiRectangle)
        updateColorViews(pm25, pm25Rectangle)
        updateColorViews(pm10, pm10Rectangle)
        updateColorViews(no2, no2Rectangle)
        updateColorViews(o3, o3Rectangle)

    }

    //END CARD 2
    private fun getSharedPreferenceValue(prefKey: String): Boolean {
        val sp = getSharedPreferences(MapsActivity.sharedPref, 0)
        return sp.getBoolean(prefKey, false)

    }

    private fun runMapsActivity() {
        // TODO: remove cards content so it can be replaced laters
    }
}