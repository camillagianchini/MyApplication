package it.progmob.myconcerts.viewmodels

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
    val dateTimestamp: Timestamp? = null,
    val artistError: String? = null,
    val emoji: String = "",
    val locationError: String? = null,
    val dateError: String? = null,
    val emojiError: String? = null,
    val isLoading: Boolean = false
)

sealed class AddConcertEvent {
    data class ArtistChanged(val artist: String) : AddConcertEvent()
    data class LocationChanged(val location: String) : AddConcertEvent()
    data class DateChanged(val date: Timestamp?) : AddConcertEvent()
    data class EmojiChanged(val emoji: String) : AddConcertEvent()

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
                _uiState.update { it.copy(dateTimestamp = event.date, dateError = null) }
            }

            is AddConcertEvent.EmojiChanged -> {
                _uiState.update { it.copy(emoji = event.emoji, emojiError = null) }     // 👈 aggiunto
            }

            AddConcertEvent.SaveConcertClicked -> {
                saveConcert()
            }

            else -> {

            }
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true
        val currentArtistError =
            if (_uiState.value.artist.isBlank()) "artist is required" else null
        val currentLocationError =
            if (_uiState.value.location.isBlank()) "location is required" else null
        val currentDateError =
            if (_uiState.value.dateTimestamp == null) "date is required" else null
        val currentEmojiError =
            if (_uiState.value.emoji.isBlank()) "emoji is required" else null

        if (currentArtistError != null) isValid = false
        if (currentLocationError != null) isValid = false
        if (currentDateError != null) isValid = false
        if (currentEmojiError != null) isValid = false

        _uiState.update {
            it.copy(
                artistError = currentArtistError,
                locationError = currentLocationError,
                dateError = currentDateError,
                emojiError = currentEmojiError
            )
        }
        return isValid
    }

    private fun saveConcert() {
        if (!validateFields()) {
            viewModelScope.launch {
                _effect.emit(AddConcertEffect.ShowSnackbar("Please correct the highlighted fields."))
            }
            return
        }

        val currentUser = auth.currentUser
        if (currentUser == null) {
            viewModelScope.launch {
                _effect.emit(AddConcertEffect.ShowSnackbar( "Error: User not authenticated."))
            }
            return
        }

        _uiState.update { it.copy(isLoading = true) }

        val newConcert = Concert(
            artist = _uiState.value.artist.trim(),
            location = _uiState.value.location.trim(),
            date = _uiState.value.dateTimestamp!!,
            emoji = _uiState.value.emoji,
            userId = currentUser.uid
        )

        firestore.collection("concerts")
            .add(newConcert)
            .addOnSuccessListener { documentReference ->
                val concertId = documentReference.id
                _uiState.update { it.copy(isLoading = false) }
                viewModelScope.launch {
                    _effect.emit(AddConcertEffect.ShowSnackbar( "Concert saved successfully!"))
                    _effect.emit(AddConcertEffect.NavigateBack)
                }
            }
            .addOnFailureListener { e ->
                _uiState.update { it.copy(isLoading = false) }
                viewModelScope.launch {
                    _effect.emit(AddConcertEffect.ShowSnackbar("Saving error: ${e.message}"))
                }
            }
    }
}