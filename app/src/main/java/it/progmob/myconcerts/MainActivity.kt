package it.progmob.myconcerts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.progmob.myconcerts.screens.AddConcertScreen // Importa le tue schermate
import it.progmob.myconcerts.screens.HomeScreen      // Importa le tue schermate
import it.progmob.myconcerts.ui.theme.MyApplicationTheme
import it.progmob.myconcerts.navigation.ScreenRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme { // Applica il tuo tema Compose
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController() // Crea il NavController

    NavHost(
        navController = navController,
        startDestination = ScreenRoute.Home.route // Usa la route dalla tua sealed class
    ) {
        composable(route = ScreenRoute.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(route = ScreenRoute.AddConcert.route) {
            AddConcertScreen(navController = navController)
        }
        // Aggiungi altre destinazioni (composable) qui
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        AppNavigation()
    }
}