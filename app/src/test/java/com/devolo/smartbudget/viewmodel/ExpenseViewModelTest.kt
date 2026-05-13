package com.devolo.smartbudget.viewmodel

import com.devolo.smartbudget.data.model.Category
import com.devolo.smartbudget.data.model.Expense
import com.devolo.smartbudget.data.model.MonthlyBudget
import com.devolo.smartbudget.data.repository.Repository
import com.devolo.smartbudget.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FakeBudgetRepository : Repository {
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    private val _allCategories = MutableStateFlow<List<Category>>(emptyList())
    private val _expenses = MutableStateFlow<List<Expense>>(emptyList())

    override val allCategories: Flow<List<Category>> = _categories
    override val allCategoriesIncludingInactive: Flow<List<Category>> = _allCategories
    override val allExpenses: Flow<List<Expense>> = _expenses

    private val expensesMap = mutableListOf<Expense>()
    private val categoriesMap = mutableListOf<Category>()

    override suspend fun getCategoryById(id: Long): Category? = categoriesMap.find { it.id == id }

    override suspend fun insertCategory(category: Category) {
        val newCat = category.copy(id = (categoriesMap.maxOfOrNull { it.id } ?: 0) + 1)
        categoriesMap.add(newCat)
        _categories.value = categoriesMap.filter { it.isActive }
        _allCategories.value = categoriesMap.toList()
    }

    override suspend fun updateCategory(category: Category) {
        val idx = categoriesMap.indexOfFirst { it.id == category.id }
        if (idx >= 0) {
            categoriesMap[idx] = category
            _categories.value = categoriesMap.filter { it.isActive }
            _allCategories.value = categoriesMap.toList()
        }
    }

    override suspend fun deleteCategoryById(id: Long) {
        categoriesMap.removeAll { it.id == id }
        _categories.value = categoriesMap.filter { it.isActive }
        _allCategories.value = categoriesMap.toList()
    }

    override suspend fun reassignExpensesToCategory(oldCategoryId: Long, newCategoryId: Long) {
        expensesMap.forEachIndexed { idx, e ->
            if (e.categoryId == oldCategoryId) {
                expensesMap[idx] = e.copy(categoryId = newCategoryId)
            }
        }
        _expenses.value = expensesMap.toList()
    }

    override suspend fun insertExpense(expense: Expense) {
        val newExpense = if (expense.id == 0L) {
            expense.copy(id = (expensesMap.maxOfOrNull { it.id } ?: 0) + 1)
        } else expense
        expensesMap.add(newExpense)
        _expenses.value = expensesMap.toList()
    }

    override suspend fun updateExpense(expense: Expense) {
        val idx = expensesMap.indexOfFirst { it.id == expense.id }
        if (idx >= 0) {
            expensesMap[idx] = expense
            _expenses.value = expensesMap.toList()
        }
    }

    override suspend fun deleteExpense(expense: Expense) {
        expensesMap.removeAll { it.id == expense.id }
        _expenses.value = expensesMap.toList()
    }

    override suspend fun getExpenseById(id: Long): Expense? = expensesMap.find { it.id == id }

    override suspend fun getExpensesByCategoryId(categoryId: Long): List<Expense> =
        expensesMap.filter { it.categoryId == categoryId }

    override suspend fun getExpenseCountForCategory(categoryId: Long): Int =
        expensesMap.count { it.categoryId == categoryId }

    override suspend fun deleteAllExpenses() {
        expensesMap.clear()
        _expenses.value = emptyList()
    }

    override suspend fun deleteAllCategories() {
        categoriesMap.clear()
        _categories.value = emptyList()
        _allCategories.value = emptyList()
    }

    override fun getExpensesBetweenDates(startDate: Long, endDate: Long): Flow<List<Expense>> {
        return MutableStateFlow(
            expensesMap.filter { it.date in startDate..endDate }
        )
    }

    private val budgetsMap = mutableListOf<MonthlyBudget>()

    override fun getBudgetsForMonth(month: String): Flow<List<MonthlyBudget>> {
        return MutableStateFlow(budgetsMap.filter { it.month == month })
    }

    override suspend fun getBudgetForMonthAndCategory(month: String, categoryId: Long): MonthlyBudget? {
        return budgetsMap.find { it.month == month && it.categoryId == categoryId }
    }

    override suspend fun insertBudget(budget: MonthlyBudget) {
        val existing = budgetsMap.indexOfFirst { it.month == budget.month && it.categoryId == budget.categoryId }
        if (existing >= 0) {
            budgetsMap[existing] = budget
        } else {
            budgetsMap.add(budget)
        }
    }

    override suspend fun deleteBudget(budget: MonthlyBudget) {
        budgetsMap.removeAll { it.id == budget.id }
    }

    override suspend fun deleteBudgetByMonthAndCategory(month: String, categoryId: Long) {
        budgetsMap.removeAll { it.month == month && it.categoryId == categoryId }
    }

    override suspend fun getRecurringExpenses(): List<Expense> {
        return expensesMap.filter { it.isRecurring }
    }
}

