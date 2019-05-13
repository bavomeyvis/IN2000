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
    private val returnIntent = Intent()
    override fun onCreate(savedInstanceState: Bundle?) {
        // Displays activity_week
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_week)

        val weekdaysPlural = arrayOf(
            getString(R.string.week_monday_pl),
            getString(R.string.week_tuesday_pl),
            getString(R.string.week_wednesday_pl),
            getString(R.string.week_thursday_pl),
            getString(R.string.week_friday_pl),
            getString(R.string.week_saturday_pl),
            getString(R.string.week_sunday_pl)
        )
        val maxValues: Array<Double?> = Array(7) {i -> (0.00)}
        val minValues: Array<Double?> = Array(7) {i -> (24.00)}
        val seekbars: Array<RangeSeekBar<Double>> = arrayOf(
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

        for (i in 0..6) {
            seekbars[i].setRangeValues(0.00, 24.00)
            seekbars[i].setOnRangeSeekBarChangeListener(object : RangeSeekBar.OnRangeSeekBarChangeListener<Double> {
                override fun onRangeSeekBarValuesChanged(bar: RangeSeekBar<*>?, minValue: Double?, maxValue: Double?) {
                    minValues[i] = minValue
                    maxValues[i] = maxValue
                    Toast.makeText(applicationContext, getString(R.string.alert_time, minValues[i], maxValues[i]), Toast.LENGTH_SHORT).show()
                }
            })

            switches[i].setOnCheckedChangeListener{_, isChecked -> run {
                if (isChecked)
                    Toast.makeText(applicationContext, getString(R.string.no_alert_weekday, minValues[i], maxValues[i], weekdaysPlural[i]), Toast.LENGTH_LONG).show()
                else
                    Toast.makeText(applicationContext, getString(R.string.allow_alert_weekday, weekdaysPlural[i]), Toast.LENGTH_LONG).show()
            }}
        }
    }

    // Forces main activity to always recreate()
    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }
}