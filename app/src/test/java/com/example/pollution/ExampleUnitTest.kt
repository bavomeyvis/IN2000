package com.example.pollution

import org.junit.Test
import com.example.pollution.response.WeatherService
import org.junit.Assert.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://in2000-apiproxy.ifi.uio.no/weatherapi/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(WeatherService::class.java)

        val weather = service.getWeather(59.915780, 10.752913).execute().body()
        assertEquals(weather?.data?.time?.get(1)?.variables?.aQI?.value, 1.7429293394088745) // 1.6452500820159912
    }
}
