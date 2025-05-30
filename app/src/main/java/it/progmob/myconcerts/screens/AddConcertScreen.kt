package it.progmob.myconcerts.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
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

    val snackbarHostState = remember { SnackbarHostState() }

    var selectedDateCalendar by remember { mutableStateOf<Calendar?>(null) }
    var selectedTimeHour by remember { mutableStateOf<Int?>(null) }
    var selectedTimeMinute by remember { mutableStateOf<Int?>(null) }
    val emojiOptions = listOf("🎵", "🎸", "🎤", "🎷", "🎹", "🥁")
    var expanded by remember { mutableStateOf(false) }

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
        topBar = {
            TopAppBar(
                title = { Text("Add a New Concert") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Enter concert details", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = uiState.artist,
                onValueChange = { viewModel.onEvent(AddConcertEvent.ArtistChanged(it)) },
                label = { Text("Artist Name *") },
                singleLine = true,
                isError = uiState.artistError != null,
                supportingText = {
                    uiState.artistError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.location,
                onValueChange = { viewModel.onEvent(AddConcertEvent.LocationChanged(it)) },
                label = { Text("Location *") },
                singleLine = true,
                isError = uiState.locationError != null,
                supportingText = {
                    uiState.locationError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Box {
                OutlinedTextField(
                    value = uiState.emoji,
                    onValueChange = { }, // disabilitato, si seleziona dal menu
                    readOnly = true,
                    label = { Text("Emoji") },
                    isError = uiState.emojiError != null,
                    trailingIcon = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Apri Emoji Menu")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = true }
                )

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    emojiOptions.forEach { emoji ->
                        DropdownMenuItem(
                            text = { Text(emoji) },
                            onClick = {
                                viewModel.onEvent(AddConcertEvent.EmojiChanged(emoji))
                                expanded = false
                            }
                        )
                    }
                }
            }

            if (uiState.emojiError != null) {
                Text(
                    text = uiState.emojiError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                Button(onClick = { datePickerDialog.show() }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.DateRange, contentDescription = "Select Date", modifier = Modifier.padding(end = 4.dp))
                    Text("Date")
                }
                Spacer(Modifier.width(8.dp))
                Button(onClick = { timePickerDialog.show() }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Filled.Schedule, contentDescription = "Select Time", modifier = Modifier.padding(end = 4.dp))
                    Text("Time")
                }
            }

            if (uiState.dateTimestamp != null) {
                val formattedDateTime = remember(uiState.dateTimestamp) {
                    dateTimeFormatter.format(uiState.dateTimestamp!!.toDate())
                }

                Text(
                    text = "Selected: $formattedDateTime",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.onEvent(AddConcertEvent.SaveConcertClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Save Concert")
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
