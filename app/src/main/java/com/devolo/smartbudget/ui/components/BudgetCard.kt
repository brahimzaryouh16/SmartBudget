package com.devolo.smartbudget.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devolo.smartbudget.ui.theme.Emerald600
import com.devolo.smartbudget.ui.theme.Emerald500
import java.util.Locale

@Composable
fun BudgetCard(
    totalAmount: Double,
    expenseCount: Int,
    currency: String = "MAD",
    previousTotal: Double = 0.0,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Emerald600),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Emerald600, Emerald500)
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = (-20).dp)
                    .size(120.dp)
                    .blur(50.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.12f))
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 22.dp)
            ) {
                Text(
                    text = "DÉPENSES TOTALES",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.75f),
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%,.2f", totalAmount),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currency,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                if (previousTotal > 0) {
                    val percentageChange = ((totalAmount - previousTotal) / previousTotal * 100).toInt()
                    val isIncrease = percentageChange > 0
                    val iconImage = if (isIncrease) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color.White.copy(alpha = 0.18f),
                            shape = CircleShape,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = iconImage,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${if (isIncrease) "+" else ""}$percentageChange%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "vs mois dernier",
                            fontSize = 10.sp,
                            color = Color.White.copy(alpha = 0.65f)
                        )
                    }
                }
            }
        }
    }
}
