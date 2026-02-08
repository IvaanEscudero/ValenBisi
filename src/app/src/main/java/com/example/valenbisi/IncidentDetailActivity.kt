package com.example.valenbisi

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.textfield.TextInputEditText
import java.io.ByteArrayOutputStream

/*
 * Adapter class for managing the list of incidents in a RecyclerView
 */
class IncidentDetailActivity : AppCompatActivity() {
    private lateinit var stationtitle: TextView
    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: TextInputEditText
    private lateinit var stateSpinner: Spinner
    private lateinit var typeSpinner: Spinner
    private lateinit var imageView: ImageView
    private val viewModel: IncidentReportViewModel by viewModels()

    private var incidentId: Int = -1
    private var stationId: Int = -1
    private var stationName: String = "Error"
    private var originalImageBitmap: Bitmap? = null
    private var selectedImageBitmap: Bitmap? = null

    private val CAMERA_PERMISSION_REQUEST_CODE = 100

    // Check camera permission and launch camera
    private fun checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        } else {
            dispatchTakePictureIntent()
        }
    }

    // Handle permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                dispatchTakePictureIntent()
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Start the camera
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            selectedImageBitmap = it
            imageView.setImageBitmap(it)
        } ?: run {
            Toast.makeText(this, "No se pudo capturar la imagen", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.detail_incident)

        // Load intent data
        intent?.let {
            incidentId = it.getIntExtra("incident_id", -1)
            stationId = it.getIntExtra("station_id", -1)
            stationName = it.getStringExtra("station_name") ?: "Error"
        } ?: run {
            Toast.makeText(this, "Error al cargar incidente", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Setup Toolbar
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Setup components
        stationtitle = findViewById(R.id.incident_stationtitle)
        titleEditText = findViewById(R.id.incident_editabletitle)
        descriptionEditText = findViewById(R.id.incident_detail_description)
        stateSpinner = findViewById(R.id.spinner)
        typeSpinner = findViewById(R.id.spinner2)
        imageView = findViewById(R.id.incident_image)

        // Initialize Spinners with predefined options
        val stateAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Open", "Processing", "Closed")
        )
        stateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        stateSpinner.adapter = stateAdapter

        // Initialize Spinners with predefined options
        val typeAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            listOf("Mechanical", "Electric", "Painting", "Masonry")
        )
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        typeSpinner.adapter = typeAdapter

        // Load existing incident data if editing
        if (incidentId != -1) {
            // Load incident to edit it
            viewModel.getIncidentById(incidentId) { incident ->
                incident?.let {
                    stationtitle.text = "$stationId. $stationName"
                    titleEditText.setText(it.title)
                    descriptionEditText.setText(it.description)
                    stateSpinner.setSelection(stateAdapter.getPosition(it.status))
                    typeSpinner.setSelection(typeAdapter.getPosition(it.type))

                    // Load image from the database
                    it.image?.let { byteArray ->
                        val bitmap = getImage(byteArray)
                        originalImageBitmap = bitmap  // Save the original image
                        imageView.setImageBitmap(bitmap)
                    }
                }
            }
        } else {
            stationtitle.text = "$stationId. $stationName"
            titleEditText.setText("")
            descriptionEditText.setText("")
            stateSpinner.setSelection(0)
            typeSpinner.setSelection(0)
        }
    }

    // Create menu options
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    // Handle menu item selections
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                val title = titleEditText.text.toString()
                val description = descriptionEditText.text.toString()
                val status = stateSpinner.selectedItem.toString()
                val type = typeSpinner.selectedItem.toString()

                if (title.isEmpty() || description.isEmpty()) {
                    Toast.makeText(this, "Title and Description are required", Toast.LENGTH_SHORT).show()
                    return false
                }

                // We update or maintain the image
                val imageByteArray = if (originalImageBitmap != null && selectedImageBitmap == null) {
                    getBytes(originalImageBitmap!!)  // Mantain the original map
                } else {
                    selectedImageBitmap?.let { getBytes(it) }  // We use the new image if the old one is changed
                }

                val updatedIncident = IncidentReport(
                    id = incidentId,
                    title = title,
                    description = description,
                    status = status,
                    type = type,
                    stationId = stationId,
                    image = imageByteArray
                )

                if (incidentId == -1) {
                    // If the incident is new we insert it
                    viewModel.generateUniqueIncidentId { newId ->
                        val newIncident = updatedIncident.copy(id = newId)
                        viewModel.insertIncident(newIncident)
                        finish()
                    }
                } else {
                    // If the incident is being edited we update it
                    viewModel.updateIncident(updatedIncident)
                    finish()
                }

                true
            }
            // Set up the delete button
            R.id.action_delete -> {
                if (incidentId != -1) {
                    viewModel.deleteIncidentById(incidentId)
                    finish()
                } else {
                    finish()
                }
                true
            }
            // Set up take an image button
            R.id.action_image -> {
                checkCameraPermissionAndOpenCamera()
                true
            }
            // Set up return to home image
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    // Start the camera
    private fun dispatchTakePictureIntent() {
        try {
            cameraLauncher.launch(null)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "No se encontró una aplicación de cámara", Toast.LENGTH_SHORT).show()
        }
    }

    // Convert Bitmap to ByteArray
    private fun getBytes(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    // Get image from ByteArray
    private fun getImage(image: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(image, 0, image.size)
    }
}