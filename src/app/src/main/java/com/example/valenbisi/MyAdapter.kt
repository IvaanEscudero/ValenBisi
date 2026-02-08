package com.example.valenbisi

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

/*
 * Class to populate the recycler view of bike stations, with its details, in the main station
 */
class MyAdapter(private val data: List<BikeStation>, private val onImageClick: (BikeStation) -> Unit) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    // Retrieve the elements of the view
    class MyViewHolder(row: View) : RecyclerView.ViewHolder(row) {
        val idParada: TextView = row.findViewById(R.id.idParada)
        val nombreParada: TextView = row.findViewById(R.id.nombreParada)
        val bikesNumber: TextView = row.findViewById(R.id.bikesNumber)
        val bikeStation: ImageView = row.findViewById(R.id.bikeStation)
        val bikeNumber: TextView = row.findViewById(R.id.bikeNumber)
        val ticketType: TextView = row.findViewById(R.id.ticketType)
        val locationNumber: TextView = row.findViewById(R.id.locationNumber)
        val tiempoActualizado: TextView = row.findViewById(R.id.tiempoActualizado)
        val statustxt: TextView = row.findViewById(R.id.statustxt)
        val statusimg: ImageView = row.findViewById(R.id.statusimg)
        val vistaAvanzada: ImageView = row.findViewById(R.id.vistaAvanzada)
    }

    // Load the view that we're going to modify and use to populate the recycler view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layout = LayoutInflater.from(parent.context).inflate(
            R.layout.station,
            parent, false
        )
        return MyViewHolder(layout)
    }

    //
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val station: BikeStation = data[position]
        val context = holder.itemView.context

        // Set text to TextViews
        holder.idParada.text = "${station.number}." // StationID
        holder.nombreParada.text = station.address  // Station address
        holder.bikesNumber.text = station.total.toString()  // Total bikes capacity
        holder.bikeNumber.text = station.available.toString()  // Available bikes
        holder.ticketType.text = station.ticket  // Ticket type
        holder.locationNumber.text = if (station.distance < 1000) {
            "${station.distance.toInt()} m" // Display distance in meters without decimals when less than 1000 meters
        } else {
            "${(station.distance / 1000).toInt()} km" // Display distance in kilometers without decimals
        }
        holder.tiempoActualizado.text = station.updatedAt  // Last updated timestamp

        // Set status text, image, and color base on its state
        val isOpen = station.open == "T"
        holder.statustxt.text = if (isOpen) "Open" else "Closed"
        holder.statustxt.setTextColor(
            if (isOpen) ContextCompat.getColor(context, R.color.green)
            else ContextCompat.getColor(context, R.color.red)
        )
        holder.statusimg.setImageResource(
            if (isOpen) R.drawable.green_circle
            else R.drawable.red_circle
        )

        // Handle click on the advanced view (detail click)
        holder.vistaAvanzada.setOnClickListener {
            onImageClick(station)  // Pass the whole BikeStation object
        }

        // Set color and image based on available bikes
        val (color, imageRes) = when {
            station.available > 10 -> Pair(ContextCompat.getColor(context, R.color.green), R.drawable.bike) // Green → bike
            station.available in 5..10 -> Pair(ContextCompat.getColor(context, R.color.orange), R.drawable.bike2) // Orange → bike2
            else -> Pair(ContextCompat.getColor(context, R.color.red), R.drawable.bike3) // Red → bike3
        }

        // Apply text color and image resource
        holder.bikeNumber.setTextColor(color)
        holder.bikeStation.setImageResource(imageRes)
    }

    override fun getItemCount(): Int = data.size
}