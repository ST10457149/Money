package com.moneygoat.app.data.database
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.moneygoat.app.data.dao.*
import com.moneygoat.app.data.entity.*

/**
 * The Room Database for the application.
 * Defines the entities and provides access to the DAOs.
 */
@Database(entities = [User::class, Category::class, Expense::class, BudgetGoal::class, Account::class, RecurringTransaction::class, NetWorthHistory::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    /**
     * @return The Data Access Object for the User entity.
     */
    abstract fun userDao(): UserDao

    /**
     * @return The Data Access Object for the Category entity.
     */
    abstract fun categoryDao(): CategoryDao

    /**
     * @return The Data Access Object for the Expense entity.
     */
    abstract fun expenseDao(): ExpenseDao

    /**
     * @return The Data Access Object for the BudgetGoal entity.
     */
    abstract fun budgetGoalDao(): BudgetGoalDao

    abstract fun accountDao(): AccountDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao

    companion object {
        // Singleton prevents multiple instances of database opening at the same time.
        @Volatile private var INSTANCE: AppDatabase? = null

        /**
         * Gets the singleton instance of the AppDatabase.
         * @param context The application context.
         * @return The AppDatabase instance.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "moneygoat_database"
                )
                .fallbackToDestructiveMigration() // Simplifies database migration by wiping and rebuilding
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
