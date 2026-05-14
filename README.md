# SmartBudget

SmartBudget is a personal finance tracking Android app built with Kotlin, Jetpack Compose, and Material 3. It helps you track monthly expenses, analyze spending by category, and set budget limits — all stored locally on your device with no accounts or network permissions.

## Features

- **Expense Dashboard** — Monthly view with navigation, budget progress bar, total spend, and a list of recent expenses
- **Add / Edit Expenses** — Amount, category selector, date picker, and optional note — all in a clean form
- **Category Analytics** — Spending breakdown by category with percentages, progress bars, and budget comparison
- **Budget Limits** — Set a global monthly budget and per-category limits with visual over-budget alerts
- **Search & Filter** — Search by note or amount, filter by category, sort by date or amount
- **CSV Import / Export** — Export current month or a custom date range; import expenses from CSV files
- **Category Management** — Add, edit, delete, or toggle categories on/off
- **Dark Mode** — Full light and dark theme support
- **Demo Data** — One-tap seeding of 35 sample expenses across 2 months
- **100% Private** — Everything stored locally. No internet permission, no accounts.
- **French UI** — Entire interface is in French
- **Pull to Refresh** — Refresh expense data with a pull gesture
- **Swipe to Delete** — Swipe expenses to delete with confirmation dialog
- **In-App Review** — Google Play In-App Review prompt after 5+ expenses

## Project Structure

```
app/src/main/java/com/devolo/smartbudget/
├── data/
│   ├── local/
│   │   ├── SmartBudgetDatabase.kt      # Room database setup
│   │   └── BudgetDao.kt                # DAO interface
│   ├── model/
│   │   ├── Expense.kt                  # Expense entity
│   │   ├── Category.kt                 # Category entity
│   │   └── MonthlyBudget.kt            # MonthlyBudget entity
│   └── repository/
│       ├── Repository.kt               # Repository interface
│       └── BudgetRepository.kt         # Repository implementation
├── ui/
│   ├── SmartBudgetApp.kt               # Main composable, navigation, bottom bar
│   ├── components/                      # Shared composables (cards, items, shimmer)
│   ├── screens/
│   │   ├── ExpensesScreen.kt           # Main dashboard
│   │   ├── AddEditExpenseScreen.kt     # Add / edit expense form
│   │   ├── SearchFilterScreen.kt       # Search, filter, sort
│   │   ├── StatsScreen.kt              # Category analytics
│   │   ├── SettingsScreen.kt           # Currency, budgets, categories, CSV, reset
│   │   └── WelcomeScreen.kt            # Onboarding
│   ├── theme/
│   │   ├── Color.kt                    # Color palette (Emerald, Slate, Indigo)
│   │   ├── Theme.kt                    # Light / dark Material 3 theme
│   │   └── Type.kt                     # Typography
│   ├── viewmodel/
│   │   └── ExpenseViewModel.kt         # ViewModel with StateFlow, CSV, budgets
│   └── UiEvent.kt                      # Sealed class for UI events
```

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose, Material 3 |
| Navigation | Navigation Compose |
| Architecture | MVVM (ViewModel + Repository) |
| Database | Room (SQLite) |
| Async | Kotlin Coroutines, StateFlow |
| DI | Manual (ViewModel + Factory) |
| Build | Gradle KTS, KSP |
| Min SDK / Target | 24 / 35 |

## Requirements

- Android 7.0 (API 24) or higher
- Kotlin 2.x
- Android Studio Hedgehog or later

## Setup

```bash
git clone https://github.com/brahimzaryouh16/SmartBudget.git
cd SmartBudget
```

Open the project in Android Studio, let Gradle sync, then run on an emulator or device.

## License

MIT
