package com.devolo.smartbudget.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val currency by viewModel.currency.collectAsState()
    val focusManager = LocalFocusManager.current

    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var date by remember { mutableStateOf(System.currentTimeMillis()) }

    var existingExpense by remember { mutableStateOf<Expense?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf<String?>(null) }
    var categoryError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showUnsavedDialog by remember { mutableStateOf(false) }

    val hasUnsavedChanges by remember {
        derivedStateOf {
            val amountValue = amount.replace(",", ".").toDoubleOrNull() ?: 0.0
            val isNew = expenseId == 0L
            val existingAmount = existingExpense?.let { String.format(Locale.US, "%.2f", it.amount) } ?: ""
            amount.isNotEmpty() || note.isNotEmpty() ||
                (isNew && selectedCategoryId != null) ||
                (!isNew && (amount != existingAmount || note != (existingExpense?.note ?: "")))
        }
    }

    BackHandler(enabled = hasUnsavedChanges) {
        showUnsavedDialog = true
    }

    LaunchedEffect(expenseId) {
        if (expenseId != 0L) {
            existingExpense = viewModel.getExpenseById(expenseId)
            existingExpense?.let {
                amount = String.format(Locale.US, "%.2f", it.amount)
                note = it.note ?: ""
                selectedCategoryId = it.categoryId
                date = it.date
            }
        } else if (categories.isNotEmpty()) {
            selectedCategoryId = categories.first().id
        }
    }

    if (showUnsavedDialog) {
        AlertDialog(
            onDismissRequest = { showUnsavedDialog = false },
            title = { Text("Modifications non enregistrées", fontWeight = FontWeight.Bold) },
            text = { Text("Voulez-vous vraiment quitter ? Vos modifications seront perdues.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnsavedDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Danger)
                ) {
                    Text("Quitter", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnsavedDialog = false }) {
                    Text("Continuer")
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = {
                            if (hasUnsavedChanges) showUnsavedDialog = true
                            else onNavigateBack()
                        }),
                    shape = RoundedCornerShape(12.dp),
                    color = Slate50
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = Slate500, modifier = Modifier.size(20.dp))
                    }
                }
                Text(
                    text = if (expenseId == 0L) "Nouvelle dépense" else "Modifier",
                    fontSize = 17.sp,
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
                .padding(horizontal = 20.dp)
                .fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "MONTANT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate400,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = currency,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate300
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    BasicTextField(
                        value = amount,
                        onValueChange = { sanitizeAmountInput(it, amount)?.let { amount = it } },
                        textStyle = TextStyle(
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = Emerald600,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        singleLine = true,
                        cursorBrush = SolidColor(Emerald600),
                        modifier = Modifier.widthIn(min = 90.dp)
                    )
                }

                if (amountError != null) {
                    Text(
                        text = amountError!!,
                        color = Danger,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "CATÉGORIE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    if (categories.isEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            color = Amber50
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Amber600, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Aucune catégorie active. Activez-en dans les Réglages.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Slate700
                                )
                            }
                        }
                    } else {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            val rows = categories.chunked(2)
                            rows.forEach { row ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    row.forEach { category ->
                                        CategoryOption(
                                            category = category,
                                            isSelected = selectedCategoryId == category.id,
                                            onClick = { selectedCategoryId = category.id },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (row.size < 2) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                    if (categoryError != null) {
                        Text(
                            text = categoryError!!,
                            color = Danger,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "DATE & NOTE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = Slate50,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDatePicker = true },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Changer la date", tint = Slate400, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.FRANCE)
                                Text(
                                    text = dateFormat.format(Date(date))
                                        .replaceFirstChar { it.uppercase() },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Slate700
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.EditCalendar, contentDescription = "Modifier la date", tint = Slate300, modifier = Modifier.size(16.dp))
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = Slate200,
                                thickness = 0.5.dp
                            )
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = Slate400, modifier = Modifier.size(18.dp).padding(top = 2.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                BasicTextField(
                                    value = note,
                                    onValueChange = { note = it },
                                    textStyle = TextStyle(fontSize = 14.sp, color = Slate700),
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                    singleLine = true,
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

                Spacer(modifier = Modifier.height(24.dp))
            }

            Column {
                Button(
                    onClick = {
                        amountError = null
                        categoryError = null
                        val amountValue = amount.replace(",", ".").toDoubleOrNull() ?: 0.0
                        var hasError = false
                        if (amountValue <= 0) {
                            amountError = "Le montant doit être strictement positif"
                            hasError = true
                        } else if (amountValue > 999_999_999.0) {
                            amountError = "Le montant est trop élevé"
                            hasError = true
                        }
                        if (selectedCategoryId == null && categories.isNotEmpty()) {
                            categoryError = "Veuillez sélectionner une catégorie"
                            hasError = true
                        }
                        if (!hasError) {
                            focusManager.clearFocus()
                            val expense = Expense(
                                id = expenseId,
                                amount = amountValue,
                                currency = currency,
                                date = date,
                                categoryId = selectedCategoryId ?: categories.first().id,
                                note = note,
                                paymentMethod = null,
                                createdAt = existingExpense?.createdAt ?: System.currentTimeMillis()
                            )
                            viewModel.saveExpense(expense)
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate900),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text("Enregistrer la dépense", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }

                if (expenseId != 0L) {
                    TextButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, bottom = 8.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.Red.copy(alpha = 0.65f))
                    ) {
                        Text("Supprimer cette dépense", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { date = it }
                    showDatePicker = false
                }) {
                    Text("Confirmer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annuler")
                }
            }
        ) {
            DatePicker(state = datePickerState)
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

private fun sanitizeAmountInput(newValue: String, currentValue: String): String? {
    if (newValue.isEmpty()) return ""
    val filtered = newValue.filter { it.isDigit() || it == '.' || it == ',' }
    if (filtered != newValue) return null
    val dotCount = filtered.count { it == '.' }
    val commaCount = filtered.count { it == ',' }
    if (dotCount > 1 || commaCount > 1 || (dotCount > 0 && commaCount > 0)) return null
    val normalized = filtered.replace(",", ".")
    if (normalized.startsWith(".")) return "0$normalized"
    val parts = normalized.split(".")
    if (parts.size == 2 && parts[1].length > 2) return currentValue
    return filtered
}

@Composable
fun CategoryOption(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(category.color))
    } catch (_: Exception) { Emerald600 }
    val bgColor = if (isSelected) categoryColor.copy(alpha = 0.1f) else Slate50
    val borderColor = if (isSelected) categoryColor.copy(alpha = 0.3f) else Color.Transparent
    val iconBgColor = if (isSelected) categoryColor else Slate200
    val textColor = if (isSelected) categoryColor else Slate700
    val iconTintColor = if (isSelected) Color.White else Slate500

    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(30.dp),
                shape = RoundedCornerShape(10.dp),
                color = iconBgColor
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = category.icon, fontSize = 15.sp)
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
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
