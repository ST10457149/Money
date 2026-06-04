package com.moneygoat.app.data.entity
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data class representing a user in the application.
 * This class is used as a Room entity to store user credentials.
 */
@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // Unique identifier for each user
    val username: String, // Unique username for login
    val password: String // Encrypted or hashed password (for security in a real app)
)
