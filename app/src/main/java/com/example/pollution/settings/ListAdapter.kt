package com.example.pollution.settings

import android.app.PendingIntent.getActivity
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import com.example.pollution.R
import com.example.pollution.ui.SettingsActivity

class ListAdapter(val titles: ArrayList<String>, val desc: ArrayList<String>, private val context: Context) : RecyclerView.Adapter<ListAdapter.ViewHolder>() {

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

        holder.switchBtn.setOnCheckedChangeListener { _, isChecked ->
            val msg = if (isChecked) "ON" else "OFF"
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val elemTitle: TextView = view.findViewById(R.id.elem_title)
        val elemDesc: TextView = view.findViewById(R.id.elem_desc)
        val switchBtn: Switch = view.findViewById(R.id.switchBtn)
    }
}