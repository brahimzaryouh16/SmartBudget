package com.devolo.smartbudget.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.devolo.smartbudget.ui.components.BudgetCard
import com.devolo.smartbudget.ui.components.ExpenseItem
import com.devolo.smartbudget.ui.components.MonthSelector
import com.devolo.smartbudget.ui.viewmodel.ExpenseViewModel

import androidx.compose.ui.tooling.preview.Preview
import com.devolo.smartbudget.data.model.Category
import com.devolo.smartbudget.data.model.Expense
import com.devolo.smartbudget.ui.theme.SmartBudgetTheme

@Composable
fun ExpensesScreen(
    viewModel: ExpenseViewModel,
    onEditExpense: (Long) -> Unit
) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val expenses by viewModel.filteredExpenses.collectAsState()
    val totalAmount by viewModel.totalMonthAmount.collectAsState()
    val categories by viewModel.categories.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.seedCategoriesIfEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        MonthSelector(
            currentMonth = currentMonth,
            onMonthChange = { viewModel.changeMonth(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        BudgetCard(
            totalAmount = totalAmount,
            expenseCount = expenses.size
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (expenses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Aucune dépense ce mois-ci")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(expenses) { expense ->
                    val category = categories.find { it.id == expense.categoryId }
                    ExpenseItem(
                        expense = expense,
                        category = category,
                        onClick = { onEditExpense(expense.id) }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ExpensesScreenContentPreview() {
    SmartBudgetTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            MonthSelector(
                currentMonth = java.util.Calendar.getInstance(),
                onMonthChange = {}
            )
            Spacer(modifier = Modifier.height(16.dp))
            BudgetCard(
                totalAmount = 2450.0,
                expenseCount = 32
            )
            Spacer(modifier = Modifier.height(16.dp))
            ExpenseItem(
                expense = Expense(amount = 85.0, date = System.currentTimeMillis(), categoryId = 1, note = "Déjeuner avec amis"),
                category = Category(id = 1, name = "Alimentation", icon = "🍔", color = "#f59e0b"),
                onClick = {}
            )
        }
    }
}
