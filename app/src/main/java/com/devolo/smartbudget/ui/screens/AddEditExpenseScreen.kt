package com.devolo.smartbudget.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
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
    var showCategorySheet by remember { mutableStateOf(false) }
    val hasUnsavedChanges by remember {
        derivedStateOf {
            val isNew = expenseId == 0L
            val existingAmount = existingExpense?.let { String.format(Locale.US, "%.2f", it.amount) } ?: ""
            val existingNote = existingExpense?.note ?: ""
            val existingCategory = existingExpense?.categoryId

            if (isNew) {
                amount.isNotEmpty() || note.isNotEmpty() || (selectedCategoryId != null && categories.isNotEmpty() && selectedCategoryId != categories.firstOrNull()?.id)
            } else {
                amount != existingAmount || note != existingNote || selectedCategoryId != existingCategory
            }
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
            title = { Text("Modifications non enregistrées", style = MaterialTheme.typography.titleLarge) },
            text = { Text("Voulez-vous vraiment quitter ? Vos modifications seront perdues.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnsavedDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier
                        .size(44.dp)
                        .clickable(onClick = {
                            if (hasUnsavedChanges) showUnsavedDialog = true
                            else onNavigateBack()
                        }),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shadowElevation = 0.5.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
                    }
                }
                Text(
                    text = if (expenseId == 0L) "Nouvelle dépense" else "Modifier la dépense",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Box(modifier = Modifier.size(44.dp))
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
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "MONTANT",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BasicTextField(
                            value = amount,
                            onValueChange = { sanitizeAmountInput(it, amount)?.let { amount = it } },
                            textStyle = TextStyle(
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            ),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            singleLine = true,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            modifier = Modifier.fillMaxWidth(),
                            decorationBox = { innerTextField ->
                                if (amount.isEmpty()) {
                                    Text(
                                        text = "0,00",
                                        style = TextStyle(
                                            fontSize = 48.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                            textAlign = TextAlign.Center
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = currency,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 2.sp
                        )
                    }
                }

                if (amountError != null) {
                    Text(
                        text = amountError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(36.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "CATÉGORIE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val selectedCategory = categories.find { it.id == selectedCategoryId }

                    Surface(
                        onClick = {
                            if (categories.isNotEmpty()) showCategorySheet = true
                        },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (selectedCategory != null) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = (try {
                                        Color(android.graphics.Color.parseColor(selectedCategory.color))
                                    } catch (_: Exception) { MaterialTheme.colorScheme.primary }).copy(alpha = 0.15f)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = selectedCategory.icon, fontSize = 22.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = selectedCategory.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.UnfoldMore,
                                    contentDescription = "Changer",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(44.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Category,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(
                                    text = "Sélectionner une catégorie",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.UnfoldMore,
                                    contentDescription = "Sélectionner",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    if (categoryError != null) {
                        Text(
                            text = categoryError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "DATE & NOTE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showDatePicker = true },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "Changer la date", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.FRANCE)
                                Text(
                                    text = dateFormat.format(Date(date))
                                        .replaceFirstChar { it.uppercase() },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
                            }
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                thickness = 0.5.dp
                            )
                            Row(verticalAlignment = Alignment.Top) {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp).padding(top = 2.dp))
                                Spacer(modifier = Modifier.width(16.dp))
                                BasicTextField(
                                    value = note,
                                    onValueChange = { note = it },
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
                                    modifier = Modifier.fillMaxWidth(),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                                    singleLine = true,
                                    decorationBox = { innerTextField ->
                                        if (note.isEmpty()) {
                                            Text("Ajouter une note facultative...", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                                        }
                                        innerTextField()
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            Column(modifier = Modifier.padding(bottom = 16.dp)) {
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
                                createdAt = existingExpense?.createdAt ?: System.currentTimeMillis()
                            )
                            viewModel.saveExpense(expense)
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurface),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text("Enregistrer", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                if (expenseId != 0L) {
                    TextButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Supprimer cette dépense", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
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
            title = { Text("Confirmer la suppression", style = MaterialTheme.typography.titleLarge) },
            text = { Text("Voulez-vous vraiment supprimer cette dépense ? Cette action est irréversible.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        existingExpense?.let { viewModel.deleteExpense(it) }
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
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

    if (showCategorySheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showCategorySheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = "Choisir une catégorie",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                categories.forEach { category ->
                    val catColor = try {
                        Color(android.graphics.Color.parseColor(category.color))
                    } catch (_: Exception) { MaterialTheme.colorScheme.primary }

                    Surface(
                        onClick = {
                            selectedCategoryId = category.id
                            showCategorySheet = false
                        },
                        shape = RoundedCornerShape(14.dp),
                        color = if (selectedCategoryId == category.id) catColor.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        border = if (selectedCategoryId == category.id)
                                androidx.compose.foundation.BorderStroke(1.dp, catColor.copy(alpha = 0.3f))
                                else null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(10.dp),
                                color = if (selectedCategoryId == category.id) catColor
                                        else MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = category.icon,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = if (selectedCategoryId == category.id) FontWeight.Bold else FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                            if (selectedCategoryId == category.id) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Sélectionné",
                                    tint = catColor,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
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
