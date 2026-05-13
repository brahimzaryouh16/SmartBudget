package com.devolo.smartbudget.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.devolo.smartbudget.data.local.SmartBudgetDatabase
import com.devolo.smartbudget.data.repository.BudgetRepository
import com.devolo.smartbudget.ui.screens.AddEditExpenseScreen
import com.devolo.smartbudget.ui.screens.ExpensesScreen
import com.devolo.smartbudget.ui.screens.StatsScreen
import com.devolo.smartbudget.ui.viewmodel.ExpenseViewModel
import com.devolo.smartbudget.ui.viewmodel.ExpenseViewModelFactory

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Expenses : Screen("expenses", "Dépenses", Icons.Default.Wallet)
    object Stats : Screen("stats", "Stats", Icons.Default.ShowChart)
    object Settings : Screen("settings", "Paramètres", Icons.Default.Settings)
    object AddEditExpense : Screen("add_edit_expense?expenseId={expenseId}", "Dépense", Icons.Default.Add)
}

@Composable
fun SmartBudgetApp() {
    val context = LocalContext.current
    val database = SmartBudgetDatabase.getDatabase(context)
    val repository = BudgetRepository(database.budgetDao())
    val viewModel: ExpenseViewModel = viewModel(factory = ExpenseViewModelFactory(repository))
    
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            if (currentDestination?.route in listOf(Screen.Expenses.route, Screen.Stats.route, Screen.Settings.route)) {
                NavigationBar {
                    val screens = listOf(Screen.Expenses, Screen.Stats, Screen.Settings)
                    screens.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.route == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            if (currentDestination?.route == Screen.Expenses.route) {
                FloatingActionButton(onClick = { navController.navigate("add_edit_expense?expenseId=0") }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense")
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Expenses.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Expenses.route) {
                ExpensesScreen(
                    viewModel = viewModel,
                    onEditExpense = { id -> navController.navigate("add_edit_expense?expenseId=$id") }
                )
            }
            composable(Screen.Stats.route) {
                StatsScreen(viewModel = viewModel)
            }
            composable(Screen.Settings.route) {
                // Placeholder for Settings
                Text("Settings Screen")
            }
            composable(
                route = Screen.AddEditExpense.route,
                arguments = listOf(navArgument("expenseId") { type = NavType.LongType })
            ) { backStackEntry ->
                val expenseId = backStackEntry.arguments?.getLong("expenseId") ?: 0L
                AddEditExpenseScreen(
                    viewModel = viewModel,
                    expenseId = expenseId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
