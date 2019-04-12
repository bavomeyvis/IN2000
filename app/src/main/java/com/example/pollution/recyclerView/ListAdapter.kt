import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.pollution.R


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
