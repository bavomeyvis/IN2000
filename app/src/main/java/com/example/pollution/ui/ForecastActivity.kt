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
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView
import com.example.pollution.R
import com.example.pollution.response.WeatherService
import kotlinx.android.synthetic.main.activity_forecast.*
import kotlinx.android.synthetic.main.activity_forecast_card.view.*
import kotlinx.android.synthetic.main.activity_maps.*
import org.jetbrains.anko.doAsync
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*

private const val TAG = "ForecastActivity"

class ForecastActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {

    private var dataReceived: Boolean = false

    private val arraySizes = 49

    //CARD 1
    private val card1timeValues = arrayOfNulls<String>(arraySizes)

    private val card1aqiValues = arrayOfNulls<Double>(arraySizes)
    private val card1pm25Values = arrayOfNulls<Double>(arraySizes)
    private val card1pm10Values = arrayOfNulls<Double>(arraySizes)
    private val card1no2Values = arrayOfNulls<Double>(arraySizes)
    private val card1o3Values = arrayOfNulls<Double>(arraySizes)

    private lateinit var card1timeTextView: TextView
    private lateinit var card1aqiRectangle: View
    private lateinit var card1pm25Rectangle: View
    private lateinit var card1pm10Rectangle: View
    private lateinit var card1no2Rectangle: View
    private lateinit var card1o3Rectangle: View

    private var card1ValueTexts = arrayOfNulls<TextView>(5)
    private var card1CompareTexts = arrayOfNulls<TextView>(5)

    private lateinit var card1aqiTextView: TextView
    private lateinit var card1pm25TextView: TextView
    private lateinit var card1pm10TextView: TextView
    private lateinit var card1no2TextView: TextView
    private lateinit var card1o3TextView: TextView

    private lateinit var card1graphButton: ImageView
    private lateinit var card1_star: ImageView
    //END CARD 1


    //CARD 2
    private val card2timeValues = arrayOfNulls<String>(arraySizes)

    private val card2aqiValues = arrayOfNulls<Double>(arraySizes)
    private val card2pm25Values = arrayOfNulls<Double>(arraySizes)
    private val card2pm10Values = arrayOfNulls<Double>(arraySizes)
    private val card2no2Values = arrayOfNulls<Double>(arraySizes)
    private val card2o3Values = arrayOfNulls<Double>(arraySizes)

    private lateinit var card2timeTextView: TextView
    private lateinit var card2aqiRectangle: View
    private lateinit var card2pm25Rectangle: View
    private lateinit var card2pm10Rectangle: View
    private lateinit var card2no2Rectangle: View
    private lateinit var card2o3Rectangle: View

    private var card2ValueTexts = arrayOfNulls<TextView>(5)
    private var card2CompareTexts = arrayOfNulls<TextView>(5)

    private lateinit var card2aqiTextView: TextView
    private lateinit var card2pm25TextView: TextView
    private lateinit var card2pm10TextView: TextView
    private lateinit var card2no2TextView: TextView
    private lateinit var card2o3TextView: TextView

    private lateinit var card2graphButton: ImageView
    private lateinit var card2_star: ImageView
    //END CARD 2


    override fun onCreate(savedInstanceState: Bundle?) {
        // Sets UI theme
        if (getSharedPreferenceValue("theme")) setTheme(R.style.DarkTheme)
        else setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast)

