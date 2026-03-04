package com.example.freshnesstracker

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val nameEditText = findViewById<TextInputEditText>(R.id.name_edit_text)
        val emailEditText = findViewById<TextInputEditText>(R.id.signup_email_edit_text)
        val passwordEditText = findViewById<TextInputEditText>(R.id.signup_password_edit_text)
        val signupButton = findViewById<MaterialButton>(R.id.signup_button)
        val loginText = findViewById<TextView>(R.id.login_text)

        signupButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (validateSignup(name, email, password)) {
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                finish() // Go back to login
            }
        }

        loginText.setOnClickListener {
            finish()
        }
    }

    private fun validateSignup(name: String, email: String, pass: String): Boolean {
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show()
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
            return false
        }
        if (pass.length < 6 || !pass.any { it.isDigit() } || !pass.any { it.isLetter() }) {
            Toast.makeText(this, "Password must be at least 6 characters with letters and numbers", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }
}