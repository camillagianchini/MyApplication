package it.progmob.myconcerts

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.*

class AddConcertoFragment : Fragment(R.layout.fragment_add_concerto) {

    private lateinit var editTextArtista: EditText
    private lateinit var editTextLuogo: EditText
    private lateinit var textViewData: TextView
    private lateinit var textViewOra: TextView
    private lateinit var btnSalva: Button

    private var dataSelezionata: Calendar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Collega le view
        editTextArtista = view.findViewById(R.id.editTextArtist)
        editTextLuogo = view.findViewById(R.id.editTextLocation)
        textViewData = view.findViewById(R.id.textViewDate)
        textViewOra = view.findViewById(R.id.textViewTime)
        btnSalva = view.findViewById(R.id.btnSave)

        textViewData.setOnClickListener {
            mostraDatePicker()
        }

        textViewOra.setOnClickListener {
            mostraTimePicker()
        }

        btnSalva.setOnClickListener {
            salvaConcerto()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun mostraDatePicker() {
        val oggi = Calendar.getInstance()
        DatePickerDialog(requireContext(),
            { _, year, month, dayOfMonth ->
                if (dataSelezionata == null) dataSelezionata = Calendar.getInstance()
                dataSelezionata?.set(year, month, dayOfMonth)
                textViewData.text = "$dayOfMonth/${month + 1}/$year"
            },
            oggi.get(Calendar.YEAR),
            oggi.get(Calendar.MONTH),
            oggi.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    @SuppressLint("DefaultLocale")
    private fun mostraTimePicker() {
        val oraCorrente = Calendar.getInstance()
        TimePickerDialog(requireContext(),
            { _, hourOfDay, minute ->
                if (dataSelezionata == null) dataSelezionata = Calendar.getInstance()
                dataSelezionata?.set(Calendar.HOUR_OF_DAY, hourOfDay)
                dataSelezionata?.set(Calendar.MINUTE, minute)
                dataSelezionata?.set(Calendar.SECOND, 0)
                textViewOra.text = String.format("%02d:%02d", hourOfDay, minute)
            },
            oraCorrente.get(Calendar.HOUR_OF_DAY),
            oraCorrente.get(Calendar.MINUTE),
            true
        ).show()
    }

    @SuppressLint("SetTextI18n")
    private fun salvaConcerto() {
        val artista = editTextArtista.text.toString().trim()
        val luogo = editTextLuogo.text.toString().trim()
        val calendar = dataSelezionata

        if (artista.isEmpty() || luogo.isEmpty() || calendar == null) {
            Toast.makeText(requireContext(), "Compila tutti i campi", Toast.LENGTH_SHORT).show()
            return
        }

        val dataOra = Timestamp(calendar.time)
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val concerto = Concert(artista, luogo, dataOra, userId)

        Firebase.firestore.collection("concerti")
            .add(concerto)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Concerto salvato!", Toast.LENGTH_SHORT).show()
                // (opzionale) pulisci i campi
                editTextArtista.text.clear()
                editTextLuogo.text.clear()
                textViewData.text = "Seleziona Data"
                textViewOra.text = "Seleziona Ora"
                dataSelezionata = null
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Errore durante il salvataggio", Toast.LENGTH_SHORT).show()
                Log.e("Firestore", "Errore", it)
            }
    }
}
