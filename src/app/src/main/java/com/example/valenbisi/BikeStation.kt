package com.example.valenbisi

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/*
* Data class of Bike Station
 */
@Parcelize
data class BikeStation(
    val address: String,     // Address of the bike station
    val number: Int,         // ID or number for the station
    val open: String,        // Indicates if the station is open ("T" for true, "F" for false)
    val available: Int,      // Number of available bikes at the station
    val free: Int,           // Number of free bike slots
    val total: Int,          // Total capacity of the station (available + free)
    val ticket: String,      // Indicates if a ticket machine is available ("T" or "F")
    val updatedAt: String,   // Last time the station information was updated
    val geoShape: GeoShape,  // Geographical data about the station's shape
    val geoPoint2d: GeoPoint, // The 2D geographical point (longitude and latitude)
    val updateJCD: String,   // ID or timestamp for internal updates
    val distance: Double     // Distance from the user's current location to this station
) : Parcelable {
    // The class implements Parcelable to allow passing objects between Android components

    // This nested class represents the geographical shape of the bike station
    @Parcelize
    data class GeoShape(
        val type: String,
        val geometry: Geometry,
        val properties: Map<String, String> = emptyMap()
    ) : Parcelable
    // The class implements Parcelable to allow passing objects between Android components

    // This nested class represents the geometry of the station's location
    @Parcelize
    data class Geometry(
        val type: String,
        val coordinates: List<Double>
    ) : Parcelable
    // The class implements Parcelable to allow passing objects between Android components

    // This nested class represents the 2D geographical point of the station
    @Parcelize
    data class GeoPoint(
        val lon: Double,
        val lat: Double
    ) : Parcelable
    // The class implements Parcelable to allow passing objects between Android components
}