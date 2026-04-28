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
import androidx.lifecycle.ViewModelProvider
import com.moneygoat.app.R
import com.moneygoat.app.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {
    private val TAG = "MoneyGoat_LoginUI"
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "LoginActivity created")
        val prefs = getSharedPreferences("moneygoat_prefs", Context.MODE_PRIVATE)
        val savedUserId = prefs.getLong("user_id", -1)
        if (savedUserId != -1L) {
            val savedUsername = prefs.getString("username", "") ?: ""
            Log.d(TAG, "Auto-logging in user: $savedUsername")
            navigateToMain(savedUserId, savedUsername)
            return
        }
        setContentView(R.layout.activity_login)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        val etUsername = findViewById<EditText>(R.id.etUsername)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)

        btnLogin.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (username.isEmpty() || password.isEmpty()) {
                Log.w(TAG, "Login attempt with empty fields")
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.login(username, password)
        }
        tvRegister.setOnClickListener {
            Log.d(TAG, "Navigating to Registration")
            startActivity(Intent(this, RegisterActivity::class.java))
        }
        viewModel.loginResult.observe(this) { user ->
            if (user != null) {
                Log.d(TAG, "Login successful, saving session for ${user.username}")
                prefs.edit().putLong("user_id", user.id).putString("username", user.username).apply()
                navigateToMain(user.id, user.username)
            }
        }
        viewModel.errorMessage.observe(this) {
            Log.e(TAG, "Login error: $it")
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToMain(userId: Long, username: String) {
        Log.d(TAG, "Navigating to MainActivity for user $username")
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra("USER_ID", userId); putExtra("USERNAME", username)
        })
        finish()
    }
}
