package com.example.hotelio

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Locale

class PaymentManagement : Fragment() {

    private lateinit var database: DatabaseReference
    private lateinit var billContainer: LinearLayout
    private lateinit var share: SharePrefrence
    private lateinit var hotelUid: String
    private var billListener: ValueEventListener? = null

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_payment_management, container, false)
        billContainer = view.findViewById(R.id.paymentContainer)
        database = FirebaseDatabase.getInstance().reference
        share = SharePrefrence(requireContext())
        hotelUid = share.UserUid().toString()

        fetchBills() // Load bill requests

        return view
    }

    private fun fetchBills() {
        billListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || context == null) return

                billContainer.removeAllViews()
                val billList = mutableListOf<Pair<Long, DataSnapshot>>()

                for (tableSnapshot in snapshot.children) {
                    for (billSnapshot in tableSnapshot.children) {
                        val timestamp = billSnapshot.child("timestamp").value.toString()
                        val timeInMillis = parseTimestamp(timestamp)
                        billList.add(Pair(timeInMillis, billSnapshot))
                    }
                }

                billList.sortByDescending { it.first }

                for ((_, billSnapshot) in billList) {
                    val billId = billSnapshot.key!!
                    displayBill(billId, billSnapshot)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        database.child("hotels").child(hotelUid).child("Bills").addValueEventListener(billListener!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        billListener?.let { database.child("Bills").child(hotelUid).removeEventListener(it) }
    }

    private fun parseTimestamp(timestamp: String): Long {
        return try {
            val format = SimpleDateFormat("d/M/yyyy, hh:mm:ss a", Locale.getDefault())
            format.parse(timestamp)?.time ?: 0L
        } catch (e: Exception) {
            println("Timestamp parsing error: ${e.message}")
            0L
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun displayBill(billId: String, billSnapshot: DataSnapshot) {
        if (!isAdded || context == null) return

        val inflater = LayoutInflater.from(requireContext())
        val billView = inflater.inflate(R.layout.bill_item, billContainer, false)

        val tableNoText = billView.findViewById<TextView>(R.id.tableNoText)
        val billIdText = billView.findViewById<TextView>(R.id.billIdText)
        val totalAmountText = billView.findViewById<TextView>(R.id.totalAmountText)
        val timestampText = billView.findViewById<TextView>(R.id.timestampText)
        val confirmBtn = billView.findViewById<Button>(R.id.confirmBtn)
        val rejectBtn = billView.findViewById<Button>(R.id.rejectBtn)
        val generateBillBtn = billView.findViewById<Button>(R.id.generateBillBtn)

        val tableNo = billSnapshot.ref.parent?.key ?: "Unknown"
        val totalAmount = billSnapshot.child("totalAmount").value.toString()
        val timestamp = billSnapshot.child("timestamp").value.toString()
        val status = billSnapshot.child("status").value.toString()

        // Fetch hotel name
        database.child("hotels").child(hotelUid).child("hotelName").get().addOnSuccessListener { hotelSnapshot ->
            val hotelName = hotelSnapshot.value?.toString() ?: "Unknown Hotel"

            tableNoText.text = "Table No: $tableNo"
            billIdText.text = "Bill ID: $billId"
            totalAmountText.text = "Total: ₹$totalAmount"
            timestampText.text = "Time: $timestamp"

            // Initially hide "Generate Bill" button
            generateBillBtn.visibility = View.GONE

            when (status) {
                "Confirmed" -> {
                    confirmBtn.text = "Confirmed"
                    confirmBtn.isEnabled = false
                    confirmBtn.setBackgroundColor(Color.GRAY)
                    rejectBtn.visibility = View.GONE
                    generateBillBtn.visibility = View.VISIBLE
                }
                "Rejected" -> {
                    rejectBtn.text = "Rejected"
                    rejectBtn.isEnabled = false
                    rejectBtn.setBackgroundColor(Color.GRAY)
                    confirmBtn.visibility = View.GONE
                }
            }

            confirmBtn.setOnClickListener {
                database.child("hotels").child(hotelUid).child("Bills").child(tableNo).child(billId)
                    .child("status").setValue("Confirmed").addOnCompleteListener {
                        if (it.isSuccessful) {
                            confirmBtn.text = "Confirmed"
                            confirmBtn.isEnabled = false
                            confirmBtn.setBackgroundColor(Color.GRAY)
                            rejectBtn.visibility = View.GONE
                            generateBillBtn.visibility = View.VISIBLE
                        }
                    }
            }

            rejectBtn.setOnClickListener {
                database.child("hotels").child(hotelUid).child("Bills").child(tableNo).child(billId)
                    .child("status").setValue("Rejected").addOnCompleteListener {
                        if (it.isSuccessful) {
                            rejectBtn.text = "Rejected"
                            rejectBtn.isEnabled = false
                            rejectBtn.setBackgroundColor(Color.GRAY)
                            confirmBtn.visibility = View.GONE
                            billContainer.removeView(billView)
                        }
                    }
            }

            generateBillBtn.setOnClickListener {
                fetchItemsAndGenerateBill(billId, tableNo, totalAmount, timestamp, hotelName, billSnapshot)
            }

            billContainer.addView(billView, 0)
            billContainer.invalidate()
            billContainer.requestLayout()
        }
    }


    private fun fetchItemsAndGenerateBill(
        billId: String,
        tableNo: String,
        totalAmount: String,
        timestamp: String,
        hotelName: String,
        billSnapshot: DataSnapshot
    ) {
        val itemsList = mutableListOf<String>()

        for (orderSnapshot in billSnapshot.child("items").children) {
            for (itemSnapshot in orderSnapshot.child("items").children) { // ✅ Corrected Access
                val itemName = itemSnapshot.child("item").value.toString() // ✅ Corrected Key
                val quantity = itemSnapshot.child("quantity").value.toString()
                val price = itemSnapshot.child("total").value.toString() // ✅ "total" instead of "price"

                itemsList.add("$itemName x$quantity - ₹$price")
            }
        }

        val itemDetails = itemsList.joinToString("\n")

        // Create a view dynamically to store as an image
        val inflater = LayoutInflater.from(requireContext())
        val billView = inflater.inflate(R.layout.bill_layout, null)

        val hotelNameText = billView.findViewById<TextView>(R.id.hotelNameText)
        val tableNoText = billView.findViewById<TextView>(R.id.tableNoText)
        val billIdText = billView.findViewById<TextView>(R.id.billIdText)
        val timestampText = billView.findViewById<TextView>(R.id.timestampText)
        val totalAmountText = billView.findViewById<TextView>(R.id.totalAmountText)
        val itemDetailsText = billView.findViewById<TextView>(R.id.itemDetailsText)

        hotelNameText.text = hotelName
        tableNoText.text = "Table No: $tableNo"
        billIdText.text = "Bill ID: $billId"
        timestampText.text = "Date: $timestamp"
        itemDetailsText.text = itemDetails
        totalAmountText.text = "Total: ₹$totalAmount"

        // Convert the view to an image
        val bitmap = createBitmapFromView(billView)

        // Save the image to storage
        saveImageToStorage(bitmap, billId)
    }

    private fun createBitmapFromView(view: View): Bitmap {
        // Measure the view
        view.measure(
            View.MeasureSpec.makeMeasureSpec(800, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )

        // Layout the view with measured width and height
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        // Create a bitmap with correct dimensions
        val bitmap = Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Optional: Set background color if needed
        view.setBackgroundColor(Color.WHITE)

        // Draw the view onto the canvas
        view.draw(canvas)

        return bitmap
    }


    private fun saveImageToStorage(bitmap: Bitmap, billId: String) {
        val contentResolver = requireContext().contentResolver
        val imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "img_$billId.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val imageUri = contentResolver.insert(imageCollection, contentValues)
        imageUri?.let { uri ->
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                Toast.makeText(requireContext(), "Bill saved successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }





}
