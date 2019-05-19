package com.example.pollution.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.example.pollution.R
import com.example.pollution.classes.ActivityBooter
import com.example.pollution.response.Client
import kotlinx.android.synthetic.main.activity_forecast.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*

class ForecastActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {
    companion object {
        private var card1City : String = "Add a card ->"
        private var card1Lat : Double = 0.0
        private var card1Lon : Double = 0.0

        private var card2City : String = "Add a card ->"
        private var card2Lat : Double = 0.0
        private var card2Lon : Double = 0.0

        private var card2BeChanged = false
    }


    private var cardsChecked = 0
    private var card2IsEmpty = false


    // TODO: Make a data class of this.
    private val booter = ActivityBooter(this@ForecastActivity)


    // Size of arrays
    private val arraySizes = 49

    //  C A R D 1
    // Title, time, rectangles
    private lateinit var card1title: TextView
    private lateinit var card1time: TextView
    private lateinit var card1aqiRect: View
    private lateinit var card1pm25Rect: View
    private lateinit var card1pm10Rect: View
    private lateinit var card1no2Rect: View
    private lateinit var card1o3Rect: View
    // Numerical TextViews
    private lateinit var card1value1: TextView
    private lateinit var card1value2: TextView
    private lateinit var card1value3: TextView
    private lateinit var card1value4: TextView
    private lateinit var card1value5: TextView
    // Numerical values
    private val card1timeValues = arrayOfNulls<String>(arraySizes)
    private val card1aqiValues = arrayOfNulls<Double>(arraySizes)
    private val card1pm25Values = arrayOfNulls<Double>(arraySizes)
    private val card1pm10Values = arrayOfNulls<Double>(arraySizes)
    private val card1no2Values = arrayOfNulls<Double>(arraySizes)
    private val card1o3Values = arrayOfNulls<Double>(arraySizes)
    // Comparison TextViews
    private lateinit var card1comp1: TextView
    private lateinit var card1comp2: TextView
    private lateinit var card1comp3: TextView
    private lateinit var card1comp4: TextView
    private lateinit var card1comp5: TextView

