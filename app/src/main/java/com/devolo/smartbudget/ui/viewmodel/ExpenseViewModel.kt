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

    private val _sortByDateDesc = MutableStateFlow(true)
    val sortByDateDesc: StateFlow<Boolean> = _sortByDateDesc.asStateFlow()

    private val _currency = MutableStateFlow("MAD")
    val currency: StateFlow<String> = _currency.asStateFlow()

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategoriesIncludingInactive: StateFlow<List<Category>> = repository.allCategoriesIncludingInactive
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredExpenses: StateFlow<List<Expense>> = combine(
        currentMonth,
        selectedCategoryId,
        sortByDateDesc,
        repository.allExpenses
    ) { month, categoryId, sortDesc, allExpenses ->
        val startOfMonth = getStartOfMonth(month)
        val endOfMonth = getEndOfMonth(month)

        allExpenses
            .filter { expense ->
                val matchesMonth = expense.date in startOfMonth..endOfMonth
                val matchesCategory = categoryId == null || expense.categoryId == categoryId
                matchesMonth && matchesCategory
            }
            .let { list ->
                if (sortDesc) list.sortedByDescending { it.date }
                else list.sortedByDescending { it.amount }
            }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalMonthAmount: StateFlow<Double> = filteredExpenses.map { expenses ->
        expenses.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private fun getStartOfMonth(month: Calendar): Long {
        return (month.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun getEndOfMonth(month: Calendar): Long {
        return (month.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    val previousMonthTotal: StateFlow<Double> = combine(
        currentMonth,
        repository.allExpenses
    ) { month, allExpenses ->
        val prevMonth = (month.clone() as Calendar).apply { add(Calendar.MONTH, -1) }
        val start = getStartOfMonth(prevMonth)
        val end = getEndOfMonth(prevMonth)
        allExpenses.filter { it.date in start..end }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun changeMonth(delta: Int) {
        _currentMonth.value = (_currentMonth.value.clone() as Calendar).apply {
            add(Calendar.MONTH, delta)
        }
    }

    fun selectCategory(categoryId: Long?) {
        _selectedCategoryId.value = categoryId
    }

    fun toggleSort() {
        _sortByDateDesc.value = !_sortByDateDesc.value
    }

    fun setCurrency(newCurrency: String) {
        _currency.value = newCurrency
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

    fun toggleCategoryActive(category: Category) {
        viewModelScope.launch {
            repository.updateCategory(category.copy(isActive = !category.isActive))
        }
    }

    fun deleteCategory(categoryId: Long, reassignToDefault: Boolean) {
        viewModelScope.launch {
            if (reassignToDefault) {
                val autre = repository.getCategoryById(categoryId)
                val defaultCategory = categories.value.find { it.name == "Autre" }
                if (defaultCategory != null) {
                    repository.reassignExpensesToCategory(categoryId, defaultCategory.id)
                }
            }
            repository.deleteCategoryById(categoryId)
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.deleteAllExpenses()
            repository.deleteAllCategories()
            seedCategoriesIfEmpty()
        }
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

    fun seedDemoData() {
        viewModelScope.launch {
            val existingCats = repository.allCategoriesIncludingInactive.first()
            if (existingCats.isEmpty()) {
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
            val cats = repository.allCategoriesIncludingInactive.first()
            if (cats.isEmpty()) return@launch

            val catMap = cats.associateBy { it.name }
            val cal = Calendar.getInstance()

            // March 2026
            cal.set(2026, Calendar.MARCH, 1, 0, 0, 0)
            val march = cal.timeInMillis
            // April 2026
            cal.set(2026, Calendar.APRIL, 1, 0, 0, 0)
            val april = cal.timeInMillis

            val demoExpenses = listOf(
                // March expenses (18)
                Expense(amount = 45.50, date = march + 86400000L * 2, categoryId = catMap["Alimentation"]?.id ?: 1, note = "Courses Marjane"),
                Expense(amount = 150.00, date = march + 86400000L * 3, categoryId = catMap["Transport"]?.id ?: 2, note = "Abonnement Bus mensuel"),
                Expense(amount = 1200.00, date = march + 86400000L * 1, categoryId = catMap["Logement"]?.id ?: 3, note = "Loyer Mars"),
                Expense(amount = 85.00, date = march + 86400000L * 4, categoryId = catMap["Santé"]?.id ?: 4, note = "Pharmacie"),
                Expense(amount = 65.00, date = march + 86400000L * 5, categoryId = catMap["Loisirs"]?.id ?: 5, note = "Ciné Atlas"),
                Expense(amount = 200.00, date = march + 86400000L * 6, categoryId = catMap["Alimentation"]?.id ?: 1, note = "Marché de quartier"),
                Expense(amount = 35.00, date = march + 86400000L * 7, categoryId = catMap["Transport"]?.id ?: 2, note = "Taxi"),
                Expense(amount = 50.00, date = march + 86400000L * 8, categoryId = catMap["Études"]?.id ?: 6, note = "Photocopies cours"),
                Expense(amount = 320.00, date = march + 86400000L * 10, categoryId = catMap["Shopping"]?.id ?: 7, note = "Vêtements Zara"),
                Expense(amount = 78.00, date = march + 86400000L * 12, categoryId = catMap["Alimentation"]?.id ?: 1, note = "Restaurant"),
                Expense(amount = 25.00, date = march + 86400000L * 14, categoryId = catMap["Loisirs"]?.id ?: 5, note = "Netflix"),
                Expense(amount = 90.00, date = march + 86400000L * 15, categoryId = catMap["Transport"]?.id ?: 2, note = "Essence"),
                Expense(amount = 55.00, date = march + 86400000L * 18, categoryId = catMap["Alimentation"]?.id ?: 1, note = "Café et pain"),
                Expense(amount = 150.00, date = march + 86400000L * 20, categoryId = catMap["Études"]?.id ?: 6, note = "Livres"),
                Expense(amount = 40.00, date = march + 86400000L * 22, categoryId = catMap["Santé"]?.id ?: 4, note = "RDV médecin"),
                Expense(amount = 180.00, date = march + 86400000L * 24, categoryId = catMap["Loisirs"]?.id ?: 5, note = "Sortie bowling"),
                Expense(amount = 30.00, date = march + 86400000L * 26, categoryId = catMap["Autre"]?.id ?: 8, note = "Don caritatif"),
                Expense(amount = 95.00, date = march + 86400000L * 28, categoryId = catMap["Alimentation"]?.id ?: 1, note = "Supermarché"),
                // April expenses (17)
                Expense(amount = 1200.00, date = april + 86400000L * 1, categoryId = catMap["Logement"]?.id ?: 3, note = "Loyer Avril"),
                Expense(amount = 150.00, date = april + 86400000L * 3, categoryId = catMap["Transport"]?.id ?: 2, note = "Abonnement Bus"),
                Expense(amount = 52.00, date = april + 86400000L * 2, categoryId = catMap["Alimentation"]?.id ?: 1, note = "Courses Carrefour"),
                Expense(amount = 70.00, date = april + 86400000L * 5, categoryId = catMap["Loisirs"]?.id ?: 5, note = "Théâtre"),
                Expense(amount = 45.00, date = april + 86400000L * 6, categoryId = catMap["Transport"]?.id ?: 2, note = "Taxi aller-retour"),
                Expense(amount = 230.00, date = april + 86400000L * 8, categoryId = catMap["Shopping"]?.id ?: 7, note = "Chaussures"),
                Expense(amount = 38.00, date = april + 86400000L * 10, categoryId = catMap["Alimentation"]?.id ?: 1, note = "Pizza"),
                Expense(amount = 60.00, date = april + 86400000L * 11, categoryId = catMap["Études"]?.id ?: 6, note = "Fournitures"),
                Expense(amount = 100.00, date = april + 86400000L * 12, categoryId = catMap["Santé"]?.id ?: 4, note = "Lunettes"),
                Expense(amount = 25.00, date = april + 86400000L * 14, categoryId = catMap["Loisirs"]?.id ?: 5, note = "Spotify"),
                Expense(amount = 85.00, date = april + 86400000L * 16, categoryId = catMap["Alimentation"]?.id ?: 1, note = "Marché bio"),
                Expense(amount = 40.00, date = april + 86400000L * 18, categoryId = catMap["Transport"]?.id ?: 2, note = "Tramway"),
                Expense(amount = 75.00, date = april + 86400000L * 20, categoryId = catMap["Loisirs"]?.id ?: 5, note = "Piscine"),
                Expense(amount = 35.00, date = april + 86400000L * 22, categoryId = catMap["Alimentation"]?.id ?: 1, note = "Boulangerie"),
                Expense(amount = 110.00, date = april + 86400000L * 24, categoryId = catMap["Études"]?.id ?: 6, note = "Formation en ligne"),
                Expense(amount = 20.00, date = april + 86400000L * 26, categoryId = catMap["Autre"]?.id ?: 8, note = "Timbre"),
                Expense(amount = 48.00, date = april + 86400000L * 28, categoryId = catMap["Alimentation"]?.id ?: 1, note = "Snacks")
            )

            val existing = repository.allExpenses.first()
            if (existing.isEmpty()) {
                demoExpenses.forEach { repository.insertExpense(it) }
            }
        }
    }

    fun buildCsvContent(): String {
        val sb = StringBuilder()
        sb.appendLine("id,amount,currency,date,categoryId,note,paymentMethod")
        val expenses = filteredExpenses.value
        expenses.forEach { e ->
            sb.appendLine("${e.id},${e.amount},${e.currency},${e.date},${e.categoryId},${e.note ?: ""},${e.paymentMethod ?: ""}")
        }
        return sb.toString()
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
