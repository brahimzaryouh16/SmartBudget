package com.devolo.smartbudget.data.repository

import com.devolo.smartbudget.data.model.Category
import com.devolo.smartbudget.data.model.Expense
import org.junit.Assert.*
import org.junit.Test

class BudgetRepositoryTest {

    @Test
    fun `category data class stores values correctly`() {
        val category = Category(id = 1, name = "Alimentation", icon = "\uD83C\uDF54", color = "#f59e0b", isActive = true)
        assertEquals(1, category.id)
        assertEquals("Alimentation", category.name)
        assertEquals("\uD83C\uDF54", category.icon)
        assertEquals("#f59e0b", category.color)
        assertTrue(category.isActive)
    }

    @Test
    fun `expense data class stores values correctly`() {
        val expense = Expense(
            id = 1,
            amount = 150.0,
            currency = "MAD",
            date = 1000000L,
            categoryId = 1,
            note = "Test",
            paymentMethod = null
        )
        assertEquals(1, expense.id)
        assertEquals(150.0, expense.amount, 0.001)
        assertEquals("MAD", expense.currency)
        assertEquals(1000000L, expense.date)
        assertEquals(1, expense.categoryId)
        assertEquals("Test", expense.note)
    }

    @Test
    fun `expense copy creates new instance with modified values`() {
        val expense = Expense(amount = 100.0, date = 1000L, categoryId = 1)
        val modified = expense.copy(amount = 200.0, note = "Updated")
        assertEquals(200.0, modified.amount, 0.001)
        assertEquals("Updated", modified.note)
        assertEquals(expense.date, modified.date)
        assertEquals(expense.categoryId, modified.categoryId)
    }
}
