// File: src/main/java/it/progmob/myconcerts/screens/AddConcertScreen.kt
package it.progmob.myconcerts.screens

import android.app.DatePickerDialog
// Rimuovi: import android.util.Log // Il logging principale sarà nel ViewModel
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    viewModel: AddConcertViewModel = viewModel() // Inietta il ViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle() // Osserva lo UiState
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    val snackbarHostState = remember { SnackbarHostState() }
    // val scope = rememberCoroutineScope() // Non più necessario qui per lo snackbar principale

    // Gestisci gli effetti collaterali (Snackbar, Navigazione)
    LaunchedEffect(key1 = Unit) { // key1 = Unit per eseguirlo una sola volta o quando cambia la composizione
        viewModel.effect.collect { effect ->
            when (effect) {
                is AddConcertEffect.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = effect.message,
                        duration = SnackbarDuration.Short // o Long a seconda del messaggio
                    )
                }
                AddConcertEffect.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val selectedCalendar = Calendar.getInstance()
            selectedCalendar.set(year, month, dayOfMonth)
            viewModel.onEvent(AddConcertEvent.DateChanged(Timestamp(selectedCalendar.time)))
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Aggiungi Concerto") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Indietro"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
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
            Text(
                "Inserisci i dettagli del concerto",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            OutlinedTextField(
                value = uiState.artist,
                onValueChange = { viewModel.onEvent(AddConcertEvent.ArtistChanged(it)) },
                label = { Text("Nome Artista *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.artistError != null,
                supportingText = {
                    if (uiState.artistError != null) {
                        Text(text = uiState.artistError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            OutlinedTextField(
                value = uiState.location,
                onValueChange = { viewModel.onEvent(AddConcertEvent.LocationChanged(it)) },
                label = { Text("Luogo *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.locationError != null,
                supportingText = {
                    if (uiState.locationError != null) {
                        Text(text = uiState.locationError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            OutlinedTextField(
                value = uiState.dateTimestamp?.toDate()?.let { dateFormatter.format(it) } ?: "",
                onValueChange = { /* Non modificabile direttamente */ },
                label = { Text("Data *") },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { datePickerDialog.show() },
                trailingIcon = {
                    Icon(
                        Icons.Filled.DateRange,
                        contentDescription = "Seleziona Data",
                        Modifier.clickable { datePickerDialog.show() }
                    )
                },
                isError = uiState.dateError != null,
                supportingText = {
                    if (uiState.dateError != null) {
                        Text(text = uiState.dateError!!, color = MaterialTheme.colorScheme.error)
                    }
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.onEvent(AddConcertEvent.SaveConcertClicked) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !uiState.isLoading // Disabilita il pulsante durante il caricamento
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

@Preview(showBackground = true, device = "spec:width=360dp,height=640dp,dpi=480")
@Composable
fun AddConcertScreenPreviewWithViewModel() {
    MyApplicationTheme {
        // Per il preview, il ViewModel non sarà "reale" a meno di non usare librerie di mocking
        // o un ViewModel fittizio. Qui verrà usato il ViewModel di default.
        AddConcertScreen(navController = rememberNavController())
    }
}