package it.progmob.myconcerts

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import it.progmob.myconcerts.navigation.ScreenRoute
import it.progmob.myconcerts.screens.AddConcertScreen
import it.progmob.myconcerts.screens.ConcertDetailScreen
import it.progmob.myconcerts.screens.HomeScreen
import it.progmob.myconcerts.ui.theme.MyApplicationTheme // cambia qui se il tema si chiama diversamente
import it.progmob.myconcerts.viewmodels.HomeViewModel

class MainActivity : ComponentActivity() {

    // Launcher per chiedere il permesso
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                println("🔕 Notification permission denied")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Richiesta permesso notifica solo se Android >= 13
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

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
