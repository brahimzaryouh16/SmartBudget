package com.devolo.smartbudget.data.repository

import com.devolo.smartbudget.data.local.BudgetDao
import com.devolo.smartbudget.data.model.Category
import com.devolo.smartbudget.data.model.Expense
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) : Repository {
    override val allCategories: Flow<List<Category>> = budgetDao.getAllCategories()
    override val allCategoriesIncludingInactive: Flow<List<Category>> = budgetDao.getAllCategoriesIncludingInactive()
    override val allExpenses: Flow<List<Expense>> = budgetDao.getAllExpenses()

    override fun getExpensesBetweenDates(startDate: Long, endDate: Long): Flow<List<Expense>> {
        return budgetDao.getExpensesBetweenDates(startDate, endDate)
    }

    override suspend fun getCategoryById(id: Long): Category? = budgetDao.getCategoryById(id)

    override suspend fun insertCategory(category: Category) {
        budgetDao.insertCategory(category)
    }

    override suspend fun updateCategory(category: Category) {
        budgetDao.updateCategory(category)
    }

    override suspend fun deleteCategoryById(id: Long) {
        budgetDao.deleteCategoryById(id)
    }

    override suspend fun getExpenseCountForCategory(categoryId: Long): Int {
        return budgetDao.getExpenseCountForCategory(categoryId)
    }

    override suspend fun reassignExpensesToCategory(oldCategoryId: Long, newCategoryId: Long) {
        budgetDao.reassignExpensesToCategory(oldCategoryId, newCategoryId)
    }

    override suspend fun getExpensesByCategoryId(categoryId: Long): List<Expense> {
        return budgetDao.getExpensesByCategoryId(categoryId)
    }

    override suspend fun insertExpense(expense: Expense) {
        budgetDao.insertExpense(expense)
    }

    override suspend fun updateExpense(expense: Expense) {
        budgetDao.updateExpense(expense)
    }

    override suspend fun deleteExpense(expense: Expense) {
        budgetDao.deleteExpense(expense)
    }

    override suspend fun getExpenseById(id: Long): Expense? {
        return budgetDao.getExpenseById(id)
    }

    override suspend fun deleteAllExpenses() {
        budgetDao.deleteAllExpenses()
    }

    override suspend fun deleteAllCategories() {
        budgetDao.deleteAllCategories()
    }
}
