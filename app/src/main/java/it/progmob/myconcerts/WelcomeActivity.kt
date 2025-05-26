package it.progmob.myconcerts

import it.progmob.myconcerts.MainActivity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseAuth

class WelcomeActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val authLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)


        // Simplified for testing - remove email/password fields from XML or use them properly
        findViewById<Button>(R.id.loginButton).setOnClickListener {
            startAuthFlow()
        }
    }

//    private fun startAuthFlow() {
//        val providers = listOf(
//            AuthUI.IdpConfig.EmailBuilder().build(),
//            AuthUI.IdpConfig.GoogleBuilder().build()
//        )
//
//        val intent = AuthUI.getInstance()
//            .createSignInIntentBuilder()
//            .setAvailableProviders(providers)
//            .build()
//
//        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
//        authLauncher.launch(intent)
//    }
private fun startAuthFlow() {
    val providers = listOf(
        AuthUI.IdpConfig.EmailBuilder().build(),  // Login con email
        AuthUI.IdpConfig.GoogleBuilder().build() // Login con Google
    )

    val intent = AuthUI.getInstance()
        .createSignInIntentBuilder()
        .setTheme(R.style.FirebaseAuthTheme) // Applica il nostro stile
        .setAvailableProviders(providers)
        //.setLogo(R.drawable.your_logo) // (Opzionale) Aggiungi un logo
        .setTosAndPrivacyPolicyUrls(
            "https://example.com/terms",
            "https://example.com/privacy"
        )
        .build()

    authLauncher.launch(intent)
}

}