package com.devolo.smartbudget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.devolo.smartbudget.data.model.Expense
import com.devolo.smartbudget.ui.components.CategoryChip
import com.devolo.smartbudget.ui.components.ExpenseItem
import com.devolo.smartbudget.ui.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFilterScreen(
    viewModel: ExpenseViewModel,
    onEditExpense: (Long) -> Unit,
    onNavigateBack: () -> Unit
) {
    val allExpenses by viewModel.allExpenses.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var localSearchQuery by remember { mutableStateOf("") }
    var localSelectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var localSortByDateDesc by remember { mutableStateOf(true) }

    val filteredExpenses = remember(allExpenses, localSearchQuery, localSelectedCategoryId, localSortByDateDesc) {
        allExpenses
            .filter { expense ->
                val matchesCategory = localSelectedCategoryId == null || expense.categoryId == localSelectedCategoryId
                val matchesSearch = localSearchQuery.isBlank() ||
                    (expense.note?.contains(localSearchQuery, ignoreCase = true) == true) ||
                    expense.amount.toString().contains(localSearchQuery, ignoreCase = true)
                matchesCategory && matchesSearch
            }
            .let { list ->
                if (localSortByDateDesc) list.sortedByDescending { it.date }
                else list.sortedByDescending { it.amount }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
            }
            Text(
                text = "Recherche & Filtres",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = localSearchQuery,
            onValueChange = { localSearchQuery = it },
            placeholder = { Text("Rechercher par note ou montant...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (localSearchQuery.isNotEmpty()) {
                    IconButton(onClick = { localSearchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Effacer")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dépenses",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(onClick = { localSortByDateDesc = !localSortByDateDesc }) {
                Icon(
                    imageVector = if (localSortByDateDesc) Icons.Default.Sort else Icons.Default.AttachMoney,
                    contentDescription = "Trier",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (localSortByDateDesc) "Date" else "Montant"
                )
            }
        }

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            item {
                CategoryChip(
                    name = "Toutes",
                    isSelected = localSelectedCategoryId == null,
                    onClick = { localSelectedCategoryId = null }
                )
            }
            items(categories) { category ->
                val catColor = try {
                    Color(android.graphics.Color.parseColor(category.color))
                } catch (_: Exception) { null }
                CategoryChip(
                    name = category.name,
                    isSelected = localSelectedCategoryId == category.id,
                    onClick = { localSelectedCategoryId = category.id },
                    color = catColor
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredExpenses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (localSearchQuery.isNotBlank()) "Aucun résultat pour \"$localSearchQuery\""
                               else "Aucune dépense",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredExpenses, key = { it.id }) { expense ->
                    val category = categories.find { it.id == expense.categoryId }
                    ExpenseItem(
                        expense = expense,
                        category = category,
                        onClick = { onEditExpense(expense.id) },
                        onSwipeDelete = { viewModel.deleteExpense(expense) }
                    )
                }
            }
        }
    }
}
