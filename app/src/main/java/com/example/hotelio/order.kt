package com.example.hotelio

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class order : Fragment() {

    private lateinit var ordersContainer: LinearLayout
    private lateinit var database: DatabaseReference
    private lateinit var share : SharePrefrence
    private lateinit var hotelUid : String// Replace with dynamic UID

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_order, container, false)
        ordersContainer = view.findViewById(R.id.ordersContainer)
        share = SharePrefrence(requireContext())
        hotelUid = share.UserUid().toString()

        database = FirebaseDatabase.getInstance().reference.child("hotels")
            .child(hotelUid)
            .child("Orders")

        fetchOrders()
        return view
    }

    private var ordersListener: ValueEventListener? = null

    private fun fetchOrders() {
        ordersListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!isAdded || context == null) return


                if (!snapshot.exists()) {
                    Toast.makeText(requireContext(), "No Orders Found!", Toast.LENGTH_LONG).show()
                    return
                }

                ordersContainer.removeAllViews()
                val ordersList = mutableListOf<Pair<Long, DataSnapshot>>()

                for (tableSnapshot in snapshot.children) {
                    for (orderSnapshot in tableSnapshot.children) {
                        val timestamp = orderSnapshot.child("timestamp").value?.toString() ?: "0"
                        val timeInMillis = parseTimestamp(timestamp)
                        ordersList.add(Pair(timeInMillis, orderSnapshot))
                    }
                }

                ordersList.sortByDescending { it.first }
                for ((_, orderSnapshot) in ordersList) {
                    orderSnapshot.key?.let { orderId -> displayOrder(orderId, orderSnapshot) }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }

        val ordersRef = FirebaseDatabase.getInstance().reference
            .child("hotels")
            .child(hotelUid)
            .child("Orders")

        ordersRef.addValueEventListener(ordersListener!!)
    }


    // Remove listener when fragment is destroyed
    override fun onDestroyView() {
        super.onDestroyView()
        ordersListener?.let { database.removeEventListener(it) }
    }


    private fun parseTimestamp(timestamp: String): Long {
        return try {
            val format = SimpleDateFormat("d/M/yyyy, hh:mm:ss a", Locale.getDefault())
            format.parse(timestamp)?.time ?: 0L  // Convert timestamp to milliseconds
        } catch (e: Exception) {
            println("Timestamp parsing error: ${e.message}")
            0L
        }
    }

    @SuppressLint("MissingInflatedId")
    private fun displayOrder(orderId: String, orderSnapshot: DataSnapshot) {
        if (!isAdded || context == null) return  // Prevent crash if fragment is detached

        val inflater = LayoutInflater.from(requireContext()) // Safe usage now
        val orderView = inflater.inflate(R.layout.order_item, ordersContainer, false)

        val tableNoText = orderView.findViewById<TextView>(R.id.tableNoText)
        val itemDetailsText = orderView.findViewById<TextView>(R.id.itemDetailsText)
        val priceText = orderView.findViewById<TextView>(R.id.priceText)
        val confirmBtn = orderView.findViewById<Button>(R.id.confirmBtn)
        val rejectBtn = orderView.findViewById<Button>(R.id.rejectBtn)
        val deleteBtn = orderView.findViewById<Button>(R.id.deleteBtn)

        val tableNo = orderSnapshot.child("tableNo").value.toString()
        val status = orderSnapshot.child("status").value.toString()

        val items = orderSnapshot.child("items").children.map { itemSnapshot ->
            val itemName = itemSnapshot.child("item").value.toString()
            val quantity = itemSnapshot.child("quantity").value.toString()
            val price = itemSnapshot.child("total").value.toString().toInt()
            Triple(itemName, quantity, price)
        }

        tableNoText.text = "Table No: $tableNo"
        itemDetailsText.text = items.joinToString("\n") { "${it.first} x${it.second}" }
        priceText.text = "₹${items.sumOf { it.third }}"


        when (status) {
            "confirmed" -> {
                confirmBtn.text = "Confirmed"
                confirmBtn.isEnabled = false
                confirmBtn.setBackgroundColor(Color.GRAY)
                rejectBtn.visibility = View.GONE
                deleteBtn.visibility=View.VISIBLE
            }
            "rejected" -> {
                rejectBtn.text = "Rejected"
                rejectBtn.isEnabled = false
                rejectBtn.setBackgroundColor(Color.GRAY)
                confirmBtn.visibility=View.GONE
                deleteBtn.visibility=View.VISIBLE
            }
        }

        confirmBtn.setOnClickListener {
            database.child(tableNo).child(orderId).child("status").setValue("confirmed")
        }
        deleteBtn.setOnClickListener{
            database.child(tableNo).child(orderId).removeValue().addOnCompleteListener {
                if (it.isSuccessful) {
                    ordersContainer.removeView(orderView)
                }
            }
        }
        rejectBtn.setOnClickListener {
            database.child(tableNo).child(orderId).child("status").setValue("rejected")
        }

        ordersContainer.addView(orderView, 0)
        ordersContainer.invalidate()
        ordersContainer.requestLayout()
    }

}
