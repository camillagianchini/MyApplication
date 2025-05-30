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
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import it.progmob.myconcerts.Concert
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConcertDetailScreen(navController: NavController, concert: Concert?) {
    val dateFormatter = remember {
        SimpleDateFormat("EEEE dd MMM yyyy HH:mm", Locale.getDefault())
    }

    var remainingTime by remember { mutableStateOf(formatCountdown(concert?.date)) }

    LaunchedEffect(concert?.date) {
        while (true) {
            remainingTime = formatCountdown(concert?.date)
            delay(1000)
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.primaryContainer

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Concert Details",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor
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
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 🎉 Emoji in alto
            Box(
                modifier = Modifier
                    .size(150.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = concert?.emoji ?: "🎵",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 56.sp)
                )
            }

            // Nome artista
            Text(
                text = concert?.artist ?: "Unknown",
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 30.sp),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // Data evento
            Text(
                text = concert?.date?.let { dateFormatter.format(it.toDate()) } ?: "",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            // Countdown formattato
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = remainingTime,
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier
                        .padding(vertical = 20.dp, horizontal = 32.dp)
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
