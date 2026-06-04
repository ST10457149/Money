package com.moneygoat.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.moneygoat.app.R
import com.moneygoat.app.viewmodel.LoginViewModel
import java.util.concurrent.Executor

/**
 * Entry activity for the application.
 * Handles user login and checks for an existing session to auto-login.
 */
class LoginActivity : AppCompatActivity() {
    private val TAG = "MoneyGoat_LoginUI"
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "LoginActivity created")
        
        setContentView(R.layout.activity_login)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnBiometric = findViewById<Button>(R.id.btnBiometricLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        // Check for existing user session and show biometric option if available
        val prefs = getSharedPreferences("moneygoat_prefs", Context.MODE_PRIVATE)
        val savedUserId = prefs.getLong("user_id", -1)
        val savedUsername = prefs.getString("username", "") ?: ""

        if (savedUserId != -1L) {
            btnBiometric.visibility = android.view.View.VISIBLE
            btnBiometric.text = "Fingerprint Login ($savedUsername)"
            btnBiometric.setOnClickListener {
                showBiometricPrompt(savedUserId, savedUsername)
            }
        }

        // Handle login button click
        btnLogin.setOnClickListener {
            val email = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Log.w(TAG, "Login attempt with empty fields")
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.login(email, password)
        }
        
        // Navigate to registration screen
        tvRegister.setOnClickListener {
            Log.d(TAG, "Navigating to Registration")
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        
        // Observe login result from ViewModel
        viewModel.loginResult.observe(this) { user ->
            if (user != null) {
                Log.d(TAG, "Login successful, saving session for ${user.username}")
                // Save user session
                prefs.edit().putLong("user_id", user.id).putString("username", user.username).apply()
                navigateToMain(user.id, user.username)
            }
        }
        
        // Observe error messages (e.g., invalid credentials)
        viewModel.errorMessage.observe(this) {
            Log.e(TAG, "Login error: $it")
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shows the biometric prompt for quick login.
     */
    private fun showBiometricPrompt(userId: Long, username: String) {
        val executor = ContextCompat.getMainExecutor(this)
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    navigateToMain(userId, username)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Toast.makeText(applicationContext, "Biometric failed: $errString", Toast.LENGTH_SHORT).show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("MoneyGoat Login")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use Password")
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Navigates to the main application activity and clears this activity from the stack.
     */
    private fun navigateToMain(userId: Long, username: String) {
        Log.d(TAG, "Navigating to MainActivity for user $username")
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra("USER_ID", userId)
            putExtra("USERNAME", username)
        })
        finish()
    }
}
