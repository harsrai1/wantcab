package com.example.wantcab

data class RideRequest(
    val driverId: String = "",
    val driverName: String = "",
    val price: String = "",
    val distance: String = "",
    val pickupAddress: String = "",
    val dropAddress: String = "",
    val cabDriverName: String = "",
    val carNo: String = "",
    val status: String = ""
)
