package com.moneygoat.app.viewmodel
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.moneygoat.app.data.database.AppDatabase
import com.moneygoat.app.data.entity.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * ViewModel for handling user authentication (login and registration).
 * Uses Firebase Auth for cloud-based account management and local Room for session data.
 */
class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MoneyGoat_Login"
    private val userDao = AppDatabase.getDatabase(application).userDao()
    private val auth = FirebaseAuth.getInstance()
    
    // LiveData to observe login status
    val loginResult = MutableLiveData<User?>()
    
    // LiveData to observe registration status
    val registerResult = MutableLiveData<Boolean>()
    
    // LiveData to observe error messages
    val errorMessage = MutableLiveData<String>()

    /**
     * Attempts to log in a user with the provided credentials via Firebase.
     */
    fun login(email: String, password: String) {
        Log.d(TAG, "Login attempt for user: $email")
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = result.user
                if (firebaseUser != null) {
                    // Check local DB for additional profile info or create it
                    var localUser = userDao.getByUsername(email)
                    if (localUser == null) {
                        val newId = userDao.insert(User(username = email, password = "EXTERNAL_AUTH"))
                        localUser = User(id = newId, username = email, password = "EXTERNAL_AUTH")
                    }
                    Log.d(TAG, "Login successful for user: $email")
                    loginResult.postValue(localUser)
                } else {
                    errorMessage.postValue("Invalid email or password")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during login", e)
                errorMessage.postValue(e.message ?: "Authentication failed")
            }
        }
    }

    /**
     * Attempts to register a new user via Firebase.
     */
    fun register(email: String, password: String) {
        Log.d(TAG, "Registration attempt for user: $email")
        viewModelScope.launch {
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = result.user
                if (firebaseUser != null) {
                    // 1. Save to local Room DB
                    val localUser = User(username = email, password = "EXTERNAL_AUTH")
                    val newId = userDao.insert(localUser)
                    
                    // 2. Sync to Firebase Realtime Database (RTDB)
                    val fbDb = com.google.firebase.database.FirebaseDatabase.getInstance()
                    fbDb.getReference("users").child(newId.toString())
                        .setValue(mapOf("email" to email, "uid" to firebaseUser.uid))

                    Log.d(TAG, "Registration & Sync successful for user: $email")
                    registerResult.postValue(true)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during registration", e)
                errorMessage.postValue(e.message ?: "Registration failed")
            }
        }
    }
}
