package com.example.pollution.classes

import android.os.AsyncTask
import com.example.pollution.ui.AlertActivity

class CheckAlertConditions: AsyncTask<Double, Double, Boolean>() {
    override fun doInBackground(vararg params: Double?): Boolean {
        // If notifications are off: break.
        // Compare current location's AQI with threshold.
        //
        return false
    }

    override fun onPreExecute() {
        super.onPreExecute()
        TODO()
    }

    override fun onPostExecute(result: Boolean?) {
        super.onPostExecute(result)
        if (result == true) {
            // Send notification
        }
    }
}