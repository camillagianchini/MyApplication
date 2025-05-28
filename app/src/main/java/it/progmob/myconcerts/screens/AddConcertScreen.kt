// File: src/main/java/it/progmob/myconcerts/screens/AddConcertScreen.kt
package it.progmob.myconcerts.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog // Import TimePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule // Icon for time
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.Timestamp
import it.progmob.myconcerts.ui.theme.MyApplicationTheme
import it.progmob.myconcerts.viewmodels.AddConcertEffect
import it.progmob.myconcerts.viewmodels.AddConcertEvent
import it.progmob.myconcerts.viewmodels.AddConcertViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddConcertScreen(
    navController: NavController,
    viewModel: AddConcertViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Formatter for displaying date and time
    val dateTimeFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault()) // For the time field if separate

    val snackbarHostState = remember { SnackbarHostState() }

    // Temporary states to hold selected date and time components before combining
    var selectedDateCalendar by remember { mutableStateOf<Calendar?>(null) }
    var selectedTimeHour by remember { mutableStateOf<Int?>(null) }
    var selectedTimeMinute by remember { mutableStateOf<Int?>(null) }


    LaunchedEffect(key1 = Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is AddConcertEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short
                    )
                }
                AddConcertEffect.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    // Function to combine date and time and update ViewModel
    fun updateViewModelWithDateTime() {
        if (selectedDateCalendar != null && selectedTimeHour != null && selectedTimeMinute != null) {
            val combinedCalendar = Calendar.getInstance().apply {
                timeInMillis = selectedDateCalendar!!.timeInMillis // Set date part
                set(Calendar.HOUR_OF_DAY, selectedTimeHour!!)
                set(Calendar.MINUTE, selectedTimeMinute!!)
                set(Calendar.SECOND, 0) // Optional: Reset seconds
                set(Calendar.MILLISECOND, 0) // Optional: Reset milliseconds
            }
            viewModel.onEvent(AddConcertEvent.DateChanged(Timestamp(combinedCalendar.time)))
        } else if (selectedDateCalendar != null && (selectedTimeHour == null || selectedTimeMinute == null)) {
            // If only date is selected, but time was previously selected and now cleared,
            // or if we want to default time if not picked.
            // For now, let's assume we need both, or only date if time is not picked.
            // If only date is picked, the timestamp in ViewModel will have default time (00:00 or current if not reset).
            // This logic can be refined based on desired UX.
            // For simplicity, let's send only the date if time is not yet picked,
            // the ViewModel's Timestamp will reflect this.
            val dateOnlyCalendar = Calendar.getInstance().apply{
                timeInMillis = selectedDateCalendar!!.timeInMillis
                // Optionally reset time to midnight if you want date selection to imply start of day
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            viewModel.onEvent(AddConcertEvent.DateChanged(Timestamp(dateOnlyCalendar.time)))
        }
    }


    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            selectedDateCalendar = Calendar.getInstance().apply {
                set(year, month, dayOfMonth)
            }
            updateViewModelWithDateTime() // Update VM after date is picked
        },
        selectedDateCalendar?.get(Calendar.YEAR) ?: calendar.get(Calendar.YEAR),
        selectedDateCalendar?.get(Calendar.MONTH) ?: calendar.get(Calendar.MONTH),
        selectedDateCalendar?.get(Calendar.DAY_OF_MONTH) ?: calendar.get(Calendar.DAY_OF_MONTH)
    )

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay: Int, minute: Int ->
            selectedTimeHour = hourOfDay
            selectedTimeMinute = minute
            updateViewModelWithDateTime() // Update VM after time is picked
        },
        selectedTimeHour ?: calendar.get(Calendar.HOUR_OF_DAY),
        selectedTimeMinute ?: calendar.get(Calendar.MINUTE),
        true // true for 24-hour format
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { /* ... TopAppBar remains the same ... */ }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Inserisci i dettagli del concerto",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = uiState.artist,
                onValueChange = { viewModel.onEvent(AddConcertEvent.ArtistChanged(it)) },
                label = { Text("Nome Artista *") },
                // ... rest of artist TextField
                isError = uiState.artistError != null,
                supportingText = {
                    if (uiState.artistError != null) {
                        Text(text = uiState.artistError!!, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.location,
                onValueChange = { viewModel.onEvent(AddConcertEvent.LocationChanged(it)) },
                label = { Text("Luogo *") },
                // ... rest of location TextField
                isError = uiState.locationError != null,
                supportingText = {
                    if (uiState.locationError != null) {
                        Text(text = uiState.locationError!!, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Combined Date and Time display or separate fields
            // Option 1: Single field showing combined date and time from ViewModel's Timestamp
// ⬇️ Mostra solo se la data è stata scelta
            uiState.dateTimestamp?.let { timestamp ->
                val formattedDateTime = remember(timestamp) {
                    dateTimeFormatter.format(timestamp.toDate())
                }

                Text(
                    text = "Data Concerto: $formattedDateTime",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }


// ⬇️ Pulsanti per selezionare data e ora
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Button(onClick = { datePickerDialog.show() }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.DateRange, contentDescription = "Seleziona Data", modifier = Modifier.padding(end = 4.dp))
                    Text("Data")
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { timePickerDialog.show() }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Schedule, contentDescription = "Seleziona Ora", modifier = Modifier.padding(end = 4.dp))
                    Text("Ora")
                }
            }



            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.onEvent(AddConcertEvent.SaveConcertClicked) },
                // ... rest of save Button
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Salva Concerto")
                }
            }
        }
    }
}

// ... Preview remains the same or can be updated to test initial date/time values ...
@Preview(showBackground = true, device = "spec:width=360dp,height=640dp,dpi=480")
@Composable
fun AddConcertScreenPreviewWithViewModel() {
    MyApplicationTheme {
        AddConcertScreen(navController = rememberNavController())
    }
}