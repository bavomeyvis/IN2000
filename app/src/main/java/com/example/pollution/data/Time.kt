package com.example.pollution.data

data class Time(
    val from: String,
    val reason: Reason,
    val to: String,
    val variables: Variables
)