package com.devolo.smartbudget.ui

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.devolo.smartbudget.ui.screens.SearchFilterScreen
import com.devolo.smartbudget.ui.screens.SettingsScreen
import com.devolo.smartbudget.ui.screens.StatsScreen
import com.devolo.smartbudget.ui.screens.WelcomeScreen
import com.devolo.smartbudget.ui.theme.*
import com.devolo.smartbudget.ui.viewmodel.ExpenseViewModel
import com.devolo.smartbudget.ui.viewmodel.ExpenseViewModelFactory
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Expenses : Screen("expenses", "Dépenses", Icons.Default.Home)
    object Stats : Screen("stats", "Stats", Icons.Default.ShowChart)
    object Settings : Screen("settings", "Réglages", Icons.Default.Settings)
    object AddEditExpense : Screen("add_edit_expense?expenseId={expenseId}", "Dépense", Icons.Default.Add)
    object SearchFilter : Screen("search_filter", "Recherche", Icons.Default.Search)
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

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val categories by viewModel.categories.collectAsState()
    val expenses by viewModel.filteredExpenses.collectAsState()
    var showOnboarding by remember { mutableStateOf(false) }
    var hasRequestedReview by remember { mutableStateOf(false) }

    LaunchedEffect(expenses.size) {
        if (expenses.size >= 5 && !hasRequestedReview) {
            hasRequestedReview = true
            try {
                val activity = context as? Activity
                if (activity != null) {
                    val reviewManager = ReviewManagerFactory.create(context)
                    val request = reviewManager.requestReviewFlow()
                    request.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val reviewInfo = task.result
                            reviewManager.launchReviewFlow(activity, reviewInfo)
                        }
                    }
                }
            } catch (_: Exception) { }
        }
    }

    LaunchedEffect(categories) {
        if (categories.isEmpty() && !viewModel.isLoading.value) {
            showOnboarding = true
        }
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = event.message,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
                is UiEvent.ShowOnboarding -> {
                    showOnboarding = true
                }
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        bottomBar = {
            if (currentDestination?.route in listOf(Screen.Expenses.route, Screen.Stats.route, Screen.Settings.route)) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    modifier = Modifier.navigationBarsPadding()
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
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            selected = selected,
                            alwaysShowLabel = true,
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
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
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.padding(bottom = 0.dp),
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Ajouter une dépense", modifier = Modifier.size(24.dp))
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Expenses.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = { slideInHorizontally(tween(300)) { it / 4 } + fadeIn(tween(300)) },
            exitTransition = { slideOutHorizontally(tween(300)) { -it / 4 } + fadeOut(tween(200)) },
            popEnterTransition = { slideInHorizontally(tween(300)) { -it / 4 } + fadeIn(tween(300)) },
            popExitTransition = { slideOutHorizontally(tween(300)) { it / 4 } + fadeOut(tween(200)) }
        ) {
            composable(Screen.Expenses.route) {
                ExpensesScreen(
                    viewModel = viewModel,
                    onEditExpense = { id -> navController.navigate("add_edit_expense?expenseId=$id") },
                    onSearchFilter = { navController.navigate(Screen.SearchFilter.route) }
                )
            }
            composable(Screen.Stats.route) {
                StatsScreen(viewModel = viewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel)
            }
            composable(Screen.SearchFilter.route) {
                SearchFilterScreen(
                    viewModel = viewModel,
                    onEditExpense = { id -> navController.navigate("add_edit_expense?expenseId=$id") },
                    onNavigateBack = { navController.popBackStack() }
                )
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

    if (showOnboarding) {
        WelcomeScreen(onDismiss = {
            showOnboarding = false
            navController.navigate(Screen.Expenses.route) {
                popUpTo(navController.graph.findStartDestination().id) {
                    saveState = false
                }
                launchSingleTop = true
                restoreState = false
            }
        })
    }
}
