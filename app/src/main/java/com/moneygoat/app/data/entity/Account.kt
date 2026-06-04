package com.moneygoat.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "accounts",
    foreignKeys = [ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["userId"])])
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val type: String, // CASH, BANK, CREDIT_CARD, INVESTMENT
    val balance: Double,
    val currency: String = "ZAR"
)