    //  C A R D 2
    // Title, time, rectangles
    private lateinit var card2title: TextView
    private lateinit var card2time: TextView
    private lateinit var card2aqiRect: View
    private lateinit var card2pm25Rect: View
    private lateinit var card2pm10Rect: View
    private lateinit var card2no2Rect: View
    private lateinit var card2o3Rect: View
    // Numerical TextViews
    private lateinit var card2value1: TextView
    private lateinit var card2value2: TextView
    private lateinit var card2value3: TextView
    private lateinit var card2value4: TextView
    private lateinit var card2value5: TextView
    // Numerical values
    private val card2timeValues = arrayOfNulls<String>(arraySizes)
    private val card2aqiValues = arrayOfNulls<Double>(arraySizes)
    private val card2pm25Values = arrayOfNulls<Double>(arraySizes)
    private val card2pm10Values = arrayOfNulls<Double>(arraySizes)
    private val card2no2Values = arrayOfNulls<Double>(arraySizes)
    private val card2o3Values = arrayOfNulls<Double>(arraySizes)
    // Comparison TextViews
    private lateinit var card2comp1: TextView
    private lateinit var card2comp2: TextView
    private lateinit var card2comp3: TextView
    private lateinit var card2comp4: TextView
    private lateinit var card2comp5: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        // Sets UI theme
        if (getSharedPreferenceValue("theme")) setTheme(R.style.DarkTheme)
        else setTheme(R.style.AppTheme)
        // Inflate layout
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast)
        //Find and initializes Views of both cards and sets replace button
    }

    override fun onResume() {
        super.onResume()
        // Set scroller
        this.forecast_time_scroller!!.setOnSeekBarChangeListener(this)
        // Sets the views
        initCardViews()
        //Set the info for cards
        setCardInfo()
        // Sets card 1 data
        initCard1()
        // Sets card 2 data if necessary
        initCard2()
        // Sets current hour of scroller
        val currentHour: Int = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        forecast_time_scroller.progress = currentHour
        // Set the companion objects with their info received from MapsActivity
    }
    override fun onBackPressed() {
        card2BeChanged = false
        super.onBackPressed()
    }

    private fun setCardInfo() {
        // Set the variable companion static variables appropriately
        if(card2BeChanged) {
            card2Lat = intent.getDoubleExtra("lat", 0.0)
            card2Lon = intent.getDoubleExtra("lon", 0.0)
            card2City = intent.getStringExtra("cityTitle")
        }
        else {
            card1Lat = intent.getDoubleExtra("lat", 0.0)
            card1Lon = intent.getDoubleExtra("lon", 0.0)
            card1City = intent.getStringExtra("cityTitle")
        }
    }

    private fun initCardViews() {
        // ------------ CARD 1 ------------
        // title, time, rects
        card1title = findViewById(R.id.forecast_card1_title)
        card1time = findViewById(R.id.forecast_card1_time)
        card1aqiRect = findViewById<View>(R.id.card1_unit1)
        card1pm25Rect = findViewById<View>(R.id.card1_unit2)
        card1pm10Rect = findViewById<View>(R.id.card1_unit3)
        card1no2Rect = findViewById<View>(R.id.card1_unit4)
        card1o3Rect = findViewById<View>(R.id.card1_unit5)
        // numerical TextViews
        card1value1 = findViewById(R.id.card1_value_1)
        card1value2 = findViewById(R.id.card1_value_2)
        card1value3 = findViewById(R.id.card1_value_3)
        card1value4 = findViewById(R.id.card1_value_4)
        card1value5 = findViewById(R.id.card1_value_5)
        // comparison TextViews
        card1comp1 = findViewById(R.id.card1_comp1)
        card1comp2 = findViewById(R.id.card1_comp2)
        card1comp3 = findViewById(R.id.card1_comp3)
        card1comp4 = findViewById(R.id.card1_comp4)
        card1comp5 = findViewById(R.id.card1_comp5)
        // Set replaceBtn1 instructions
        val replaceBtn1 : ImageView = findViewById(R.id.forecast_card1_replace)
        replaceBtn1.setOnClickListener {
            card2BeChanged = false
            card1City = "Add a card ->"
            card1Lat  = 0.0
            card1Lon = 0.0
            val intent = Intent(this, MapsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivityIfNeeded(intent, 0)
        }

        // --------------- CARD  2 -----------------
        // title, time, rects
        card2title = findViewById(R.id.forecast_card2_title)
        card2time = findViewById(R.id.forecast_card2_time)
        card2aqiRect = findViewById<View>(R.id.card2_unit1)
        card2pm25Rect = findViewById<View>(R.id.card2_unit2)
        card2pm10Rect = findViewById<View>(R.id.card2_unit3)
        card2no2Rect = findViewById<View>(R.id.card2_unit4)
        card2o3Rect = findViewById<View>(R.id.card2_unit5)
        // numerical TextViews
        card2value1 = findViewById(R.id.card2_value_1)
        card2value2 = findViewById(R.id.card2_value_2)
        card2value3 = findViewById(R.id.card2_value_3)
        card2value4 = findViewById(R.id.card2_value_4)
        card2value5 = findViewById(R.id.card2_value_5)
        // comparison TextViews
        card2comp1 = findViewById(R.id.card2_comp1)
        card2comp2 = findViewById(R.id.card2_comp2)
        card2comp3 = findViewById(R.id.card2_comp3)
        card2comp4 = findViewById(R.id.card2_comp4)
        card2comp5 = findViewById(R.id.card2_comp5)
        // Set replaceBtn2 instructions
        val replaceBtn2 : ImageView = findViewById(R.id.forecast_card2_replace)
        replaceBtn2.setOnClickListener {
            card2BeChanged = true
            card2City = "Add a card ->"
            card2Lat  = 0.0
            card2Lon = 0.0
            val intent = Intent(this, MapsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
            startActivityIfNeeded(intent, 0)
        }
    }

    private fun initCard1() {
        // Graph button of card 1
        forecast_card1_graph.setOnClickListener { booter.runGraphActivity(card1Lat, card1Lon, card1City) }
        // Set title of card 1
        card1title.text = card1City
        // sets data for card1
        doAsync {
            // in case it's null
            val weather = Client.client.getWeather(card1Lat, card1Lon).execute().body()
            for (i in card1aqiValues.indices + 1) {
                card1aqiValues[i] = weather?.data?.time?.get(i)?.variables?.aQI?.value
                card1pm25Values[i] = weather?.data?.time?.get(i)?.variables?.pm25Concentration?.value
                card1pm10Values[i] = weather?.data?.time?.get(i)?.variables?.pm10Concentration?.value
                card1no2Values[i] = weather?.data?.time?.get(i)?.variables?.no2Concentration?.value
                card1o3Values[i] = weather?.data?.time?.get(i)?.variables?.o3Concentration?.value
                card1timeValues[i] = weather?.data?.time?.get(i)?.from
            }
            uiThread {
                cardsChecked++
            }
        }
    }

    private fun initCard2() {
        // Graph button of card 2
        forecast_card2_graph.setOnClickListener { booter.runGraphActivity(card2Lat, card2Lon, card2City) }
        // Set title of card 2
        card2title.text = card2City
        // sets data for card 2
        doAsync {
            if (card2Lat != 0.0) {
                val weather = Client.client.getWeather(card2Lat, card2Lon).execute().body()
                for (i in card2aqiValues.indices + 1) {
                    card2aqiValues[i] = weather?.data?.time?.get(i)?.variables?.aQI?.value
                    card2pm25Values[i] = weather?.data?.time?.get(i)?.variables?.pm25Concentration?.value
                    card2pm10Values[i] = weather?.data?.time?.get(i)?.variables?.pm10Concentration?.value
                    card2no2Values[i] = weather?.data?.time?.get(i)?.variables?.no2Concentration?.value
                    card2o3Values[i] = weather?.data?.time?.get(i)?.variables?.o3Concentration?.value
                    card2timeValues[i] = weather?.data?.time?.get(i)?.from
                }
            } else this@ForecastActivity.card2IsEmpty = true
            uiThread {
                this@ForecastActivity.cardsChecked += 2
            }
        }
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        // as a way to prevent errors if not everything is ready
        if(cardsChecked == 3 && card2IsEmpty) handleChanges(progress, false)
        else if (cardsChecked == 3 && !card2IsEmpty) handleChanges(progress, true)
    }

    private fun updateComparedValues(card1Value: Double?, card2Value: Double?, comp1View : TextView, comp2View : TextView) {
        var difference = card1Value!! - card2Value!!
        // Card 1 difference textView
        if(difference < 0) comp1View.setTextColor(Color.parseColor("#C13500"))
        else  comp1View?.setTextColor(Color.parseColor("#1E90FF"))
        comp1View.text = difference.toString().substring(0,4)
        // Second card difference textView
        difference = card2Value - card1Value
        if(difference < 0) comp2View.setTextColor(Color.parseColor("#C13500"))
        else  comp2View?.setTextColor(Color.parseColor("#1E90FF"))
        comp2View.text = difference.toString().substring(0,4)
    }

    // Changes the numerical value and rectangle of card 1
    private fun handleChanges(progress: Int, card2IsActive : Boolean) {
        // --------- CARD 1 (no compare) -------------
        // set variables
        val aqi = card1aqiValues[progress]
        val pm25 = card1pm25Values[progress]
        val pm10 = card1pm10Values[progress]
        val no2 = card1no2Values[progress]
        val o3 = card1o3Values[progress]
        card1time.text = card1timeValues[progress]?.substring(11, 16)
        card1value1.text = aqi.toString().substring(0, 3)
        card1value2.text = pm25.toString().substring(0, 3)
        card1value3.text = pm10.toString().substring(0, 3)
        card1value4.text = no2.toString().substring(0, 4)
        card1value5.text = o3.toString().substring(0, 4)
        // Change colours
        changeRectColor(aqi, card1aqiRect)
        changeRectColor(pm25, card1pm25Rect)
        changeRectColor(pm10, card1pm10Rect)
        changeRectColor(no2, card1no2Rect)
        changeRectColor(o3, card1o3Rect)

        //  ------- CARD 2 (no compare) ---------
        if(card2IsActive) {
            // set variables
            val c2aqi = card2aqiValues[progress]
            val c2pm25 = card2pm25Values[progress]
            val c2pm10 = card2pm10Values[progress]
            val c2no2 = card2no2Values[progress]
            val c2o3 = card2o3Values[progress]
            card2time.text = card2timeValues[progress]?.substring(11, 16)
            card2value1.text = c2aqi.toString().substring(0, 3)
            card2value2.text = c2pm25.toString().substring(0, 3)
            card2value3.text = c2pm10.toString().substring(0, 3)
            card2value4.text = c2no2.toString().substring(0, 4)
            card2value5.text = c2o3.toString().substring(0, 4)
            // Comparison
            updateComparedValues(card1aqiValues[progress], card2aqiValues[progress], card1_comp1, card2_comp1)
            updateComparedValues(card1pm25Values[progress], card2pm25Values[progress], card1_comp2, card2_comp2)
            updateComparedValues(card1pm10Values[progress], card2pm10Values[progress], card1_comp3, card2_comp3)
            updateComparedValues(card1no2Values[progress], card2no2Values[progress], card1_comp4, card2_comp4)
            updateComparedValues(card1o3Values[progress], card2o3Values[progress], card1_comp5, card2_comp5)
            // Change colours
            changeRectColor(c2aqi, card2aqiRect)
            changeRectColor(c2pm25, card2pm25Rect)
            changeRectColor(c2pm10, card2pm10Rect)
            changeRectColor(c2no2, card2no2Rect)
            changeRectColor(c2o3, card2o3Rect)


        }
    }

    private fun changeRectColor(value: Double?, view: View) {
        if (value != null) {
            if (value >= 4) view.setBackgroundColor(Color.parseColor("#4900AC"))
            else if (value >= 3) view.setBackgroundColor(Color.parseColor("#C13500"))
            else if (value >= 2) view.setBackgroundColor(Color.parseColor("#FFCB00"))
            else view.setBackgroundColor(Color.parseColor("#3F9F41"))
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {

    }
    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
    private fun getSharedPreferenceValue(prefKey: String): Boolean {
        val sp = getSharedPreferences(MapsActivity.sharedPref, 0)
        return sp.getBoolean(prefKey, false)

    }
}