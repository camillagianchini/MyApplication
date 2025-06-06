package it.progmob.myconcerts

import com.google.firebase.Timestamp

data class Concert(
    val emoji: String = "",
    val id: String = "",
    val artist: String = "",
    val location: String = "",
    val date: Timestamp = Timestamp.now(),
    val userId: String = "",
    val colorHex: String = ""
)