class ExpenseViewModelTest {

    private lateinit var repository: FakeBudgetRepository
    private lateinit var viewModel: ExpenseViewModel
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeBudgetRepository()
        viewModel = ExpenseViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `saveExpense inserts expense and updates list`() = runTest {
        repository.insertCategory(Category(name = "Test", icon = "T", color = "#000000"))

        viewModel.saveExpense(Expense(amount = 100.0, date = System.currentTimeMillis(), categoryId = 1, note = "Test expense"))

        testDispatcher.scheduler.advanceUntilIdle()
        val expenses = viewModel.filteredExpenses.value
        assertTrue(expenses.isNotEmpty())
        assertEquals(100.0, expenses.first().amount, 0.001)
    }

    @Test
    fun `deleteExpense removes expense from list`() = runTest {
        repository.insertCategory(Category(name = "Test", icon = "T", color = "#000000"))
        repository.insertExpense(Expense(amount = 100.0, date = System.currentTimeMillis(), categoryId = 1, note = "Test"))

        testDispatcher.scheduler.advanceUntilIdle()
        val saved = viewModel.filteredExpenses.value.first()

        viewModel.deleteExpense(saved)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.filteredExpenses.value.isEmpty())
    }

    @Test
    fun `filteredExpenses filters by category`() = runTest {
        repository.insertCategory(Category(name = "Food", icon = "F", color = "#000000"))
        repository.insertCategory(Category(name = "Transport", icon = "T", color = "#000000"))

        repository.insertExpense(Expense(amount = 50.0, date = System.currentTimeMillis(), categoryId = 1, note = "Food"))
        repository.insertExpense(Expense(amount = 30.0, date = System.currentTimeMillis(), categoryId = 2, note = "Bus"))

        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(2, viewModel.filteredExpenses.value.size)

        viewModel.selectCategory(1)
        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(1, viewModel.filteredExpenses.value.size)
        assertEquals(50.0, viewModel.filteredExpenses.value.first().amount, 0.001)
    }

    @Test
    fun `totalMonthAmount sums correct amount`() = runTest {
        repository.insertCategory(Category(name = "Test", icon = "T", color = "#000000"))
        repository.insertExpense(Expense(amount = 100.0, date = System.currentTimeMillis(), categoryId = 1))
        repository.insertExpense(Expense(amount = 200.0, date = System.currentTimeMillis(), categoryId = 1))

        testDispatcher.scheduler.advanceUntilIdle()
        assertEquals(300.0, viewModel.totalMonthAmount.value, 0.001)
    }

    @Test
    fun `setCurrency updates currency state`() {
        viewModel.setCurrency("EUR")
        assertEquals("EUR", viewModel.currency.value)
    }

    @Test
    fun `searchQuery filters expenses by note`() = runTest {
        repository.insertCategory(Category(name = "Test", icon = "T", color = "#000000"))
        repository.insertExpense(Expense(amount = 50.0, date = System.currentTimeMillis(), categoryId = 1, note = "Courses"))
        repository.insertExpense(Expense(amount = 30.0, date = System.currentTimeMillis(), categoryId = 1, note = "Restaurant"))

        testDispatcher.scheduler.advanceUntilIdle()

        viewModel.setSearchQuery("Courses")
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(1, viewModel.filteredExpenses.value.size)
        assertEquals("Courses", viewModel.filteredExpenses.value.first().note)
    }

    @Test
    fun `toggleSort changes sort order`() {
        assertTrue(viewModel.sortByDateDesc.value)
        viewModel.toggleSort()
        assertFalse(viewModel.sortByDateDesc.value)
    }
}
