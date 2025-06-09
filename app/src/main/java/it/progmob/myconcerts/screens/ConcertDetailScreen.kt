package it.progmob.myconcerts.screens

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
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

    // 👇 Estrai MaterialTheme prima di usarlo in remember
    val fallbackColor = MaterialTheme.colorScheme.primary

    val backgroundBrush = remember(concert?.colorHex, fallbackColor) {
        try {
            val color = Color(AndroidColor.parseColor(concert?.colorHex ?: "#90CAF9"))
            Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.6f), color.copy(alpha = 0.3f))
            )
        } catch (e: Exception) {
            Brush.verticalGradient(
                colors = listOf(fallbackColor.copy(alpha = 0.6f), fallbackColor.copy(alpha = 0.3f))
            )
        }
    }

    Scaffold(
        modifier = Modifier.background(brush = backgroundBrush),
        containerColor = Color.Transparent,
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
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                    }
                },
                actions = {
                    IconButton(onClick = {
                        concert?.id?.let {
                            navController.navigate("add_concert?editId=$it")
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    IconButton(onClick = {
                        concert?.id?.let { id ->
                            FirebaseFirestore.getInstance()
                                .collection("concerts")
                                .document(id)
                                .delete()
                                .addOnSuccessListener {
                                    navController.popBackStack()
                                }
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }

    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = concert?.emoji ?: "🎵",
                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp)
                )
            }

            Text(
                text = concert?.artist ?: "Unknown",
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 32.sp),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Text(
                text = concert?.date?.let { dateFormatter.format(it.toDate()) } ?: "",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 20.sp),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.15f),
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(
                    text = remainingTime,
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 28.sp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 32.dp)
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

    return String.format("%02d : %02d : %02d : %02d", days, hours, minutes, seconds)
}
