package com.moneygoat.app.ui

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.moneygoat.app.R
import com.moneygoat.app.viewmodel.LoginViewModel

/**
 * Activity for registering new users.
 * Performs basic validation on input fields before attempting database insertion.
 */
class RegisterActivity : AppCompatActivity() {
    private val TAG = "MoneyGoat_RegisterUI"
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "RegisterActivity created")
        setContentView(R.layout.activity_register)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        
        // Initialize UI components
        val etUsername = findViewById<EditText>(R.id.etRegUsername)
        val etPassword = findViewById<EditText>(R.id.etRegPassword)
        val etConfirm = findViewById<EditText>(R.id.etRegConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvBack = findViewById<TextView>(R.id.tvBackToLogin)

        // Set registration button listener
        btnRegister.setOnClickListener {
            val email = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirm = etConfirm.text.toString().trim()
            
            // Validate that fields are not empty
            if (email.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Log.w(TAG, "Registration attempt with empty fields")
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Basic email validation
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Validate that passwords match
            if (password != confirm) {
                Log.w(TAG, "Registration failed: Passwords do not match")
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Firebase requires at least 6 characters
            if (password.length < 6) {
                Log.w(TAG, "Registration failed: Password too short")
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            Log.d(TAG, "Initiating registration for: $email")
            viewModel.register(email, password)
        }
        
        // Return to login screen
        tvBack.setOnClickListener {
            Log.d(TAG, "Navigating back to Login")
            finish()
        }
        
        // Observe registration success
        viewModel.registerResult.observe(this) { success ->
            if (success) {
                Log.d(TAG, "Registration successful")
                Toast.makeText(this, "Registration successful! Please log in.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
        
        // Observe errors (e.g., username already exists)
        viewModel.errorMessage.observe(this) {
            Log.e(TAG, "Registration error: $it")
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }
}
