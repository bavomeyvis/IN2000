package com.example.pollution.ui

// Main imports
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
// R = Resource. R.layout.x refers to layout in res.
import com.example.pollution.R
import com.example.pollution.settings.ListAdapter


// RecyclerView
import android.support.v7.widget.LinearLayoutManager


//AppCompatActivity: Base class for activities that use the support library action bar features.
//SettingsActivity: Represents UI of the settings menu
// Guidelines: https://www.androidhive.info/2016/01/android-working-with-recycler-view/
class SettingsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // sample data
        val titles = ArrayList<String>()
        val desc = ArrayList<String>()
        titles.add("Dark mode")
        desc.add("Only for the Gs")
        for (i in 1..20) {
            titles.add("Title")
            desc.add("Description")
        }

        titles.add("Privacy")
        desc.add("The reason why you lock the bathroom when taking a shit")



        // The setup of recycler view
        viewManager = LinearLayoutManager(this)
        viewAdapter = ListAdapter(titles, desc, this)
        recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        //decorations
        val mDividerItemDecoration = DividerItemDecoration(this, LinearLayoutManager.VERTICAL)
        recyclerView.addItemDecoration(mDividerItemDecoration)

    }
}
