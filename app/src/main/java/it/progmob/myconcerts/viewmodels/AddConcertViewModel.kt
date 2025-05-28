import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.progmob.myconcerts.Concert
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AddConcertUiState(
    val artist: String = "",
    val location: String = "",
    val dateTimestamp: Timestamp? = null, // This will now hold combined date and time
    val artistError: String? = null,
    val locationError: String? = null,
    val dateError: String? = null, // Error for the combined date/time
    val isLoading: Boolean = false
)

sealed class AddConcertEvent {
    data class ArtistChanged(val artist: String) : AddConcertEvent()
    data class LocationChanged(val location: String) : AddConcertEvent()
    data class DateChanged(val date: Timestamp?) :
        AddConcertEvent() // Receives the combined Timestamp

    object SaveConcertClicked : AddConcertEvent()
}

sealed class AddConcertEffect { /* ... remains the same ... */
    data class ShowSnackbar(val message: String) : AddConcertEffect()
    object NavigateBack : AddConcertEffect()
}

class AddConcertViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(AddConcertUiState())
    val uiState: StateFlow<AddConcertUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<AddConcertEffect>()
    val effect = _effect.asSharedFlow()

    fun onEvent(event: AddConcertEvent) {
        when (event) {
            is AddConcertEvent.ArtistChanged -> {
                _uiState.update { it.copy(artist = event.artist, artistError = null) }
            }

            is AddConcertEvent.LocationChanged -> {
                _uiState.update { it.copy(location = event.location, locationError = null) }
            }

            is AddConcertEvent.DateChanged -> {
                // The incoming 'event.date' Timestamp should now have the correct time set by the UI
                _uiState.update { it.copy(dateTimestamp = event.date, dateError = null) }
            }

            AddConcertEvent.SaveConcertClicked -> {
                saveConcert()
            }
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true
        // The validation for dateTimestamp now implicitly checks if a valid date AND time have been combined
        // into a non-null Timestamp. You might add more specific checks if needed (e.g., time is not 00:00 if that's invalid).
        val currentArtistError =
            if (_uiState.value.artist.isBlank()) "Il nome dell'artista è obbligatorio" else null
        val currentLocationError =
            if (_uiState.value.location.isBlank()) "Il luogo è obbligatorio" else null
        val currentDateError =
            if (_uiState.value.dateTimestamp == null) "Data e ora sono obbligatorie" else null

        if (currentArtistError != null) isValid = false
        if (currentLocationError != null) isValid = false
        if (currentDateError != null) isValid = false

        _uiState.update {
            it.copy(
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
            // ... (user not authenticated error handling remains the same) ...
            viewModelScope.launch {
                _effect.emit(AddConcertEffect.ShowSnackbar("Errore: Utente non autenticato."))
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        // The dateTimestamp already contains the correct time
        val newConcert = Concert(
            artist = _uiState.value.artist.trim(),
            location = _uiState.value.location.trim(),
            date = _uiState.value.dateTimestamp!!, // This now has date and time
            userId = currentUser.uid
        )

        firestore.collection("concerts")
            .add(newConcert)
            .addOnSuccessListener { documentReference ->
                // ... (success handling remains the same) ...
                _uiState.update { it.copy(isLoading = false) }
                viewModelScope.launch {
                    _effect.emit(AddConcertEffect.ShowSnackbar("Concerto salvato con successo!"))
                    _effect.emit(AddConcertEffect.NavigateBack)
                }
            }
            .addOnFailureListener { e ->
                // ... (failure handling remains the same) ...
                _uiState.update { it.copy(isLoading = false) }
                viewModelScope.launch {
                    _effect.emit(AddConcertEffect.ShowSnackbar("Errore nel salvataggio: ${e.message}"))
                }
            }
    }
}