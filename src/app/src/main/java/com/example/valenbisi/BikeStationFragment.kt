package com.example.valenbisi

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/*
 * Fragment list of bike station
 */
class BikeStationFragment : Fragment() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var searchBar: EditText
    private lateinit var searchButton: ImageButton
    private var allBikeStations = ArrayList<BikeStation>()
    private var userLocation: Location? = null
    private lateinit var recyclerView: RecyclerView
    //LiveData to observe bike station changes and update the UI dynamically
    private val bikeStationsLiveData = MutableLiveData<List<BikeStation>>()

    // Permission request location access permission
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            // If user grants location
            if (granted) {
                requestLocation()
            } else {
                Log.e("LocationError", "Permission denied")
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Create layout
        return inflater.inflate(R.layout.fragment_bike_station, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Setup items in layout
        searchBar = view.findViewById(R.id.search_bar)
        searchButton = view.findViewById(R.id.search_button)

        recyclerView = view.findViewById(R.id.rv)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        // Floating Button for Filter
        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            val dialog = DialogFiltro()
            dialog.show(parentFragmentManager, "DialogFiltro")
        }

        // Request user location
        requestLocation()

        // Cargar estaciones de bicicletas
        loadBikeStations()

        // Observing LiveData to update RecyclerView when data changes
        bikeStationsLiveData.observe(viewLifecycleOwner) { stations ->
            recyclerView.adapter = MyAdapter(stations) { station ->
                val detailFragment = StationDetailFragment().apply {
                    // Pass the selected bike station as a Parcelable argument
                    arguments = Bundle().apply {
                        putParcelable("bike_station", station)
                    }
                }
                // Replace the current fragment with the detail fragment
                parentFragmentManager.beginTransaction()
                    .setCustomAnimations(
                        android.R.anim.slide_in_left,   // Slide in left animation
                        android.R.anim.slide_out_right   // Slide out right animation
                    )
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)  // Add fragment to the stack
                    .commit()
            }
        }

        // Add listener refresh
        val refresh: ImageButton = view.findViewById(R.id.refresh_button)
        refresh.setOnClickListener {
            fetchLocationAndReload()
        }



        // Add listener search
        searchButton.setOnClickListener {
            val query = searchBar.text.toString().trim() // Obtener el texto de búsqueda
            filterBikeStations(query) // Llamar a la función de filtrado
        }

        // Listen for filter results from the filter dialog
        parentFragmentManager.setFragmentResultListener("filterRequest", viewLifecycleOwner) { _, bundle ->
            val selectedFilter = bundle.getString("selectedFilter") ?: "Total de bicis"
            val isAscending = bundle.getBoolean("isAscending")
            val isOpenFilterEnabled = bundle.getBoolean("isOpenFilterEnabled")
            val isAvailableFilterEnabled = bundle.getBoolean("isAvailableFilterEnabled")

            if (bundle.getBoolean("resetFilters", false)) {
                resetFilters()
            } else {
                applyFilters(selectedFilter, isAscending, isOpenFilterEnabled, isAvailableFilterEnabled)
            }
        }

    }

    // Fetch user's location and reload the bike stations based on it
    private fun fetchLocationAndReload() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        // Current location using high accuracy mode
        fusedLocationClient.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    userLocation = location
                    Log.d("Location", "Updated location: ${location.latitude}, ${location.longitude}")
                } else {
                    Log.e("LocationError", "Failed to update location")
                }
                loadBikeStations() // Reload with updated location
            }
            .addOnFailureListener { e ->
                Log.e("LocationError", "Error fetching updated location: ${e.message}")
                loadBikeStations() // Reload even if location fails
            }
    }

    // Filter list of bike stations based on query
    private fun filterBikeStations(query: String) {
        val filteredList = if (query.isEmpty()) {
            allBikeStations // If the query is empty, show all stations
        } else {
            allBikeStations.filter {
                it.address.contains(query, ignoreCase = true) || it.number.toString().contains(query)
            }
        }
        bikeStationsLiveData.postValue(filteredList) // Update the LiveData with the filtered list
    }

    // Request location permissions or fetch the location if permissions are granted
    private fun requestLocation() {
        when {
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED -> fetchLocation()
            else -> requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // Fetch the user's last known location and load bike stations
    private fun fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Handle permission request
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    userLocation = location
                    Log.d("Location", "User location: ${location.latitude}, ${location.longitude}")
                } else {
                    Log.e("LocationError", "Failed to get location, using default (0,0)")
                }
                loadBikeStations() // Reload with updated location
            }
            .addOnFailureListener { e ->
                Log.e("LocationError", "Error fetching location: ${e.message}")
                loadBikeStations() // Reload if location fails
            }
    }

    // Load bike stations from the API using coroutines
    private fun loadBikeStations() {
        CoroutineScope(Dispatchers.IO).launch {
            val bikeStationsList = getData() // Obtener estaciones de la API
            withContext(Dispatchers.Main) {
                allBikeStations.clear()
                allBikeStations.addAll(bikeStationsList) // Guardar todas las estaciones en memoria
                bikeStationsLiveData.value = allBikeStations // Mostrar todas en la lista inicialmente
            }
        }
    }
    // Get data from API
    private fun getData(): ArrayList<BikeStation> {
        val bikeStationsList = ArrayList<BikeStation>()
        val url = "https://valencia.opendatasoft.com/api/explore/v2.1/catalog/datasets/valenbisi-disponibilitat-valenbisi-dsiponibilidad/records?limit=100"
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("Accept-Language", "es")

        try {
            // Read the response from the API
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()

            // Parse the JSON response and extract bike station data
            val jsonResponse = JSONObject(response.toString())
            val resultsArray = jsonResponse.getJSONArray("results")

            for (i in 0 until resultsArray.length()) {
                val jsonObject = resultsArray.getJSONObject(i)
                // Extract data for each bike station
                val address = jsonObject.getString("address")
                val number = jsonObject.getInt("number")
                val open = jsonObject.getString("open")
                val available = jsonObject.getInt("available")
                val free = jsonObject.getInt("free")
                val total = jsonObject.getInt("total")
                val ticket = jsonObject.getString("ticket")
                val updatedAt = jsonObject.getString("updated_at")
                val updateJcd = jsonObject.getString("update_jcd")

                val geoShapeObject = jsonObject.getJSONObject("geo_shape")
                val geometryObject = geoShapeObject.getJSONObject("geometry")
                val coordinatesArray = geometryObject.getJSONArray("coordinates")
                val geoShape = BikeStation.GeoShape(
                    type = geoShapeObject.getString("type"),
                    geometry = BikeStation.Geometry(
                        type = geometryObject.getString("type"),
                        coordinates = listOf(
                            coordinatesArray.getDouble(0),
                            coordinatesArray.getDouble(1)
                        )
                    )
                )

                val geoPointObject = jsonObject.getJSONObject("geo_point_2d")
                val geoPoint = BikeStation.GeoPoint(
                    lon = geoPointObject.getDouble("lon"),
                    lat = geoPointObject.getDouble("lat")
                )
                // Calculate the distance between the user and the bike station
                val distance = userLocation?.let {
                    calculateDistance(it.latitude, it.longitude, geoPoint.lat, geoPoint.lon)
                } ?: -1.0 // Default to -1.0 if location is unavailable

                bikeStationsList.add(
                    BikeStation(
                        address, number, open, available, free, total, ticket, updatedAt,
                        geoShape, geoPoint, updateJcd, distance
                    )
                )
            }
        } catch (e: Exception) {
            Log.e("NetworkError", "Failed to fetch data", e)
        }
        return bikeStationsList
    }
    // Calculate the distance between two geographical points using the Haversine formula
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000 // Earth radius in km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)

        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)

        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c // Distance in km
    }
    // Apply the user's selected filters and update the displayed bike stations
    private fun applyFilters(
        selectedFilter: String,
        isAscending: Boolean,
        isOpen: Boolean,
        isAvailable: Boolean
    ) {
        // Start a corutine to obtain and filter the bike stations
        CoroutineScope(Dispatchers.IO).launch {
            // Obtener las estaciones con la función suspendida getData()
            val allStations = getData()

            // Filter stations based on open status and availability
            val filteredStations = allStations.filter { station ->
                val openCondition = if (isOpen) station.open == "T" else true
                val availableCondition = if (isAvailable) station.available > 0 else true
                openCondition && availableCondition
            }

            // Sort stations based on the selected query
            val sortedStations = when (selectedFilter) {
                "Total de bicis" -> filteredStations.sortedBy { it.total }
                "Bicis libres" -> filteredStations.sortedBy { it.available }
                "Distancia" -> filteredStations.sortedBy { it.distance }
                "ID" -> filteredStations.sortedBy { it.number }
                else -> filteredStations
            }

            // Reverse the list if descending order is selected
            val finalList = if (isAscending) sortedStations else sortedStations.reversed()

            // Update the LiveData on the main thread to refresh the UI
            withContext(Dispatchers.Main) {
                bikeStationsLiveData.value = finalList
            }
        }
    }
    // Reset all filters and reload the bike stations
    private fun resetFilters() {
        loadBikeStations()
    }
}