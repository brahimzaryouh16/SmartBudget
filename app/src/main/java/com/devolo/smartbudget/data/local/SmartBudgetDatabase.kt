package com.devolo.smartbudget.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.devolo.smartbudget.data.model.Category
import com.devolo.smartbudget.data.model.Expense

@Database(entities = [Category::class, Expense::class], version = 1, exportSchema = false)
abstract class SmartBudgetDatabase : RoomDatabase() {
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: SmartBudgetDatabase? = null

        fun getDatabase(context: Context): SmartBudgetDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SmartBudgetDatabase::class.java,
                    "smartbudget_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
