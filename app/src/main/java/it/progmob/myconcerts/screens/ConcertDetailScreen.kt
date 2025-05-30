package it.progmob.myconcerts.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import it.progmob.myconcerts.Concert
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import kotlinx.coroutines.time.delay
import java.text.SimpleDateFormat
import java.util.*

import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConcertDetailScreen(navController: NavController, concert: Concert?) {
    val dateFormatter = remember { SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault()) }
    var remainingTime by remember { mutableStateOf(calculateRemainingTime(concert?.date)) }

    // Aggiorna countdown ogni secondo
    LaunchedEffect(concert?.date) {
        while (true) {
            remainingTime = calculateRemainingTime(concert?.date)
            kotlinx.coroutines.delay(1000)
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Concert Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Nome artista
            Text(
                text = concert?.artist ?: "Unknown Artist",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Location
            Text(
                text = concert?.location ?: "Unknown Location",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Data
            Text(
                text = concert?.date?.let { dateFormatter.format(it.toDate()) } ?: "Unknown Date",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            Text(
                text = remainingTime,
                style = MaterialTheme.typography.bodyLarge,
                color = if (remainingTime == "Concert has passed") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        }
    }
}


private fun calculateRemainingTime(date: Timestamp?): String {
    if (date == null) return "N/A"

    val now = Calendar.getInstance()
    val concertDate = Calendar.getInstance().apply { time = date.toDate() }

    val diff = concertDate.timeInMillis - now.timeInMillis

    if (diff <= 0) return "Concert has passed"

    val seconds = (diff / 1000) % 60
    val minutes = (diff / (1000 * 60)) % 60
    val hours = (diff / (1000 * 60 * 60)) % 24
    val days = diff / (1000 * 60 * 60 * 24)

    return "$days days, $hours hours, $minutes minutes, $seconds seconds"
}