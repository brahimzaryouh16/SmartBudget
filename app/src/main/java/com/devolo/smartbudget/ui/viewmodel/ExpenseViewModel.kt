package com.devolo.smartbudget.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.devolo.smartbudget.data.model.Category
import com.devolo.smartbudget.data.model.Expense
import com.devolo.smartbudget.data.repository.BudgetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class ExpenseViewModel(private val repository: BudgetRepository) : ViewModel() {

    private val _currentMonth = MutableStateFlow(Calendar.getInstance())
    val currentMonth: StateFlow<Calendar> = _currentMonth.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredExpenses: StateFlow<List<Expense>> = combine(
        currentMonth,
        selectedCategoryId,
        repository.allExpenses
    ) { month, categoryId, allExpenses ->
        val startOfMonth = (month.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val endOfMonth = (month.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        allExpenses.filter { expense ->
            val matchesMonth = expense.date in startOfMonth..endOfMonth
            val matchesCategory = categoryId == null || expense.categoryId == categoryId
            matchesMonth && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalMonthAmount: StateFlow<Double> = filteredExpenses.map { expenses ->
        expenses.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun changeMonth(delta: Int) {
        _currentMonth.value = (_currentMonth.value.clone() as Calendar).apply {
            add(Calendar.MONTH, delta)
        }
    }

    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun saveExpense(expense: Expense) {
        viewModelScope.launch {
            if (expense.id == 0L) {
                repository.insertExpense(expense)
            } else {
                repository.updateExpense(expense)
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }

    suspend fun getExpenseById(id: Long): Expense? {
        return repository.getExpenseById(id)
    }

    fun seedCategoriesIfEmpty() {
        viewModelScope.launch {
            if (categories.value.isEmpty()) {
                val defaultCategories = listOf(
                    Category(name = "Alimentation", icon = "🍔", color = "#f59e0b"),
                    Category(name = "Transport", icon = "🚌", color = "#3b82f6"),
                    Category(name = "Logement", icon = "🏠", color = "#8b5cf6"),
                    Category(name = "Santé", icon = "💊", color = "#ef4444"),
                    Category(name = "Loisirs", icon = "🎬", color = "#10b981"),
                    Category(name = "Études", icon = "📚", color = "#6366f1"),
                    Category(name = "Shopping", icon = "🛍️", color = "#ec4899"),
                    Category(name = "Autre", icon = "📦", color = "#94a3b8")
                )
                defaultCategories.forEach { repository.insertCategory(it) }
            }
        }
    }
}

class ExpenseViewModelFactory(private val repository: BudgetRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
