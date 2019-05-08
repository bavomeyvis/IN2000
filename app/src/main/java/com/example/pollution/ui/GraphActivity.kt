package com.example.pollution.ui

import android.graphics.Color
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.example.pollution.R
import com.example.pollution.data.APIData
import com.example.pollution.response.WeatherService
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.*
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.Utils
import org.jetbrains.anko.doAsync
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GraphActivity: AppCompatActivity() {
    private lateinit var graph: LineChart
    private lateinit var weather: APIData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)
        val intent = intent
        val lat = intent.getDoubleExtra(MapsActivity.LAT, 0.0)
        val lon = intent.getDoubleExtra(MapsActivity.LON, 0.0)


        //Must draw graph in background thread because of the apidata
        doAsync {
            //Fetching api data
            val client = Retrofit.Builder()
                .baseUrl("https://in2000-apiproxy.ifi.uio.no/weatherapi/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherService::class.java)

            weather = client.getWeather(lat, lon).execute().body()!!


            //Setting different settings for the graph
            graph = findViewById(R.id.lineChart)
            graph.isDragEnabled = true
            graph.setTouchEnabled(true)
            graph.setPinchZoom(true)
            graph.setScaleEnabled(true)
            graph.description.isEnabled = false
            graph.axisRight.isEnabled = false

            //Creating the array of entries that contain the weather data
            val weatherData = arrayListOf<Entry>()
            for (x in 0..48) {
                val aqi = weather.data.time.get(x).variables.aQI.value
                val entry = Entry(x.toFloat(), aqi.toFloat())
                weatherData.add(entry)
            }

            //Adding the array of weather data to a LineDataSet and setting different settings for the line
            val setWeatherData1 = LineDataSet(weatherData, "AQI")
            setWeatherData1.axisDependency = YAxis.AxisDependency.LEFT
            setWeatherData1.color = Color.BLACK
            setWeatherData1.setCircleColor(Color.BLACK)
            setWeatherData1.lineWidth = 1f
            setWeatherData1.valueTextSize = 10f
            setWeatherData1.setDrawFilled(true)
            setWeatherData1.setDrawCircleHole(false)
            setWeatherData1.enableDashedLine(5f, 5f, 0f)

            //Adding fill under the graph
            if (Utils.getSDKInt() >= 18) {
                val drawablePurple = ContextCompat.getDrawable(this@GraphActivity, R.drawable.fade)
                setWeatherData1.fillDrawable = drawablePurple
            } else {
                setWeatherData1.fillColor = Color.BLACK
            }

            //Adding the LineDataSet to the list of datasets
            val dataSets = arrayListOf<ILineDataSet>()
            dataSets.add(setWeatherData1)

            //Adding the data to the graph
            val data = LineData(dataSets)
            graph.data = data

            //Setting different settings for the legend
            val legend = graph.legend
            legend.isEnabled = true
            legend.textSize = 12f
            legend.textColor = Color.BLACK
            legend.form = Legend.LegendForm.LINE
            legend.formSize = 20f
            legend.formToTextSpace = 10f

            //Setting different settings for the xAxis
            val xAxis = graph.xAxis
            xAxis.granularity = 1f
            xAxis.position = XAxis.XAxisPosition.TOP
        }
    }
}
