package it.progmob.myconcerts

import com.example.myapplication.MainActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
            // AuthUI.IdpConfig.GoogleBuilder().build() // se l’hai abilitato
        )

        val intent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(false)
            .build()

        signInLauncher.launch(intent)
    }

    private fun onSignInResult(res: FirebaseAuthUIAuthenticationResult) {
        if (res.resultCode == RESULT_OK) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()               // non torna al welcome con “back”
        } else {
            Toast.makeText(this,
                res.idpResponse?.error?.message ?: "Login cancellato",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
