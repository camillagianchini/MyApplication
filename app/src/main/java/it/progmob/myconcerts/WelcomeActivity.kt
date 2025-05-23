package it.progmob.myconcerts

import com.example.myapplication.MainActivity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import android.util.Log
import com.google.firebase.auth.UserProfileChangeRequest
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.error
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import it.progmob.myconcerts.R // Make sure this matches your R file import
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class WelcomeActivity : AppCompatActivity() {

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract(),
        ::onSignInResult         // versione “method reference”
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)
        launchSignInFlow()
    }

    private fun launchSignInFlow() {
        val providers = listOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),

        )

        val intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .build()

        signInLauncher.launch(intent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser

            val nameEditText = findViewById<EditText>(R.id.usernameEditText)
            val nomeUtente = nameEditText.text.toString().trim()

            if (nomeUtente.isNotBlank() && user != null) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(nomeUtente)
                    .build()

                user.updateProfile(profileUpdates)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("FirebaseAuth", "Profilo aggiornato con nome: $nomeUtente")
                            navigateToMainActivity(user)
                        }
                    }
            } else {
                navigateToMainActivity(user)
            }


        } else {
            Toast.makeText(this,
                response?.error?.message ?: "Login cancellato",
                Toast.LENGTH_LONG
            ).show()
        }
    }
    private fun navigateToMainActivity(user: FirebaseUser?) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("user_name", user?.displayName)
        intent.putExtra("user_uid", user?.uid)
        startActivity(intent)
        finish()
    }

} // fine WelcomeActivity

