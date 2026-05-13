package com.devolo.smartbudget.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devolo.smartbudget.data.model.Category
import com.devolo.smartbudget.ui.theme.*
import com.devolo.smartbudget.ui.viewmodel.ExpenseViewModel
import java.io.OutputStream

@Composable
fun SettingsScreen(viewModel: ExpenseViewModel) {
    val allCategories by viewModel.allCategoriesIncludingInactive.collectAsState()
    val currency by viewModel.currency.collectAsState()
    val context = LocalContext.current

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var showDeleteCategoryDialog by remember { mutableStateOf<Category?>(null) }
    var showSeedDialog by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate100)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Réglages",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Slate900,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(bottom = 88.dp)
        ) {
            item { ProfileSection() }

            item { SectionHeader(title = "GÉNÉRAL") }

            item {
                SettingsClickableItem(
                    icon = Icons.Default.Category,
                    iconBg = Blue50,
                    iconColor = Blue600,
                    title = "Catégories métier",
                    subtitle = "${allCategories.size} catégories",
                    onClick = { showCategoryDialog = true }
                )
            }

            item {
                SettingsClickableItem(
                    icon = Icons.Default.AttachMoney,
                    iconBg = Purple50,
                    iconColor = Purple600,
                    title = "Devise par défaut",
                    trailing = { Text(currency, fontWeight = FontWeight.Bold, color = Emerald600, fontSize = 13.sp) },
                    onClick = { showCurrencyDialog = true }
                )
            }

            item { SectionHeader(title = "DONNÉES & EXPORT") }

            item {
                SettingsClickableItem(
                    icon = Icons.Default.FileDownload,
                    iconBg = Emerald50,
                    iconColor = Emerald600,
                    title = "Exporter en CSV",
                    subtitle = "Dépenses du mois en cours",
                    onClick = { csvLauncher.launch("smartbudget_export.csv") }
                )
            }

            item {
                SettingsClickableItem(
                    icon = Icons.Default.Storage,
                    iconBg = Amber50,
                    iconColor = Amber600,
                    title = "Charger données de démo",
                    subtitle = "35 dépenses sur 2 mois",
                    onClick = { showSeedDialog = true }
                )
            }

            item {
                SettingsClickableItem(
                    icon = Icons.Default.DeleteForever,
                    iconBg = Red50,
                    iconColor = Danger,
                    title = "Réinitialiser les données",
                    subtitle = "Supprime toutes les dépenses et catégories",
                    onClick = { showResetDialog = true }
                )
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "SmartBudget v1.0.0",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate300,
                    letterSpacing = 0.2.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
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

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Réinitialiser", fontWeight = FontWeight.Bold) },
            text = { Text("Voulez-vous vraiment supprimer toutes les données ? Cette action est irréversible.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetAllData()
                    showResetDialog = false
                    Toast.makeText(context, "Données réinitialisées", Toast.LENGTH_SHORT).show()
                }, colors = ButtonDefaults.textButtonColors(contentColor = Danger)) {
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
            title = { Text("Données de démonstration", fontWeight = FontWeight.Bold) },
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

    if (showCategoryDialog) {
        CategoryManagementDialog(
            categories = allCategories,
            onToggleActive = { viewModel.toggleCategoryActive(it) },
            onDeleteCategory = { showDeleteCategoryDialog = it },
            onDismiss = { showCategoryDialog = false }
        )
    }

    showDeleteCategoryDialog?.let { category ->
        AlertDialog(
            onDismissRequest = { showDeleteCategoryDialog = null },
            title = { Text("Supprimer ${category.name}", fontWeight = FontWeight.Bold) },
            text = { Text("Voulez-vous supprimer cette catégorie ? Les dépenses associées seront basculées vers la catégorie 'Autre'.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteCategory(category.id, reassignToDefault = true)
                    showDeleteCategoryDialog = null
                    Toast.makeText(context, "Catégorie supprimée", Toast.LENGTH_SHORT).show()
                }, colors = ButtonDefaults.textButtonColors(contentColor = Danger)) {
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
private fun ProfileSection() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(16.dp),
                color = Emerald500,
                tonalElevation = 4.dp,
                shadowElevation = 4.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("SB", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text("SmartBudget", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Slate900)
                Text("Gestion de budget personnel", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Slate400)
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = Slate400,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 4.dp)
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
                color = iconBg
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Slate700)
                if (subtitle != null) {
                    Text(subtitle, fontSize = 11.sp, fontWeight = FontWeight.Medium, color = Slate400)
                }
            }
            if (trailing != null) {
                trailing()
            } else {
                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Slate300, modifier = Modifier.size(20.dp))
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
        title = { Text("Devise par défaut", fontWeight = FontWeight.Bold) },
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
                            colors = RadioButtonDefaults.colors(selectedColor = Emerald600)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(c, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Slate700)
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
private fun CategoryManagementDialog(
    categories: List<Category>,
    onToggleActive: (Category) -> Unit,
    onDeleteCategory: (Category) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catégories", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.heightIn(max = 400.dp)) {
                categories.sortedBy { it.name }.forEachIndexed { index, category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(category.icon, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = category.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (category.isActive) Slate700 else Slate400,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = category.isActive,
                            onCheckedChange = { onToggleActive(category) },
                            colors = SwitchDefaults.colors(
                                checkedTrackColor = Emerald600,
                                uncheckedTrackColor = Slate200
                            )
                        )
                        if (category.name != "Autre") {
                            IconButton(
                                onClick = { onDeleteCategory(category) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Supprimer",
                                    tint = Slate400,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    if (index < categories.size - 1) {
                        HorizontalDivider(color = Slate100, thickness = 0.5.dp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer", fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

private val Blue50 = Color(0xFFEFF6FF)
private val Blue600 = Color(0xFF2563EB)
private val Purple50 = Color(0xFFF5F3FF)
private val Purple600 = Color(0xFF7C3AED)
private val Amber50 = Color(0xFFFFFBEB)
private val Amber600 = Color(0xFFD97706)
private val Red50 = Color(0xFFFEF2F2)

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SmartBudgetTheme {
        Surface(color = Slate100) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                Text("Réglages", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Slate900, modifier = Modifier.padding(top = 16.dp, bottom = 24.dp))

                ProfileSection()

                SectionHeader(title = "GÉNÉRAL")

                SettingsClickableItem(
                    icon = Icons.Default.Category,
                    iconBg = Blue50,
                    iconColor = Blue600,
                    title = "Catégories métier",
                    subtitle = "8 catégories",
                    onClick = { }
                )

                SettingsClickableItem(
                    icon = Icons.Default.AttachMoney,
                    iconBg = Purple50,
                    iconColor = Purple600,
                    title = "Devise par défaut",
                    trailing = { Text("MAD", fontWeight = FontWeight.Bold, color = Emerald600, fontSize = 13.sp) },
                    onClick = { }
                )

                SectionHeader(title = "DONNÉES & EXPORT")

                SettingsClickableItem(
                    icon = Icons.Default.FileDownload,
                    iconBg = Emerald50,
                    iconColor = Emerald600,
                    title = "Exporter en CSV",
                    subtitle = "Dépenses du mois en cours",
                    onClick = { }
                )
            }
        }
    }
}