package com.example.tcgtrader

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Get references to UI elements
        val emailField = findViewById<EditText>(R.id.email)
        val passwordField = findViewById<EditText>(R.id.password)
        val signupButton = findViewById<Button>(R.id.signup_button)

        // Handle Signup Button Click
        signupButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                // Invalid email
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            } else if (password.isEmpty() || password.length < 6) {
                // Invalid password
                Toast.makeText(this, "Please enter a password with at least 6 characters", Toast.LENGTH_SHORT).show()
            } else {
                createAccount(email, password)
            }
        }
    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Signup successful
                    Toast.makeText(this, "Signup Successful!", Toast.LENGTH_SHORT).show()
                    finish() // Close the activity and return to Login
                } else {
                    // Signup failed
                    Toast.makeText(this, "Signup Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
