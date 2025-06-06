package it.progmob.myconcerts

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class WelcomeActivity : ComponentActivity() {

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
        ::onSignInResult
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val forTesting = false

        if (forTesting) {
            FirebaseAuth.getInstance().signOut()
        }

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            checkUserInFirestore(currentUser)
        } else {
            launchSignInFlow()
        }
    }

    private fun launchSignInFlow() {
        val providers = listOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )

        val intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setTheme(R.style.FirebaseAuthPickerTheme) // ✅ Applica il tuo tema principale
            .setTosAndPrivacyPolicyUrls( // facoltativo ma raccomandato da Firebase
                "https://yourapp.com/terms",
                "https://yourapp.com/privacy"
            )
            .build()

        signInLauncher.launch(intent)
    }


    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                checkUserInFirestore(user)
            }
        } else {
            Toast.makeText(this, "Login fallito. Riprova.", Toast.LENGTH_SHORT).show()
            launchSignInFlow()
        }
    }

    private fun checkUserInFirestore(user: FirebaseUser) {
        val db = Firebase.firestore
        val uid = user.uid
        val userRef = db.collection("users").document(uid)

        userRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val newUser = hashMapOf(
                    "name" to (user.displayName ?: "unknown"),
                    "email" to user.email,
                    "registrationDate" to Timestamp.now()
                )
                userRef.set(newUser)
                    .addOnSuccessListener { navigateToMainActivity() }
                    .addOnFailureListener { navigateToMainActivity() }
            } else {
                navigateToMainActivity()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Errore durante il login", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
