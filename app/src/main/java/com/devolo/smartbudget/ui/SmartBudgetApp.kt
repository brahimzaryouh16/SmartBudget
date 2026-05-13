package com.devolo.smartbudget.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.devolo.smartbudget.ui.theme.*
import com.devolo.smartbudget.ui.viewmodel.ExpenseViewModel
import com.devolo.smartbudget.ui.viewmodel.ExpenseViewModelFactory

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Expenses : Screen("expenses", "Dépenses", Icons.Default.Home)
    object Stats : Screen("stats", "Statistiques", Icons.Default.ShowChart)
    object Settings : Screen("settings", "Réglages", Icons.Default.Settings)
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
        containerColor = Slate100,
        bottomBar = {
            if (currentDestination?.route in listOf(Screen.Expenses.route, Screen.Stats.route, Screen.Settings.route)) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp,
                    modifier = Modifier.padding(bottom = 0.dp)
                ) {
                    val screens = listOf(Screen.Expenses, Screen.Stats, Screen.Settings)
                    screens.forEach { screen ->
                        val selected = currentDestination?.route == screen.route
                        NavigationBarItem(
                            icon = { 
                                Icon(
                                    imageVector = screen.icon, 
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(24.dp)
                                ) 
                            },
                            label = { 
                                Text(
                                    text = screen.title,
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
                                ) 
                            },
                            selected = selected,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Emerald600,
                                selectedTextColor = Emerald600,
                                unselectedIconColor = Slate400,
                                unselectedTextColor = Slate400,
                                indicatorColor = Emerald50
                            ),
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
                FloatingActionButton(
                    onClick = { navController.navigate("add_edit_expense?expenseId=0") },
                    containerColor = Emerald500,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .size(56.dp)
                        .padding(bottom = 0.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense", modifier = Modifier.size(32.dp))
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
                Box(modifier = Modifier.fillMaxSize().background(Slate100).padding(24.dp)) {
                    Text("Paramètres", style = MaterialTheme.typography.titleLarge)
                }
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
