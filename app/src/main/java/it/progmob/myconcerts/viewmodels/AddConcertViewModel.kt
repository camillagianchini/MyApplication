package it.progmob.myconcerts.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.progmob.myconcerts.Concert
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow // Importa questo
import kotlinx.coroutines.flow.StateFlow       // Importa questo
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow     // Importa questo
import kotlinx.coroutines.flow.update          // Importa questo (opzionale ma utile)
import kotlinx.coroutines.launch

// ... (AddConcertUiState, AddConcertEvent, AddConcertEffect rimangono invariati) ...
data class AddConcertUiState(
    val artist: String = "",
    val location: String = "",
    val dateTimestamp: Timestamp? = null,
    val artistError: String? = null,
    val locationError: String? = null,
    val dateError: String? = null,
    val isLoading: Boolean = false
)

sealed class AddConcertEvent {
    data class ArtistChanged(val artist: String) : AddConcertEvent()
    data class LocationChanged(val location: String) : AddConcertEvent()
    data class DateChanged(val date: Timestamp?) : AddConcertEvent()
    object SaveConcertClicked : AddConcertEvent()
}

sealed class AddConcertEffect {
    data class ShowSnackbar(val message: String) : AddConcertEffect()
    object NavigateBack : AddConcertEffect()
}


class AddConcertViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    // 1. Definisci un MutableStateFlow privato
    private val _uiState = MutableStateFlow(AddConcertUiState())
    // 2. Esponilo come StateFlow pubblico e immutabile
    val uiState: StateFlow<AddConcertUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<AddConcertEffect>()
    val effect = _effect.asSharedFlow()

    fun onEvent(event: AddConcertEvent) {
        when (event) {
            is AddConcertEvent.ArtistChanged -> {
                // 3. Usa .update { currentState -> newState } per aggiornare il MutableStateFlow
                _uiState.update { currentState ->
                    currentState.copy(artist = event.artist, artistError = null)
                }
            }
            is AddConcertEvent.LocationChanged -> {
                _uiState.update { currentState ->
                    currentState.copy(location = event.location, locationError = null)
                }
            }
            is AddConcertEvent.DateChanged -> {
                _uiState.update { currentState ->
                    currentState.copy(dateTimestamp = event.date, dateError = null)
                }
            }
            AddConcertEvent.SaveConcertClicked -> {
                saveConcert()
            }
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true
        var currentArtistError: String? = null
        var currentLocationError: String? = null
        var currentDateError: String? = null

        // Accedi ai valori tramite _uiState.value
        if (_uiState.value.artist.isBlank()) {
            isValid = false
            currentArtistError = "Il nome dell'artista è obbligatorio"
        }
        if (_uiState.value.location.isBlank()) {
            isValid = false
            currentLocationError = "Il luogo è obbligatorio"
        }
        if (_uiState.value.dateTimestamp == null) {
            isValid = false
            currentDateError = "La data è obbligatoria"
        }

        _uiState.update { currentState ->
            currentState.copy(
                artistError = currentArtistError,
                locationError = currentLocationError,
                dateError = currentDateError
            )
        }
        return isValid
    }

    private fun saveConcert() {
        if (!validateFields()) {
            viewModelScope.launch {
                _effect.emit(AddConcertEffect.ShowSnackbar("Per favore, correggi i campi evidenziati."))
            }
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w("AddConcertViewModel", "Utente non autenticato. Impossibile salvare il concerto.")
            viewModelScope.launch {
                _effect.emit(AddConcertEffect.ShowSnackbar("Errore: Utente non autenticato."))
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        val newConcert = Concert(
            artist = _uiState.value.artist.trim(),
            location = _uiState.value.location.trim(),
            date = _uiState.value.dateTimestamp!!,
            userId = currentUser.uid
        )

        firestore.collection("concerts")
            .add(newConcert)
            .addOnSuccessListener { documentReference ->
                Log.d("AddConcertViewModel", "Concerto salvato con ID: ${documentReference.id}")
                _uiState.update { it.copy(isLoading = false) }
                viewModelScope.launch {
                    _effect.emit(AddConcertEffect.ShowSnackbar("Concerto salvato con successo!"))
                    _effect.emit(AddConcertEffect.NavigateBack)
                }
            }
            .addOnFailureListener { e ->
                Log.w("AddConcertViewModel", "Errore nel salvataggio del concerto", e)
                _uiState.update { it.copy(isLoading = false) }
                viewModelScope.launch {
                    _effect.emit(AddConcertEffect.ShowSnackbar("Errore nel salvataggio: ${e.message}"))
                }
            }
    }
}