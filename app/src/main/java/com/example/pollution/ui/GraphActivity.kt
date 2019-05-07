package com.example.pollution.ui

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.pollution.R
import com.example.pollution.data.APIData
import com.example.pollution.response.WeatherService
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import org.jetbrains.anko.doAsync
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GraphActivity: AppCompatActivity() {
    private lateinit var graph: LineChart
    private lateinit var weather: APIData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        doAsync {
            val client = Retrofit.Builder()
                .baseUrl("https://in2000-apiproxy.ifi.uio.no/weatherapi/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherService::class.java)

            weather = client.getWeather(59.915780, 10.752913).execute().body()
        }

        graph = findViewById(R.id.lineChart)

//        graph.onChartGestureListener(this)
        //       graph.setOnChartValueSelectedListener(this)
        graph.isDragEnabled = true
        graph.setScaleEnabled(true)
        graph.setDrawGridBackground(false)
        graph.setDrawBorders(false)

        val upper_limit = LimitLine(150000f, "Danger")
        upper_limit.lineWidth = 4f
        upper_limit.enableDashedLine(10f, 10f, 0f)
        upper_limit.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
        upper_limit.textSize = 15f

        val lower_limit = LimitLine(100000f, "Too low")
        lower_limit.lineWidth = 4f
        lower_limit.enableDashedLine(10f, 10f, 10f)
        lower_limit.labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM
        lower_limit.textSize = 15f

        val leftAxis = graph.axisLeft
        leftAxis.removeAllLimitLines()
        leftAxis.addLimitLine(upper_limit)
        leftAxis.addLimitLine(lower_limit)
        leftAxis.axisMaximum = 160000f
        leftAxis.axisMinimum = 0f
        leftAxis.enableGridDashedLine(10f, 10f, 0F)
        leftAxis.setDrawLimitLinesBehindData(true)

        graph.axisRight.isEnabled = false


        val valsComp1 = arrayListOf<Entry>()
        val valsComp2 = arrayListOf<Entry>()
        val weatherData = arrayListOf<Entry>()

        for (x in 0..48) {
            val aqi = weather?.data?.time?.get(x)?.variables?.aQI?.value
            val entry = Entry(xf, aqif)
        }


        val c1e1 = Entry(0f, 100000f)
        val c1e2 = Entry(1f, 140000f)
        val c1e3 = Entry(2f, 120000f)
        val c1e4 = Entry(3f, 140000f)
        valsComp1.add(c1e1)
        valsComp1.add(c1e2)
        valsComp1.add(c1e3)
        valsComp1.add(c1e4)

        val c2e1 = Entry(0f, 130000f)
        val c2e2 = Entry(1f, 115000f)
        val c2e3 = Entry(2f, 90000f)
        val c2e4 = Entry(3f, 105000f)
        valsComp2.add(c2e1)
        valsComp2.add(c2e2)
        valsComp2.add(c2e3)
        valsComp2.add(c2e4)

        val setComp1 = LineDataSet(valsComp1, "Company 1")
        val setComp2 = LineDataSet(valsComp2, "Company 2")
        setComp1.axisDependency = YAxis.AxisDependency.LEFT
        setComp2.axisDependency = YAxis.AxisDependency.LEFT

        setComp1.color = Color.RED
        setComp1.lineWidth = 3f
        setComp1.valueTextSize = 10f

        setComp2.color = Color.BLUE
        setComp2.lineWidth = 3f
        setComp2.valueTextSize = 10f

        val dataSets = arrayListOf<ILineDataSet>()
        dataSets.add(setComp1)
        dataSets.add(setComp2)

        val data = LineData(dataSets)
        graph.data = data
        graph.invalidate()

        val quarters = arrayOf("Q1", "Q2", "Q3", "Q4")

        val formatter = object : ValueFormatter() {
            override fun getAxisLabel(value:Float, axis:AxisBase):String {
                return quarters[value.toInt()]
            }
        }

        val xAxis = graph.xAxis
        xAxis.granularity = 1f
        xAxis.valueFormatter = formatter
        xAxis.position = XAxis.XAxisPosition.BOTTOM
    }
}
