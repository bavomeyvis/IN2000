package com.example.pollution.data

import com.google.gson.annotations.SerializedName

//specify gson is used.
data class Variables(
    @SerializedName("AQI")
    val aQI: AQI,
    @SerializedName("AQI_no2")
    val aQINo2: AQINo2,
    @SerializedName("AQI_o3")
    val aQIO3: AQIO3,
    @SerializedName("AQI_pm10")
    val aQIPm10: AQIPm10,
    @SerializedName("AQI_pm25")
    val aQIPm25: AQIPm25,
    @SerializedName("no2_concentration")
    val no2Concentration: No2Concentration,
    @SerializedName("o3_concentration")
    val o3Concentration: O3Concentration,
    @SerializedName("pm10_concentration")
    val pm10Concentration: Pm10Concentration,
    @SerializedName("pm25_concentration")
    val pm25Concentration: Pm25Concentration
)