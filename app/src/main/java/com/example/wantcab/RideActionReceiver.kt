package com.example.wantcab

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class RideActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val requestId = intent.getStringExtra("REQUEST_ID")
        val notificationId = intent.getIntExtra("NOTIFICATION_ID", -1)

        if (requestId.isNullOrEmpty()) {
            Log.e("RideActionReceiver", "❌ Missing ride request ID")
            return
        }

        Log.d("RideActionReceiver", "✅ Received action: ${intent.action}, Notification ID: $notificationId")

        val rideRef = FirebaseDatabase.getInstance().getReference("fetchingdriver").child(requestId)

        // Check current ride status before updating
        rideRef.child("status").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentStatus = snapshot.value as? String ?: "pending"

                // Prevent multiple responses
                if (currentStatus == "successful" || currentStatus == "denied") {
                    Log.w("RideActionReceiver", "⚠️ Ride request already processed (Status: $currentStatus)")
                    Toast.makeText(context, "This ride request has already been responded to.", Toast.LENGTH_SHORT).show()
                    dismissNotification(context, notificationId)
                    return
                }

                when (intent.action) {
                    "ACCEPT_RIDE" -> handleAcceptRide(context, intent, rideRef, notificationId)
                    "DENY_RIDE" -> handleDenyRide(context, rideRef, notificationId)
                    else -> Log.e("RideActionReceiver", "❌ Unknown action received")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("RideActionReceiver", "❌ Firebase error: ${error.message}")
            }
        })
    }

    private fun handleAcceptRide(
        context: Context,
        intent: Intent,
        rideRef: DatabaseReference,
        notificationId: Int
    ) {
        val driverId = intent.getStringExtra("DRIVER_ID")
        val driverName = intent.getStringExtra("DRIVER_NAME")
        val carNo = intent.getStringExtra("CAR_NO")

        if (driverId.isNullOrEmpty() || driverName.isNullOrEmpty() || carNo.isNullOrEmpty()) {
            Log.e("RideActionReceiver", "❌ Missing driver details")
            Toast.makeText(context, "Failed: Missing driver details", Toast.LENGTH_SHORT).show()
            return
        }

        val updateMap = mapOf(
            "driverId" to driverId,
            "driverName" to driverName,
            "carNo" to carNo,
            "status" to "successful"
        )

        rideRef.updateChildren(updateMap)
            .addOnSuccessListener {
                Log.d("RideActionReceiver", "✅ Ride accepted successfully")
                Toast.makeText(context, "Ride Accepted", Toast.LENGTH_SHORT).show()
                dismissNotification(context, notificationId)
            }
            .addOnFailureListener { e ->
                Log.e("RideActionReceiver", "❌ Failed to accept ride: ${e.message}")
            }
    }

    private fun handleDenyRide(context: Context, rideRef: DatabaseReference, notificationId: Int) {
        rideRef.updateChildren(mapOf("status" to "denied"))
            .addOnSuccessListener {
                Log.d("RideActionReceiver", "✅ Ride denied successfully")
                Toast.makeText(context, "Ride Denied", Toast.LENGTH_SHORT).show()
                dismissNotification(context, notificationId)
            }
            .addOnFailureListener { e ->
                Log.e("RideActionReceiver", "❌ Failed to deny ride: ${e.message}")
            }
    }

    private fun dismissNotification(context: Context, notificationId: Int) {
        if (notificationId != -1) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(notificationId)
            Log.d("RideActionReceiver", "✅ Notification dismissed (ID: $notificationId)")
        } else {
            Log.e("RideActionReceiver", "❌ Invalid notification ID, cannot dismiss")
        }
    }

}
