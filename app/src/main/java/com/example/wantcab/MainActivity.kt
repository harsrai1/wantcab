//package com.example.wantcab
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.util.Log
//import android.widget.Button
//import android.widget.Toast
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import com.google.android.gms.location.*
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.GoogleMap
//import com.google.android.gms.maps.OnMapReadyCallback
//import com.google.android.gms.maps.SupportMapFragment
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.MarkerOptions
//import com.google.firebase.database.FirebaseDatabase
//import java.util.*
//
//class MainActivity : AppCompatActivity(), OnMapReadyCallback {
//
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private lateinit var locationCallback: LocationCallback
//    private lateinit var googleMap: GoogleMap
//    private var isOnDuty = false
//    private lateinit var driverId: String
//    private lateinit var driverName: String
//    private lateinit var carName: String
//    private lateinit var phoneNo: String
//    private lateinit var carNo: String
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        // Get the driver details from the Intent passed from DriverDetailsActivity
//        driverId = intent.getStringExtra("driverId") ?: UUID.randomUUID().toString()
//        driverName = intent.getStringExtra("driverName") ?: "Unknown"
//        carName = intent.getStringExtra("carName") ?: "Unknown"
//        phoneNo = intent.getStringExtra("phoneNo") ?: "Unknown"
//        carNo = intent.getStringExtra("carNo") ?: "Unknown"
//
//        // Initialize location services
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//
//        // Initialize the map fragment using SupportMapFragment
//        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)
//
//        val toggleDutyButton: Button = findViewById(R.id.toggleDutyButton)
//        toggleDutyButton.setOnClickListener {
//            isOnDuty = !isOnDuty
//            toggleDutyButton.text = if (isOnDuty) "Go Off Duty" else "Go On Duty"
//            if (isOnDuty) {
//                // Store the driver data in Firebase when going on duty
//                startLocationUpdates()
//                saveDriverData()
//            } else {
//                // Stop location updates and remove data from Firebase when going off duty
//                stopLocationUpdates()
//            }
//        }
//
//        setupLocationCallback()
//    }
//
//    override fun onMapReady(map: GoogleMap) {
//        googleMap = map
//        googleMap.uiSettings.isZoomControlsEnabled = true
//    }
//
//    private fun setupLocationCallback() {
//        locationCallback = object : LocationCallback() {
//            override fun onLocationResult(locationResult: LocationResult) {
//                locationResult.lastLocation?.let { location ->
//                    if (::googleMap.isInitialized) {
//                        val currentLatLng = LatLng(location.latitude, location.longitude)
//                        googleMap.clear()
//                        googleMap.addMarker(MarkerOptions().position(currentLatLng).title("Driver Location"))
//                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
//
//                        // Update location in Firebase Realtime Database
//                        val databaseRef = FirebaseDatabase.getInstance().getReference("drivers/$driverId")
//                        val locationData = mapOf(
//                            "latitude" to location.latitude,
//                            "longitude" to location.longitude,
//                            "status" to if (isOnDuty) "on" else "off"
//                        )
//                        databaseRef.updateChildren(locationData).addOnSuccessListener {
//                            Log.d("Firebase", "Location updated successfully: $locationData")
//                        }.addOnFailureListener {
//                            Log.e("Firebase", "Failed to update location", it)
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun startLocationUpdates() {
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//            return
//        }
//
//        val locationRequest = LocationRequest.Builder(
//            Priority.PRIORITY_HIGH_ACCURACY,
//            5000L
//        ).build()
//
//        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
//        Toast.makeText(this, "Started location updates", Toast.LENGTH_SHORT).show()
//    }
//
//    private fun stopLocationUpdates() {
//        fusedLocationClient.removeLocationUpdates(locationCallback)
//        Toast.makeText(this, "Stopped location updates", Toast.LENGTH_SHORT).show()
//
//        // Set the driver's status to "off" and remove all data (location, car name, car number, etc.) from Firebase
//        val databaseRef = FirebaseDatabase.getInstance().getReference("drivers/$driverId")
//        databaseRef.removeValue().addOnSuccessListener {
//            Log.d("Firebase", "Driver data removed successfully")
//        }.addOnFailureListener { exception ->
//            Log.e("Firebase", "Failed to remove driver data", exception)
//        }
//    }
//
//    private fun saveDriverData() {
//        // Save the driver data (including car details) to Firebase Realtime Database
//        val databaseRef = FirebaseDatabase.getInstance().getReference("drivers/$driverId")
//        val driverData = mapOf(
//            "name" to driverName,
//            "car_name" to carName,
//            "phone_number" to phoneNo,
//            "car_number" to carNo,
//            "status" to "on", // Driver is now on duty
//            "latitude" to 0.0, // Default location
//            "longitude" to 0.0
//        )
//
//        databaseRef.setValue(driverData).addOnSuccessListener {
//            Log.d("Firebase", "Driver data saved successfully")
//        }.addOnFailureListener { exception ->
//            Log.e("Firebase", "Failed to save driver data", exception)
//        }
//    }
//
//    private val requestPermissionLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//            if (isGranted) {
//                startLocationUpdates()
//            } else {
//                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
//            }
//        }
//}
//
//
//
//




//package com.example.wantcab
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Bundle
//import android.provider.Settings
//import android.util.Log
//import android.view.Gravity
//import android.widget.Button
//import android.widget.ImageButton
//import android.widget.Toast
//import androidx.activity.result.contract.ActivityResultContracts
//import androidx.appcompat.app.AlertDialog
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.app.NotificationCompat
//import androidx.core.view.GravityCompat
//import androidx.drawerlayout.widget.DrawerLayout
//import com.google.android.gms.location.*
//import com.google.android.gms.maps.CameraUpdateFactory
//import com.google.android.gms.maps.GoogleMap
//import com.google.android.gms.maps.OnMapReadyCallback
//import com.google.android.gms.maps.SupportMapFragment
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.MarkerOptions
//import com.google.firebase.database.FirebaseDatabase
//import com.google.firebase.messaging.FirebaseMessaging
//import com.google.firebase.messaging.RemoteMessage
//import java.util.*
//
//class MainActivity : AppCompatActivity(), OnMapReadyCallback {
//
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private lateinit var locationCallback: LocationCallback
//    private lateinit var googleMap: GoogleMap
//    private var isOnDuty = false
//    private lateinit var driverId: String
//    private lateinit var driverName: String
//    private lateinit var carName: String
//    private lateinit var phoneNo: String
//    private lateinit var carNo: String
//    private var rideRequest: RemoteMessage? = null
//
//    private lateinit var acceptButton: Button
//    private lateinit var denyButton: Button
//
//    private lateinit var drawerLayout: androidx.drawerlayout.widget.DrawerLayout
//
//    @SuppressLint("MissingInflatedId")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        // Initialize DrawerLayout
//        drawerLayout = findViewById(R.id.drawerLayout)
//
//        // Check if the driver details are saved in SharedPreferences
//        val sharedPreferences = getSharedPreferences("DriverPrefs", MODE_PRIVATE)
//        driverId = sharedPreferences.getString("driverId", null) ?: ""
//        driverName = sharedPreferences.getString("driverName", null) ?: ""
//        carName = sharedPreferences.getString("carName", null) ?: ""
//        phoneNo = sharedPreferences.getString("phoneNo", null) ?: ""
//        carNo = sharedPreferences.getString("carNo", null) ?: ""
//
//        // If driver details are found, populate the UI (or use them for other purposes)
//        if (driverId.isNotEmpty()) {
//            Toast.makeText(this, "Welcome back, $driverName!", Toast.LENGTH_SHORT).show()
//        } else {
//            // If no driver details are found, notify the user to enter the details
//            Toast.makeText(this, "Please enter your driver details.", Toast.LENGTH_SHORT).show()
//            val intent = Intent(this, DriverDetailsActivity::class.java)
//            startActivity(intent)
//            finish()
//        }
//
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
//
//        // Initialize map
//        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
//        mapFragment.getMapAsync(this)
//
//        // Button Initialization
//        acceptButton = findViewById(R.id.acceptButton)
//        denyButton = findViewById(R.id.denyButton)
//
//        // Handle toggle duty button
//        val toggleDutyButton: Button = findViewById(R.id.toggleDutyButton)
//        toggleDutyButton.setOnClickListener {
//            isOnDuty = !isOnDuty
//            toggleDutyButton.text = if (isOnDuty) "Go Off Duty" else "Go On Duty"
//            if (isOnDuty) {
//                startLocationUpdates()
//                saveDriverData()
//            } else {
//                stopLocationUpdates()
//            }
//        }
//
//        acceptButton.setOnClickListener {
//            acceptRide()
//        }
//
//        denyButton.setOnClickListener {
//            denyRide()
//        }
//
//        setupLocationCallback()
//        setupFirebaseMessaging()
//
//        // Check for notification settings
//        checkNotificationSettings()
//
//        // Handle menu button click to open navigation drawer
//        val menuButton: ImageButton = findViewById(R.id.menuButton)
//        val drawerLayout: DrawerLayout = findViewById(R.id.drawerLayout)
//
//        menuButton.setOnClickListener {
//            drawerLayout.openDrawer(GravityCompat.START)  // Open the drawer when the button is clicked
//        }
//
//    }
//
//    override fun onMapReady(map: GoogleMap) {
//        googleMap = map
//        googleMap.uiSettings.isZoomControlsEnabled = true
//    }
//
//    private fun setupLocationCallback() {
//        locationCallback = object : LocationCallback() {
//            override fun onLocationResult(locationResult: LocationResult) {
//                locationResult.lastLocation?.let { location ->
//                    if (::googleMap.isInitialized) {
//                        val currentLatLng = LatLng(location.latitude, location.longitude)
//                        googleMap.clear()
//                        googleMap.addMarker(MarkerOptions().position(currentLatLng).title("Driver Location"))
//                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
//
//                        val databaseRef = FirebaseDatabase.getInstance().getReference("drivers/$driverId")
//                        val locationData = mapOf(
//                            "latitude" to location.latitude,
//                            "longitude" to location.longitude,
//                            "status" to if (isOnDuty) "on" else "off"
//                        )
//                        databaseRef.updateChildren(locationData)
//                    }
//                }
//            }
//        }
//    }
//
//    @SuppressLint("MissingPermission")
//    private fun startLocationUpdates() {
//        if (ActivityCompat.checkSelfPermission(
//                this,
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
//            return
//        }
//
//        val locationRequest = LocationRequest.Builder(
//            Priority.PRIORITY_HIGH_ACCURACY,
//            5000L
//        ).build()
//
//        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
//        Toast.makeText(this, "Started location updates", Toast.LENGTH_SHORT).show()
//    }
//
//    private fun stopLocationUpdates() {
//        fusedLocationClient.removeLocationUpdates(locationCallback)
//        Toast.makeText(this, "Stopped location updates", Toast.LENGTH_SHORT).show()
//
//        val databaseRef = FirebaseDatabase.getInstance().getReference("drivers/$driverId")
//        databaseRef.removeValue()
//    }
//
//    private fun saveDriverData() {
//        val databaseRef = FirebaseDatabase.getInstance().getReference("drivers/$driverId")
//        val driverData = mapOf(
//            "name" to driverName,
//            "car_name" to carName,
//            "phone_number" to phoneNo,
//            "car_number" to carNo,
//            "status" to "on",
//            "latitude" to 0.0,
//            "longitude" to 0.0
//        )
//
//        databaseRef.setValue(driverData)
//    }
//
//    private fun acceptRide() {
//        rideRequest?.let { request ->
//            val rideData = mapOf(
//                "driver_id" to driverId,
//                "status" to "accepted"
//            )
//
//            FirebaseDatabase.getInstance().getReference("rides/${request.data["ride_id"]}")
//                .updateChildren(rideData)
//
//            Toast.makeText(this, "Ride Accepted", Toast.LENGTH_SHORT).show()
//
//            // Show notification with rider details
//            showRideNotification(
//                riderName = request.data["rider_name"] ?: "Unknown",
//                pickupLocation = request.data["pickup_address"] ?: "Unknown",
//                dropLocation = request.data["drop_address"] ?: "Unknown",
//                amount = request.data["amount"] ?: "0"
//            )
//
//            // Change button colors after acceptance
//            acceptButton.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
//            denyButton.setBackgroundColor(resources.getColor(android.R.color.holo_red_light))
//        }
//    }
//
//    private fun denyRide() {
//        rideRequest?.let { request ->
//            val rideData = mapOf(
//                "driver_id" to driverId,
//                "status" to "denied"
//            )
//
//            FirebaseDatabase.getInstance().getReference("rides/${request.data["ride_id"]}")
//                .updateChildren(rideData)
//
//            Toast.makeText(this, "Ride Denied", Toast.LENGTH_SHORT).show()
//
//            // Change button colors after denial
//            acceptButton.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
//            denyButton.setBackgroundColor(resources.getColor(android.R.color.darker_gray))
//        }
//    }
//
//    // Firebase messaging setup to listen for ride requests
//    private fun setupFirebaseMessaging() {
//        FirebaseMessaging.getInstance().subscribeToTopic("rideRequests")
//            .addOnCompleteListener { task ->
//                val msg = if (task.isSuccessful) "Subscribed to ride requests topic" else "Subscription failed"
//                Log.d("FirebaseMessaging", msg)
//            }
//    }
//
//    // Permission request for location updates
//    private val requestPermissionLauncher =
//        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
//            if (isGranted) {
//                startLocationUpdates()
//            } else {
//                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//    // Notification Settings Check
//    private fun checkNotificationSettings() {
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        if (!notificationManager.areNotificationsEnabled()) {
//            AlertDialog.Builder(this)
//                .setTitle("Enable Notifications")
//                .setMessage("Notifications are essential to receive ride requests. Please enable notifications for WantCab.")
//                .setPositiveButton("Go to Settings") { _, _ ->
//                    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
//                        putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
//                    }
//                    startActivity(intent)
//                }
//                .setNegativeButton("Cancel") { dialog, _ ->
//                    dialog.dismiss()
//                }
//                .show()
//        }
//    }
//
//    // Show ride details in notification
//    private fun showRideNotification(riderName: String, pickupLocation: String, dropLocation: String, amount: String) {
//        val channelId = "ride_channel"
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(channelId, "Ride Notifications", NotificationManager.IMPORTANCE_HIGH)
//            notificationManager.createNotificationChannel(channel)
//        }
//
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(android.R.drawable.ic_menu_info_details)
//            .setContentTitle("Ride Accepted: $riderName")
//            .setContentText("Pickup: $pickupLocation, Drop: $dropLocation, Amount: $$amount")
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .build()
//
//        notificationManager.notify(1, notification)
//    }
//}

//package com.example.wantcab
//
//import android.Manifest
//import android.annotation.SuppressLint
//import android.app.Notification
//import android.app.NotificationChannel
//import android.app.NotificationManager
//import android.app.PendingIntent
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.os.Build
//import android.os.Bundle
//import android.os.Handler
//import android.os.Looper
//import android.util.Log
//import android.widget.Button
//import android.widget.ImageButton
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.app.NotificationCompat
//import androidx.core.view.GravityCompat
//import androidx.drawerlayout.widget.DrawerLayout
//import com.google.firebase.database.*
//import com.google.firebase.database.ktx.getValue
//
//class MainActivity : AppCompatActivity() {
//
//    private lateinit var database: DatabaseReference
//    private lateinit var acceptButton: Button
//    private lateinit var denyButton: Button
//    private lateinit var drawerLayout: DrawerLayout
//    private lateinit var driverId: String
//    private lateinit var driverName: String
//    private lateinit var carNo: String
//    private val handler = Handler(Looper.getMainLooper())
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        // Initialize Firebase
//        database = FirebaseDatabase.getInstance().getReference("fetchingdriver")
//
//        // Initialize UI
//        acceptButton = findViewById(R.id.acceptButton)
//        denyButton = findViewById(R.id.denyButton)
//        drawerLayout = findViewById(R.id.drawerLayout)
//
//        // Retrieve driver details from SharedPreferences
//        val sharedPreferences = getSharedPreferences("DriverPrefs", MODE_PRIVATE)
//        driverId = sharedPreferences.getString("driverId", "") ?: ""
//        driverName = sharedPreferences.getString("driverName", "") ?: ""
//        carNo = sharedPreferences.getString("carNo", "") ?: ""
//
//        // Handle menu button
//        val menuButton: ImageButton = findViewById(R.id.menuButton)
//        menuButton.setOnClickListener {
//            drawerLayout.openDrawer(GravityCompat.START)
//        }
//
//        // Start checking database every 30 seconds
//        startRideRequestListener()
//    }
//
//    private fun startRideRequestListener() {
//        handler.postDelayed(object : Runnable {
//            override fun run() {
//                checkForRideRequests()
//                handler.postDelayed(this, 30000) // Run every 30 seconds
//            }
//        }, 30000)
//    }
//
//    private fun checkForRideRequests() {
//        database.orderByChild("status").equalTo("pending").limitToFirst(1)
//            .addListenerForSingleValueEvent(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    for (rideSnapshot in snapshot.children) {
//                        val rideRequest = rideSnapshot.getValue<RideRequest>()
//                        rideRequest?.let {
//                            showRideNotification(it, rideSnapshot.key!!)
//                        }
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {
//                    Log.e("Firebase", "Database error: ${error.message}")
//                }
//            })
//    }
//
//    private fun showRideNotification(rideRequest: RideRequest, requestId: String) {
//        val channelId = "ride_request_channel"
//        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(channelId, "Ride Requests", NotificationManager.IMPORTANCE_HIGH)
//            notificationManager.createNotificationChannel(channel)
//        }
//
//        // Accept Intent
//        val acceptIntent = Intent(this, RideActionReceiver::class.java).apply {
//            action = "ACCEPT_RIDE"
//            putExtra("REQUEST_ID", requestId)
//            putExtra("DRIVER_ID", driverId)
//            putExtra("DRIVER_NAME", driverName)
//            putExtra("CAR_NO", carNo)
//        }
//        val acceptPendingIntent = PendingIntent.getBroadcast(
//            this, 0, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        // Deny Intent
//        val denyIntent = Intent(this, RideActionReceiver::class.java).apply {
//            action = "DENY_RIDE"
//            putExtra("REQUEST_ID", requestId)
//        }
//        val denyPendingIntent = PendingIntent.getBroadcast(
//            this, 1, denyIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
//        )
//
//        val notification = NotificationCompat.Builder(this, channelId)
//            .setSmallIcon(android.R.drawable.ic_menu_info_details)
//            .setContentTitle("New Ride Request")
//            .setContentText("Pickup: ${rideRequest.pickupAddress}, Drop: ${rideRequest.dropAddress}, Price: ${rideRequest.price}")
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .addAction(android.R.drawable.ic_menu_compass, "Accept", acceptPendingIntent)
//            .addAction(android.R.drawable.ic_delete, "Deny", denyPendingIntent)
//            .setAutoCancel(true)
//            .build()
//
//        notificationManager.notify(1, notification)
//
//        // Change button colors
//        acceptButton.setBackgroundColor(resources.getColor(android.R.color.holo_blue_light))
//        denyButton.setBackgroundColor(resources.getColor(android.R.color.holo_red_light))
//
//        // Dismiss notification after 25 seconds
//        Handler(Looper.getMainLooper()).postDelayed({
//            notificationManager.cancel(1)
//        }, 25000)
//    }
//}


package com.example.wantcab

import android.Manifest
import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.*
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var database: DatabaseReference
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var googleMap: GoogleMap
    private var isOnDuty = false
    private lateinit var driverId: String
    private lateinit var driverName: String
    private lateinit var carName: String
    private lateinit var phoneNo: String
    private lateinit var carNo: String
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var toggleDutyButton: Button
    private lateinit var drawerLayout: DrawerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = FirebaseDatabase.getInstance().getReference("fetchingdriver")

        drawerLayout = findViewById(R.id.drawerLayout)
        toggleDutyButton = findViewById(R.id.toggleDutyButton)
        val menuButton: ImageButton = findViewById(R.id.menuButton)

        loadDriverDetails()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        toggleDutyButton.setOnClickListener {
            toggleDuty()
        }

        setupLocationCallback()

        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

    }

    private fun loadDriverDetails() {
        val sharedPreferences = getSharedPreferences("DriverPrefs", MODE_PRIVATE)
        driverId = sharedPreferences.getString("driverId", "") ?: ""
        driverName = sharedPreferences.getString("driverName", "") ?: ""
        carName = sharedPreferences.getString("carName", "") ?: ""
        phoneNo = sharedPreferences.getString("phoneNo", "") ?: ""
        carNo = sharedPreferences.getString("carNo", "") ?: ""

        if (driverId.isNotEmpty()) {
            Toast.makeText(this, "Welcome back, $driverName!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Please enter your driver details.", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, DriverDetailsActivity::class.java))
            finish()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.uiSettings.isZoomControlsEnabled = true
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    if (::googleMap.isInitialized) {
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        googleMap.clear()
                        googleMap.addMarker(MarkerOptions().position(currentLatLng).title("Driver Location"))
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))

                        val databaseRef = FirebaseDatabase.getInstance().getReference("drivers/$driverId")
                        val locationData = mapOf(
                            "latitude" to location.latitude,
                            "longitude" to location.longitude,
                            "status" to if (isOnDuty) "on" else "off"
                        )
                        databaseRef.updateChildren(locationData)
                    }
                }
            }
        }
    }

    private fun toggleDuty() {
        isOnDuty = !isOnDuty
        toggleDutyButton.text = if (isOnDuty) "Go Off Duty" else "Go On Duty"
        updateButtonColor()
        if (isOnDuty) {
            startLocationUpdates()
            saveDriverData()
            startRideRequestListener() // ✅ Start listening for rides when on duty
        } else {
            stopLocationUpdates()
            stopRideRequestListener()  // ✅ Stop ride listener when off duty
        }
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            5000L
        ).build()

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
        Toast.makeText(this, "Started location updates", Toast.LENGTH_SHORT).show()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Toast.makeText(this, "Stopped location updates", Toast.LENGTH_SHORT).show()

        val databaseRef = FirebaseDatabase.getInstance().getReference("drivers/$driverId")
        databaseRef.removeValue()
    }

    private fun saveDriverData() {
        val databaseRef = FirebaseDatabase.getInstance().getReference("drivers/$driverId")
        val driverData = mapOf(
            "name" to driverName,
            "car_name" to carName,
            "phone_number" to phoneNo,
            "car_number" to carNo,
            "status" to "on",
            "latitude" to 0.0,
            "longitude" to 0.0
        )
        databaseRef.setValue(driverData)
    }

    private fun startRideRequestListener() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isOnDuty) {
                    checkForRideRequests()
                    handler.postDelayed(this, 10000)
                }
            }
        }, 10000)
    }

    private fun stopRideRequestListener() {
        handler.removeCallbacksAndMessages(null)
    }
    private fun updateButtonColor() {
        if (isOnDuty) {
            toggleDutyButton.setBackgroundColor(Color.parseColor("#F44336")) // Green
        } else {
            toggleDutyButton.setBackgroundColor(Color.parseColor("#4CAF50")) // Red
        }
    }

    private fun checkForRideRequests() {
        if (!isOnDuty) return  // ✅ Stop checking if driver is off duty

        database.orderByChild("status").equalTo("pending").limitToFirst(1)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (rideSnapshot in snapshot.children) {
                        val rideRequest = rideSnapshot.getValue<RideRequest>()
                        rideRequest?.let {
                            showRideNotification(it, rideSnapshot.key!!)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Firebase", "Database error: ${error.message}")
                }
            })
    }
    private fun showRideNotification(rideRequest: RideRequest, requestId: String) {
        val channelId = "ride_request_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 1 // Unique ID for each notification

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Ride Requests", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
        }

        // ✅ Accept Ride Intent
        val acceptIntent = Intent(this, RideActionReceiver::class.java).apply {
            action = "ACCEPT_RIDE"
            putExtra("REQUEST_ID", requestId)
            putExtra("NOTIFICATION_ID", notificationId)  // Send Notification ID to dismiss it
            putExtra("DRIVER_ID", driverId)
            putExtra("DRIVER_NAME", driverName)
            putExtra("CAR_NO", carNo)
        }
        val acceptPendingIntent = PendingIntent.getBroadcast(this, 0, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        // ❌ Deny Ride Intent
        val denyIntent = Intent(this, RideActionReceiver::class.java).apply {
            action = "DENY_RIDE"
            putExtra("REQUEST_ID", requestId)
            putExtra("NOTIFICATION_ID", notificationId)  // Send Notification ID to dismiss it
        }
        val denyPendingIntent = PendingIntent.getBroadcast(this, 1, denyIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)

        // 🔔 Build Notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("New Ride Request")
            .setContentText("Pickup: ${rideRequest.pickupAddress}, Drop: ${rideRequest.dropAddress}, Price: ${rideRequest.price}")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_menu_compass, "Accept", acceptPendingIntent)
            .addAction(android.R.drawable.ic_delete, "Deny", denyPendingIntent)
            .build()

        // 🛑 Show Notification
        notificationManager.notify(notificationId, notification)

        // ⏳ Auto-dismiss after 25 seconds *ONLY IF NOT ALREADY DISMISSED*
        Handler(Looper.getMainLooper()).postDelayed({
            notificationManager.cancel(notificationId)
            Log.d("Notification", "⏳ Auto-dismissed notification (ID: $notificationId)")
        }, 25000)
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) startLocationUpdates() else Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
    }
}


