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
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
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

        //
        val client = Retrofit.Builder()
            .baseUrl("https://in2000-apiproxy.ifi.uio.no/weatherapi/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherService::class.java)

        doAsync {
            weather = client.getWeather(59.91273, 10.74609).execute().body()!!

            runOnUiThread {
                //Setting different settings for the graph
                graph = findViewById(R.id.lineChart)
                graph.isDragEnabled = true
                graph.setTouchEnabled(true)
                graph.setPinchZoom(true)
                graph.setScaleEnabled(true)
                graph.description.text = "Oslo, Norge"
                graph.description.textColor = Color.GRAY
                graph.description.textSize = 10f
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
                for (x in 0..48) {
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

                val hours = arrayOf("0h", "1h", "2h", "3h", "4h", "5h", "6h", "7h", "8h", "9h", "10h",
                    "11h", "12h", "13h", "14h", "15h", "16h", "17h", "18h", "19h", "20h",
                    "21h", "22h", "23h", "24h", "25h", "26h", "27h", "28h", "29h", "30h",
                    "31h", "32h", "33h", "34h", "35h", "36h", "37h", "38h", "39h", "40h",
                    "41h", "42h", "43h", "44h", "45h", "46h", "47h", "48h")
                //Creating custom values for the x axis
                val xFormatter = object:ValueFormatter() {
                    override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                        return hours[value.toInt()]
                    }
                }

                //Setting different settings for the xAxis
                val xAxis = graph.xAxis
                xAxis.granularity = 1f
                xAxis.position = XAxis.XAxisPosition.TOP
                xAxis.textSize = 15f
                xAxis.textColor = Color.GRAY
                xAxis.valueFormatter = xFormatter

                val leftAxis = graph.axisLeft
                leftAxis.textSize = 15f
                leftAxis.textColor = Color.GRAY
                leftAxis.granularity = 0.01f

            }
        }
    }
}
