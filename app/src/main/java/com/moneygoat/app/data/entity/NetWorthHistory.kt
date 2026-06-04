package com.moneygoat.app.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "net_worth_history",
    foreignKeys = [ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["userId"])])
data class NetWorthHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val date: String,
    val totalAssets: Double,
    val totalLiabilities: Double,
    val netWorth: Double
)
