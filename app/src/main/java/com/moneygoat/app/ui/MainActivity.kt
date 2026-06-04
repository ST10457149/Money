package com.moneygoat.app.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.moneygoat.app.R
import com.moneygoat.app.viewmodel.CategoryViewModel
import com.moneygoat.app.viewmodel.ExpenseViewModel
import com.moneygoat.app.viewmodel.GoalViewModel
import com.moneygoat.app.worker.BudgetCheckWorker
import java.util.concurrent.TimeUnit

/**
 * Main Activity of the application.
 * Hosts the fragments for Home, Analytics, Add Expense, Categories, and Goals.
 * Manages the Bottom Navigation and the top menu.
 */
class MainActivity : AppCompatActivity() {
    private val TAG = "MoneyGoat_MainUI"
    var userId: Long = -1L
    var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity created")
        setContentView(R.layout.activity_main)
        
        // Retrieve user information passed from LoginActivity
        userId = intent.getLongExtra("USER_ID", -1L)
        username = intent.getStringExtra("USERNAME") ?: ""
        Log.d(TAG, "Active user: $username (ID: $userId)")
        
        // Check notification permission for Android 13+
        checkNotificationPermission()

        // Setup Action Bar
        supportActionBar?.title = "MoneyGoat"
        supportActionBar?.subtitle = "Welcome, $username"

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        
        // Load initial fragment if this is a fresh start
        if (savedInstanceState == null) {
            Log.d(TAG, "Initial fragment load: HomeFragment")
            loadFragment(HomeFragment())
        }

        // Handle bottom navigation item selection
        bottomNav.setOnItemSelectedListener { item ->
            Log.d(TAG, "Navigation item selected: ${item.title}")
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_add -> AddExpenseFragment()
                R.id.nav_analytics -> CategoryTotalsFragment()
                R.id.nav_more -> SavingsGoalsFragment()
                else -> HomeFragment()
            }
            loadFragment(fragment)
            true
        }

        // Trigger Firebase Sync
        syncData()

        // Schedule Budget Checks
        scheduleBudgetChecks()
    }

    /**
     * Requests notification permission if needed (Android 13+).
     */
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                    if (!isGranted) {
                        Toast.makeText(this, "Notifications disabled. You won't receive budget alerts.", Toast.LENGTH_LONG).show()
                    }
                }.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    /**
     * Schedules a periodic worker to check budget goals and inactivity.
     */
    private fun scheduleBudgetChecks() {
        if (userId == -1L) return
        
        val data = Data.Builder().putLong("USER_ID", userId).build()
        val workRequest = PeriodicWorkRequestBuilder<BudgetCheckWorker>(6, TimeUnit.HOURS)
            .setInputData(data)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "BudgetCheckWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    /**
     * Triggers data synchronization from Firebase Realtime Database.
     */
    private fun syncData() {
        if (userId != -1L) {
            val expenseVM = ViewModelProvider(this)[ExpenseViewModel::class.java]
            expenseVM.syncData(userId)
            Toast.makeText(this, "Syncing data...", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Replaces the current fragment in the container with the provided fragment.
     */
    private fun loadFragment(fragment: Fragment) {
        Log.d(TAG, "Loading fragment: ${fragment.javaClass.simpleName}")
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean { 
        menuInflater.inflate(R.menu.main_menu, menu)
        return true 
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "Options item selected: ${item.title}")
        return when (item.itemId) {
            R.id.action_logout -> { 
                logout()
                true 
            }
            R.id.action_accounts -> {
                loadFragment(AccountsFragment())
                true
            }
            R.id.action_categories -> {
                loadFragment(CategoryManagerFragment())
                true
            }
            R.id.action_expense_list -> { 
                loadFragment(ExpenseListFragment())
                true 
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * Clears the user session and returns to the LoginActivity.
     */
    private fun logout() {
        Log.d(TAG, "User logging out: $username")
        getSharedPreferences("moneygoat_prefs", Context.MODE_PRIVATE).edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
