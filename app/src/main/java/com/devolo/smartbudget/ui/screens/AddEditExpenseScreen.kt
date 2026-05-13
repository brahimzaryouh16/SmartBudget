package com.devolo.smartbudget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devolo.smartbudget.data.model.Category
import com.devolo.smartbudget.data.model.Expense
import com.devolo.smartbudget.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch
import java.util.*

import androidx.compose.ui.tooling.preview.Preview
import com.devolo.smartbudget.ui.theme.SmartBudgetTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(
    viewModel: ExpenseViewModel,
    expenseId: Long,
    onNavigateBack: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val scope = rememberCoroutineScope()

    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var paymentMethod by remember { mutableStateOf("Espèce") }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }

    var existingExpense by remember { mutableStateOf<Expense?>(null) }

    LaunchedEffect(expenseId) {
        if (expenseId != 0L) {
            existingExpense = viewModel.getExpenseById(expenseId)
            existingExpense?.let {
                amount = it.amount.toString()
                note = it.note ?: ""
                selectedCategoryId = it.categoryId
                paymentMethod = it.paymentMethod ?: "Espèce"
                date = it.date
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (expenseId == 0L) "Nouvelle dépense" else "Modifier la dépense") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Amount Input
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it },
                label = { Text("Montant") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("MAD ") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Category Selection
            Text("Catégorie *", style = MaterialTheme.typography.labelLarge)
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier.height(200.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { category ->
                    CategoryOption(
                        category = category,
                        isSelected = selectedCategoryId == category.id,
                        onClick = { selectedCategoryId = category.id }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Note Input
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optionnel)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Payment Method
            Text("Méthode de paiement", style = MaterialTheme.typography.labelLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Espèce", "Carte", "Virement").forEach { method ->
                    FilterChip(
                        selected = paymentMethod == method,
                        onClick = { paymentMethod = method },
                        label = { Text(method) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    if (amountValue > 0 && selectedCategoryId != null) {
                        val expense = Expense(
                            id = expenseId,
                            amount = amountValue,
                            date = date,
                            categoryId = selectedCategoryId!!,
                            note = note,
                            paymentMethod = paymentMethod
                        )
                        viewModel.saveExpense(expense)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Enregistrer")
            }
            
            if (expenseId != 0L) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = {
                        existingExpense?.let { viewModel.deleteExpense(it) }
                        onNavigateBack()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Supprimer")
                }
            }
        }
    }
}

@Composable
fun CategoryOption(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val categoryColor = try { Color(android.graphics.Color.parseColor(category.color)) } catch (e: Exception) { Color.Gray }
    
    Column(
        modifier = Modifier
            .border(
                width = 2.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(14.dp)
            )
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = category.icon, fontSize = 24.sp)
        Text(
            text = category.name,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AddEditExpenseScreenPreview() {
    SmartBudgetTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = "85.00",
                onValueChange = {},
                label = { Text("Montant") },
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("MAD ") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Catégorie *", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CategoryOption(
                    category = Category(name = "Alimentation", icon = "🍔", color = "#f59e0b"),
                    isSelected = true,
                    onClick = {}
                )
                CategoryOption(
                    category = Category(name = "Transport", icon = "🚌", color = "#3b82f6"),
                    isSelected = false,
                    onClick = {}
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = "Déjeuner avec amis",
                onValueChange = {},
                label = { Text("Note (optionnel)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Enregistrer")
            }
        }
    }
}
