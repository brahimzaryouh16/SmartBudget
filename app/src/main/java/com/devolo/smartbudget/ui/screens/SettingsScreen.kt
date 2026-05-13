package com.devolo.smartbudget.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devolo.smartbudget.data.model.Category
import com.devolo.smartbudget.ui.theme.*
import com.devolo.smartbudget.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: ExpenseViewModel) {
    val allCategories by viewModel.allCategoriesIncludingInactive.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val context = LocalContext.current

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showDeleteCategoryDialog by remember { mutableStateOf<Category?>(null) }
    var showSeedDialog by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showExportDateRangeDialog by remember { mutableStateOf(false) }
    var showCategoryBudgetSheet by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<Category?>(null) }

    var pendingExportStart by remember { mutableStateOf(0L) }
    var pendingExportEnd by remember { mutableStateOf(0L) }

    val csvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        uri?.let {
            val csvContent = viewModel.buildCsvContent()
            try {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(csvContent.toByteArray())
                }
                Toast.makeText(context, "CSV exporté avec succès", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Erreur d'export: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val dateRangeCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv"),
    ) { uri ->
        uri?.let {
            val csvContent = viewModel.buildCsvContentForDateRangeSync(pendingExportStart, pendingExportEnd)
            try {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(csvContent.toByteArray())
                }
                Toast.makeText(context, "CSV exporté avec succès", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Erreur d'export: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val csvImportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val content = inputStream.bufferedReader().readText()
                    viewModel.importCsvContent(content)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erreur d'import: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Réglages",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {

            item { SectionHeader(title = "GÉNÉRAL") }

            item {
                SettingsClickableItem(
                    icon = Icons.Default.Category,
                    iconBg = MaterialTheme.colorScheme.primaryContainer,
                    iconColor = MaterialTheme.colorScheme.primary,
                    title = "Catégories métier",
                    subtitle = "${allCategories.size} catégories disponibles",
                    onClick = { showCategorySheet = true }
                )
            }

            item {
                SettingsClickableItem(
                    icon = Icons.Default.AttachMoney,
                    iconBg = MaterialTheme.colorScheme.secondaryContainer,
                    iconColor = MaterialTheme.colorScheme.secondary,
                    title = "Devise par défaut",
                    trailing = { Text(currency, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    onClick = { showCurrencyDialog = true }
                )
            }

            item {
                SettingsClickableItem(
                    icon = Icons.Default.AccountBalance,
                    iconBg = Emerald50,
                    iconColor = Success,
                    title = "Budget mensuel",
                    subtitle = if (monthlyBudget > 0) "${String.format(Locale.getDefault(), "%.0f", monthlyBudget)} $currency/mois"
                              else "Non défini",
                    onClick = { showBudgetDialog = true }
                )
            }

            item {
                SettingsClickableItem(
                    icon = Icons.Default.BarChart,
                    iconBg = Purple50,
                    iconColor = Purple600,
                    title = "Budgets par catégorie",
                    subtitle = "Limites mensuelles personnalisées",
                    onClick = { showCategoryBudgetSheet = true }
                )
            }

            item { SectionHeader(title = "DONNÉES & EXPORT") }

            item {
                SettingsClickableItem(
                    icon = Icons.Default.FileDownload,
                    iconBg = Emerald50,
                    iconColor = Success,
                    title = "Exporter en CSV",
                    subtitle = "Dépenses du mois en cours",
                    onClick = { csvLauncher.launch("smartbudget_export.csv") }
                )
            }

            item {
                SettingsClickableItem(
                    icon = Icons.Default.DateRange,
                    iconBg = InfoLight,
                    iconColor = Info,
                    title = "Exporter par période",
                    subtitle = "Choisir une date de début et fin",
                    onClick = { showExportDateRangeDialog = true }
                )
            }

            item {
                SettingsClickableItem(
                    icon = Icons.Default.FileUpload,
                    iconBg = InfoLight,
                    iconColor = Info,
                    title = "Importer un CSV",
                    subtitle = "Importer des dépenses depuis un fichier CSV",
                    onClick = { csvImportLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*")) }
                )
            }

            item {
                SettingsClickableItem(
                    icon = Icons.Default.Storage,
                    iconBg = WarningLight,
                    iconColor = Warning,
                    title = "Charger données de démo",
                    subtitle = "35 dépenses sur 2 mois",
                    onClick = { showSeedDialog = true }
                )
            }

            item {
                SettingsClickableItem(
                    icon = Icons.Default.DeleteForever,
                    iconBg = DangerLight,
                    iconColor = MaterialTheme.colorScheme.error,
                    title = "Réinitialiser les données",
                    subtitle = "Supprime toutes les dépenses et catégories",
                    onClick = { showResetDialog = true }
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = "SmartBudget v1.1.0",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showCurrencyDialog) {
        CurrencyPickerDialog(
            currentCurrency = currency,
            onSelect = { viewModel.setCurrency(it); showCurrencyDialog = false },
            onDismiss = { showCurrencyDialog = false }
        )
    }

    if (showBudgetDialog) {
        BudgetSettingDialog(
            currentBudget = monthlyBudget,
            currency = currency,
            onSave = { viewModel.setMonthlyBudget(it); showBudgetDialog = false },
            onDismiss = { showBudgetDialog = false }
        )
    }

    if (showExportDateRangeDialog) {
        ExportDateRangeDialog(
            onExport = { start, end ->
                showExportDateRangeDialog = false
                pendingExportStart = start
                pendingExportEnd = end
                val dateFormat = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                dateRangeCsvLauncher.launch("smartbudget_${dateFormat.format(Date(start))}_${dateFormat.format(Date(end))}.csv")
            },
            onDismiss = { showExportDateRangeDialog = false }
        )
    }

    if (showCategoryBudgetSheet) {
        CategoryBudgetSheet(
            categories = allCategories.filter { it.isActive },
            currency = currency,
            viewModel = viewModel,
            onDismiss = { showCategoryBudgetSheet = false }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Réinitialiser", style = MaterialTheme.typography.titleLarge) },
            text = { Text("Voulez-vous vraiment supprimer toutes les données ? Cette action est irréversible.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetAllData()
                    showResetDialog = false
                }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Réinitialiser", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Annuler") }
            }
        )
    }

    if (showSeedDialog) {
        AlertDialog(
            onDismissRequest = { showSeedDialog = false },
            title = { Text("Données de démonstration", style = MaterialTheme.typography.titleLarge) },
            text = { Text("Cela va ajouter 35 dépenses réparties sur Mars et Avril 2026. Continuer ?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.seedDemoData()
                    showSeedDialog = false
                    Toast.makeText(context, "Données de démo chargées", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Charger", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSeedDialog = false }) { Text("Annuler") }
            }
        )
    }

    if (showCategorySheet) {
        CategoryManagementSheet(
            categories = allCategories,
            onToggleActive = { viewModel.toggleCategoryActive(it) },
            onDeleteCategory = { showDeleteCategoryDialog = it },
            onAddCategory = {
                categoryToEdit = null
                showAddCategoryDialog = true
            },
            onEditCategory = {
                categoryToEdit = it
                showAddCategoryDialog = true
            },
            onDismiss = { showCategorySheet = false }
        )
    }

    if (showAddCategoryDialog) {
        AddEditCategoryDialog(
            category = categoryToEdit,
            onSave = { name, icon, color ->
                viewModel.saveCategory(
                    categoryToEdit?.copy(name = name, icon = icon, color = color)
                        ?: Category(name = name, icon = icon, color = color)
                )
                showAddCategoryDialog = false
            },
            onDismiss = { showAddCategoryDialog = false }
        )
    }

    showDeleteCategoryDialog?.let { category ->
        AlertDialog(
            onDismissRequest = { showDeleteCategoryDialog = null },
            title = { Text("Supprimer ${category.name}", style = MaterialTheme.typography.titleLarge) },
            text = { Text("Voulez-vous supprimer cette catégorie ? Les dépenses associées seront basculées vers la catégorie 'Autre'.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCategory(category.id, reassignToDefault = true)
                    showDeleteCategoryDialog = null
                }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Text("Supprimer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteCategoryDialog = null }) { Text("Annuler") }
            }
        )
    }
}


@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 8.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsClickableItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconBg: Color,
    iconColor: Color,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = RoundedCornerShape(12.dp),
                color = iconBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = title, tint = iconColor, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (trailing != null) {
                trailing()
            } else {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun CurrencyPickerDialog(
    currentCurrency: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val currencies = listOf("MAD", "EUR", "USD", "GBP", "CAD")
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Devise par défaut", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                currencies.forEach { c ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(c) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = c == currentCurrency,
                            onClick = { onSelect(c) },
                            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(c, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fermer") }
        }
    )
}

@Composable
private fun BudgetSettingDialog(
    currentBudget: Double,
    currency: String,
    onSave: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var budgetText by remember { mutableStateOf(if (currentBudget > 0) String.format(Locale.US, "%.0f", currentBudget) else "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Budget mensuel", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Text("Définissez un budget mensuel pour suivre vos dépenses.", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { budgetText = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Budget ($currency)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = { onSave(0.0) }) {
                    Text("Supprimer la limite", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val budget = budgetText.toDoubleOrNull() ?: 0.0
                onSave(budget)
            }) {
                Text("Enregistrer", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportDateRangeDialog(
    onExport: (Long, Long) -> Unit,
    onDismiss: () -> Unit
) {
    var startDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var endDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Exporter par période", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Text("Choisissez la période à exporter.", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedButton(
                    onClick = { showStartPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Du : ${dateFormat.format(Date(startDate))}")
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { showEndPicker = true },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Au : ${dateFormat.format(Date(endDate))}")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onExport(startDate, endDate) }) {
                Text("Exporter", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Annuler") }
        }
    )

    if (showStartPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = startDate)
        DatePickerDialog(
            onDismissRequest = { showStartPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { startDate = it }
                    showStartPicker = false
                }) { Text("Confirmer") }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) { Text("Annuler") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }

    if (showEndPicker) {
        val pickerState = rememberDatePickerState(initialSelectedDateMillis = endDate)
        DatePickerDialog(
            onDismissRequest = { showEndPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { endDate = it }
                    showEndPicker = false
                }) { Text("Confirmer") }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) { Text("Annuler") }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryManagementSheet(
    categories: List<Category>,
    onToggleActive: (Category) -> Unit,
    onDeleteCategory: (Category) -> Unit,
    onAddCategory: () -> Unit,
    onEditCategory: (Category) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Catégories métier",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Gérez vos catégories de dépenses",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                FilledIconButton(
                    onClick = onAddCategory,
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter", modifier = Modifier.size(24.dp))
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories.sortedBy { it.name }, key = { it.id }) { category ->
                    val catColor = try {
                        Color(android.graphics.Color.parseColor(category.color))
                    } catch (_: Exception) { MaterialTheme.colorScheme.primary }

                    Surface(
                        onClick = { onEditCategory(category) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (category.isActive) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(catColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(category.icon, fontSize = 20.sp)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (category.isActive) MaterialTheme.colorScheme.onSurface
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (category.isActive) "Activée" else "Désactivée",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (category.isActive) Success else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }

                            Switch(
                                checked = category.isActive,
                                onCheckedChange = { onToggleActive(category) },
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = catColor,
                                    checkedThumbColor = Color.White
                                )
                            )

                            if (category.name != "Autre") {
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { onDeleteCategory(category) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Supprimer",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryBudgetSheet(
    categories: List<Category>,
    currency: String,
    viewModel: ExpenseViewModel,
    onDismiss: () -> Unit
) {
    val monthlyBudgets by viewModel.monthlyBudgets.collectAsState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var editingCategoryId by remember { mutableStateOf<Long?>(null) }
    var editAmount by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Budgets par catégorie",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Fixez des limites mensuelles pour contrôler vos dépenses",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(categories.sortedBy { it.name }, key = { it.id }) { category ->
                    val budget = monthlyBudgets.find { it.categoryId == category.id }
                    val catColor = try {
                        Color(android.graphics.Color.parseColor(category.color))
                    } catch (_: Exception) { MaterialTheme.colorScheme.primary }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        border = if (budget != null) androidx.compose.foundation.BorderStroke(1.dp, catColor.copy(alpha = 0.3f)) else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(catColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(category.icon, fontSize = 18.sp)
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (budget != null) {
                                    Text(
                                        text = "${String.format(Locale.getDefault(), "%.0f", budget.limitAmount)} $currency",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = catColor
                                    )
                                } else {
                                    Text(
                                        text = "Aucune limite",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            if (editingCategoryId == category.id) {
                                OutlinedTextField(
                                    value = editAmount,
                                    onValueChange = { editAmount = it.filter { c -> c.isDigit() || c == '.' } },
                                    modifier = Modifier.width(100.dp),
                                    singleLine = true,
                                    placeholder = { Text("0") },
                                    suffix = { Text(currency, style = MaterialTheme.typography.labelSmall) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    textStyle = MaterialTheme.typography.bodyMedium,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = {
                                        val amount = editAmount.toDoubleOrNull()
                                        if (amount != null && amount > 0) {
                                            viewModel.saveCategoryBudget(category.id, amount)
                                        }
                                        editingCategoryId = null
                                        editAmount = ""
                                    },
                                    modifier = Modifier.size(40.dp).background(Success.copy(alpha = 0.1f), RoundedCornerShape(10.dp))
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = "Valider", tint = Success, modifier = Modifier.size(20.dp))
                                }
                            } else {
                                IconButton(
                                    onClick = {
                                        editingCategoryId = category.id
                                        editAmount = budget?.let { String.format(Locale.US, "%.0f", it.limitAmount) } ?: ""
                                    },
                                    modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp))
                                ) {
                                    Icon(
                                        if (budget != null) Icons.Default.Edit else Icons.Default.Add,
                                        contentDescription = "Définir budget",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                if (budget != null) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = { viewModel.deleteCategoryBudget(category.id) },
                                        modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.error.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "Supprimer",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddEditCategoryDialog(
    category: Category?,
    onSave: (String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var icon by remember { mutableStateOf(category?.icon ?: "\uD83D\uDCE6") }
    var color by remember { mutableStateOf(category?.color ?: "#94a3b8") }

    val icons = listOf("\uD83C\uDF54", "\uD83D\uDE8C", "\uD83C\uDFE0", "\uD83D\uDC8A", "\uD83C\uDFAC", "\uD83D\uDCDA", "\uD83D\uDECD\uFE0F", "\uD83D\uDCE6", "\uD83C\uDF71", "\uD83D\uDCA1", "\uD83C\uDF93", "\uD83D\uDCAF", "\uD83D\uDCB0", "\uD83C\uDF81", "\uD83C\uDFB5")
    val colors = listOf("#f59e0b", "#3b82f6", "#8b5cf6", "#ef4444", "#10b981", "#6366f1", "#ec4899", "#94a3b8", "#06b6d4", "#f97316")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "Nouvelle catégorie" else "Modifier la catégorie") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom de la catégorie") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text("Icône", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    icons.forEach { i ->
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (icon == i) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { icon = i },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(i, fontSize = 20.sp)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text("Couleur", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    colors.forEach { c ->
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(32.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(Color(android.graphics.Color.parseColor(c)))
                                .clickable { color = c },
                            contentAlignment = Alignment.Center
                        ) {
                            if (color == c) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name, icon, color) },
                enabled = name.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Enregistrer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
