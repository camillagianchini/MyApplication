package it.progmob.myconcerts

import com.google.firebase.Timestamp

data class Concert(
    val artist: String = "",
    val location: String = "",
    val date: Timestamp = Timestamp.now(),
    val userId: String = ""
)
