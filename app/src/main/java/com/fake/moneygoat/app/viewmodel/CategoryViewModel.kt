package com.moneygoat.app.viewmodel
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.moneygoat.app.data.database.AppDatabase
import com.moneygoat.app.data.entity.Category
import kotlinx.coroutines.launch

class CategoryViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MoneyGoat_Category"
    private val dao = AppDatabase.getDatabase(application).categoryDao()
    fun getCategories(userId: Long): LiveData<List<Category>> {
        Log.d(TAG, "Reading categories for user $userId")
        return dao.getCategoriesByUser(userId)
    }
    suspend fun getCategoriesList(userId: Long): List<Category> {
        Log.d(TAG, "Reading categories list for user $userId")
        return dao.getCategoriesByUserList(userId)
    }
    fun addCategory(name: String, userId: Long) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Saving category: $name for user $userId")
                dao.insert(Category(name = name, userId = userId))
                Log.d(TAG, "Category saved successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving category", e)
            }
        }
    }
    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Deleting category: ${category.name} (id: ${category.id})")
                dao.delete(category)
                Log.d(TAG, "Category deleted successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Error deleting category", e)
            }
        }
    }
}

