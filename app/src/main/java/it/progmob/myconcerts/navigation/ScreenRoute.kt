// File: src/main/java/it/progmob/myconcerts/navigation/ScreenRoute.kt
package it.progmob.myconcerts.navigation

sealed class ScreenRoute(val route: String) {
    object Home : ScreenRoute("home_screen")
    object AddConcert : ScreenRoute("add_concert") {
        fun createRoute(editId: String?) = if (editId != null) {
            "add_concert?editId=$editId"
        } else {
            "add_concert"
        }
    }


    object ConcertDetail : ScreenRoute("concert_detail/{concertId}") {
        fun createRoute(concertId: String) = "concert_detail/$concertId"
    }


// Aggiungi qui altre route man mano che l'app cresce
    // Esempio con argomenti:
    // object ConcertDetails : ScreenRoute("concert_details_screen/{concertId}") {
    //    fun createRoute(concertId: String) = "concert_details_screen/$concertId"
    // }
}