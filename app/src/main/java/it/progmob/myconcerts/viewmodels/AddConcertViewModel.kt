package it.progmob.myconcerts.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import it.progmob.myconcerts.Concert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

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

sealed class AddConcertEffect {
    data class ShowSnackbar(val message: String) : AddConcertEffect()
    object NavigateBack : AddConcertEffect()
}

class AddConcertViewModel(application: Application) : AndroidViewModel(application) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(AddConcertUiState())
    val uiState: StateFlow<AddConcertUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<AddConcertEffect>()
    val effect = _effect.asSharedFlow()

    private val _venueSuggestions = MutableStateFlow<List<String>>(emptyList())
    val venueSuggestions: StateFlow<List<String>> = _venueSuggestions

    init {
        loadVenuesFromAssets()
    }

    private fun loadVenuesFromAssets() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val assetManager = getApplication<Application>().assets
                val inputStream = assetManager.open("italian_concert_venues.json")
                val reader = BufferedReader(InputStreamReader(inputStream))
                val content = reader.readText()
                reader.close()

                val jsonArray = JSONArray(content)
                val venues = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    venues.add(jsonArray.getString(i))
                }
                _venueSuggestions.value = venues
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onEvent(event: AddConcertEvent) {
        when (event) {
            is AddConcertEvent.ArtistChanged ->
                _uiState.update { it.copy(artist = event.artist, artistError = null) }

            is AddConcertEvent.LocationChanged ->
                _uiState.update { it.copy(location = event.location, locationError = null) }

            is AddConcertEvent.DateChanged ->
                _uiState.update { it.copy(dateTimestamp = event.date, dateError = null) }

            is AddConcertEvent.EmojiChanged -> {
                val emoji = event.emoji
                val codePoints = emoji.codePoints().toArray()

                val emojiString = try {
                    if (codePoints.isNotEmpty()) {
                        String(codePoints, 0, codePoints.size.coerceAtMost(8))

                    } else ""
                } catch (e: Exception) {
                    ""
                }
                android.util.Log.d("EmojiInput", "emoji: '$emojiString' from raw input '${event.emoji}'")

                _uiState.update { it.copy(emoji = emojiString, emojiError = null) }
            }


            AddConcertEvent.SaveConcertClicked -> saveConcert()
        }
    }

    private fun validateFields(): Boolean {
        val artistError = if (_uiState.value.artist.isBlank()) "artist is required" else null
        val locationError = if (_uiState.value.location.isBlank()) "location is required" else null
        val dateError = if (_uiState.value.dateTimestamp == null) "date is required" else null
        val emojiError = if (_uiState.value.emoji.isBlank()) "emoji is required" else null

        val isValid = listOf(artistError, locationError, dateError, emojiError).all { it == null }

        _uiState.update {
            it.copy(
                artistError = artistError,
                locationError = locationError,
                dateError = dateError,
                emojiError = emojiError
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
                _effect.emit(AddConcertEffect.ShowSnackbar("Error: User not authenticated."))
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
            .addOnSuccessListener {
                _uiState.update { it.copy(isLoading = false) }
                viewModelScope.launch {
                    _effect.emit(AddConcertEffect.ShowSnackbar("Concert saved successfully!"))
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

private fun Char.isEmoji(): Boolean {
    val type = Character.getType(this)
    return type == Character.SURROGATE.toInt() || type == Character.OTHER_SYMBOL.toInt()
}
