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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.progmob.myconcerts.navigation.ScreenRoute
import it.progmob.myconcerts.screens.AddConcertScreen
import it.progmob.myconcerts.screens.ConcertDetailScreen
import it.progmob.myconcerts.screens.HomeScreen
import it.progmob.myconcerts.ui.theme.MyApplicationTheme
import it.progmob.myconcerts.viewmodels.HomeViewModel
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
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
fun AppNavigation(homeViewModel: HomeViewModel = viewModel()) {
    val navController = rememberNavController()
    // Osserva lo StateFlow correttamente
    val concerts by homeViewModel.concerts.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = ScreenRoute.Home.route
    ) {
        composable(route = ScreenRoute.Home.route) {
            HomeScreen(navController = navController, viewModel = homeViewModel)
        }
        composable(route = ScreenRoute.AddConcert.route) {
            AddConcertScreen(navController = navController)
        }
        composable(ScreenRoute.ConcertDetail.route) { backStackEntry ->
            val concertId = backStackEntry.arguments?.getString("concertId")
            // Ora usiamo la lista già osservata correttamente
            val concert = concerts.find { it.id == concertId }

            ConcertDetailScreen(
                navController = navController,
                concert = concert
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    MyApplicationTheme {
        AppNavigation()
    }
}