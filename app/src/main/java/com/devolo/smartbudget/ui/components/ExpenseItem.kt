package com.devolo.smartbudget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.devolo.smartbudget.data.model.Category
import com.devolo.smartbudget.data.model.Expense
import com.devolo.smartbudget.ui.theme.Slate400
import com.devolo.smartbudget.ui.theme.Slate50
import com.devolo.smartbudget.ui.theme.Slate900
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ExpenseItem(
    expense: Expense,
    category: Category?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    val categoryColor = if (category != null) {
        try { Color(category.color.toColorInt()) } catch (_: Exception) { Color(0xFF64748B) }
    } else {
        Color(0xFF64748B)
    }

    val dateLabel = remember(expense.date) {
        val cal = Calendar.getInstance().apply { timeInMillis = expense.date }
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

        when {
            cal[Calendar.YEAR] == today[Calendar.YEAR] &&
                cal[Calendar.DAY_OF_YEAR] == today[Calendar.DAY_OF_YEAR] -> "Aujourd'hui"
            cal[Calendar.YEAR] == yesterday[Calendar.YEAR] &&
                cal[Calendar.DAY_OF_YEAR] == yesterday[Calendar.DAY_OF_YEAR] -> "Hier"
            else -> dateFormat.format(Date(expense.date))
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(categoryColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = category?.icon ?: "❓", fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.note ?: category?.name ?: "Dépense",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Slate900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$dateLabel • ${category?.name ?: "Alimentation"}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate400
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = String.format(Locale.getDefault(), "-%.2f", expense.amount),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Slate900
            )
        }
    }
}
