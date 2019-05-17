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


//SettingsActivity: Represents UI of the settings menu
// Guidelines: https://www.androidhive.info/2016/01/android-working-with-recycler-view/
class SettingsActivity : AppCompatActivity() {
    companion object {
        // If the users want to disable notifications. Used in CheckAlertConditions.kt.
        var doNotDisturb = false
    }
    private var bravo = 0
    private val returnIntent = Intent()

    override fun onCreate(savedInstanceState: Bundle?) {
        // Set theme.
        if(getSharedPreferenceValue("theme"))
            setTheme(R.style.DarkTheme)
        else
            setTheme(R.style.AppTheme)

        // Displays activity_settings.
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // set settingsThemeBtn according to state.
        val theme : Boolean = getSharedPreferenceValue("theme")
        if(getSharedPreferenceValue("theme"))  settingsThemeBtn.isChecked = theme

        // Set button listener for settingsThemeBtn.
        val themeBtn : Switch = findViewById(R.id.settingsThemeBtn)
        themeBtn.setOnCheckedChangeListener { _, isChecked ->
            writeToPreference("theme", isChecked)
            recreate()
        }

        // Do not disturb switch.
        val alertBtn : Switch = findViewById(R.id.settingsAlertBtn)
        alertBtn.setOnCheckedChangeListener { _, isChecked ->
            doNotDisturb = isChecked
            writeToPreference("alert", isChecked)
            if (isChecked)
                Toast.makeText(this, getString(R.string.settings_alert_off), Toast.LENGTH_SHORT).show()
            else
                Toast.makeText(this, getString(R.string.settings_alert_on), Toast.LENGTH_SHORT).show()
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
        dropdown.onItemSelectedListener = object: OnItemSelectedListener {
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
                // Refresh the activity, if a new language is selected. Without this check, the app will refresh even when just opening the drop down.
                if (current != position) recreate()
                current = position
            }
        }

        // Toolbar
        /*val toolbar : Toolbar = findViewById(R.id.settings_toolbar)
        setSupportActionBar(toolbar) */
    }

    // The below functions make the views in the activity consistent by remembering their states.
    private fun changeLocalisation(language: String) {
        val locale = Locale(language)
        val config = baseContext.resources.configuration
        config.locale = locale
        baseContext.resources.updateConfiguration(config, baseContext.resources.displayMetrics)
    }

    private fun getSharedPreferenceValue(prefKey: String):Boolean {
        val sp = getSharedPreferences(MapsActivity.sharedPref, 0)
        return sp.getBoolean(prefKey, false)
    }

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
