package com.devolo.smartbudget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Statistiques",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Surface(
                modifier = Modifier.size(42.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Slate100),
                tonalElevation = 0.dp,
                shadowElevation = 1.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = Slate400,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            item {
                ChartCard(totalAmount = totalAmount, previousTotal = previousTotal)
            }

            if (categoryStats.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "RÉPARTITION PAR CATÉGORIE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }

                items(categoryStats) { stat ->
                    CategoryProgressRow(stat = stat, totalAmount = totalAmount)
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aucune dépense ce mois-ci",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Slate400,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChartCard(totalAmount: Double, previousTotal: Double = 0.0) {
    val percentageChange = if (previousTotal > 0) {
        ((totalAmount - previousTotal) / previousTotal * 100).toInt()
    } else 0
    val isIncrease = percentageChange > 0
    val changeColor = if (isIncrease) Danger else Emerald600
    val changeLabel = if (percentageChange == 0) "0%" else "${if (isIncrease) "+" else ""}$percentageChange%"

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
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
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "%,.0f MAD", totalAmount),
                        fontSize = 22.sp,
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
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                listOf(0.35f, 0.65f, 0.45f, 1f, 0.55f, 0.3f, 0.5f).forEachIndexed { index, height ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(height)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(if (index == 3) Emerald500 else Slate100)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryProgressRow(stat: CategoryStat, totalAmount: Double) {
    val percentage = if (totalAmount > 0) (stat.amount / totalAmount).toFloat() else 0f
    val color = try { Color(stat.category.color.toColorInt()) } catch (_: Exception) { Slate400 }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stat.category.icon, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stat.category.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "%.0f", stat.amount),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Slate900
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Slate100)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(percentage)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(3.dp))
                            .background(color)
                    )
                }
            }
        }
    }
}

data class CategoryStat(val category: Category, val amount: Double)
