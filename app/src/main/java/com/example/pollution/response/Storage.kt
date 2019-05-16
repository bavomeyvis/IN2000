package com.example.pollution.response

import com.example.pollution.ui.MapsActivity

class Storage {
    companion object {
        val sharedPref = "settings"
        var doNotDisturb = false
    }

    private fun getSharedPreferenceValue(prefKey: String):Boolean {
        val sp = getSharedPreferences(sharedPref, 0)
        return sp.getBoolean(prefKey, false)
    }
}