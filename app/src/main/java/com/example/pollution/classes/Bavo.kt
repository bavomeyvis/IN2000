package com.example.pollution.classes

import com.example.pollution.ui.MapsActivity


class Bavo {
    companion object {
        private fun getSharedPreferenceValue(prefKey: String):Boolean {
            val sp = getSharedPreferences(MapsActivity.sharedPref, 0)
            return sp.getBoolean(prefKey, false)
        }
    }
}