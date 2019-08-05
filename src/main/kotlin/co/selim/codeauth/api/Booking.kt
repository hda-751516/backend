package co.selim.codeauth.api

data class Booking(
    val start: String,
    val destination: String,
    val pickUpTime: String,
    val dropOffTime: String,
    val bookingCode: Int
)