package it.progmob.myconcerts.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import it.progmob.myconcerts.Concert
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.abs

class HomeViewModel(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) : ViewModel() {
    private val _concerts = MutableStateFlow<List<Concert>>(emptyList())
    val concerts: StateFlow<List<Concert>> = _concerts.asStateFlow()

    private var concertsListener: ListenerRegistration? = null

    init {
        loadConcerts()
    }

    private fun loadConcerts() {
        concertsListener?.remove()

        concertsListener = firestore.collection("concerts")
            .orderBy("date") // Ordina per data
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Gestisci l'errore
                    return@addSnapshotListener
                }

                val concertsList = snapshot?.documents?.mapNotNull { document ->
                    document.toObject(Concert::class.java)?.copy(id = document.id)
                } ?: emptyList()

                _concerts.value = concertsList
            }
    }

    override fun onCleared() {
        super.onCleared()
        concertsListener?.remove()
    }
}