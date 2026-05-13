package com.devolo.smartbudget.data.local

import androidx.room.*
import com.devolo.smartbudget.data.model.Category
import com.devolo.smartbudget.data.model.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    // Categories
    @Query("SELECT * FROM categories WHERE isActive = 1")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    // Expenses
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getExpensesBetweenDates(startDate: Long, endDate: Long): Flow<List<Expense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): Expense?
}
