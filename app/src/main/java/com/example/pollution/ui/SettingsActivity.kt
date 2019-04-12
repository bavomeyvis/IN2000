package com.example.pollution.ui

// Main imports
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.os.Bundle
// R = Resource. R.layout.x refers to layout in res.
import com.example.pollution.R


// RecyclerView
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView


//AppCompatActivity: Base class for activities that use the support library action bar features.
//SettingsActivity: Represents UI of the settings menu
class SettingsActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // R E C Y L E R V I E W
        val titles = ArrayList<String>()
        val desc = ArrayList<String>()
        for (i in 1..2) {
            titles.add("Name $i")
            desc.add("This is just description nr. $i")
        }

        // The setup
        viewManager = LinearLayoutManager(this)
        viewAdapter = ListAdapter(titles, desc)
        recyclerView = findViewById<RecyclerView>(R.id.recycler_view).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    class ListAdapter(val titles: ArrayList<String>, val desc: ArrayList<String>) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = titles.size

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListAdapter.ViewHolder {
            // create a new view
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.settings_element, parent, false) as View
            return ViewHolder(itemView)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.elemTitle.text = titles[position]
            holder.elemDesc.text = desc[position]
        }

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val elemTitle: TextView = view.findViewById(R.id.elem_title)
            val elemDesc: TextView = view.findViewById(R.id.elem_desc)
        }
    }
}


/*Du skal selv legge til to elementer slik at listen ikke er tom ved første kjøring.
- Det skal ikke være mulig å legge til elementer i listen med tom input.
- Nye elementer trenger ikke å bli lagret til en senere kjøring*/
