package com.example.hotelio

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

class MenuManagement : Fragment() {

    private lateinit var menuContainer: LinearLayout
    private lateinit var btnAddMenu: Button
    private lateinit var btnAddCategory: Button
    private lateinit var btnSaveMenu: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_menu_management, container, false)

        menuContainer = view.findViewById(R.id.menuContainer)
        btnAddMenu = view.findViewById(R.id.btnAddMenu)
        btnAddCategory = view.findViewById(R.id.btnAddCategory)
        btnSaveMenu = view.findViewById(R.id.btnSaveMenu)

        // Show dialog when "Add Menu" is clicked
        btnAddMenu.setOnClickListener {
            showCategoryDialog()
        }

        // Add More Categories
        btnAddCategory.setOnClickListener {
            addCategorySection()
        }

        return view
    }

    private fun showCategoryDialog() {

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.alertdialogbox,null)
        // Initialize the dialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false) // Prevent dismissing on outside touch
            .create()

        // Reference UI elements inside the dialog
        val etInput = dialogView.findViewById<EditText>(R.id.etInput)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnOk = dialogView.findViewById<Button>(R.id.btnOk)

        // Handle button actions
        btnCancel.setOnClickListener {
            dialog.dismiss() // Close dialog
        }

        btnOk.setOnClickListener {
            val inputText = etInput.text.toString()
            if (inputText.isNotEmpty()) {
                for (i in 1..inputText.toInt()) {
                    addCategorySection()
                }
                btnAddCategory.visibility = View.VISIBLE
                btnSaveMenu.visibility = View.VISIBLE
                dialog.dismiss()
            } else {
                etInput.error = "Please enter a value"
            }
        }

        // Show the dialog
        dialog.show()
    }

    @SuppressLint("MissingInflatedId")
    private fun addCategorySection() {
        val categoryView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_category, menuContainer, false)

        val btnAddItem = categoryView.findViewById<Button>(R.id.btnAddItem)
        val btnDeleteitem = categoryView.findViewById<Button>(R.id.btndeleteItem)
        val itemContainer = categoryView.findViewById<LinearLayout>(R.id.itemContainer)

        btnAddItem.setOnClickListener {
            addItemField(itemContainer)
        }

        if (itemContainer.childCount >0){
            btnDeleteitem.visibility = View.VISIBLE
        }

        btnDeleteitem.setOnClickListener{
           menuContainer.removeView(categoryView)
        }

        menuContainer.addView(categoryView)
        btnSaveMenu.visibility = View.VISIBLE
    }

    private fun addItemField(container: LinearLayout) {
        val itemView = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_view, container, false)

        val etItem = itemView.findViewById<EditText>(R.id.etItem)
        val etPrice = itemView.findViewById<EditText>(R.id.etPrice)
        val btnDeleteItem = itemView.findViewById<ImageButton>(R.id.btnDeleteItem)

        // Remove the item field when delete button is clicked
        btnDeleteItem.setOnClickListener {
            container.removeView(itemView)
        }

        container.addView(itemView)
    }


}
