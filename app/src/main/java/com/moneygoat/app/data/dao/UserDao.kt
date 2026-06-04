package com.moneygoat.app.data.dao
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.moneygoat.app.data.entity.User

/**
 * Data Access Object for the User entity.
 * Provides methods for registration and authentication.
 */
@Dao
interface UserDao {
    /**
     * Inserts a new user into the database.
     * @param user The user object to insert.
     * @return The row ID of the newly inserted user.
     */
    @Insert
    suspend fun insert(user: User): Long

    /**
     * Attempts to find a user with matching username and password.
     * @param username The username to search for.
     * @param password The password to search for.
     * @return The User object if found, null otherwise.
     */
    @Query("SELECT * FROM users WHERE username = :username AND password = :password LIMIT 1")
    suspend fun login(username: String, password: String): User?

    /**
     * Finds a user by their username.
     * @param username The username to search for.
     * @return The User object if found, null otherwise.
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): User?
}
