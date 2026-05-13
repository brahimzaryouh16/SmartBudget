package com.devolo.smartbudget.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = { onMonthChange(-1) }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Previous Month")
        }
        
        Text(
            text = monthFormat.format(currentMonth.time).replaceFirstChar { it.uppercase() },
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.widthIn(min = 140.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        IconButton(onClick = { onMonthChange(1) }) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next Month")
        }
    }
}
