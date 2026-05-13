package com.devolo.smartbudget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devolo.smartbudget.ui.theme.Slate100
import com.devolo.smartbudget.ui.theme.Slate400
import com.devolo.smartbudget.ui.theme.Slate700
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MonthSelector(
    currentMonth: Calendar,
    onMonthChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MonthButton(icon = Icons.Default.ChevronLeft, onClick = { onMonthChange(-1) })
        
        Text(
            text = monthFormat.format(currentMonth.time).replaceFirstChar { it.uppercase() },
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = Slate700
        )
        
        MonthButton(icon = Icons.Default.ChevronRight, onClick = { onMonthChange(1) })
    }
}

@Composable
private fun MonthButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(40.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate100),
        shadowElevation = 1.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Slate400
            )
        }
    }
}
