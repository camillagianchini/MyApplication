package it.progmob.myconcerts

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class WelcomeActivity : AppCompatActivity() {

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
        ::onSignInResult
    )

    override fun onCreate(savedInstanceState: Bundle?) {

        installSplashScreen().setKeepOnScreenCondition { false }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)

        val forTesting = false

        if (forTesting) {
            FirebaseAuth.getInstance().signOut()
        }

        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            // Utente già autenticato → controllo Firestore
            checkUserInFirestore(user)
        } else {
            // Lancia il flusso di accesso (AuthUI)
            Handler(Looper.getMainLooper()).post {
                launchSignInFlow()
            }
        }
    }

    private fun launchSignInFlow() {
        val providers = listOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
        )

        val intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setTheme(R.style.FirebaseAuthTheme)
            .setAvailableProviders(providers)
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
            launchSignInFlow()
        }
    }

    private fun checkUserInFirestore(user: FirebaseUser) {
        val db = Firebase.firestore
        val uid = user.uid
        val userDocRef = db.collection("users").document(uid)

        userDocRef.get().addOnSuccessListener { document ->
            if (!document.exists()) {
                val newUser = hashMapOf(
                    "name" to (user.displayName ?: "unknown"),
                    "email" to user.email,
                    "registration date" to Timestamp.now()
                )
                userDocRef.set(newUser)
                    .addOnSuccessListener {
                        navigateToMainActivity(user)
                    }
                    .addOnFailureListener {
                        navigateToMainActivity(user)
                    }
            } else {
                navigateToMainActivity(user)
            }
        }.addOnFailureListener { e ->
            Log.e("Firestore", "Error Firestore", e)
            Toast.makeText(this, "Error checking user in Firestore", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToMainActivity(user: FirebaseUser?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("user_name", user?.displayName)
        intent.putExtra("user_uid", user?.uid)
        startActivity(intent)
        finish()
    }
}
