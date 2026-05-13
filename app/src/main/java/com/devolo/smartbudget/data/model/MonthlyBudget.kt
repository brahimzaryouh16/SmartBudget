package com.devolo.smartbudget.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "monthly_budgets",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["categoryId", "month"], unique = true)]
)
data class MonthlyBudget(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val month: String, // AAAA-MM
    val categoryId: Long,
    val limitAmount: Double
)
