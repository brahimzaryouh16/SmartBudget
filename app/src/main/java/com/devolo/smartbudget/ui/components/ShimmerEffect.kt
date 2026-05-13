package com.devolo.smartbudget.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp)
) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.3f)
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(1300, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 300f, 0f),
        end = Offset(translateAnim + 300f, 0f)
    )
    Box(
        modifier = modifier
            .clip(shape)
            .background(brush)
    )
}

@Composable
fun ShimmerCard(modifier: Modifier = Modifier) {
    ShimmerBox(
        modifier = modifier.fillMaxWidth().height(130.dp),
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun ShimmerExpenseItem(modifier: Modifier = Modifier) {
    ShimmerBox(
        modifier = modifier.fillMaxWidth().height(72.dp),
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun ShimmerCategoryChip(modifier: Modifier = Modifier) {
    ShimmerBox(
        modifier = modifier.width(80.dp).height(32.dp),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun ShimmerChartCard(modifier: Modifier = Modifier) {
    ShimmerBox(
        modifier = modifier.fillMaxWidth().height(200.dp),
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun ShimmerCircle(modifier: Modifier = Modifier) {
    ShimmerBox(
        modifier = modifier.size(42.dp),
        shape = RoundedCornerShape(21.dp)
    )
}
