package com.example.valenbisi

import androidx.room.*

@Dao // Annotation that marks this interface as a Data Access Object for Room Database
interface IncidentReportDao {
    // Inserts a new incident in the database
    @Insert
    suspend fun insert(incident: IncidentReport)

    // Updates an existing incident in the database
    @Update
    suspend fun update(incident: IncidentReport)

    // Delete an existing incident of the database
    @Delete
    suspend fun delete(incident: IncidentReport)

    // Retrieves an incident object based on its ID
    @Query("SELECT * FROM incident_reports WHERE id = :id LIMIT 1")
    suspend fun getIncidentById(id: Int): IncidentReport?

    // Retrieves the incidents of a bike station with its ID
    @Query("SELECT * FROM incident_reports WHERE stationId = :stationId")
    suspend fun getIncidentsByStation(stationId: Int): List<IncidentReport>

    // Retrieve the max incident ID
    @Query("SELECT MAX(id) FROM incident_reports")
    suspend fun getMaxId(): Int?
}
