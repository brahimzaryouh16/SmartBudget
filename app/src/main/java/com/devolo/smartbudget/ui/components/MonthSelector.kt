package com.devolo.smartbudget.ui.components

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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devolo.smartbudget.ui.theme.Slate100
import com.devolo.smartbudget.ui.theme.Slate500
import com.devolo.smartbudget.ui.theme.Slate700
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MonthSelector(
    currentMonth: Calendar,
    onMonthChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.FRANCE)

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        MonthButton(icon = Icons.Default.ChevronLeft, contentDesc = "Mois précédent", onClick = { onMonthChange(-1) })

        Text(
            text = monthFormat.format(currentMonth.time)
                .replaceFirstChar { it.uppercase() },
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Slate700
        )

        MonthButton(icon = Icons.Default.ChevronRight, contentDesc = "Mois suivant", onClick = { onMonthChange(1) })
    }
}

@Composable
private fun MonthButton(
    icon: ImageVector,
    contentDesc: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(44.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, Slate100),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDesc,
                modifier = Modifier.size(22.dp),
                tint = Slate500
            )
        }
    }
}
