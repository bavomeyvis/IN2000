package com.example.pollution.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
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
    companion object {
        // These variables will be used in CheckAlertConditions.kt to compute whether app should send notification.
        var threshold = 0
        var doNotDisturb = false
    }
    private val returnIntent = Intent()
    private val hour : Long = 60 * 60 * 1000 // 1000 miliseconds = 1 second.
    private val threeHours: Long = 3 * hour
    private val day : Long = 24 * hour
    private val week : Long = 7 * day
    private val month : Long = 30 * day
    // If this is true, the countdown will cancel.
    private var isCancelled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Displays activity_alert
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert)

        // Set theme
        if (getSharedPreferenceValueBool("theme"))
            setTheme(R.style.DarkTheme)
        else
            setTheme(R.style.AppTheme)

        // Threshold bar
        val threshold: SeekBar = findViewById(R.id.seekBarThreshold)
        threshold.max = 500
        threshold.progress = getSharedPreferenceValueInt("thresholdValue")
        // start and end are the range of the current AQI level. Should the thumb go beyond the range, a new Toast will appear.
        var start = 0
        var end = threshold.progress
        var current = threshold.progress
        var toast = Toast.makeText(applicationContext, getString(R.string.aqi_good), Toast.LENGTH_SHORT)
        threshold.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                AlertActivity.threshold = progress
                writeToPreferenceInt("thresholdValue", progress)
                // Reference from source: https://airnow.gov/index.cfm?action=aqibasics.aqi
                if (current !in start..end) {
                    // Cancel toast, because a new AQI level is reached.
                    toast.cancel()
                    // Send new toast to display a reference and update start and end.
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
                // Update current.
                current = progress
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        // Do not disturb switch, which brings up a popup box.
        val silence = findViewById<Switch>(R.id.switchDnD)
        silence.isChecked = getSharedPreferenceValueBool("silenceValue")
        var time = 0 // 0, one hour; 1, three hours; 2, one day; 3, one week; 4, one month. 5 is just because the variable is a dummy variable for now.

        silence.setOnCheckedChangeListener{_, isChecked -> run {
            doNotDisturb = isChecked
            writeToPreferenceBool("silenceValue", isChecked)
            if (isChecked) {
                // If checked, a popup based on do_not_disturb.xml layout will appear.
                val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val view = inflater.inflate(R.layout.do_not_disturb, null)
                // The window should not take more space than it needs.
                val popupWindow = PopupWindow(
                    view,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )

                // Fancy animations, not essential.
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

                val wheel: NumberPicker = view.findViewById(R.id.wheelview)
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
                    time = newVal // Updates time, which is used to start the timer later.
                }

                buttonPopup.setOnClickListener{
                    // Dismiss the popup window.
                    popupWindow.dismiss()
                }

                popupWindow.setOnDismissListener {
                    Toast.makeText(applicationContext, getString(R.string.disable_alerts, data[time]), Toast.LENGTH_SHORT).show()
                    doNotDisturb = true
                    isCancelled = false
                    // Start the timer. Check if it should be cancelled every minute (1000 milliseconds * 60).
                    when (time) {
                        0 -> timer(hour, 1000 * 60)
                        1 -> timer(threeHours, 1000 * 60)
                        2 -> timer(day, 1000 * 60)
                        3 -> timer(week, 1000 * 60)
                        4 -> timer(month, 1000 * 60)
                    }
                }

                popupWindow.showAtLocation(
                    view,
                    Gravity.CENTER,
                    0, 0
                )
            } else {
                time = 5
                Toast.makeText(applicationContext, getString(R.string.cancel_disable_alerts), Toast.LENGTH_SHORT).show()
                // At the next countDownInterval, the timer will reset.
                isCancelled = true
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
                // Launch WeekActivity.
                if (Build.VERSION.SDK_INT >= 26) {
                    val weekActivityIntent = Intent(this@AlertActivity, WeekActivity::class.java)
                    startActivityForResult(weekActivityIntent, 1)
                    recreate()
                }
                // Unfortunately, this function is only supported by API 26 and higher.
                else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle(getString(R.string.alert_week_unavailable_title))
                    builder.setMessage(getString(R.string.alert_week_unavailable_desc))
                    builder.setNeutralButton(getString(R.string.dnd_exit)) {_,_ ->}
                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                }
            }
    }

    private fun timer(millisInFuture: Long, countDownInterval: Long): CountDownTimer {
        return object: CountDownTimer(millisInFuture, countDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                if (isCancelled) {
                    doNotDisturb = false
                    cancel()
                }
            }

            override fun onFinish() {
                doNotDisturb = false
                writeToPreferenceBool("silenceValue", false)
            }
        }
    }

    // The below functions make the views in the activity consistent by remembering their states.
    private fun getSharedPreferenceValueInt(prefKey: String):Int {
        val sp = getSharedPreferences(MapsActivity.sharedPref, 0)
        return sp.getInt(prefKey, 0)
    }

    private fun writeToPreferenceInt(prefKey:String, prefValue:Int) {
        val editor = getSharedPreferences(MapsActivity.sharedPref, 0).edit()
        editor.putInt(prefKey, prefValue)
        editor.apply()
    }

    private fun getSharedPreferenceValueBool(prefKey: String):Boolean {
        val sp = getSharedPreferences(MapsActivity.sharedPref, 0)
        return sp.getBoolean(prefKey, false)
    }

    private fun writeToPreferenceBool(prefKey:String, prefValue:Boolean) {
        val editor = getSharedPreferences(MapsActivity.sharedPref, 0).edit()
        editor.putBoolean(prefKey, prefValue)
        editor.apply()
    }

    // Forces main activity to always recreate().
    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, returnIntent)
        finish()
    }
}