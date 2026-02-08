package com.example.valenbisi

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
/*
 * Adapter class for managing the list of incidents in a RecyclerView
 */
class IncidentAdapter(private val incidents: MutableList<IncidentReport>, private val onClick: (IncidentReport) -> Unit) :
    RecyclerView.Adapter<IncidentAdapter.ViewHolder>() {

    // ViewHolder class to hold and manage the view components for each incident item
    class ViewHolder(view: View, val context: android.content.Context) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.filtro_orden_title)  // TextView for incident title
        val category: TextView = view.findViewById(R.id.incident_category)  // TextView for category
        val status: TextView = view.findViewById(R.id.incident_status)  // TextView for incident status
        val statusImage: ImageView = view.findViewById(R.id.incident_statusimg)  // ImageView for status image
        val incidentImage: ImageView = view.findViewById(R.id.incident_image)  // ImageView for incident image
    }
    // Inflates the layout for each item in the RecyclerView
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.incident, parent, false)
        return ViewHolder(view, parent.context)
    }

    // Binds data to the ViewHolder for each item
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val incident = incidents[position]

        // Set the title and category
        holder.title.text = incident.title
        holder.category.text = incident.type

        // Set the status
        holder.status.text = incident.status

        // Change the status indicator image and text color based on the incident's status
        when (incident.status) {
            "Open" -> {
                holder.statusImage.setImageResource(R.drawable.green_circle) // Green indicator for open status
                holder.status.setTextColor(ContextCompat.getColor(holder.context, R.color.green_status)) // Green text color
            }
            "Processing" -> {
                holder.statusImage.setImageResource(R.drawable.blue_circle) // Blue indicator for processing status
                holder.status.setTextColor(ContextCompat.getColor(holder.context, R.color.blue_status)) // Change text color to blue
            }
            "Closed" -> {
                holder.statusImage.setImageResource(R.drawable.red_circle) // Red indicator for closed status
                holder.status.setTextColor(ContextCompat.getColor(holder.context, R.color.red)) // Change text color to red
            }
            else -> {
                holder.statusImage.setImageResource(R.drawable.blue_circle) // Default gray indicator
                holder.status.setTextColor(ContextCompat.getColor(holder.context, R.color.gray)) // Default color
            }
        }

        // Set the incident image if it exists (from ByteArray)
        incident.image?.let {
            val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
            holder.incidentImage.setImageBitmap(bitmap)
        }

        // Handle item click event
        holder.itemView.setOnClickListener { onClick(incident) }
    }

    // Updates the RecyclerView with a new list of incidents
    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newIncidents: List<IncidentReport>) {
        incidents.clear()
        incidents.addAll(newIncidents)
        notifyDataSetChanged()
    }
    // Returns the number of items in the dataset
    override fun getItemCount() = incidents.size
}