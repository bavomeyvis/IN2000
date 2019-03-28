package com.example.pollution

import com.squareup.moshi.Json

data class AirQualityData (
    @field:Json(name = "product")
    var product: data? = null
)

    data class data(
        @field:Json(name = "data")
        var data: List<time>? = null
    ) {
        data class time(
            @field:Json(name = "time")
            var time: List<hour>? = null
        ) {
            data class hour(
                @field:Json(name = "hour")
                var variables: List<AQI>? = null
            ) {
                data class AQI(
                    @field:Json(name = "AQI")
                    var AQI: value? = null
                ) {
                    data class value(
                        @field:Json(name = "value")
                        var value: Int? = null
                    )
                }
            }
        }
    }