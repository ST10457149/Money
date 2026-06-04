package com.moneygoat.app.data.dao
import androidx.lifecycle.LiveData
import androidx.room.*
import com.moneygoat.app.data.entity.*

/**
 * Data Access Object for the Category entity.
 * Provides methods for managing user-defined expense categories.
 */
@Dao
interface CategoryDao {
    /**
     * Inserts a new category into the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(category: Category): Long

    /**
     * Inserts a list of categories in a single transaction.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(categories: List<Category>)

    /**
     * Retrieves all categories for a specific user as LiveData for UI observation.
     */
    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    fun getCategoriesByUser(userId: Long): LiveData<List<Category>>

    /**
     * Retrieves all categories for a specific user as a standard List (one-shot).
     */
    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY name ASC")
    suspend fun getCategoriesByUserList(userId: Long): List<Category>

    /**
     * Retrieves categories with the count of associated expenses.
     */
    @Query("SELECT c.*, (SELECT COUNT(*) FROM expenses e WHERE e.categoryId = c.id) as expenseCount FROM categories c WHERE c.userId = :userId ORDER BY name ASC")
    fun getCategoriesWithCounts(userId: Long): LiveData<List<CategoryWithCount>>

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategories(): LiveData<List<Category>>

    /**
     * Deletes a category from the database.
     */
    @Delete
    suspend fun delete(category: Category)
}
