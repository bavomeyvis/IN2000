package com.example.pollution.ui

// Main imports
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AppCompatDelegate
// R = Resource. R.layout.x refers to layout in res.
import com.example.pollution.R


// RecyclerView
import android.support.v7.widget.Toolbar
import android.widget.Switch
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_settings.*


//AppCompatActivity: Base class for activities that use the support library action bar features.
//SettingsActivity: Represents UI of the settings menu
// Guidelines: https://www.androidhive.info/2016/01/android-working-with-recycler-view/
class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Set theme
        if(getSharedPreferenceValue("theme")) {
            setTheme(R.style.DarkTheme)
            MapsActivity.mapsActivity?.setTheme(R.style.DarkTheme)
            Toast.makeText(this, "Dark theme applied.", Toast.LENGTH_LONG).show()
        } else {
            setTheme(R.style.AppTheme)
            MapsActivity.mapsActivity?.setTheme(R.style.AppTheme)
            Toast.makeText(this, "Light theme applied.", Toast.LENGTH_LONG).show()
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
        // Toolbar
        val toolbar : Toolbar = findViewById(R.id.settings_toolbar)
        setSupportActionBar(toolbar)
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
}
