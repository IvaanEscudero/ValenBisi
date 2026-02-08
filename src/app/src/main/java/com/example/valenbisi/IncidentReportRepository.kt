package com.example.valenbisi

// Repository class to handle operations related to incidents
class IncidentReportRepository(private val incidentReportDao: IncidentReportDao) {
    // Function to insert a new incident report in the database
    suspend fun insertIncident(incident: IncidentReport) {
        incidentReportDao.insert(incident)
    }

    // Function to update an incident report of the database
    suspend fun updateIncident(incident: IncidentReport) {
        incidentReportDao.update(incident)
    }

    // Function to delete an incident with the ID
    suspend fun deleteIncidentById(id: Int) {
        val incident = getIncidentById(id)
        incident?.let {
            incidentReportDao.delete(it)
        }
    }

    // Function to get incidents of a station with its ID
    suspend fun getIncidentsByStation(stationId: Int): List<IncidentReport> {
        return incidentReportDao.getIncidentsByStation(stationId)
    }

    // Function get incident object with its ID
    suspend fun getIncidentById(id: Int): IncidentReport? {
        return incidentReportDao.getIncidentById(id)
    }

    // Function to get the max ID
    suspend fun getMaxId(): Int? {
        return incidentReportDao.getMaxId()
    }
}
