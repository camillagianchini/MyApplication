package it.progmob.myconcerts.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
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
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        concertsListener = firestore.collection("concerts")
            .whereEqualTo("userId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                val now = Calendar.getInstance().timeInMillis
                val concertsList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Concert::class.java)?.copy(id = doc.id)
                }?.sortedBy { concert ->
                    if (concert.date.toDate().time > now) {
                        concert.date.toDate().time
                    } else {
                        Long.MAX_VALUE - concert.date.toDate().time
                    }
                } ?: emptyList()

                _concerts.value = concertsList
            }
    }


    override fun onCleared() {
        super.onCleared()
        concertsListener?.remove()
    }
}