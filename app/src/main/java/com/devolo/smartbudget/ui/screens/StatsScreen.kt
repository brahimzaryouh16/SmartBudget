package com.devolo.smartbudget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.compose.ui.tooling.preview.Preview
import com.devolo.smartbudget.data.model.Category
import com.devolo.smartbudget.data.model.Expense
import com.devolo.smartbudget.ui.theme.*
import com.devolo.smartbudget.ui.viewmodel.ExpenseViewModel
import java.util.*

@Composable
fun StatsScreen(viewModel: ExpenseViewModel) {
    val expenses by viewModel.filteredExpenses.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val totalAmount by viewModel.totalMonthAmount.collectAsState()
    val previousTotal by viewModel.previousMonthTotal.collectAsState()

    val categoryStats = categories.map { category ->
        val amount = expenses.filter { it.categoryId == category.id }.sumOf { it.amount }
        CategoryStat(category, amount)
    }.filter { it.amount > 0 }.sortedByDescending { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
            .padding(horizontal = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Statistiques",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate100),
                shadowElevation = 1.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Slate400, modifier = Modifier.size(20.dp))
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item {
                ChartCard(totalAmount = totalAmount, previousTotal = previousTotal)
            }

            item {
                Text(
                    text = "PAR CATÉGORIE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            items(categoryStats) { stat ->
                CategoryProgressRow(stat = stat, totalAmount = totalAmount)
            }
        }
    }
}

@Composable
fun ChartCard(totalAmount: Double, previousTotal: Double = 0.0) {
    val percentageChange = if (previousTotal > 0) ((totalAmount - previousTotal) / previousTotal * 100).toInt() else 0
    val isIncrease = percentageChange > 0
    val changeColor = if (isIncrease) Danger else Emerald600
    val changeLabel = if (percentageChange == 0) "0%" else "${if (isIncrease) "+" else ""}$percentageChange%"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate100),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "TOTAL DU MOIS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%,.0f MAD", totalAmount),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }
                if (previousTotal > 0) {
                    Surface(
                        color = if (isIncrease) Danger.copy(alpha = 0.1f) else Emerald50,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = changeLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = changeColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Simulated Bar Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                listOf(0.4f, 0.7f, 0.3f, 1f, 0.5f, 0.2f, 0.6f).forEachIndexed { index, height ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .fillMaxHeight(height)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .background(if (index == 3) Emerald600 else Slate100)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryProgressRow(stat: CategoryStat, totalAmount: Double) {
    val percentage = if (totalAmount > 0) (stat.amount / totalAmount).toFloat() else 0f
    val color = try { Color(stat.category.color.toColorInt()) } catch (e: Exception) { Slate400 }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate50)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = stat.category.icon, fontSize = 20.sp)
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(text = stat.category.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
                    Text(text = String.format(Locale.getDefault(), "%.0f", stat.amount), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Slate900)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Slate100)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percentage)
                            .fillMaxHeight()
                            .background(color)
                    )
                }
            }
        }
    }
}

data class CategoryStat(val category: Category, val amount: Double)

@Preview(showBackground = true)
@Composable
fun StatsScreenPreview() {
    SmartBudgetTheme {
        val categories = listOf(
            Category(id = 1, name = "Alimentation", icon = "🛒", color = "#f97316"),
            Category(id = 2, name = "Transport", icon = "🚌", color = "#3b82f6")
        )
        val expenses = listOf(
            Expense(amount = 1850.0, categoryId = 1, date = System.currentTimeMillis()),
            Expense(amount = 1190.0, categoryId = 2, date = System.currentTimeMillis())
        )
        
        val categoryStats = categories.map { category ->
            val amount = expenses.filter { it.categoryId == category.id }.sumOf { it.amount }
            CategoryStat(category, amount)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Slate100)
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Statistiques",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Slate100),
                    shadowElevation = 1.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Slate400, modifier = Modifier.size(20.dp))
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                item {
                    ChartCard(totalAmount = 3040.0)
                }

                item {
                    Text(
                        text = "PAR CATÉGORIE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                items(categoryStats) { stat ->
                    CategoryProgressRow(stat = stat, totalAmount = 3040.0)
                }
            }
        }
    }
}
