package it.progmob.myconcerts.screens

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import it.progmob.myconcerts.Concert
import it.progmob.myconcerts.navigation.ScreenRoute
import it.progmob.myconcerts.ui.theme.MyApplicationTheme
import it.progmob.myconcerts.viewmodels.HomeViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.Timestamp
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = viewModel()) {
    val concerts by viewModel.concerts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Concerts") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(ScreenRoute.AddConcert.route) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add concert")
            }
        }
    ) { padding ->
        if (concerts.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No concerts yet",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(concerts) { concert ->
                    ConcertItem(
                        concert = concert,
                        onClick = {
                            navController.navigate(ScreenRoute.ConcertDetail.createRoute(concert.id))
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ConcertItem(concert: Concert, onClick: () -> Unit) {
    val daysRemaining = remember(concert.date) {
        calculateDaysRemaining(concert.date)
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = concert.artist,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = concert.location,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (daysRemaining >= 0) "$daysRemaining days remaining" else "Concert passed",
                style = MaterialTheme.typography.labelLarge,
                color = if (daysRemaining >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

private fun calculateDaysRemaining(date: Timestamp): Long {
    val now = Calendar.getInstance()
    val concertDate = Calendar.getInstance().apply { time = date.toDate() }
    val diff = concertDate.timeInMillis - now.timeInMillis
    return diff / (1000 * 60 * 60 * 24)
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MyApplicationTheme {
        HomeScreen(navController = rememberNavController())
    }
}