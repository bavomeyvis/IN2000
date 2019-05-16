package com.example.pollution.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Switch
import android.widget.Toast
import com.example.pollution.R
import com.yahoo.mobile.client.android.util.rangeseekbar.RangeSeekBar

class WeekActivity : AppCompatActivity() {
    companion object {
        // These variables will be used in CheckAlertConditions.kt to compute whether app should send notification.
        val doNotDisturbWeek: Array<Boolean> = Array(7) {i -> false}
        val maxValues: Array<Int> = Array(7) {i -> 0}
        val minValues: Array<Int> = Array(7) {i -> (24)}
    }
    private val returnIntent = Intent()
    override fun onCreate(savedInstanceState: Bundle?) {
        // Displays activity_week
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_week)

        // Used for localisation.
        val weekdaysPlural = arrayOf(
            getString(R.string.week_monday_pl),
            getString(R.string.week_tuesday_pl),
            getString(R.string.week_wednesday_pl),
            getString(R.string.week_thursday_pl),
            getString(R.string.week_friday_pl),
            getString(R.string.week_saturday_pl),
            getString(R.string.week_sunday_pl)
        )

        // The seekBars and Switches: one for every day of the week. Since they all share the same functionalty, all their implementation will be done in a loop.
        val seekbars: Array<RangeSeekBar<Int>> = arrayOf(
            findViewById(R.id.seekBarMonday),
            findViewById(R.id.seekBarTuesday),
            findViewById(R.id.seekBarWednesday),
            findViewById(R.id.seekBarThursday),
            findViewById(R.id.seekBarFriday),
            findViewById(R.id.seekBarSaturday),
            findViewById(R.id.seekBarSunday)
        )
        val switches: Array<Switch> = arrayOf(
            findViewById(R.id.switchMonday),
            findViewById(R.id.switchTuesday),
            findViewById(R.id.switchWednesday),
            findViewById(R.id.switchThursday),
            findViewById(R.id.switchFriday),
            findViewById(R.id.switchSaturday),
            findViewById(R.id.switchSunday)
        )

        // Every view will have the same functionality, so a loop will do the job.
        for (i in 0..6) {
            // Save their states from last time.
            switches[i].isChecked = getSharedPreferenceValueBool("switchValue$i")
            // Seekbars.
            seekbars[i].isEnabled = switches[i].isChecked
            seekbars[i].setRangeValues(0, 24)
            // The processes are set to be where the user left them.
            seekbars[i].selectedMinValue = getSharedPreferenceValueInt("seekBarValue1$i")
            seekbars[i].selectedMaxValue = getSharedPreferenceValueInt("seekBarValue2$i")
            seekbars[i].setOnRangeSeekBarChangeListener(object : RangeSeekBar.OnRangeSeekBarChangeListener<Int> {
                override fun onRangeSeekBarValuesChanged(bar: RangeSeekBar<*>, minValue: Int, maxValue: Int) {
                    // Update values.
                    writeToPreferenceInt("seekBarValue1$i", minValue)
                    writeToPreferenceInt("seekBarValue2$i", maxValue)
                    minValues[i] = minValue
                    maxValues[i] = maxValue
                    // For reference.
                    Toast.makeText(applicationContext, getString(R.string.alert_time, minValues[i], maxValues[i]), Toast.LENGTH_SHORT).show()
                }
            })

            // Switches - they activate the do not disturb schedules within the desired time scope.
            switches[i].setOnCheckedChangeListener{_, isChecked -> run {
                writeToPreferenceBool("switchValue$i", isChecked)
                doNotDisturbWeek[i] = isChecked
                seekbars[i].isEnabled = isChecked
                if (isChecked)
                    Toast.makeText(applicationContext, getString(R.string.no_alert_weekday, minValues[i], maxValues[i], weekdaysPlural[i]), Toast.LENGTH_LONG).show()
                else
                    Toast.makeText(applicationContext, getString(R.string.allow_alert_weekday, weekdaysPlural[i]), Toast.LENGTH_LONG).show()
            }}
        }
    }

    // Gets the key from the static variable in sharedPref in MapsActivity
    private fun getSharedPreferenceValueInt(prefKey: String):Int {
        val sp = getSharedPreferences(MapsActivity.sharedPref, 0)
        return sp.getInt(prefKey, 0)
    }

    // When user changes a value onclick, this method is called for
    private fun writeToPreferenceInt(prefKey:String, prefValue:Int) {
        val editor = getSharedPreferences(MapsActivity.sharedPref, 0).edit()
        editor.putInt(prefKey, prefValue)
        editor.apply()
    }

    // Gets the key from the static variable in sharedPref in MapsActivity
    private fun getSharedPreferenceValueBool(prefKey: String):Boolean {
        val sp = getSharedPreferences(MapsActivity.sharedPref, 0)
        return sp.getBoolean(prefKey, false)
    }

    // When user changes a value onclick, this method is called for
    private fun writeToPreferenceBool(prefKey:String, prefValue:Boolean) {
        val editor = getSharedPreferences(MapsActivity.sharedPref, 0).edit()
        editor.putBoolean(prefKey, prefValue)
        editor.apply()
    }

    // Forces main activity to always recreate()
    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }
}