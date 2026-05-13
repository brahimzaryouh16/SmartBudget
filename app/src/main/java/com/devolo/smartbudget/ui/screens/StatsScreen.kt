package com.devolo.smartbudget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devolo.smartbudget.data.model.Category
import com.devolo.smartbudget.data.model.Expense
import com.devolo.smartbudget.ui.viewmodel.ExpenseViewModel

import androidx.compose.ui.tooling.preview.Preview
import com.devolo.smartbudget.ui.theme.SmartBudgetTheme

@Composable
fun StatsScreen(viewModel: ExpenseViewModel) {
    val expenses by viewModel.filteredExpenses.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val totalAmount by viewModel.totalMonthAmount.collectAsState()

    val categoryStats = categories.map { category ->
        val amount = expenses.filter { it.categoryId == category.id }.sumOf { it.amount }
        CategoryStat(category, amount)
    }.filter { it.amount > 0 }.sortedByDescending { it.amount }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Statistiques",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Dépensé ce mois", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = "${String.format("%.2f", totalAmount)} MAD",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            Text(
                text = "Répartition par catégorie",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(categoryStats) { stat ->
            CategoryStatRow(stat = stat, totalAmount = totalAmount)
        }
    }
}

data class CategoryStat(val category: Category, val amount: Double)

@Composable
fun CategoryStatRow(stat: CategoryStat, totalAmount: Double) {
    val percentage = if (totalAmount > 0) (stat.amount / totalAmount).toFloat() else 0f
    val color = try { Color(android.graphics.Color.parseColor(stat.category.color)) } catch (e: Exception) { Color.Gray }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(color.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = stat.category.icon)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = stat.category.name, fontWeight = FontWeight.SemiBold)
                Text(text = "${String.format("%.2f", stat.amount)} MAD", fontWeight = FontWeight.Bold)
            }
            
            LinearProgressIndicator(
                progress = { percentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                color = color,
                trackColor = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatsScreenPreview() {
    SmartBudgetTheme {
        val categories = listOf(
            Category(id = 1, name = "Logement", icon = "🏠", color = "#8b5cf6"),
            Category(id = 2, name = "Transport", icon = "🚌", color = "#3b82f6"),
            Category(id = 3, name = "Alimentation", icon = "🍔", color = "#f59e0b")
        )
        val stats = listOf(
            CategoryStat(categories[0], 1200.0),
            CategoryStat(categories[1], 450.0),
            CategoryStat(categories[2], 320.0)
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Statistiques",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Dépensé ce mois", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "1970.00 MAD",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            items(stats) { stat ->
                CategoryStatRow(stat = stat, totalAmount = 1970.0)
            }
        }
    }
}
