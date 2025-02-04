package com.example.hotelio

import android.annotation.SuppressLint
import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

class TableManagement : Fragment() {

    private lateinit var tableContainer: LinearLayout
    private lateinit var btnAddTable: Button
    private lateinit var database: DatabaseReference
    private lateinit var userId: String
    private var tableCount = 0
    private lateinit var share : SharePrefrence

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_table_management, container, false)
        share = SharePrefrence(requireContext())

        tableContainer = view.findViewById(R.id.tableContainer)
        btnAddTable = view.findViewById(R.id.btnAddTable)

        database = FirebaseDatabase.getInstance().reference
        userId = share.UserUid().toString()  // Replace with actual user ID fetching logic

        btnAddTable.setOnClickListener {
            showTableDialog()
        }

        fetchStoredTables()

        return view
    }

    private fun fetchStoredTables() {
        database.child("hotels").child(userId).child("tables").get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                snapshot.children.forEach { table ->
                    val tableName = table.key ?: return@forEach
                    val tableNumber = tableName.replace("Table", "").toIntOrNull() ?: return@forEach
                    addTableView(tableNumber)
                }
            }
        }.addOnFailureListener {
            Toast.makeText(requireContext(), "Failed to load tables", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun showTableDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_table, null)
        val etTableCount = dialogView.findViewById<EditText>(R.id.etTableCount)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnAdd = dialogView.findViewById<Button>(R.id.btnAdd)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        btnAdd.setOnClickListener {
            val count = etTableCount.text.toString().toIntOrNull()
            if (count != null && count > 0) {
                val tablesMap = mutableMapOf<String, Boolean>()
                for (i in 1..count) {
                    val tableNumber = tableCount + i
                    addTableView(tableNumber)
                    tablesMap["Table$tableNumber"] = true  // Store table as a key in Firebase
                }
                tableCount += count

                // Store in Firebase under the user's hotel
                database.child("hotels").child(userId).child("tables").setValue(tablesMap)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Tables added successfully!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Failed to add tables", Toast.LENGTH_SHORT).show()
                    }

                dialog.dismiss()
            } else {
                etTableCount.error = "Enter a valid number"
            }
        }

        dialog.show()
    }

    private fun addTableView(tableNumber: Int) {
        val tableView = LayoutInflater.from(requireContext()).inflate(R.layout.item_table, null, false)
        val tvTableName = tableView.findViewById<TextView>(R.id.tvTableNumber)
        val btnRemove = tableView.findViewById<Button>(R.id.btnRemoveTable)
        val btnGenerateQR = tableView.findViewById<Button>(R.id.btnGenerateQr)

        tvTableName.text = "Table $tableNumber"

        btnRemove.setOnClickListener {
            tableContainer.removeView(tableView)
        }

        btnGenerateQR.setOnClickListener {

            val qrData = "https://lucky-lollipop-fa2d9b.netlify.app/?hotelUid=$userId&tableNo=$tableNumber"
            val qrCodeBitmap = generateQRCode(qrData)

            // Save the QR code to the gallery
            if (qrCodeBitmap != null) {
                saveQRCodeToGallery(qrCodeBitmap)
            } else {
                Toast.makeText(requireContext(), "Failed to generate QR code", Toast.LENGTH_SHORT).show()
            }
            // Fetch hotel UID from Firebase
            /*getHotelUidFromDatabase(userId, { hotelUid ->
                // Generate the QR code URL

            }, {
                Toast.makeText(requireContext(), "Hotel not found for this user", Toast.LENGTH_SHORT).show()
            })*/
        }

        tableContainer.addView(tableView)
    }

    private fun generateQRCode(qrData: String): Bitmap? {
        val width = 500
        val height = 500
        val bitMatrix: BitMatrix = MultiFormatWriter().encode(qrData, BarcodeFormat.QR_CODE, width, height)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bitmap
    }

    private fun saveQRCodeToGallery(bitmap: Bitmap) {
        checkAndRequestPermissions()
        val contentResolver = requireContext().contentResolver
        val imageCollection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "QRCode_${System.currentTimeMillis()}.png")
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }

        val imageUri = contentResolver.insert(imageCollection, contentValues)
        imageUri?.let { uri ->
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                Toast.makeText(requireContext(), "QR code saved successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Fetch hotel UID from Firebase
    private fun getHotelUidFromDatabase(userId: String, onSuccess: (String) -> Unit, onFailure: () -> Unit) {
        database.child("users").child(userId).child("hotelUid").get().addOnSuccessListener {
            val hotelUid = it.value as? String
            if (hotelUid != null) {
                onSuccess(hotelUid)
            } else {
                onFailure()
            }
        }.addOnFailureListener {
            onFailure()
        }
    }

    fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = mutableListOf<String>()

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            if (permissions.isNotEmpty()) {
                ActivityCompat.requestPermissions(requireActivity(), permissions.toTypedArray(), PERMISSION_REQUEST_CODE)
            }
        }
    }

    // Handle permission result
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permissions granted, proceed with saving QR code
                Toast.makeText(requireContext(), "Permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Permissions denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val PERMISSION_REQUEST_CODE = 100
    }
}
