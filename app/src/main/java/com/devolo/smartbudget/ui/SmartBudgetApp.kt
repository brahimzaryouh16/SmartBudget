package com.devolo.smartbudget.ui

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.devolo.smartbudget.ui.screens.*
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
                    onEditExpense = { id -> navController.navigate("add_edit_expense?expenseId=$id") }
                )
            }
            composable(Screen.Stats.route) {
                StatsScreen(viewModel = viewModel)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel)
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
        AlertDialog(
            onDismissRequest = { showOnboarding = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text("Bienvenue sur SmartBudget", style = MaterialTheme.typography.titleLarge)
            },
            text = {
                Column {
                    Text("Gérez vos dépenses simplement et efficacement.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    OnboardingItem(icon = Icons.Default.Home, text = "Consultez vos dépenses par mois")
                    Spacer(modifier = Modifier.height(8.dp))
                    OnboardingItem(icon = Icons.Default.ShowChart, text = "Visualisez vos statistiques")
                    Spacer(modifier = Modifier.height(8.dp))
                    OnboardingItem(icon = Icons.Default.Add, text = "Ajoutez vos dépenses en un clic")
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = "Commencez par ajouter votre première dépense !",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showOnboarding = false },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("C'est parti !")
                }
            }
        )
    }
}

@Composable
fun OnboardingItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
