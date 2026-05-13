package com.devolo.smartbudget.data.local

import androidx.room.*
import com.devolo.smartbudget.data.model.Category
import com.devolo.smartbudget.data.model.Expense
import com.devolo.smartbudget.data.model.MonthlyBudget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    // Categories
    @Query("SELECT * FROM categories WHERE isActive = 1")
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories")
    fun getAllCategoriesIncludingInactive(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): Category?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Update
    suspend fun updateCategory(category: Category)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Long)

    @Query("SELECT COUNT(*) FROM expenses WHERE categoryId = :categoryId")
    suspend fun getExpenseCountForCategory(categoryId: Long): Int

    @Query("UPDATE expenses SET categoryId = :newCategoryId WHERE categoryId = :oldCategoryId")
    suspend fun reassignExpensesToCategory(oldCategoryId: Long, newCategoryId: Long)

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

    @Query("SELECT * FROM expenses WHERE categoryId = :categoryId")
    suspend fun getExpensesByCategoryId(categoryId: Long): List<Expense>

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()

    @Query("DELETE FROM categories")
    suspend fun deleteAllCategories()

    // Monthly Budgets
    @Query("SELECT * FROM monthly_budgets WHERE month = :month")
    fun getBudgetsForMonth(month: String): Flow<List<MonthlyBudget>>

    @Query("SELECT * FROM monthly_budgets WHERE month = :month AND categoryId = :categoryId")
    suspend fun getBudgetForMonthAndCategory(month: String, categoryId: Long): MonthlyBudget?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: MonthlyBudget)

    @Delete
    suspend fun deleteBudget(budget: MonthlyBudget)

    @Query("DELETE FROM monthly_budgets WHERE month = :month AND categoryId = :categoryId")
    suspend fun deleteBudgetByMonthAndCategory(month: String, categoryId: Long)

}
