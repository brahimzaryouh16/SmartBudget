package com.devolo.smartbudget.data.repository

import com.devolo.smartbudget.data.local.BudgetDao
import com.devolo.smartbudget.data.model.Category
import com.devolo.smartbudget.data.model.Expense
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) {
    val allCategories: Flow<List<Category>> = budgetDao.getAllCategories()
    val allExpenses: Flow<List<Expense>> = budgetDao.getAllExpenses()

    fun getExpensesBetweenDates(startDate: Long, endDate: Long): Flow<List<Expense>> {
        return budgetDao.getExpensesBetweenDates(startDate, endDate)
    }

    suspend fun insertCategory(category: Category) {
        budgetDao.insertCategory(category)
    }

    suspend fun updateCategory(category: Category) {
        budgetDao.updateCategory(category)
    }

    suspend fun insertExpense(expense: Expense) {
        budgetDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: Expense) {
        budgetDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        budgetDao.deleteExpense(expense)
    }

    suspend fun getExpenseById(id: Long): Expense? {
        return budgetDao.getExpenseById(id)
    }
}
