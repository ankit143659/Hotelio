package com.example.hotelio

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

import java.util.Calendar

class HotelDetail : Fragment() {

    private lateinit var etHotelName: EditText
    private lateinit var etHotelAddress: EditText
    private lateinit var etHotelContact: EditText
    private lateinit var etHotelEmail: EditText
    private lateinit var etHotelDescription: EditText
    private lateinit var btnOpenTime: Button
    private lateinit var btnCloseTime: Button
    private lateinit var btnSave: Button
    private lateinit var btnUpdate: Button
    private lateinit var btnDelete: Button

    private lateinit var database: DatabaseReference
    private lateinit var userId: String
    private lateinit var share : SharePrefrence


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hotel_detail, container, false)

        share = SharePrefrence(requireContext())

        etHotelName = view.findViewById(R.id.et_hotel_name)
        etHotelAddress = view.findViewById(R.id.et_hotel_address)
        etHotelContact = view.findViewById(R.id.et_hotel_contact)
        etHotelEmail = view.findViewById(R.id.et_hotel_email)
        etHotelDescription = view.findViewById(R.id.et_hotel_description)
        btnOpenTime = view.findViewById(R.id.btn_open_time)
        btnCloseTime = view.findViewById(R.id.btn_close_time)
        btnSave = view.findViewById(R.id.btn_save_hotel)
        btnUpdate = view.findViewById(R.id.btn_update_hotel)
        btnDelete = view.findViewById(R.id.btn_delete_hotel)

        val uploadLogo : Button = view.findViewById(R.id.btn_upload_logo)

        uploadLogo.setOnClickListener{
            val intent = Intent(requireContext(),LoginPage::class.java)
            startActivity(intent)
        }


        database = FirebaseDatabase.getInstance().reference

        userId = share.UserUid().toString()

        btnOpenTime.setOnClickListener { showTimePicker(btnOpenTime) }
        btnCloseTime.setOnClickListener { showTimePicker(btnCloseTime) }

        checkExistingHotelDetails()

        btnSave.setOnClickListener { saveHotelDetails() }
        btnUpdate.setOnClickListener { updateHotelDetails() }
        btnDelete.setOnClickListener { deleteHotelDetails() }

        return view
    }

    // Function to show time picker dialog
    private fun showTimePicker(button: Button) {
        val calendar = Calendar.getInstance()
        val style = R.style.CustomTimePickerDialog
        val timePicker = TimePickerDialog(requireContext(), style, { _, hour, minute ->
            button.text = String.format("%02d:%02d", hour, minute)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)
        timePicker.show()
    }

    // Check if the user already has hotel details in Firebase
    private fun checkExistingHotelDetails() {
        val hotelRef = database.child("hotels").child(userId)
        hotelRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val hotel = snapshot.getValue(Hotel::class.java)
                    hotel?.let {
                        etHotelName.setText(it.hotelName)
                        etHotelAddress.setText(it.hotelAddress)
                        etHotelContact.setText(it.hotelContact)
                        etHotelEmail.setText(it.hotelEmail)
                        etHotelDescription.setText(it.hotelDescription)
                        btnOpenTime.text = it.openTime
                        btnCloseTime.text = it.closeTime

                        btnSave.visibility = View.GONE
                        btnUpdate.visibility = View.VISIBLE
                        btnDelete.visibility = View.VISIBLE
                    }
                } else {
                    btnSave.visibility = View.VISIBLE
                    btnUpdate.visibility = View.GONE
                    btnDelete.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(requireContext(), "Failed to load data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Save hotel details in Firebase
    private fun saveHotelDetails() {
        val hotel = getHotelDetails()
        if (hotel != null) {
            database.child("hotels").child(userId).setValue(hotel)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Hotel details saved", Toast.LENGTH_SHORT).show()
                    checkExistingHotelDetails()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to save", Toast.LENGTH_SHORT).show()
                }
            Toast.makeText(requireContext(), "Hotel details saved", Toast.LENGTH_SHORT).show()
        }
    }

    // Update existing hotel details
    private fun updateHotelDetails() {
        val hotel = getHotelDetails()
        if (hotel != null) {
            database.child("hotels").child(userId).setValue(hotel)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Hotel details updated", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to update", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // Delete hotel details from Firebase
    private fun deleteHotelDetails() {
        database.child("hotels").child(userId).removeValue()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Hotel details deleted", Toast.LENGTH_SHORT).show()
                etHotelName.text.clear()
                etHotelAddress.text.clear()
                etHotelContact.text.clear()
                etHotelEmail.text.clear()
                etHotelDescription.text.clear()
                btnOpenTime.text = "Select Opening Time"
                btnCloseTime.text = "Select Closing Time"

                btnSave.visibility = View.VISIBLE
                btnUpdate.visibility = View.GONE
                btnDelete.visibility = View.GONE
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to delete", Toast.LENGTH_SHORT).show()
            }
    }

    // Function to get hotel details from input fields
    private fun getHotelDetails(): Hotel? {
        val hotelName = etHotelName.text.toString().trim()
        val hotelAddress = etHotelAddress.text.toString().trim()
        val hotelContact = etHotelContact.text.toString().trim()
        val hotelEmail = etHotelEmail.text.toString().trim()
        val hotelDescription = etHotelDescription.text.toString().trim()
        val openTime = btnOpenTime.text.toString()
        val closeTime = btnCloseTime.text.toString()

        return if (hotelName.isNotEmpty() && hotelAddress.isNotEmpty() &&
            hotelContact.isNotEmpty() && hotelEmail.isNotEmpty() && hotelDescription.isNotEmpty()
        ) {
            Hotel(hotelName, hotelAddress, hotelContact, hotelEmail, hotelDescription, openTime, closeTime)
        } else {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            null
        }
    }
}



