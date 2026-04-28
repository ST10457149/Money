package com.moneygoat.app.ui

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.moneygoat.app.R
import com.moneygoat.app.viewmodel.LoginViewModel

class RegisterActivity : AppCompatActivity() {
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        //User Details
        val etUsername = findViewById<EditText>(R.id.etRegUsername)
        val etPassword = findViewById<EditText>(R.id.etRegPassword)
        val etConfirm = findViewById<EditText>(R.id.etRegConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvBack = findViewById<TextView>(R.id.tvBackToLogin)

        btnRegister.setOnClickListener {
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirm = etConfirm.text.toString().trim()
            if (username.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show(); return@setOnClickListener
            }
            if (password != confirm) { Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            if (password.length < 4) { Toast.makeText(this, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show(); return@setOnClickListener }
            viewModel.register(username, password)
        }
        tvBack.setOnClickListener { finish() }
        viewModel.registerResult.observe(this) { if (it) { Toast.makeText(this, "Registration successful! Please log in.", Toast.LENGTH_SHORT).show(); finish() } }
        viewModel.errorMessage.observe(this) { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
    }
}
