package com.devolo.smartbudget.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devolo.smartbudget.data.model.Expense
import com.devolo.smartbudget.ui.components.*
import com.devolo.smartbudget.ui.theme.*
import com.devolo.smartbudget.ui.viewmodel.ExpenseViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
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
    val previousTotal by viewModel.previousMonthTotal.collectAsState()
    val sortByDateDesc by viewModel.sortByDateDesc.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val budgetLimit by viewModel.monthlyBudget.collectAsState()
    val budgetProgress by viewModel.budgetProgress.collectAsState()
    val isOverBudget by viewModel.isOverBudget.collectAsState()

    var showSearch by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.seedCategoriesIfEmpty()
    }

    val pullToRefreshState = rememberPullToRefreshState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
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
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                modifier = Modifier.size(42.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = if (showSearch) Emerald100 else MaterialTheme.colorScheme.surface,
                                border = if (showSearch) null else androidx.compose.foundation.BorderStroke(1.dp, Slate100),
                                tonalElevation = 0.dp,
                                shadowElevation = 1.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    IconButton(onClick = { showSearch = !showSearch }) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Rechercher",
                                            modifier = Modifier.size(22.dp),
                                            tint = if (showSearch) Emerald600 else Slate400
                                        )
                                    }
                                }
                            }
                            Surface(
                                modifier = Modifier.size(42.dp),
                                shape = RoundedCornerShape(14.dp),
                                color = Emerald50
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalanceWallet,
                                        contentDescription = "Portefeuille",
                                        modifier = Modifier.size(22.dp),
                                        tint = Emerald600
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = showSearch,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Column {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { viewModel.setSearchQuery(it) },
                                placeholder = { Text("Rechercher par note ou montant...", fontSize = 13.sp) },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp), tint = Slate400) },
                                trailingIcon = {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                            Icon(Icons.Default.Close, contentDescription = "Effacer", modifier = Modifier.size(18.dp), tint = Slate400)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Emerald600,
                                    unfocusedBorderColor = Slate200,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                ),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    MonthSelector(
                        currentMonth = currentMonth,
                        onMonthChange = { viewModel.changeMonth(it) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (isLoading) {
                        ShimmerCard()
                    } else {
                        BudgetCard(
                            totalAmount = totalAmount,
                            expenseCount = expenses.size,
                            currency = currency,
                            previousTotal = previousTotal,
                            budgetLimit = budgetLimit,
                            budgetProgress = budgetProgress,
                            isOverBudget = isOverBudget
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Dépenses",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Slate700
                        )
                        TextButton(
                            onClick = { viewModel.toggleSort() },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (sortByDateDesc) Icons.Default.SortByAlpha else Icons.Default.AttachMoney,
                                contentDescription = "Trier",
                                modifier = Modifier.size(16.dp),
                                tint = Emerald600
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (sortByDateDesc) "Date" else "Montant",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Emerald600
                            )
                        }
                    }

                    if (isLoading) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 4.dp)
                        ) {
                            items(4) {
                                ShimmerCategoryChip()
                            }
                        }
                    } else {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 4.dp)
                        ) {
                            item {
                                CategoryChip(
                                    name = "Toutes",
                                    isSelected = selectedCategoryId == null,
                                    onClick = { viewModel.selectCategory(null) }
                                )
                            }
                            items(categories) { category ->
                                val catColor = try {
                                    Color(android.graphics.Color.parseColor(category.color))
                                } catch (_: Exception) { null }
                                CategoryChip(
                                    name = category.name,
                                    isSelected = selectedCategoryId == category.id,
                                    onClick = { viewModel.selectCategory(category.id) },
                                    color = catColor
                                )
                            }
                        }
                    }
                }

                if (isLoading) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(5) {
                            ShimmerExpenseItem()
                        }
                    }
                } else if (expenses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Inbox,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Slate300
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (searchQuery.isNotBlank()) "Aucun résultat pour \"$searchQuery\""
                                       else if (selectedCategoryId == null) "Aucune dépense ce mois-ci"
                                       else "Aucun résultat",
                                color = Slate400,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(expenses, key = { it.id }) { expense ->
                            val category = categories.find { it.id == expense.categoryId }
                            ExpenseItem(
                                expense = expense,
                                category = category,
                                onClick = { onEditExpense(expense.id) },
                                onSwipeDelete = {
                                    viewModel.deleteExpense(expense)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
