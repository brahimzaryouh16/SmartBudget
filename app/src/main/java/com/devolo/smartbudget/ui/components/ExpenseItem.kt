package com.devolo.smartbudget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        try { Color(category.color.toColorInt()) } catch (e: Exception) { Color(0xFF64748B) }
    } else {
        Color(0xFF64748B)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate50),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(categoryColor.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = category?.icon ?: "❓", fontSize = 22.sp)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.note ?: category?.name ?: "Dépense",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Aujourd'hui • ${category?.name ?: "Alimentation"}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate400
                )
            }
            
            Text(
                text = String.format(Locale.getDefault(), "-%.2f", expense.amount),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
        }
    }
}
