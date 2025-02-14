package com.example.wantcab

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class DriverDetailsActivity : AppCompatActivity() {

    private lateinit var driverNameEditText: EditText
    private lateinit var carNameEditText: EditText
    private lateinit var phoneNoEditText: EditText
    private lateinit var carNoEditText: EditText
    private lateinit var submitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_driver_details)

        driverNameEditText = findViewById(R.id.driverNameEditText)
        carNameEditText = findViewById(R.id.carNameEditText)
        phoneNoEditText = findViewById(R.id.phoneNoEditText)
        carNoEditText = findViewById(R.id.carNoEditText)
        submitButton = findViewById(R.id.submitButton)

        submitButton.setOnClickListener {
            val driverName = driverNameEditText.text.toString().trim()
            val carName = carNameEditText.text.toString().trim()
            val phoneNo = phoneNoEditText.text.toString().trim()
            val carNo = carNoEditText.text.toString().trim()

            // Validate input fields
            if (driverName.isEmpty() || carName.isEmpty() || phoneNo.isEmpty() || carNo.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate phone number (only 10 digits)
            if (!phoneNo.matches(Regex("^[0-9]{10}$"))) {
                Toast.makeText(this, "Enter a valid 10-digit phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Generate unique driverId
            val driverId = UUID.randomUUID().toString()

            val driverData = mapOf(
                "name" to driverName,
                "car_name" to carName,
                "phone_number" to phoneNo,
                "car_number" to carNo
            )

            // Log the driver data
            Log.d("DriverDetails", "Saving driver data: $driverData")

            val db = FirebaseFirestore.getInstance()

            db.collection("drivers")
                .document(driverId)
                .set(driverData)
                .addOnSuccessListener {
                    Log.d("DriverDetails", "Data saved successfully")
                    Toast.makeText(this, "Driver details saved successfully!", Toast.LENGTH_SHORT).show()

                    // Save driver details in SharedPreferences
                    val sharedPreferences = getSharedPreferences("DriverPrefs", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("driverId", driverId)
                    editor.putString("driverName", driverName)
                    editor.putString("carName", carName)
                    editor.putString("phoneNo", phoneNo)
                    editor.putString("carNo", carNo)
                    editor.apply()

                    // Navigate to MainActivity
                    val intent = Intent(this, MainActivity::class.java).apply {
                        putExtra("driverId", driverId)
                        putExtra("driverName", driverName)
                        putExtra("carName", carName)
                        putExtra("phoneNo", phoneNo)
                        putExtra("carNo", carNo)
                    }
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { exception ->
                    Log.e("DriverDetails", "Failed to save driver details", exception)
                    Toast.makeText(this, "Failed to save driver details. Please try again.", Toast.LENGTH_LONG).show()
                }
        }
    }
}
