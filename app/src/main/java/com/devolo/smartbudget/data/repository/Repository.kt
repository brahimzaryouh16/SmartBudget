package com.devolo.smartbudget.data.repository

import com.devolo.smartbudget.data.model.Category
import com.devolo.smartbudget.data.model.Expense
import kotlinx.coroutines.flow.Flow

interface Repository {
    val allCategories: Flow<List<Category>>
    val allCategoriesIncludingInactive: Flow<List<Category>>
    val allExpenses: Flow<List<Expense>>

    fun getExpensesBetweenDates(startDate: Long, endDate: Long): Flow<List<Expense>>
    suspend fun getCategoryById(id: Long): Category?
    suspend fun insertCategory(category: Category)
    suspend fun updateCategory(category: Category)
    suspend fun deleteCategoryById(id: Long)
    suspend fun getExpenseCountForCategory(categoryId: Long): Int
    suspend fun reassignExpensesToCategory(oldCategoryId: Long, newCategoryId: Long)
    suspend fun getExpensesByCategoryId(categoryId: Long): List<Expense>
    suspend fun insertExpense(expense: Expense)
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
    suspend fun getExpenseById(id: Long): Expense?
    suspend fun deleteAllExpenses()
    suspend fun deleteAllCategories()
}
