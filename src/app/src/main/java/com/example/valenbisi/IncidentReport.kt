package com.example.valenbisi

import androidx.room.Entity
import androidx.room.PrimaryKey

// Room database of the incidents
@Entity(tableName = "incident_reports") // Table name of the Room database
data class IncidentReport(
    @PrimaryKey(autoGenerate = true) // Marks id as the primary key and autogenerates the value if not specified
    val id: Int = 0,             // Incident ID
    val title: String,           // Incident title
    val description: String,     // Incident description
    val status: String,          // Incident status
    val type: String,            // Incident type
    val stationId: Int,          // Incident station Id
    val image: ByteArray? = null // Incident bytearray
)  {  // Compares two IncidentReport objects
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false

        other as IncidentReport

        if (id != other.id) return false
        if (title != other.title) return false
        if (description != other.description) return false
        if (status != other.status) return false
        if (type != other.type) return false
        if (stationId != other.stationId) return false
        if (image != null) {
            if (other.image == null || !image.contentEquals(other.image)) return false
        } else if (other.image != null) {
            return false
        }

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + title.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + status.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + stationId
        result = 31 * result + (image?.contentHashCode() ?: 0)
        return result
    }
}