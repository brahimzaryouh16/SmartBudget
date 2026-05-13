package com.devolo.smartbudget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devolo.smartbudget.data.model.Category
import com.devolo.smartbudget.data.model.Expense
import com.devolo.smartbudget.ui.components.BudgetCard
import com.devolo.smartbudget.ui.components.CategoryChip
import com.devolo.smartbudget.ui.components.ExpenseItem
import com.devolo.smartbudget.ui.components.MonthSelector
import com.devolo.smartbudget.ui.theme.*
import com.devolo.smartbudget.ui.viewmodel.ExpenseViewModel
import java.util.Calendar

@Composable
fun ExpensesScreen(
    viewModel: ExpenseViewModel,
    onEditExpense: (Long) -> Unit
) {
    val currentMonth by viewModel.currentMonth.collectAsState()
    val expenses by viewModel.filteredExpenses.collectAsState()
    val totalAmount by viewModel.totalMonthAmount.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val selectedCategoryId by viewModel.selectedCategoryId.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.seedCategoriesIfEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
            .padding(horizontal = 24.dp)
    ) {
        // App Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Bonjour,",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate400
                )
                Text(
                    text = "SmartBudget",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            }
            Surface(
                modifier = Modifier.size(40.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                color = Emerald100
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Emerald600
                    )
                }
            }
        }

        MonthSelector(
            currentMonth = currentMonth,
            onMonthChange = { viewModel.changeMonth(it) }
        )

        Spacer(modifier = Modifier.height(16.dp))

        BudgetCard(
            totalAmount = totalAmount,
            expenseCount = expenses.size
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Filters
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                CategoryChip(
                    name = "Toutes",
                    color = null,
                    isSelected = selectedCategoryId == null,
                    onClick = { viewModel.selectCategory(null) }
                )
            }
            items(categories) { category ->
                CategoryChip(
                    name = category.name,
                    color = category.color,
                    isSelected = selectedCategoryId == category.id,
                    onClick = { viewModel.selectCategory(category.id) }
                )
            }
        }

        if (expenses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (selectedCategoryId == null) "Aucune dépense ce mois-ci" else "Aucun résultat",
                    color = Slate400,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
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
fun ExpensesScreenPreview() {
    SmartBudgetTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate100)
                .padding(horizontal = 24.dp)
        ) {
            // App Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Bonjour,",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate400
                    )
                    Text(
                        text = "SmartBudget",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    color = Emerald100
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AccountBalanceWallet,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = Emerald600
                        )
                    }
                }
            }

            MonthSelector(
                currentMonth = Calendar.getInstance(),
                onMonthChange = { }
            )

            Spacer(modifier = Modifier.height(16.dp))

            BudgetCard(
                totalAmount = 4250.0,
                expenseCount = 12
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Filters
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                item {
                    CategoryChip(
                        name = "Toutes",
                        color = null,
                        isSelected = true,
                        onClick = { }
                    )
                }
                item {
                    CategoryChip(
                        name = "Alimentation",
                        color = "#f59e0b",
                        isSelected = false,
                        onClick = { }
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ExpenseItem(
                        expense = Expense(amount = 450.0, date = System.currentTimeMillis(), categoryId = 1, note = "Courses Marjane"),
                        category = Category(id = 1, name = "Alimentation", icon = "🛒", color = "#f59e0b"),
                        onClick = { }
                    )
                }
                item {
                    ExpenseItem(
                        expense = Expense(amount = 150.0, date = System.currentTimeMillis(), categoryId = 2, note = "Abonnement Bus"),
                        category = Category(id = 2, name = "Transport", icon = "🚌", color = "#3b82f6"),
                        onClick = { }
                    )
                }
            }
        }
    }
}
