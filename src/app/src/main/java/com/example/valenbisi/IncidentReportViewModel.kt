package com.example.valenbisi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/*
 * Class to modify the modify and manage the incidents
 */
class IncidentReportViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: IncidentReportRepository

    init {
        val dao = AppDatabase.getDatabase(application).incidentReportDao()
        repository = IncidentReportRepository(dao)
    }

    // Function to insert new incident into the database
    fun insertIncident(incident: IncidentReport) = viewModelScope.launch {
        repository.insertIncident(incident)
    }

    // Function to update existing incident in the database
    fun updateIncident(incident: IncidentReport) = viewModelScope.launch {
        repository.updateIncident(incident)
    }

    // Function to delete an incident by id from the database
    fun deleteIncidentById(id: Int) = viewModelScope.launch {
        repository.deleteIncidentById(id)
    }

    // Function to get a specific incident by its ID
    fun getIncidentById(id: Int, callback: (IncidentReport?) -> Unit) = viewModelScope.launch {
        callback(repository.getIncidentById(id))
    }

    // Function to generate a UniqueIncidentId
    fun generateUniqueIncidentId(callback: (Int) -> Unit) {
        viewModelScope.launch {
            val maxId = repository.getMaxId() ?: 0  // If no records exist, start from 0
            callback(maxId + 1)
        }
    }

    // Function to get all the incidents of a station
    fun getIncidentsByStation(stationId: Int, callback: (List<IncidentReport>) -> Unit) = viewModelScope.launch {
        val incidents = repository.getIncidentsByStation(stationId)
        callback(incidents)
    }
}