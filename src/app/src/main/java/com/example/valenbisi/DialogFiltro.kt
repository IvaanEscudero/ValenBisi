package com.example.valenbisi

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.Spinner

/*
 * Modal Dialog of filter bike station
 */
class DialogFiltro : DialogFragment() {

    private var isOpenFilterEnabled = false
    private var isAvailableFilterEnabled = false
    private var selectedFilter = "Total de bicis" // Default value
    private var isAscending = true // Sorting state

    companion object {
        private val FILTER_OPTIONS = arrayOf("Total de bicis", "Bicis libres", "Distancia", "ID")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(R.drawable.dialog_background) // Set custom dialog background
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.filtro, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup components
        val spinner: Spinner = view.findViewById(R.id.listfiltros)
        val buttonFiltrar: Button = view.findViewById(R.id.buttonFiltrar)
        val buttonBorrar: Button = view.findViewById(R.id.buttonBorrar)
        val buttonClose: ImageButton = view.findViewById(R.id.buttonClose)
        val checkBoxOpen: CheckBox = view.findViewById(R.id.checkBoxOpen)
        val checkBoxAvailable: CheckBox = view.findViewById(R.id.checkBoxAvailable)
        val buttonAsc: ImageButton = view.findViewById(R.id.imageButtonAsc)
        val buttonDesc: ImageButton = view.findViewById(R.id.imageButtonDesc)

        // Setup spinner options
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, FILTER_OPTIONS)
        spinner.adapter = adapter

        // Capture spinner selection
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedFilter = FILTER_OPTIONS[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Sorting buttons behavior
        setCheckBoxListener(checkBoxOpen) { isOpenFilterEnabled = it }
        setCheckBoxListener(checkBoxAvailable) { isAvailableFilterEnabled = it }

        // Apply filter button action
        buttonAsc.setOnClickListener {
            isAscending = true
            updateSortingButtons(buttonAsc, buttonDesc)
        }
        buttonDesc.setOnClickListener {
            isAscending = false
            updateSortingButtons(buttonAsc, buttonDesc)
        }

        // Reset filters button action
        buttonFiltrar.setOnClickListener {
            val resultBundle = Bundle().apply {
                putString("selectedFilter", selectedFilter)
                putBoolean("isAscending", isAscending)
                putBoolean("isOpenFilterEnabled", isOpenFilterEnabled)
                putBoolean("isAvailableFilterEnabled", isAvailableFilterEnabled)
            }
            parentFragmentManager.setFragmentResult("filterRequest", resultBundle)
            dismiss()
        }

        buttonBorrar.setOnClickListener {
            val resultBundle = Bundle().apply {
                putBoolean("resetFilters", true)
            }
            parentFragmentManager.setFragmentResult("filterRequest", resultBundle)
            dismiss()
        }


        // Close dialog button action
        buttonClose.setOnClickListener {
            dismiss()
        }

        // Initialize sorting button states
        updateSortingButtons(buttonAsc, buttonDesc)
    }

    // Set checkbox listener to update state
    private fun setCheckBoxListener(checkBox: CheckBox, onCheckedChanged: (Boolean) -> Unit) {
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            onCheckedChanged(isChecked)
        }
    }

    // Update visual state of sorting buttons
    private fun updateSortingButtons(buttonAsc: ImageButton, buttonDesc: ImageButton) {
        if (isAscending) {
            buttonAsc.alpha = 1.0f  // Highlight ascending button
            buttonDesc.alpha = 0.5f // Dim descending button
        } else {
            buttonAsc.alpha = 0.5f // Dim descending button
            buttonDesc.alpha = 1.0f  // Highlight descending button
        }
    }
}
