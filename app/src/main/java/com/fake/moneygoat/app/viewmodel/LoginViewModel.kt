package com.moneygoat.app.viewmodel
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.moneygoat.app.data.database.AppDatabase
import com.moneygoat.app.data.entity.User
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MoneyGoat_Login"
    private val userDao = AppDatabase.getDatabase(application).userDao()
    val loginResult = MutableLiveData<User?>()
    val registerResult = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()

    fun login(username: String, password: String) {
        Log.d(TAG, "Login attempt for user: $username")
        viewModelScope.launch {
            try {
                val user = userDao.login(username, password)
                if (user != null) {
                    Log.d(TAG, "Login successful for user: $username")
                    loginResult.postValue(user)
                } else {
                    Log.w(TAG, "Login failed for user: $username - Invalid credentials")
                    errorMessage.postValue("Invalid username or password")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during login for user: $username", e)
                errorMessage.postValue("An error occurred during login")
            }
        }
    }
    fun register(username: String, password: String) {
        Log.d(TAG, "Registration attempt for user: $username")
        viewModelScope.launch {
            try {
                if (userDao.getByUsername(username) != null) {
                    Log.w(TAG, "Registration failed: Username $username already exists")
                    errorMessage.postValue("Username already exists")
                    return@launch
                }
                userDao.insert(User(username = username, password = password))
                Log.d(TAG, "Registration successful for user: $username")
                registerResult.postValue(true)
            } catch (e: Exception) {
                Log.e(TAG, "Error during registration for user: $username", e)
                errorMessage.postValue("An error occurred during registration")
            }
        }
    }
}
