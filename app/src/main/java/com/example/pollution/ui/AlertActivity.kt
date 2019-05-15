package com.example.pollution.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.transition.Slide
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import com.example.pollution.R

class AlertActivity : AppCompatActivity() {
    private val returnIntent = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Displays activity_alert
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert)

        // Threshold bar
        val threshold: SeekBar = findViewById(R.id.seekBarThreshold)
        threshold.max = 500
        threshold.progress = getSharedPreferenceValueInt("thresholdValue")
        var start = 0
        var end = threshold.progress
        var current = threshold.progress
        var toast = Toast.makeText(applicationContext, getString(R.string.aqi_good), Toast.LENGTH_SHORT)
        threshold.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                writeToPreferenceInt("thresholdValue", progress)
                // Reference from source: https://airnow.gov/index.cfm?action=aqibasics.aqi
                if (current !in start..end) {
                    // Cancel toast unless a new AQI level is reached.
                    toast.cancel()
                    when (progress) {
                        in 0..50 -> {
                            toast = Toast.makeText(applicationContext, getString(R.string.aqi_good), Toast.LENGTH_SHORT)
                            start = 0
                            end = 50
                        }
                        in 51..100 -> {
                            toast = Toast.makeText(applicationContext, getString(R.string.aqi_moderate), Toast.LENGTH_SHORT)
                            start = 51
                            end = 100
                        }
                        in 101..150 -> {
                            toast = Toast.makeText(applicationContext, getString(R.string.aqi_potentially_unhealthy), Toast.LENGTH_SHORT)
                            start = 101
                            end = 150
                        }
                        in 151..200 -> {
                            toast = Toast.makeText(applicationContext, getString(R.string.aqi_unhealthy), Toast.LENGTH_SHORT)
                            start = 151
                            end = 200
                        }
                        in 201..300 -> {
                            toast = Toast.makeText(applicationContext, getString(R.string.aqi_very_unhealthy), Toast.LENGTH_SHORT)
                            start = 201
                            end = 300
                        }
                        in 301..500 -> {
                            toast = Toast.makeText(applicationContext, getString(R.string.aqi_hazardous), Toast.LENGTH_SHORT)
                            start = 301
                            end = 500
                        }
                    }
                    toast.show()
                }
                current = progress
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // DnD
        val silence: Switch = findViewById(R.id.switchDnD)
        silence.isChecked = getSharedPreferenceValueBool("silenceValue")
        var time = 5

        silence.setOnCheckedChangeListener{_, isChecked -> run {
            writeToPreferenceBool("silenceValue", isChecked)
            if (isChecked) {
                // If checked, a popup based on do_not_disturb.xml layout will appear.
                val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val view = inflater.inflate(R.layout.do_not_disturb, null)
                val popupWindow = PopupWindow(
                    view,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                // Fancy animations:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    popupWindow.elevation = 10.0F

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val slideIn = Slide()
                    slideIn.slideEdge = Gravity.TOP
                    popupWindow.enterTransition = slideIn

                    val slideOut = Slide()
                    slideOut.slideEdge = Gravity.RIGHT
                    popupWindow.exitTransition = slideOut
                }

                val wheel = view.findViewById<NumberPicker>(R.id.wheelview)
                val buttonPopup = view.findViewById<Button>(R.id.button_popup)

                val data = arrayOf(getString(R.string.one_hour), getString(R.string.three_hours), getString(R.string.one_day), getString(R.string.one_week), getString(R.string.one_month))
                wheel.minValue = 0
                wheel.maxValue = data.size - 1
                wheel.displayedValues = data

                /*
                Sets whether the selector wheel shown during flinging/scrolling should
                wrap around the minimum value and maximum value.
                */
                wheel.wrapSelectorWheel = false

                wheel.setOnValueChangedListener{_, _, newVal ->
                    time = newVal
                }

                buttonPopup.setOnClickListener{
                    // Dismiss the popup window
                    popupWindow.dismiss()
                }

                popupWindow.setOnDismissListener {
                    // TODO: bug: crashes when selecting first index.
                    Toast.makeText(applicationContext, getString(R.string.disable_alerts, data[time]), Toast.LENGTH_SHORT).show()
                }

                popupWindow.showAtLocation(
                    view,
                    Gravity.CENTER,
                    0, 0
                )
            } else {
                time = 5
                Toast.makeText(applicationContext, getString(R.string.cancel_disable_alerts), Toast.LENGTH_SHORT).show()
            }
        }}

        // DnD Week

        // The textViews act as buttons for opening activity_week.xml layout.
        val textViews: Array<TextView> = arrayOf(
            findViewById(R.id.AlertWeekTitle),
            findViewById(R.id.AlertWeekDesc)
        )
        for (i in textViews)
            i.setOnClickListener {
                val weekActivityIntent = Intent(this@AlertActivity, WeekActivity::class.java)
                startActivityForResult(weekActivityIntent, 1)
                recreate()
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