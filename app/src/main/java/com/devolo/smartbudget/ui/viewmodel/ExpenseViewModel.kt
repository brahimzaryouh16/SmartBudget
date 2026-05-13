package com.devolo.smartbudget.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.devolo.smartbudget.data.model.Category
import com.devolo.smartbudget.data.model.Expense
import com.devolo.smartbudget.data.model.MonthlyBudget
import com.devolo.smartbudget.data.repository.Repository
import com.devolo.smartbudget.ui.UiEvent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class ExpenseViewModel(private val repository: Repository) : ViewModel() {

    private val _currentMonth = MutableStateFlow(Calendar.getInstance())
    val currentMonth: StateFlow<Calendar> = _currentMonth.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    private val _sortByDateDesc = MutableStateFlow(true)
    val sortByDateDesc: StateFlow<Boolean> = _sortByDateDesc.asStateFlow()

    private val _currency = MutableStateFlow("MAD")
    val currency: StateFlow<String> = _currency.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _monthlyBudget = MutableStateFlow(0.0)
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    val monthlyBudgets: StateFlow<List<MonthlyBudget>> = currentMonth.map { month ->
        getMonthString(month)
    }.flatMapLatest { monthStr ->
        repository.getBudgetsForMonth(monthStr)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _uiEvent = MutableSharedFlow<UiEvent>()
    val uiEvent: SharedFlow<UiEvent> = _uiEvent.asSharedFlow()

    val categories: StateFlow<List<Category>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allCategoriesIncludingInactive: StateFlow<List<Category>> = repository.allCategoriesIncludingInactive
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExpenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val filteredExpenses: StateFlow<List<Expense>> = combine(
        currentMonth,
        selectedCategoryId,
        sortByDateDesc,
        searchQuery,
        allExpenses
    ) { month, categoryId, sortDesc, query, expenses ->
        val startOfMonth = getStartOfMonth(month)
        val endOfMonth = getEndOfMonth(month)

        expenses
            .filter { expense ->
                val matchesMonth = expense.date in startOfMonth..endOfMonth
                val matchesCategory = categoryId == null || expense.categoryId == categoryId
                val matchesSearch = query.isBlank() ||
                    (expense.note?.contains(query, ignoreCase = true) == true) ||
                    expense.amount.toString().contains(query, ignoreCase = true)
                matchesMonth && matchesCategory && matchesSearch
            }
            .let { list ->
                if (sortDesc) list.sortedByDescending { it.date }
                else list.sortedByDescending { it.amount }
            }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val totalMonthAmount: StateFlow<Double> = filteredExpenses.map { expenses ->
        expenses.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val budgetProgress: StateFlow<Float> = combine(
        totalMonthAmount,
        monthlyBudget
    ) { total, budget ->
        if (budget > 0) (total / budget).toFloat().coerceIn(0f, 1f) else 0f
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0f)

    val isOverBudget: StateFlow<Boolean> = combine(
        totalMonthAmount,
        monthlyBudget
    ) { total, budget ->
        budget > 0 && total > budget
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        viewModelScope.launch {
            try {
                repository.allExpenses.first()
                _isLoading.value = false
            } catch (e: Exception) {
                _isLoading.value = false
                _uiEvent.emit(UiEvent.ShowSnackbar("Erreur de chargement des données"))
            }
        }
    }

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

    private fun getMonthString(month: Calendar): String {
        val df = java.text.SimpleDateFormat("yyyy-MM", Locale.getDefault())
        return df.format(month.time)
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

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setMonthlyBudget(amount: Double) {
        _monthlyBudget.value = amount
    }

    fun saveExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                if (expense.id == 0L) {
                    repository.insertExpense(expense)
                } else {
                    repository.updateExpense(expense.copy(updatedAt = System.currentTimeMillis()))
                }
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Erreur lors de l'enregistrement"))
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                repository.deleteExpense(expense)
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Erreur lors de la suppression"))
            }
        }
    }

    fun restoreExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                repository.insertExpense(expense)
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Erreur lors de la restauration"))
            }
        }
    }

    suspend fun getExpenseById(id: Long): Expense? {
        return try {
            repository.getExpenseById(id)
        } catch (e: Exception) {
            null
        }
    }

    fun toggleCategoryActive(category: Category) {
        viewModelScope.launch {
            try {
                repository.updateCategory(category.copy(isActive = !category.isActive))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Erreur lors de la mise à jour"))
            }
        }
    }

    fun saveCategory(category: Category) {
        viewModelScope.launch {
            try {
                if (category.id == 0L) {
                    repository.insertCategory(category)
                    _uiEvent.emit(UiEvent.ShowSnackbar("Catégorie ajoutée"))
                } else {
                    repository.updateCategory(category)
                    _uiEvent.emit(UiEvent.ShowSnackbar("Catégorie mise à jour"))
                }
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Erreur lors de l'enregistrement"))
            }
        }
    }

    fun deleteCategory(categoryId: Long, reassignToDefault: Boolean) {
        viewModelScope.launch {
            try {
                if (reassignToDefault) {
                    repository.getCategoryById(categoryId)
                    val defaultCategory = categories.value.find { it.name == "Autre" }
                    if (defaultCategory != null) {
                        repository.reassignExpensesToCategory(categoryId, defaultCategory.id)
                    }
                }
                repository.deleteCategoryById(categoryId)
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Erreur lors de la suppression"))
            }
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            try {
                repository.deleteAllExpenses()
                repository.deleteAllCategories()
                seedCategoriesIfEmpty()
                _uiEvent.emit(UiEvent.ShowSnackbar("Données réinitialisées"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Erreur lors de la réinitialisation"))
            }
        }
    }

    fun seedCategoriesIfEmpty() {
        viewModelScope.launch {
            try {
                if (categories.value.isEmpty()) {
                    val defaultCategories = listOf(
                        Category(name = "Alimentation", icon = "\uD83C\uDF54", color = "#f59e0b"),
                        Category(name = "Transport", icon = "\uD83D\uDE8C", color = "#3b82f6"),
                        Category(name = "Logement", icon = "\uD83C\uDFE0", color = "#8b5cf6"),
                        Category(name = "Santé", icon = "\uD83D\uDC8A", color = "#ef4444"),
                        Category(name = "Loisirs", icon = "\uD83C\uDFAC", color = "#10b981"),
                        Category(name = "Études", icon = "\uD83D\uDCDA", color = "#6366f1"),
                        Category(name = "Shopping", icon = "\uD83D\uDECD\uFE0F", color = "#ec4899"),
                        Category(name = "Autre", icon = "\uD83D\uDCE6", color = "#94a3b8")
                    )
                    defaultCategories.forEach { repository.insertCategory(it) }
                }
            } catch (_: Exception) { }
        }
    }

    fun seedDemoData() {
        viewModelScope.launch {
            try {
                val existingCats = repository.allCategoriesIncludingInactive.first()
                if (existingCats.isEmpty()) {
                    val defaultCategories = listOf(
                        Category(name = "Alimentation", icon = "\uD83C\uDF54", color = "#f59e0b"),
                        Category(name = "Transport", icon = "\uD83D\uDE8C", color = "#3b82f6"),
                        Category(name = "Logement", icon = "\uD83C\uDFE0", color = "#8b5cf6"),
                        Category(name = "Santé", icon = "\uD83D\uDC8A", color = "#ef4444"),
                        Category(name = "Loisirs", icon = "\uD83C\uDFAC", color = "#10b981"),
                        Category(name = "Études", icon = "\uD83D\uDCDA", color = "#6366f1"),
                        Category(name = "Shopping", icon = "\uD83D\uDECD\uFE0F", color = "#ec4899"),
                        Category(name = "Autre", icon = "\uD83D\uDCE6", color = "#94a3b8")
                    )
                    defaultCategories.forEach { repository.insertCategory(it) }
                }
                val cats = repository.allCategoriesIncludingInactive.first()
                if (cats.isEmpty()) return@launch

                val catMap = cats.associateBy { it.name }
                val cal = Calendar.getInstance()

                cal.set(2026, Calendar.MARCH, 1, 0, 0, 0)
                val march = cal.timeInMillis
                cal.set(2026, Calendar.APRIL, 1, 0, 0, 0)
                val april = cal.timeInMillis

                val demoExpenses = listOf(
                    Expense(amount = 45.50, date = march + 86400000L * 2, categoryId = catMap["Alimentation"]?.id ?: 1, note = "Courses Marjane"),
                    Expense(amount = 150.00, date = march + 86400000L * 3, categoryId = catMap["Transport"]?.id ?: 2, note = "Abonnement Bus mensuel", isRecurring = true, recurringInterval = "mensuel"),
                    Expense(amount = 1200.00, date = march + 86400000L * 1, categoryId = catMap["Logement"]?.id ?: 3, note = "Loyer Mars", isRecurring = true, recurringInterval = "mensuel"),
                    Expense(amount = 85.00, date = march + 86400000L * 4, categoryId = catMap["Santé"]?.id ?: 4, note = "Pharmacie"),
                    Expense(amount = 65.00, date = march + 86400000L * 5, categoryId = catMap["Loisirs"]?.id ?: 5, note = "Ciné Atlas"),
                    Expense(amount = 200.00, date = march + 86400000L * 6, categoryId = catMap["Alimentation"]?.id ?: 1, note = "Marché de quartier"),
                    Expense(amount = 35.00, date = march + 86400000L * 7, categoryId = catMap["Transport"]?.id ?: 2, note = "Taxi"),
                    Expense(amount = 50.00, date = march + 86400000L * 8, categoryId = catMap["Études"]?.id ?: 6, note = "Photocopies cours"),
                    Expense(amount = 320.00, date = march + 86400000L * 10, categoryId = catMap["Shopping"]?.id ?: 7, note = "Vêtements Zara"),
                    Expense(amount = 78.00, date = march + 86400000L * 12, categoryId = catMap["Alimentation"]?.id ?: 1, note = "Restaurant"),
                    Expense(amount = 25.00, date = march + 86400000L * 14, categoryId = catMap["Loisirs"]?.id ?: 5, note = "Netflix", isRecurring = true, recurringInterval = "mensuel"),
                    Expense(amount = 90.00, date = march + 86400000L * 15, categoryId = catMap["Transport"]?.id ?: 2, note = "Essence"),
                    Expense(amount = 55.00, date = march + 86400000L * 18, categoryId = catMap["Alimentation"]?.id ?: 1, note = "Café et pain"),
                    Expense(amount = 150.00, date = march + 86400000L * 20, categoryId = catMap["Études"]?.id ?: 6, note = "Livres"),
                    Expense(amount = 40.00, date = march + 86400000L * 22, categoryId = catMap["Santé"]?.id ?: 4, note = "RDV médecin"),
                    Expense(amount = 180.00, date = march + 86400000L * 24, categoryId = catMap["Loisirs"]?.id ?: 5, note = "Sortie bowling"),
                    Expense(amount = 30.00, date = march + 86400000L * 26, categoryId = catMap["Autre"]?.id ?: 8, note = "Don caritatif"),
                    Expense(amount = 95.00, date = march + 86400000L * 28, categoryId = catMap["Alimentation"]?.id ?: 1, note = "Supermarché"),
                    Expense(amount = 1200.00, date = april + 86400000L * 1, categoryId = catMap["Logement"]?.id ?: 3, note = "Loyer Avril", isRecurring = true, recurringInterval = "mensuel"),
                    Expense(amount = 150.00, date = april + 86400000L * 3, categoryId = catMap["Transport"]?.id ?: 2, note = "Abonnement Bus", isRecurring = true, recurringInterval = "mensuel"),
                    Expense(amount = 52.00, date = april + 86400000L * 2, categoryId = catMap["Alimentation"]?.id ?: 1, note = "Courses Carrefour"),
                    Expense(amount = 70.00, date = april + 86400000L * 5, categoryId = catMap["Loisirs"]?.id ?: 5, note = "Théâtre"),
                    Expense(amount = 45.00, date = april + 86400000L * 6, categoryId = catMap["Transport"]?.id ?: 2, note = "Taxi aller-retour"),
                    Expense(amount = 230.00, date = april + 86400000L * 8, categoryId = catMap["Shopping"]?.id ?: 7, note = "Chaussures"),
                    Expense(amount = 38.00, date = april + 86400000L * 10, categoryId = catMap["Alimentation"]?.id ?: 1, note = "Pizza"),
                    Expense(amount = 60.00, date = april + 86400000L * 11, categoryId = catMap["Études"]?.id ?: 6, note = "Fournitures"),
                    Expense(amount = 100.00, date = april + 86400000L * 12, categoryId = catMap["Santé"]?.id ?: 4, note = "Lunettes"),
                    Expense(amount = 25.00, date = april + 86400000L * 14, categoryId = catMap["Loisirs"]?.id ?: 5, note = "Spotify", isRecurring = true, recurringInterval = "mensuel"),
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

                // Seed monthly budgets for demo
                val marchBudgets = listOf(
                    MonthlyBudget(month = "2026-03", categoryId = catMap["Alimentation"]?.id ?: 1, limitAmount = 600.0),
                    MonthlyBudget(month = "2026-03", categoryId = catMap["Transport"]?.id ?: 2, limitAmount = 300.0),
                    MonthlyBudget(month = "2026-03", categoryId = catMap["Logement"]?.id ?: 3, limitAmount = 1300.0),
                    MonthlyBudget(month = "2026-03", categoryId = catMap["Loisirs"]?.id ?: 5, limitAmount = 300.0)
                )
                marchBudgets.forEach { repository.insertBudget(it) }

                val aprilBudgets = listOf(
                    MonthlyBudget(month = "2026-04", categoryId = catMap["Alimentation"]?.id ?: 1, limitAmount = 600.0),
                    MonthlyBudget(month = "2026-04", categoryId = catMap["Transport"]?.id ?: 2, limitAmount = 300.0),
                    MonthlyBudget(month = "2026-04", categoryId = catMap["Logement"]?.id ?: 3, limitAmount = 1300.0),
                    MonthlyBudget(month = "2026-04", categoryId = catMap["Loisirs"]?.id ?: 5, limitAmount = 300.0)
                )
                aprilBudgets.forEach { repository.insertBudget(it) }
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Erreur lors du chargement des données de démo"))
            }
        }
    }

    // Monthly budget per category
    fun saveCategoryBudget(categoryId: Long, limitAmount: Double) {
        viewModelScope.launch {
            try {
                val monthStr = getMonthString(_currentMonth.value)
                val existing = repository.getBudgetForMonthAndCategory(monthStr, categoryId)
                if (existing != null) {
                    repository.insertBudget(existing.copy(limitAmount = limitAmount))
                } else {
                    repository.insertBudget(MonthlyBudget(month = monthStr, categoryId = categoryId, limitAmount = limitAmount))
                }
                _uiEvent.emit(UiEvent.ShowSnackbar("Budget enregistré"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Erreur lors de l'enregistrement du budget"))
            }
        }
    }

    fun deleteCategoryBudget(categoryId: Long) {
        viewModelScope.launch {
            try {
                val monthStr = getMonthString(_currentMonth.value)
                repository.deleteBudgetByMonthAndCategory(monthStr, categoryId)
                _uiEvent.emit(UiEvent.ShowSnackbar("Budget supprimé"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Erreur lors de la suppression du budget"))
            }
        }
    }

    fun getCategoryBudgetForCurrentMonth(categoryId: Long, onResult: (MonthlyBudget?) -> Unit) {
        viewModelScope.launch {
            try {
                val monthStr = getMonthString(_currentMonth.value)
                val budget = repository.getBudgetForMonthAndCategory(monthStr, categoryId)
                onResult(budget)
            } catch (_: Exception) {
                onResult(null)
            }
        }
    }

    // Recurring: generate next month's recurring expenses
    fun generateRecurringExpenses() {
        viewModelScope.launch {
            try {
                val recurring = repository.getRecurringExpenses()
                val nextMonth = (_currentMonth.value.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
                val startNext = getStartOfMonth(nextMonth)
                val endNext = getEndOfMonth(nextMonth)

                // Only generate for current-month recurring if next month doesn't have them
                val nextMonthExisting = repository.allExpenses.first()
                    .filter { it.date in startNext..endNext }
                    .map { it.note to it.amount }

                recurring.forEach { exp ->
                    val alreadyExists = nextMonthExisting.any { it.first == exp.note && it.second == exp.amount }
                    if (!alreadyExists) {
                        // Shift date to same day of next month
                        val srcCal = Calendar.getInstance().apply { timeInMillis = exp.date }
                        val newCal = Calendar.getInstance()
                        newCal.timeInMillis = startNext
                        val dayOfMonth = srcCal.get(Calendar.DAY_OF_MONTH).coerceAtMost(newCal.getActualMaximum(Calendar.DAY_OF_MONTH))
                        newCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                        repository.insertExpense(exp.copy(
                            id = 0,
                            date = newCal.timeInMillis,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        ))
                    }
                }
                _uiEvent.emit(UiEvent.ShowSnackbar("Dépenses récurrentes générées"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Erreur lors de la génération"))
            }
        }
    }

    fun importCsvContent(csvContent: String) {
        viewModelScope.launch {
            try {
                val lines = csvContent.lines().filter { it.isNotBlank() }
                if (lines.size < 2) {
                    _uiEvent.emit(UiEvent.ShowSnackbar("CSV vide ou invalide"))
                    return@launch
                }
                val header = lines.first().split(",").map { it.trim().lowercase() }
                val amountIdx = header.indexOf("amount")
                val dateIdx = header.indexOf("date")
                val categoryNameIdx = header.indexOf("category")
                val noteIdx = header.indexOf("note")
                val paymentIdx = header.indexOf("paymentmethod")

                if (amountIdx < 0 || dateIdx < 0) {
                    _uiEvent.emit(UiEvent.ShowSnackbar("Colonnes amount et date requises"))
                    return@launch
                }

                val cats = categories.value
                val defaultCat = cats.firstOrNull()?.id ?: return@launch

                var imported = 0
                lines.drop(1).forEach { line ->
                    val cols = line.split(",").map { it.trim() }
                    val amount = cols.getOrNull(amountIdx)?.toDoubleOrNull() ?: return@forEach
                    val date = cols.getOrNull(dateIdx)?.toLongOrNull() ?: return@forEach
                    val categoryName = if (categoryNameIdx >= 0) cols.getOrNull(categoryNameIdx) else null
                    val note = if (noteIdx >= 0) cols.getOrNull(noteIdx) else null
                    val paymentMethod = if (paymentIdx >= 0) cols.getOrNull(paymentIdx) else null
                    val categoryId = if (categoryName != null) {
                        cats.find { it.name.equals(categoryName, ignoreCase = true) }?.id ?: defaultCat
                    } else defaultCat

                    repository.insertExpense(Expense(
                        amount = amount,
                        date = date,
                        categoryId = categoryId,
                        note = note?.ifBlank { null },
                        paymentMethod = paymentMethod?.ifBlank { null }
                    ))
                    imported++
                }
                _uiEvent.emit(UiEvent.ShowSnackbar("$imported dépenses importées"))
            } catch (e: Exception) {
                _uiEvent.emit(UiEvent.ShowSnackbar("Erreur d'import: ${e.message}"))
            }
        }
    }

    fun buildCsvContent(): String {
        val sb = StringBuilder()
        sb.appendLine("id,amount,currency,date,categoryId,note,paymentMethod,isRecurring,recurringInterval")
        val expenses = filteredExpenses.value
        expenses.forEach { e ->
            sb.appendLine("${e.id},${e.amount},${e.currency},${e.date},${e.categoryId},${e.note ?: ""},${e.paymentMethod ?: ""},${e.isRecurring},${e.recurringInterval ?: ""}")
        }
        return sb.toString()
    }

    fun buildCsvContentForDateRangeSync(startDate: Long, endDate: Long): String {
        val sb = StringBuilder()
        sb.appendLine("id,amount,currency,date,categoryId,note,paymentMethod,isRecurring,recurringInterval")
        val expensesList = allExpenses.value
        val filtered = expensesList.filter { it.date in startDate..endDate }
        filtered.forEach { e ->
            sb.appendLine("${e.id},${e.amount},${e.currency},${e.date},${e.categoryId},${e.note ?: ""},${e.paymentMethod ?: ""},${e.isRecurring},${e.recurringInterval ?: ""}")
        }
        return sb.toString()
    }

    fun refresh() {
        _isRefreshing.value = true
        viewModelScope.launch {
            try {
                repository.allExpenses.first()
            } catch (_: Exception) { }
            _isRefreshing.value = false
        }
    }

    fun showOnboarding(): Boolean {
        return categories.value.isEmpty()
    }
}

class ExpenseViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
