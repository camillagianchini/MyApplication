package it.progmob.myconcerts

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class AddConcertFragment : Fragment() {

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        return ComposeView(requireContext()).apply {
            setContent {
                AddConcertScreen()
            }
        }
    }
}

@Composable
fun AddConcertScreen(onSaved: () -> Unit = {}) {
    val context = LocalContext.current
    val firestore = Firebase.firestore
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val calendar = remember { Calendar.getInstance() }

    var artista by remember { mutableStateOf("") }
    var luogo by remember { mutableStateOf("") }
    var dataSelezionata by remember { mutableStateOf("Seleziona Data") }
    var oraSelezionata by remember { mutableStateOf("Seleziona Ora") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = artista,
            onValueChange = { artista = it },
            label = { Text("Artista") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = luogo,
            onValueChange = { luogo = it },
            label = { Text("Luogo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = {
            val oggi = Calendar.getInstance()
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    dataSelezionata = "$dayOfMonth/${month + 1}/$year"
                },
                oggi.get(Calendar.YEAR),
                oggi.get(Calendar.MONTH),
                oggi.get(Calendar.DAY_OF_MONTH)
            ).show()
        }, modifier = Modifier.fillMaxWidth()) {
            Text(dataSelezionata)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = {
            val oraCorrente = Calendar.getInstance()
            TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    oraSelezionata = String.format("%02d:%02d", hourOfDay, minute)
                },
                oraCorrente.get(Calendar.HOUR_OF_DAY),
                oraCorrente.get(Calendar.MINUTE),
                true
            ).show()
        }, modifier = Modifier.fillMaxWidth()) {
            Text(oraSelezionata)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = {
            if (artista.isBlank() || luogo.isBlank() ||
                dataSelezionata == "Seleziona Data" || oraSelezionata == "Seleziona Ora"
            ) {
                Toast.makeText(context, "Compila tutti i campi", Toast.LENGTH_SHORT).show()
                return@Button
            }

            val concerto = Concert(
                artist = artista,
                place = luogo,
                timeanddate = Timestamp(calendar.time),
                userId = userId
            )

            firestore.collection("concerti")
                .add(concerto)
                .addOnSuccessListener {
                    Toast.makeText(context, "Concerto salvato!", Toast.LENGTH_SHORT).show()
                    onSaved()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Errore durante il salvataggio", Toast.LENGTH_SHORT).show()
                }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Salva Concerto")
        }
    }
}