        //Receives data from MapsActivity
        val intent = intent
        val inputLat = intent.getDoubleExtra(MapsActivity.LAT, 0.0)
        val inputLon = intent.getDoubleExtra(MapsActivity.LON, 0.0)
        val inputTitle = intent.getStringExtra(MapsActivity.TITLE)
        forecast_card2.graphButton2.setOnClickListener {
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

        //CARD 1
        card1timeTextView = findViewById<TextView>(R.id.forecast_card1_time)
        card1aqiRectangle = findViewById<View>(R.id.card1_unit1)
        card1pm25Rectangle = findViewById<View>(R.id.card1_unit2)
        card1pm10Rectangle = findViewById<View>(R.id.card1_unit3)
        card1no2Rectangle = findViewById<View>(R.id.card1_unit4)
        card1o3Rectangle = findViewById<View>(R.id.card1_unit5)

        card1ValueTexts[0] = findViewById<TextView>(R.id.card1_unit1_value)
        card1ValueTexts[1] = findViewById<TextView>(R.id.card1_unit2_value)
        card1ValueTexts[2] = findViewById<TextView>(R.id.card1_unit3_value)
        card1ValueTexts[3] = findViewById<TextView>(R.id.card1_unit4_value)
        card1ValueTexts[4] = findViewById<TextView>(R.id.card1_unit5_value)

        card1CompareTexts[0] = findViewById<TextView>(R.id.card1_comp1)
        card1CompareTexts[1] = findViewById<TextView>(R.id.card1_comp2)
        card1CompareTexts[2] = findViewById<TextView>(R.id.card1_comp3)
        card1CompareTexts[3] = findViewById<TextView>(R.id.card1_comp4)
        card1CompareTexts[4] = findViewById<TextView>(R.id.card1_comp5)

        card1aqiTextView = findViewById<View>(R.id.forecast_card1_units).findViewById<TextView>(R.id.textView3)
        card1pm25TextView = findViewById<View>(R.id.forecast_card1_units).findViewById<TextView>(R.id.textView4)
        card1pm10TextView = findViewById<View>(R.id.forecast_card1_units).findViewById<TextView>(R.id.textView5)
        card1no2TextView = findViewById<View>(R.id.forecast_card1_units).findViewById<TextView>(R.id.textView6)
        card1o3TextView = findViewById<View>(R.id.forecast_card1_units).findViewById<TextView>(R.id.textView7)

        card1aqiTextView.text = "AQI"
        card1pm25TextView.text = "PM2.5"
        card1pm10TextView.text = "PM10"
        card1no2TextView.text = "NO2"
        card1o3TextView.text = "O3"

        val card1placeNameTextView = findViewById<TextView>(R.id.textView2)
        card1placeNameTextView.text = address.getAddressLine(0)

        val card1lat = 60.0
        val card1lon = 10.0
        //END CARD 1

        //CARD 2
        card2timeTextView = findViewById<TextView>(R.id.forecast_card2_time)
        card2aqiRectangle = findViewById<View>(R.id.card2_unit1)
        card2pm25Rectangle = findViewById<View>(R.id.card2_unit2)
        card2pm10Rectangle = findViewById<View>(R.id.card2_unit3)
        card2no2Rectangle = findViewById<View>(R.id.card2_unit4)
        card2o3Rectangle = findViewById<View>(R.id.card2_unit5)

        card2ValueTexts[0] = findViewById<TextView>(R.id.card2_unit1_value)
        card2ValueTexts[1] = findViewById<TextView>(R.id.card2_unit2_value)
        card2ValueTexts[2] = findViewById<TextView>(R.id.card2_unit3_value)
        card2ValueTexts[3] = findViewById<TextView>(R.id.card2_unit4_value)
        card2ValueTexts[4] = findViewById<TextView>(R.id.card2_unit5_value)

        card2CompareTexts[0] = findViewById<TextView>(R.id.card2_comp1)
        card2CompareTexts[1] = findViewById<TextView>(R.id.card2_comp2)
        card2CompareTexts[2] = findViewById<TextView>(R.id.card1_comp3)
        card2CompareTexts[3] = findViewById<TextView>(R.id.card1_comp4)
        card2CompareTexts[4] = findViewById<TextView>(R.id.card1_comp5)

        card2aqiTextView = findViewById<View>(R.id.forecast_card2_units).findViewById<TextView>(R.id.textView3)
        card2pm25TextView = findViewById<View>(R.id.forecast_card2_units).findViewById<TextView>(R.id.textView4)
        card2pm10TextView = findViewById<View>(R.id.forecast_card2_units).findViewById<TextView>(R.id.textView5)
        card2no2TextView = findViewById<View>(R.id.forecast_card2_units).findViewById<TextView>(R.id.textView6)
        card2o3TextView = findViewById<View>(R.id.forecast_card2_units).findViewById<TextView>(R.id.textView7)

        card2aqiTextView.text = "AQI"
        card2pm25TextView.text = "PM2.5"
        card2pm10TextView.text = "PM10"
        card2no2TextView.text = "NO2"
        card2o3TextView.text = "O3"

        val card2placeNameTextView = findViewById<TextView>(R.id.card2textView2)
        card2placeNameTextView.text = address.getAddressLine(0)

        val card2lat = address.latitude
        val card2lon = address.longitude
        //END CARD 2


        val client = Retrofit.Builder()
            .baseUrl("https://in2000-apiproxy.ifi.uio.no/weatherapi/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)


        doAsync {
            val card1weather = client.getWeather(card1lat, card1lon).execute().body()
            val card2weather = client.getWeather(card2lat, card2lon).execute().body()

            for (i in card2aqiValues.indices + 1) {

                //CARD1
                card1aqiValues[i] = card1weather?.data?.time?.get(i)?.variables?.aQI?.value
                card1pm25Values[i] = card1weather?.data?.time?.get(i)?.variables?.aQIPm25?.value
                card1pm10Values[i] = card1weather?.data?.time?.get(i)?.variables?.aQIPm10?.value
                card1no2Values[i] = card1weather?.data?.time?.get(i)?.variables?.aQINo2?.value
                card1o3Values[i] = card1weather?.data?.time?.get(i)?.variables?.aQIO3?.value
                card1timeValues[i] = card1weather?.data?.time?.get(i)?.from

                //CARD 2
                card2aqiValues[i] = card2weather?.data?.time?.get(i)?.variables?.aQI?.value
                card2pm25Values[i] = card2weather?.data?.time?.get(i)?.variables?.aQIPm25?.value
                card2pm10Values[i] = card2weather?.data?.time?.get(i)?.variables?.aQIPm10?.value
                card2no2Values[i] = card2weather?.data?.time?.get(i)?.variables?.aQINo2?.value
                card2o3Values[i] = card2weather?.data?.time?.get(i)?.variables?.aQIO3?.value
                card2timeValues[i] = card2weather?.data?.time?.get(i)?.from

            }

            val rightNow = Calendar.getInstance()
            val currentHourIn24Format = rightNow.get(Calendar.HOUR_OF_DAY) - 1

            updateColorViews(currentHourIn24Format)
            forecast_time_scroller.progress = currentHourIn24Format

            dataReceived = true
        }
    }

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

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
//        if (dataReceived) {
        updateColorViews(progress)

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

        println("progress: " + progress)

//        }
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
        //CARD 1
        card1timeTextView.text = card1timeValues[progress]?.substring(11, 16)

        updateColorViews2(card1aqiValues[progress], card1aqiRectangle)
        updateColorViews2(card1pm25Values[progress], card1pm25Rectangle)
        updateColorViews2(card1pm10Values[progress], card1pm10Rectangle)
        updateColorViews2(card1no2Values[progress], card1no2Rectangle)
        updateColorViews2(card1o3Values[progress], card1o3Rectangle)

        card1ValueTexts[0]?.text = card1aqiValues[progress].toString().substring(0, 3)
        card1ValueTexts[1]?.text = card1pm25Values[progress].toString().substring(0, 3)
        card1ValueTexts[2]?.text = card1pm10Values[progress].toString().substring(0, 3)
        card1ValueTexts[3]?.text = card1no2Values[progress].toString().substring(0, 3)
        card1ValueTexts[4]?.text = card1o3Values[progress].toString().substring(0, 3)
        //END CARD 1

        //CARD 2
        card2timeTextView.text = card2timeValues[progress]?.substring(11, 16)

        updateColorViews2(card2aqiValues[progress], card2aqiRectangle)
        updateColorViews2(card2pm25Values[progress], card2pm25Rectangle)
        updateColorViews2(card2pm10Values[progress], card2pm10Rectangle)
        updateColorViews2(card2no2Values[progress], card2no2Rectangle)
        updateColorViews2(card2o3Values[progress], card2o3Rectangle)

        card2ValueTexts[0]?.text = card2aqiValues[progress].toString().substring(0, 3)
        card2ValueTexts[1]?.text = card2pm25Values[progress].toString().substring(0, 3)
        card2ValueTexts[2]?.text = card2pm10Values[progress].toString().substring(0, 3)
        card2ValueTexts[3]?.text = card2no2Values[progress].toString().substring(0, 3)
        card2ValueTexts[4]?.text = card2o3Values[progress].toString().substring(0, 3)
        //END CARD 2

    }

    private fun getSharedPreferenceValue(prefKey: String): Boolean {
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
