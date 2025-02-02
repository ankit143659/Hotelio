package com.example.hotelio

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class Slide2 : Fragment() {


    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_slide2, container, false)
        val btnNext = view.findViewById<Button>(R.id.nextBtn)
        btnNext.setOnClickListener{
            val intent = Intent(requireContext(),HomeMainPage::class.java)
            startActivity(intent)
        }
        return view
    }

}