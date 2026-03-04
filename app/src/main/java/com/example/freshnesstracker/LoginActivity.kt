package com.example.freshnesstracker

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailEditText = findViewById<TextInputEditText>(R.id.email_edit_text)
        val passwordEditText = findViewById<TextInputEditText>(R.id.password_edit_text)
        val loginButton = findViewById<MaterialButton>(R.id.login_button)
        val forgotPasswordText = findViewById<TextView>(R.id.forgot_password_text)
        val signupText = findViewById<TextView>(R.id.signup_text)

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (validateLogin(email, password)) {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
        }

        signupText.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        forgotPasswordText.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    private fun validateLogin(email: String, pass: String): Boolean {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            return false
        }
        if (pass.length < 6 || !pass.any { it.isDigit() } || !pass.any { it.isLetter() }) {
            Toast.makeText(this, "Password must be at least 6 characters with letters and numbers", Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    private fun showForgotPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Reset Password")
        builder.setMessage("Enter your email address to receive a reset link.")

        val input = TextInputEditText(this)
        input.hint = "Email Address"
        builder.setView(input)

        builder.setPositiveButton("Send") { _, _ ->
            val email = input.text.toString()
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Reset link sent to $email", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}