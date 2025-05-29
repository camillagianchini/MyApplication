// File: src/main/java/it/progmob/myconcerts/screens/AddConcertScreen.kt
package it.progmob.myconcerts.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
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

    val dateTimeFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    val snackbarHostState = remember { SnackbarHostState() }

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

    fun updateViewModelWithDateTime() {
        if (selectedDateCalendar != null && selectedTimeHour != null && selectedTimeMinute != null) {
            val combinedCalendar = Calendar.getInstance().apply {
                timeInMillis = selectedDateCalendar!!.timeInMillis
                set(Calendar.HOUR_OF_DAY, selectedTimeHour!!)
                set(Calendar.MINUTE, selectedTimeMinute!!)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            viewModel.onEvent(AddConcertEvent.DateChanged(Timestamp(combinedCalendar.time)))
        } else if (selectedDateCalendar != null && (selectedTimeHour == null || selectedTimeMinute == null)) {

            val dateOnlyCalendar = Calendar.getInstance().apply{
                timeInMillis = selectedDateCalendar!!.timeInMillis
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
            updateViewModelWithDateTime()
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
            updateViewModelWithDateTime()
        },
        selectedTimeHour ?: calendar.get(Calendar.HOUR_OF_DAY),
        selectedTimeMinute ?: calendar.get(Calendar.MINUTE),
        true
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
                "Insert concert details",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = uiState.artist,
                onValueChange = { viewModel.onEvent(AddConcertEvent.ArtistChanged(it)) },
                label = { Text("Name artist *") },
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
                label = { Text("Location *") },
                // ... rest of location TextField
                isError = uiState.locationError != null,
                supportingText = {
                    if (uiState.locationError != null) {
                        Text(text = uiState.locationError!!, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            uiState.dateTimestamp?.let { timestamp ->
                val formattedDateTime = remember(timestamp) {
                    dateTimeFormatter.format(timestamp.toDate())
                }

                Text(
                    text = "Date and time: $formattedDateTime",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Button(onClick = { datePickerDialog.show() }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.DateRange, contentDescription = "Select date", modifier = Modifier.padding(end = 4.dp))
                    Text("Date")
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { timePickerDialog.show() }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Schedule, contentDescription = "Select time", modifier = Modifier.padding(end = 4.dp))
                    Text("Time")
                }
            }



            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.onEvent(AddConcertEvent.SaveConcertClicked) },
                enabled = !uiState.isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save concert")
                }
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=360dp,height=640dp,dpi=480")
@Composable
fun AddConcertScreenPreviewWithViewModel() {
    MyApplicationTheme {
        AddConcertScreen(navController = rememberNavController())
    }
}