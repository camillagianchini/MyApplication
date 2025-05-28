package it.progmob.myconcerts.screens


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController // Per il preview
import it.progmob.myconcerts.navigation.ScreenRoute
import it.progmob.myconcerts.ui.theme.MyApplicationTheme // Assicurati che il nome del tema sia corretto


@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Benvenuto a MyConcerts!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = {
            // Naviga alla route definita per AddConcertScreen
            navController.navigate(ScreenRoute.AddConcert.route)
        }) {
            Text("Aggiungi un nuovo Concerto")
        }
        // Qui potresti aggiungere una lista dei concerti esistenti, ecc.
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MyApplicationTheme {
        // Passa un NavController fittizio per l'anteprima, non eseguirà la navigazione reale.
        HomeScreen(navController = rememberNavController())
    }
}