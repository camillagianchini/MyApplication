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
import com.example.myapplication.R // Make sure this matches your R file import
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class WelcomeActivity : AppCompatActivity() {

    // Register for the FirebaseUI sign-in activity result
    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { res ->
        this.onSignInResult(res)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_welcome)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Start the sign-in flow
        launchSignInFlow()
    }

    private fun launchSignInFlow() {
        // Choose authentication providers
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build()
            // Add other providers here, e.g.,
            // AuthUI.IdpConfig.GoogleBuilder().build(),
            // AuthUI.IdpConfig.PhoneBuilder().build()
        )

        // Create and launch sign-in intent
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            // Optionally, set a theme
            // .setTheme(R.style.YourAppTheme_FirebaseAuth) // Create this style in your themes.xml
            // Optionally, set a logo
            // .setLogo(R.drawable.your_app_logo)
            .build()
        signInLauncher.launch(signInIntent)
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            // You can now navigate to your MainActivity or perform other actions
            navigateToMainActivity(user)
        } else {
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error.
            if (response == null) {
                // User pressed back button
                Toast.makeText(this, "Sign in cancelled", Toast.LENGTH_SHORT).show()
                // You might want to finish the WelcomeActivity or allow another attempt
                // finish()
            } else if (response.error != null) {
                Toast.makeText(this, "Sign in failed: ${response.error?.message}", Toast.LENGTH_LONG).show()
                // Log the error for debugging:
                // Log.e("WelcomeActivity", "Sign-in error: ", response.error)
            }
        }
    }

    private fun navigateToMainActivity(user: FirebaseUser?) {
        // Example: Start MainActivity after successful login
        // Make sure you have a MainActivity created in your project
        val intent = Intent(this, MainActivity::class.java)
        // You can pass user information to MainActivity if needed,
        // but often it's better to observe the auth state in MainActivity directly.
        // For simplicity, we'll just start it.
        startActivity(intent)
        finish() // Finish WelcomeActivity so the user can't go back to it without signing out
    }
}