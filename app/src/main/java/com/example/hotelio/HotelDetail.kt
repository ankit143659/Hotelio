package com.example.hotelio

import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_hotel_detail, container, false)

        etHotelName = view.findViewById(R.id.et_hotel_name)
        etHotelAddress = view.findViewById(R.id.et_hotel_address)
        etHotelContact = view.findViewById(R.id.et_hotel_contact)
        etHotelEmail = view.findViewById(R.id.et_hotel_email)
        etHotelDescription = view.findViewById(R.id.et_hotel_description)
        btnOpenTime = view.findViewById(R.id.btn_open_time)
        btnCloseTime = view.findViewById(R.id.btn_close_time)
        btnSave = view.findViewById(R.id.btn_save_hotel)

        btnOpenTime.setOnClickListener { showTimePicker(btnOpenTime) }
        btnCloseTime.setOnClickListener { showTimePicker(btnCloseTime) }

        return view
    }

    private fun showTimePicker(button: Button) {
        val calendar = Calendar.getInstance()
        val style = R.style.CustomTimePickerDialog
        val timePicker = TimePickerDialog(requireContext(),style, { _, hour, minute ->
            button.text = String.format("%02d:%02d", hour, minute)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false)
        timePicker.show()
    }
}