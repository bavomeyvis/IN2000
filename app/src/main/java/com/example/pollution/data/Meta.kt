package com.example.pollution.data

data class Meta(
    val location: Location,
    val reftime: String,
    val sublocations: List<Any>,
    val superlocation: Superlocation
)