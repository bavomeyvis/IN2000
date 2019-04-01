import com.example.pollution.response.WeatherService

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

fun main() {
    print("API Data:\n")
    val retrofit = Retrofit.Builder()
        .baseUrl("https://in2000-apiproxy.ifi.uio.no/weatherapi/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(WeatherService::class.java)

    val weather = service.getWeather(60.1, 9.58).execute().body()
    print(weather)
    print("\nCheck if values above were extracted correctly...\n")
}