package com.devolo.smartbudget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devolo.smartbudget.ui.theme.*

@Composable
fun CategoryChip(
    name: String,
    color: String?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val chipBackground = if (isSelected) Slate900 else Color.White
    val chipTextColor = if (isSelected) Color.White else Slate500
    val chipBorderColor = if (isSelected) Slate900 else Slate100

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(chipBackground)
            .border(1.dp, chipBorderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            color = chipTextColor,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            maxLines = 1
        )
    }
}
