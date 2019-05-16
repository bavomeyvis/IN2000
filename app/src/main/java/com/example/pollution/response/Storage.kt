package com.example.pollution.response

import android.content.Context
import com.example.pollution.ui.MapsActivity
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity

class Storage (context : Context) {
    val context: Context = context

    companion object {
        val sharedPref = "storage"
    }

    private fun getSharedPreferenceValue(prefKey: String):Boolean {
        val sp = context.getSharedPreferences(sharedPref, 0)
        return sp.getBoolean(prefKey, false)
    }
}