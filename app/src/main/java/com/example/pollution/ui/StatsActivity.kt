package com.example.pollution.ui

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.pollution.R

class StatsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        if(getSharedPreferenceValue("theme")) setTheme(R.style.DarkTheme)
        else setTheme(R.style.AppTheme)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
    }

    // TODO: Repetitive code
    private fun getSharedPreferenceValue(prefKey: String):Boolean {
        val sp = getSharedPreferences(MapsActivity.sharedPref, 0)
        return sp.getBoolean(prefKey, false)
    }
}
