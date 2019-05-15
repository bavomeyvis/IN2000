package com.example.pollution.ui

// Main imports
import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatDelegate
import com.example.pollution.R
import android.support.v7.widget.Toolbar
import android.widget.*
import kotlinx.android.synthetic.main.activity_settings.*
import org.jetbrains.anko.find
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.util.Log
import android.view.View
import java.util.*
import android.widget.Toast
import android.widget.SeekBar




//AppCompatActivity: Base class for activities that use the support library action bar features.
//SettingsActivity: Represents UI of the settings menu
// Guidelines: https://www.androidhive.info/2016/01/android-working-with-recycler-view/
class SettingsActivity : AppCompatActivity() {
    private var bravo = 0
    private val returnIntent = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set theme
        if(getSharedPreferenceValue("theme")) {
            setTheme(R.style.DarkTheme)
            MapsActivity.mapsActivity?.setTheme(R.style.DarkTheme)
        } else {
            setTheme(R.style.AppTheme)
            MapsActivity.mapsActivity?.setTheme(R.style.AppTheme)
        }
        // Displays activity_settings
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // set settingsThemeBtn according to state
        val theme : Boolean = getSharedPreferenceValue("theme")
        if(getSharedPreferenceValue("theme"))  settingsThemeBtn.isChecked = theme

        // Set button listener for settingsThemeBtn
        val themeBtn : Switch = findViewById(R.id.settingsThemeBtn)
        themeBtn.setOnCheckedChangeListener { _, isChecked ->
            writeToPreference("theme", isChecked)
            recreate()
        }

        val alertBtn : Switch = findViewById(R.id.settingsAlertBtn)
        alertBtn.setOnCheckedChangeListener { _, isChecked ->
            writeToPreference("alert", isChecked)
        }

        findViewById<TextView>(R.id.settingsVersionDesc)
        alertBtn.setOnClickListener {
            if(++bravo == 3) Toast.makeText(this,R.string.bravo, Toast.LENGTH_LONG).show()
        }

        // Language drop-down menu.
        val dropdown: Spinner = findViewById(R.id.spinner)
        val languages = listOf("English", "Norsk (bokmål)", "Norsk (nynorsk)", "Nederlands", "Afrikaans")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, languages)
        dropdown.adapter = adapter
        // Set the default selection to be the previously selected one.
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val language = preferences.getString("language", "")
        if (!language.equals("", true)) {
            val pos = adapter.getPosition(language)
            dropdown.setSelection(pos)
        }
        var current = dropdown.selectedItemPosition
        // On usage:
        dropdown.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                // Apply selected change.
                val editor = preferences.edit()
                editor.putString("language", dropdown.selectedItem.toString())
                editor.apply()
                // Switch to selected language.
                when (position) {
                    0 -> changeLocalisation("")
                    1 -> changeLocalisation("no")
                    2 -> changeLocalisation("nn")
                    3 -> changeLocalisation("nl")
                    4 -> changeLocalisation("af")
                }
                // Refresh the activity, if a new language is selected.
                if (current != position) recreate()
                current = position
            }
        }

        // Number wheel for threshold.
        val data = Array(100) {i -> (i + 1).toString()}
        val number_wheel: NumberPicker = findViewById(R.id.wheelview)
        number_wheel.minValue = 0
        number_wheel.maxValue = data.size - 1
        number_wheel.displayedValues = data

        val seekBar: SeekBar = findViewById(R.id.seekBar)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar, progress: Int,
                fromUser: Boolean
            ) {
                Toast.makeText(applicationContext, "seekbar progress: $progress", Toast.LENGTH_SHORT).show()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                //Toast.makeText(applicationContext, "seekbar touch started!", Toast.LENGTH_SHORT).show()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                //Toast.makeText(applicationContext, "seekbar touch stopped!", Toast.LENGTH_SHORT).show()
            }
        })


        // Toolbar
        /*val toolbar : Toolbar = findViewById(R.id.settings_toolbar)
        setSupportActionBar(toolbar) */

    }

    // Change local strings.
    private fun changeLocalisation(language: String) {
        val locale = Locale(language)
        val config = baseContext.resources.configuration
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }

    // Gets the key from the static variable in sharedPref in MapsActivity
    private fun getSharedPreferenceValue(prefKey: String):Boolean {
        val sp = getSharedPreferences(MapsActivity.sharedPref, 0)
        return sp.getBoolean(prefKey, false)

    }

    // When user changes a value onclick, this method is called for
    private fun writeToPreference(prefKey:String, prefValue:Boolean) {
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
