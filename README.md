# SmartBudget

SmartBudget is a personal finance tracking Android app built with Jetpack Compose and Material 3. It helps you track monthly expenses, analyze spending by category, and set budget limits — all stored locally on your device.

## Features

- **Expense Dashboard** — View your monthly expenses with a month selector, budget progress bar, and quick overview
- **Add / Edit Expenses** — Add expenses with amount, category, date, and optional note
- **Category Analytics** — See spending breakdown by category with percentage and progress bars
- **Budget Limits** — Set monthly budget limits per category and get visual warnings when exceeded
- **Search & Filter** — Search expenses by note or amount, filter by category, sort by date or amount
- **CSV Import / Export** — Import expenses from CSV or export your data
- **Dark Mode** — Full dark theme support
- **100% Private** — All data stays on your device. No accounts, no network permissions
- **French UI** — Entire interface in French
- **Demo Data** — One-tap demo data seeding to explore the app

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose, Material 3 |
| Navigation | Navigation Compose |
| Architecture | MVVM (ViewModel + Repository) |
| Database | Room (SQLite) |
| DI | Manual (ViewModel + Factory) |
| Async | Kotlin Coroutines, StateFlow |
| Build | Gradle KTS, KSP |

## Requirements

- Android 7.0 (API 24) or higher
- Kotlin 2.x / Compose BOM

## Setup

Clone the repo and open in Android Studio:

```bash
git clone <repo-url>
cd SmartBudget
```

Open the project in Android Studio, sync Gradle, and run on an emulator or device.

## License

MIT
