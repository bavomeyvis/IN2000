package com.example.pollution.classes

import android.os.AsyncTask
import android.os.Build
import com.example.pollution.ui.*
import java.time.LocalDate
import java.util.*

class CheckAlertConditions: AsyncTask<Double, Double, Boolean>() {
    override fun doInBackground(vararg params: Double?): Boolean {
        // First, check if the user has granted permission to receive notifications through settings.
        if (SettingsActivity.doNotDisturb)
            return false
        // Has the user turned on do not disturb?
        if (AlertActivity.doNotDisturb)
            return false
        // Is the current time within the user's selected time frame to not be disturbed?
        if (Build.VERSION.SDK_INT >= 26) {
            val date = LocalDate.now()
            val dow = date.dayOfWeek.value - 1
            if (WeekActivity.doNotDisturbWeek[dow]) {
                val time = Calendar.getInstance()
                val hours = time.get(Calendar.HOUR_OF_DAY)
                if (hours >= WeekActivity.maxValues[dow] && hours <= WeekActivity.minValues[dow])
                    return false
            }
        }
        // If the program reaches here, the user has given permission for the app to send the alert.
        val temp = params[0]?: 0.0
        // Does current location's AQI not exceed user set threshold?
        if (temp <= AlertActivity.threshold)
            return false
        // If all above are false, the app will send a notification.
        return true
    }

    override fun onPostExecute(result: Boolean?) {
        super.onPostExecute(result)
        if (result == true) {
            //dangerAlert(this, "channel0")
        }
    }
}
