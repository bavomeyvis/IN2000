package com.example.pollution.ui

import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.example.pollution.R
import com.example.pollution.data.APIData
import com.example.pollution.response.WeatherService
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.Utils
import org.jetbrains.anko.doAsync
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "GraphActivity"

class GraphActivity: AppCompatActivity() {
    private lateinit var graph: LineChart
    private lateinit var weather: APIData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)
        //Gets the data from the intent (lat, lon)
        val intent = intent
        val inputLat = intent.getDoubleExtra("lat", 0.0)
        val inputLon = intent.getDoubleExtra("lon", 0.0)
        val inputTitle = intent.getStringExtra("cityTitle")
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


        //
        val client = Retrofit.Builder()
            .baseUrl("https://in2000-apiproxy.ifi.uio.no/weatherapi/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)

        doAsync {
            weather = client.getWeather(inputLat, inputLon).execute().body()!!
            //Gets all the times from the weather object
            /*val timeArray = Array(weather.data.time.size) { "n = $it"}
            for (i in weather.data.time.indices) {
                timeArray[i] = weather.data.time[i].from
            }*/

            runOnUiThread {
                //Setting different settings for the graph
                graph = findViewById(R.id.lineChart)
                graph.isDragEnabled = true
                graph.setTouchEnabled(true)
                graph.setPinchZoom(true)
                graph.setScaleEnabled(true)
                graph.description.text = address.getAddressLine(0)
                graph.description.textColor = Color.GRAY
                graph.description.textSize = 13f
                graph.axisRight.isEnabled = false
                graph.animateX(1000)
                graph.setDrawBorders(true)
                graph.setBorderColor(Color.GRAY)
                graph.extraTopOffset = 10f
                graph.setNoDataText("Try clicking the graph. If no data shows up there is no data available here")

                //Creating the arrays of entries that contain the weather data
                val aqiData = arrayListOf<Entry>()
                val pm25Data = arrayListOf<Entry>()
                val pm10Data = arrayListOf<Entry>()
                val no2Data = arrayListOf<Entry>()
                val o3Data = arrayListOf<Entry>()
                for (x in weather.data.time.indices) {
                    println("Size of array is: " + weather.data.time.size)
                    val aqi = weather.data.time[x].variables.aQI.value
                    val pm25 = weather.data.time[x].variables.aQIPm25.value
                    val pm10 = weather.data.time[x].variables.aQIPm10.value
                    val no2 = weather.data.time[x].variables.aQINo2.value
                    val o3 = weather.data.time[x].variables.aQIO3.value

                    val aqiEntry = Entry(x.toFloat(), aqi.toFloat())
                    val pm25Entry = Entry(x.toFloat(), pm25.toFloat())
                    val pm10Entry = Entry(x.toFloat(), pm10.toFloat())
                    val no2Entry = Entry(x.toFloat(), no2.toFloat())
                    val o3Entry = Entry(x.toFloat(), o3.toFloat())

                    aqiData.add(aqiEntry)
                    pm25Data.add(pm25Entry)
                    pm10Data.add(pm10Entry)
                    no2Data.add(no2Entry)
                    o3Data.add(o3Entry)
                }

                //Adding the arrays of weather data to a LineDataSet and setting different settings for the line
                val setWeatherData1 = LineDataSet(aqiData, "AQI")
                setWeatherData1.axisDependency = YAxis.AxisDependency.LEFT
                setWeatherData1.color = Color.BLACK
                setWeatherData1.lineWidth = 3f
                setWeatherData1.mode = LineDataSet.Mode.CUBIC_BEZIER
                setWeatherData1.cubicIntensity = 0.1f
                setWeatherData1.setDrawValues(false)
                setWeatherData1.setDrawCircles(false)

                val setWeatherData2 = LineDataSet(pm25Data, "PM25")
                setWeatherData2.axisDependency = YAxis.AxisDependency.LEFT
                setWeatherData2.color = Color.BLUE
                setWeatherData2.lineWidth = 3f
                setWeatherData2.mode = LineDataSet.Mode.CUBIC_BEZIER
                setWeatherData2.cubicIntensity = 0.1f
                setWeatherData2.setDrawValues(false)
                setWeatherData2.setDrawCircles(false)

                val setWeatherData3 = LineDataSet(pm10Data, "PM10")
                setWeatherData3.axisDependency = YAxis.AxisDependency.LEFT
                setWeatherData3.color = Color.RED
                setWeatherData3.lineWidth = 3f
                setWeatherData3.mode = LineDataSet.Mode.CUBIC_BEZIER
                setWeatherData3.cubicIntensity = 0.1f
                setWeatherData3.setDrawValues(false)
                setWeatherData3.setDrawCircles(false)

                val setWeatherData4 = LineDataSet(no2Data, "NO2")
                setWeatherData4.axisDependency = YAxis.AxisDependency.LEFT
                setWeatherData4.color = Color.GREEN
                setWeatherData4.lineWidth = 3f
                setWeatherData4.mode = LineDataSet.Mode.CUBIC_BEZIER
                setWeatherData4.cubicIntensity = 0.1f
                setWeatherData4.setDrawValues(false)
                setWeatherData4.setDrawCircles(false)

                val setWeatherData5 = LineDataSet(o3Data, "O3")
                setWeatherData5.axisDependency = YAxis.AxisDependency.LEFT
                setWeatherData5.color = Color.YELLOW
                setWeatherData5.lineWidth = 3f
                setWeatherData5.mode = LineDataSet.Mode.CUBIC_BEZIER
                setWeatherData5.cubicIntensity = 0.1f
                setWeatherData5.setDrawValues(false)
                setWeatherData5.setDrawCircles(false)


                //Adding the LineDataSet to the list of datasets
                val dataSets = arrayListOf<ILineDataSet>()
                dataSets.add(setWeatherData1)
                dataSets.add(setWeatherData2)
                dataSets.add(setWeatherData3)
                dataSets.add(setWeatherData4)
                dataSets.add(setWeatherData5)

                //Adding the data to the graph
                val data = LineData(dataSets)
                graph.data = data
                graph.invalidate()

                //Setting different settings for the legend
                val legend = graph.legend
                legend.isEnabled = true
                legend.textSize = 15f
                legend.textColor = Color.GRAY
                legend.form = Legend.LegendForm.CIRCLE
                legend.formSize = 10f
                legend.formToTextSpace = 3f
                legend.yOffset = 5f
                legend.xEntrySpace = 30f

                val hours = arrayOf("01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00", "10:00", "11:00",
                    "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00",
                    "23:00", "00:00", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00",
                    "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00",
                    "20:00", "21:00", "22:00", "23:00", "00:00", "01:00")
                //Creating custom values for the x axis
                val xFormatter = object: ValueFormatter() {
                    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                        return hours[value.toInt()]
                    }
                }

                //Gets the current time and marks it on the x axis
                val currentTime = Calendar.getInstance()
                val format = SimpleDateFormat("HH", Locale.GERMAN)
                val time: String = format.format(currentTime.time)

                val currentTimeMark = LimitLine(time.toFloat()-1, "Current Time")
                currentTimeMark.lineWidth = 1f
                currentTimeMark.enableDashedLine(10f, 10f, 0f)
                currentTimeMark.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                currentTimeMark.textSize = 13f
                currentTimeMark.textColor = Color.GRAY

                //Setting different settings for the xAxis
                val xAxis = graph.xAxis
                xAxis.granularity = 1f
                xAxis.position = XAxis.XAxisPosition.TOP
                xAxis.textSize = 15f
                xAxis.textColor = Color.GRAY
                xAxis.valueFormatter = xFormatter
                xAxis.addLimitLine(currentTimeMark)


                val leftAxis = graph.axisLeft
                leftAxis.textSize = 15f
                leftAxis.textColor = Color.GRAY
                leftAxis.granularity = 0.01f

            }
        }
    }
}
