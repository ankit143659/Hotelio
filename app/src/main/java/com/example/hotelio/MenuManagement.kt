package com.example.hotelio

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.UUID

class MenuManagement : Fragment() {

    private lateinit var menuContainer: LinearLayout
    private lateinit var btnAddCategory: Button
    private lateinit var btnSaveMenu: Button
    private lateinit var database: DatabaseReference
    private lateinit var userId: String
    private lateinit var share: SharePrefrence

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_menu_management, container, false)

        share = SharePrefrence(requireContext())
        menuContainer = view.findViewById(R.id.menuContainer)
        btnAddCategory = view.findViewById(R.id.btnAddCategory)
        btnSaveMenu = view.findViewById(R.id.btnSaveMenu)

        val btnAddMenu: Button = view.findViewById(R.id.btnAddMenu)

        btnAddMenu.setOnClickListener {
            showCategoryDialog()
        }

        database = FirebaseDatabase.getInstance().reference
        userId = share.UserUid().toString()

        btnAddCategory.setOnClickListener {
            addCategorySection(null) // Add an empty category
        }

        btnSaveMenu.setOnClickListener {
            saveMenuDetails()
        }

        loadMenuDetails() // Load existing menu details

        return view
    }

    private fun showCategoryDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.alertdialogbox, null)
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
                    addCategorySection(null) // Add empty category
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


    // Function to load menu details from Firebase
    private fun loadMenuDetails() {
        val menuRef = database.child("hotels").child(userId).child("menu")

        menuRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                menuContainer.removeAllViews()
                if (snapshot.exists()) {
                    for (categorySnapshot in snapshot.children) {
                        btnAddCategory.visibility = View.VISIBLE
                        btnSaveMenu.visibility = View.VISIBLE
                        val categoryName = categorySnapshot.key ?: ""
                        val items = mutableListOf<Pair<String, String>>()

                        for (itemSnapshot in categorySnapshot.children) {
                            val itemName = itemSnapshot.key ?: ""
                            val price = itemSnapshot.value.toString()
                            items.add(Pair(itemName, price))
                        }

                        addCategorySection(categoryName, items)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load menu", Toast.LENGTH_SHORT).show()
            }
        })
    }

    @SuppressLint("InflateParams", "MissingInflatedId")
    private fun addCategorySection(
        categoryName: String?,
        items: List<Pair<String, String>>? = null
    ) {
        val categoryView =
            LayoutInflater.from(requireContext()).inflate(R.layout.item_category, null, false)

        val etCategoryName = categoryView.findViewById<EditText>(R.id.etCategory)
        val btnAddItem = categoryView.findViewById<Button>(R.id.btnAddItem)
        val btnDeleteCategory = categoryView.findViewById<Button>(R.id.btndeleteCategory)
        val itemContainer = categoryView.findViewById<LinearLayout>(R.id.itemContainer)

        etCategoryName.setText(categoryName ?: "")

        btnAddItem.setOnClickListener {
                addItemField(itemContainer,"", "", categoryName)

        }

        btnDeleteCategory.setOnClickListener {
            menuContainer.removeView(categoryView)
            if (!categoryName.isNullOrEmpty()) {
                deleteCategoryFromDatabase(categoryName)
            }
        }

        menuContainer.addView(categoryView)

        // If items exist, populate them
        items?.forEach { (itemName, price) ->
                addItemField(itemContainer, itemName, price,categoryName)

        }
    }
    private fun addItemField(container: LinearLayout, itemName: String = "", price: String = "", categoryName: String?) {
        Log.d("MenuManagement", "Adding item for category: $categoryName")

        val itemView = LayoutInflater.from(requireContext()).inflate(R.layout.item_view, container, false)
        val etItem = itemView.findViewById<EditText>(R.id.etItem)
        val etPrice = itemView.findViewById<EditText>(R.id.etPrice)
        val btnDeleteItem = itemView.findViewById<ImageButton>(R.id.btnDeleteItem)



        // Set the text in the EditText and Price field
        etItem.setText(itemName)
        etPrice.setText(price)

        // Delete item functionality
        btnDeleteItem.setOnClickListener {
            val parenntCategory = container.parent as View
            val etCategory = parenntCategory.findViewById<EditText>(R.id.etCategory)
            val currentCategory = etCategory.text.toString().trim()
            val itemName = etItem.text.toString().trim()

            if (itemName.isNotEmpty()) {
                container.removeView(itemView)

                // Ensure categoryName is not null
                val category = categoryName ?: run {
                    Log.d("MenuManagement", "Category name is null. Cannot delete item.")
                    return@setOnClickListener
                }

                // Log item deletion
                Log.d("MenuManagement", "Deleting item: $itemName from category: $category")

                // Firebase reference: Using itemName as the key (not the unique ID) for deletion
                val itemRef = database.child("hotels").child(userId).child("menu")
                    .child(currentCategory) // Use the category directly
                    .child(itemName) // Use itemName instead of uniqueItemId

                itemRef.removeValue().addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d("MenuManagement", "Item deleted successfully from Firebase")
                    } else {
                        Log.d("MenuManagement", "Failed to delete item from Firebase")
                        Toast.makeText(requireContext(), "Failed to delete item", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        // Add the item to the container
        container.addView(itemView)
        Log.d("MenuManagement", "Item added successfully for category: $categoryName")
    }





    private fun deleteCategoryFromDatabase(categoryName: String) {
        val categoryRef = database.child("hotels").child(userId).child("menu").child(categoryName)

        categoryRef.removeValue().addOnSuccessListener {
            Toast.makeText(requireContext(), "Category deleted", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to delete category", Toast.LENGTH_SHORT).show()
        }
    }


    private fun saveMenuDetails() {
        val menuData = mutableMapOf<String, MutableMap<String, String>>()

        // Iterate through categories
        for (i in 0 until menuContainer.childCount) {
            val categoryView = menuContainer.getChildAt(i)
            val etCategoryName = categoryView.findViewById<EditText>(R.id.etCategory)
            val itemContainer = categoryView.findViewById<LinearLayout>(R.id.itemContainer)

            val categoryName = etCategoryName.text.toString().trim()
            if (categoryName.isEmpty()) continue  // Skip empty categories

            val items = mutableMapOf<String, String>()

            // Iterate through items in this category
            for (j in 0 until itemContainer.childCount) {
                val itemView = itemContainer.getChildAt(j)
                val etItem = itemView.findViewById<EditText>(R.id.etItem)
                val etPrice = itemView.findViewById<EditText>(R.id.etPrice)

                val itemName = etItem.text.toString().trim()
                val price = etPrice.text.toString().trim()

                if (itemName.isNotEmpty() && price.isNotEmpty()) {
                    items[itemName] = price
                }
            }

            if (items.isNotEmpty()) {
                menuData[categoryName] = items
            }
        }

        if (menuData.isNotEmpty()) {
            database.child("hotels").child(userId).child("menu").setValue(menuData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Menu saved successfully", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to save menu", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(requireContext(), "No valid menu data to save", Toast.LENGTH_SHORT).show()
        }
    }

}
