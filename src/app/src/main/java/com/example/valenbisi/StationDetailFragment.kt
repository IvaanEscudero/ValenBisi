package com.example.valenbisi

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.valenbisi.databinding.FragmentStationDetailBinding
import java.util.Locale

/*
 * Class to manage the station detail fragment
 */
class StationDetailFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var incidentAdapter: IncidentAdapter
    private val viewModel: IncidentReportViewModel by viewModels()

    companion object {
        private const val ARG_BIKE_STATION = "bike_station"

        // Static method to create a new instance of the fragment with data
        fun newInstance(station: BikeStation): StationDetailFragment {
            val fragment = StationDetailFragment()
            val args = Bundle()
            // Passing the BikeStation object as argument
            args.putParcelable(ARG_BIKE_STATION, station)
            fragment.arguments = args
            return fragment
        }
    }

    private lateinit var bikeStation: BikeStation

    // Function to execute when the view is created
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentStationDetailBinding.inflate(inflater, container, false)

        // Retrieve the BikeStation object safely from Bundle arguments
        // This is the safe version cast which is not deprectated but is not compatible with previous versions
        /* bikeStation = arguments?.getParcelable(ARG_BIKE_STATION, BikeStation::class.java)
            ?: throw IllegalArgumentException("Bike station data is missing!")*/
        // Compatible version
        bikeStation =  arguments?.getParcelable(ARG_BIKE_STATION)
            ?: throw IllegalArgumentException("Bike station data is missing!")

        // Function to setup the UI
        displayStationDetails(binding)
        setupIncidents(binding)
        setupMapIframe(binding)
        setupFAB(binding)

        return binding.root // Return the view
    }

    // Function to modify the fragment with the data retrieved of the bike station
    private fun displayStationDetails(binding: FragmentStationDetailBinding) {
        binding.nameStationDetail.text = bikeStation.address
        binding.idStationDetail.text = String.format(Locale.getDefault(), "%d", bikeStation.number)
        binding.stateStationDetail.text = if (bikeStation.open == "T") {
            getString(R.string.station_open)
        } else {
            getString(R.string.station_closed)
        }

        // Change text color based on station status
        if (bikeStation.open == "T") {
            binding.stateStationDetail.setTextColor(ContextCompat.getColor(requireContext(), R.color.green_status))  // Open: Green
        } else {
            binding.stateStationDetail.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_status))  // Closed: Red
        }
        binding.freeStationDetail.text = String.format(Locale.getDefault(), "%d", bikeStation.available)
        binding.totalStationDetail.text = String.format(Locale.getDefault(), "%d", bikeStation.total)
        binding.ticketStateDetail.text = bikeStation.ticket
        binding.dateStationDetail.text = bikeStation.updatedAt
        val formattedLat = String.format(Locale.getDefault(), "%.6f", bikeStation.geoPoint2d.lat)
        val formattedLon = String.format(Locale.getDefault(), "%.6f", bikeStation.geoPoint2d.lon)
        binding.coordenateStationDetail.text = getString(R.string.coordinates_format, formattedLat, formattedLon)

        // Change bike icon based on the bike availability
        val bikeImageResId = when {
            bikeStation.available > 10 -> R.drawable.bike  // High availability (green)
            bikeStation.available in 5..10 -> R.drawable.bike2  // Medium availability (orange)
            else -> R.drawable.bike3  // Low availability (red)
        }

        binding.freeStationDetailimg.setImageResource(bikeImageResId)
    }

    // Retrieve from the database the associated incidents to the bike station
    private fun setupIncidents(binding: FragmentStationDetailBinding) {
        recyclerView = binding.recyclerIncidents
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Fetch incidents based on station ID
        viewModel.getIncidentsByStation(bikeStation.number) { incidents ->
            incidentAdapter = IncidentAdapter(incidents.toMutableList()) { incident ->
                openIncidentDetailActivity(incident)
            }
            recyclerView.adapter = incidentAdapter
        }
    }

    // Function to set up a dynamic google map based on the lat and lon of the bike station
    // making use of html code to avoid using a Google API key
    @SuppressLint("SetJavaScriptEnabled")
    private fun setupMapIframe(binding: FragmentStationDetailBinding) {
        val webView: WebView = binding.webView

        // Enable JavaScript (Google Maps requires it)
        val webSettings = webView.settings
        webSettings.javaScriptEnabled = true

        // Set WebViewClient to avoid opening links in an external browser
        webView.webViewClient = WebViewClient()

        // Construct the HTML with an iframe tag
        val lat = bikeStation.geoPoint2d.lat
        val lon = bikeStation.geoPoint2d.lon
        val html = """
            <html>
            <body style="margin: 0; padding: 0; overflow: hidden;">
                <iframe width="100%" height="200" style="border: 0; pointer-events: none;"
                src="https://maps.google.com/maps?q=$lat,$lon&hl=es&z=14&output=embed">
                </iframe>
            </body>
            </html>
        """
        // Load the HTML into the WebView
        webView.loadData(html, "text/html", "UTF-8")
    }

    // Function to set up the floating action button to open a new window to create an incident
    private fun setupFAB(binding: FragmentStationDetailBinding) {
        val fab = binding.detailIncidentFab
        fab.setOnClickListener {
            openIncidentDetailActivity(null) // Nulll because we are creating an incident instead of modifying an existing one
        }
    }

    // Function to open an existing incident, passing the needed data to the new window to recognise the station id where the incident occurs
    private fun openIncidentDetailActivity(incident: IncidentReport?) {
        val intent = Intent(requireContext(), IncidentDetailActivity::class.java).apply {
            putExtra("incident_id", incident?.id ?: -1)  // -1 for new incident
            putExtra("station_id", bikeStation.number)
            putExtra("station_name", bikeStation.address)
        }
        startActivity(intent)
    }

    // Override function to ensure that data is updated when the fragment is resumed after returning from an incident
    override fun onResume() {
        super.onResume()

        // Update incidents list
        if (::incidentAdapter.isInitialized) {
            viewModel.getIncidentsByStation(bikeStation.number) { incidents ->
                incidentAdapter.updateList(incidents.toMutableList()) // Update the list
            }
        }
    }
}
