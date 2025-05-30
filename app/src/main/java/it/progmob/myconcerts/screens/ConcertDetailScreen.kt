package it.progmob.myconcerts.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
    val dateFormatter = remember { SimpleDateFormat("EEEE dd MMM yyyy HH:mm", Locale.getDefault()) }
    var remainingTime by remember { mutableStateOf(formatCountdown(concert?.date)) }

    LaunchedEffect(concert?.date) {
        while (true) {
            remainingTime = formatCountdown(concert?.date)
            kotlinx.coroutines.delay(1000)
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.primaryContainer

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = { Text("Concert Details", color = MaterialTheme.colorScheme.onPrimaryContainer) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🎉 Emoji in alto
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = concert?.emoji ?: "🎵",
                    style = MaterialTheme.typography.displayLarge
                )
            }


            // Nome artista
            Text(
                text = concert?.artist ?: "Unknown",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Data evento
            Text(
                text = concert?.date?.let { dateFormatter.format(it.toDate()) } ?: "",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Countdown formattato
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = formatCountdown(concert?.date),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(vertical = 12.dp, horizontal = 24.dp)
                )
            }
        }
    }
}


@SuppressLint("DefaultLocale")
private fun formatCountdown(date: Timestamp?): String {
    if (date == null) return "-- : -- : -- : --"

    val now = Calendar.getInstance()
    val concertDate = Calendar.getInstance().apply { time = date.toDate() }

    val diff = concertDate.timeInMillis - now.timeInMillis
    if (diff <= 0) return "00 : 00 : 00 : 00"

    val seconds = (diff / 1000) % 60
    val minutes = (diff / (1000 * 60)) % 60
    val hours = (diff / (1000 * 60 * 60)) % 24
    val days = diff / (1000 * 60 * 60 * 24)

    return String.format(
        "%02d : %02d : %02d : %02d",
        days, hours, minutes, seconds
    )
}

