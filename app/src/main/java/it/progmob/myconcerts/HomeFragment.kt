package it.progmob.myconcerts

// File: fragments/HomeFragment.kt
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController

class HomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                // Qui chiami la tua composable function
                HomeScreen(navController = findNavController())
            }
        }
    }
}

@Composable
fun HomeScreen(navController: NavController) {
    // Tutto il codice Compose che ti ho mostrato prima
    val concerts = remember { getSampleConcerts() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("addConcert") }
            ) {
                Icon(Icons.Filled.Add, "Add concert")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            ConcertList(concerts = concerts)
        }
    }
}