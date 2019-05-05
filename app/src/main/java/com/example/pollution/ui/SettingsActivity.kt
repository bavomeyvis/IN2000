package com.example.pollution.ui

// Main imports
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
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
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        // Toolbar
        val toolbar : Toolbar = findViewById(R.id.settings_toolbar)
        setSupportActionBar(toolbar)

        // Set buttons appropriately
        revertSettings()

        // get reference to button
        val themeBtn = findViewById<Switch>(R.id.settingsThemeBtn)

        // set on-change listener
        themeBtn.setOnCheckedChangeListener { _, isChecked ->
            //Toast.makeText(this, "isChecked $isChecked", Toast.LENGTH_LONG).show()
            writeToPreference("theme", isChecked)
        }

    }

    private fun revertSettings() {
        //Theme should return boolean
        val theme : Boolean = getSharedPreferenceValue("theme")
        Toast.makeText(this, "Theme contains value: $theme", Toast.LENGTH_LONG).show()
        //if (theme) settingsBtn1 = "on"
        //else settingsBtn1 = "off"
        settingsThemeBtn.isChecked = theme
    }


    // Gets the key from the static variable in sharedPref in MapsActivity
    private fun getSharedPreferenceValue(prefKey: String):Boolean {
        val sp = getSharedPreferences(MapsActivity.sharedPref, 0)
        print(sp.getBoolean(prefKey, false))
        return sp.getBoolean(prefKey, false)
    }


    // When user changes a value onclick, this method is called for
    private fun writeToPreference(prefKey:String, prefValue:Boolean) {
        val editor = getSharedPreferences(MapsActivity.sharedPref, 0).edit()
        editor.putBoolean(prefKey, prefValue)
        //Toast.makeText(this, "Put key $prefKey with value $prefValue", Toast.LENGTH_LONG).show()
        editor.apply()
    }
}
