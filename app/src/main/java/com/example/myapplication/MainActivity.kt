package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.myapplication.ui.theme.MyApplicationTheme
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = Firebase.firestore
        val nomeUtente = intent.getStringExtra("user_name") ?: "Anonimo"
        val uid = intent.getStringExtra("user_uid")

        val user = hashMapOf(
            "nome" to nomeUtente,
            "email" to FirebaseAuth.getInstance().currentUser?.email,
            "registrato il" to com.google.firebase.Timestamp.now()
        )

        if (uid != null) {
            db.collection("utenti")
                .document(uid)
                .set(user)
                .addOnSuccessListener {
                    Log.d("Firestore", "Utente $nomeUtente salvato")
                }
                .addOnFailureListener { e ->
                    Log.w("Firestore", "Errore durante il salvataggio", e)
                }
        }

        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = nomeUtente,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}


    @Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}