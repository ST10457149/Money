package com.moneygoat.app.data.entity
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Data class representing an expense category.
 * Each category is associated with a specific user.
 */
@Entity(tableName = "categories",
    foreignKeys = [ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["userId"])])
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // Unique identifier for the category
    val name: String, // Name of the category (e.g., Food, Travel)
    val icon: String? = null, // Icon name or resource string
    val color: Int? = null, // Color identifier for UI
    val isSystem: Boolean = false, // System default categories cannot be deleted
    val userId: Long // ID of the user who owns this category
)
