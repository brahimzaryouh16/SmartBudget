package com.devolo.smartbudget.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.devolo.smartbudget.data.model.Category
import com.devolo.smartbudget.data.model.Expense
import com.devolo.smartbudget.ui.theme.*
import com.devolo.smartbudget.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(
    viewModel: ExpenseViewModel,
    expenseId: Long,
    onNavigateBack: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()

    var amount by remember { mutableStateOf("0.00") }
    var note by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var paymentMethod by remember { mutableStateOf("Espèce") }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }

    var existingExpense by remember { mutableStateOf<Expense?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(expenseId) {
        if (expenseId != 0L) {
            existingExpense = viewModel.getExpenseById(expenseId)
            existingExpense?.let {
                amount = String.format(Locale.getDefault(), "%.2f", it.amount)
                note = it.note ?: ""
                selectedCategoryId = it.categoryId
                paymentMethod = it.paymentMethod ?: "Espèce"
                date = it.date
            }
        } else if (categories.isNotEmpty()) {
            selectedCategoryId = categories.first().id
        }
    }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp).clickable { onNavigateBack() },
                    shape = RoundedCornerShape(12.dp),
                    color = Slate50
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400, modifier = Modifier.size(20.dp))
                    }
                }
                Text(
                    text = if (expenseId == 0L) "Nouvelle dépense" else "Modifier",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Box(modifier = Modifier.size(40.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 32.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ENTREZ LE MONTANT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Slate400,
                letterSpacing = 1.sp
            )
            
            Row(
                modifier = Modifier.padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "MAD",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate300
                )
                Spacer(modifier = Modifier.width(8.dp))
                BasicTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    textStyle = TextStyle(
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Emerald600,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    cursorBrush = SolidColor(Emerald600),
                    modifier = Modifier.width(IntrinsicSize.Min).widthIn(min = 100.dp)
                )
            }

            if (amountError != null) {
                Text(
                    text = amountError!!,
                    color = Danger,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "CATÉGORIE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(140.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(categories.take(4)) { category ->
                        CategoryOption(
                            category = category,
                            isSelected = selectedCategoryId == category.id,
                            onClick = { selectedCategoryId = category.id }
                        )
                    }
                }
                if (categoryError != null) {
                    Text(
                        text = categoryError!!,
                        color = Danger,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "DATE & NOTE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Slate50,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Slate400, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.FRANCE)
                            Text(text = dateFormat.format(Date(date)).replaceFirstChar { it.uppercase() }, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Slate700)
                        }
                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = Slate100)
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = Slate400, modifier = Modifier.size(20.dp).padding(top = 2.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            BasicTextField(
                                value = note,
                                onValueChange = { note = it },
                                textStyle = TextStyle(fontSize = 14.sp, color = Slate700),
                                modifier = Modifier.fillMaxWidth().height(60.dp),
                                decorationBox = { innerTextField ->
                                    if (note.isEmpty()) {
                                        Text("Ajouter une note facultative...", fontSize = 14.sp, color = Slate300)
                                    }
                                    innerTextField()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    amountError = null
                    categoryError = null
                    val amountValue = amount.replace(",", ".").toDoubleOrNull() ?: 0.0
                    var hasError = false
                    if (amountValue <= 0) {
                        amountError = "Le montant doit être strictement positif"
                        hasError = true
                    }
                    if (selectedCategoryId == null) {
                        categoryError = "Veuillez sélectionner une catégorie"
                        hasError = true
                    }
                    if (!hasError) {
                        val expense = Expense(
                            id = expenseId,
                            amount = amountValue,
                            date = date,
                            categoryId = selectedCategoryId!!,
                            note = note,
                            paymentMethod = paymentMethod,
                            createdAt = existingExpense?.createdAt ?: System.currentTimeMillis()
                        )
                        viewModel.saveExpense(expense)
                        onNavigateBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text("Enregistrer la dépense", fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            
            if (expenseId != 0L) {
                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red.copy(alpha = 0.7f))
                ) {
                    Text("Supprimer cette dépense", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmer la suppression", fontWeight = FontWeight.Bold) },
            text = { Text("Voulez-vous vraiment supprimer cette dépense ? Cette action est irréversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        existingExpense?.let { viewModel.deleteExpense(it) }
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Supprimer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun CategoryOption(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bgColor = if (isSelected) Emerald50 else Slate50
    val borderColor = if (isSelected) Emerald100 else Color.Transparent
    val iconBgColor = if (isSelected) Emerald600 else Slate200
    val textColor = if (isSelected) Emerald600 else Slate700
    val iconTintColor = if (isSelected) Color.White else Slate500

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = RoundedCornerShape(10.dp),
                color = iconBgColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = category.icon, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = category.name,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = textColor,
                maxLines = 1
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddEditExpenseScreenPreview() {
    SmartBudgetTheme {
        val categories = listOf(
            Category(id = 1, name = "Courses", icon = "🛒", color = "#10b981"),
            Category(id = 2, name = "Transport", icon = "🚌", color = "#64748b"),
            Category(id = 3, name = "Loisirs", icon = "🎬", color = "#8b5cf6"),
            Category(id = 4, name = "Santé", icon = "🏥", color = "#ef4444")
        )

        Scaffold(
            containerColor = Color.White,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Slate50
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate400, modifier = Modifier.size(20.dp))
                        }
                    }
                    Text(
                        text = "Nouvelle dépense",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Box(modifier = Modifier.size(40.dp))
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(horizontal = 32.dp)
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "ENTREZ LE MONTANT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    letterSpacing = 1.sp
                )
                
                Row(
                    modifier = Modifier.padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "MAD",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate300
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "450.00",
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color = Emerald600
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "CATÉGORIE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.height(140.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(categories) { category ->
                            CategoryOption(
                                category = category,
                                isSelected = category.id == 1L,
                                onClick = { }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "DATE & NOTE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        color = Slate50,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Slate400, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = "Mercredi, 13 Mai 2026", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Slate700)
                            }
                            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Slate100)
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = Slate400, modifier = Modifier.size(20.dp).padding(top = 2.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Courses Marjane", fontSize = 14.sp, color = Slate700)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = { },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Text("Enregistrer la dépense", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
