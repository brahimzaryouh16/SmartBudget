package com.devolo.smartbudget.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.res.painterResource
import com.devolo.smartbudget.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devolo.smartbudget.ui.theme.*

@Composable
fun WelcomeScreen(onDismiss: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(0.3f))

                AnimatedVisibility(
                    visible = startAnimation,
                    enter = fadeIn(tween(800)) + scaleIn(initialScale = 0.9f, animationSpec = tween(800, easing = FastOutSlowInEasing))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            modifier = Modifier
                                .size(120.dp),
                            shape = RoundedCornerShape(28.dp),
                            color = Color(0xFF059669),
                            shadowElevation = 4.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_launcher_foreground),
                                    contentDescription = null,
                                    modifier = Modifier.size(80.dp),
                                    tint = Color.Unspecified
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "SmartBudget",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Prenez le contrôle de vos finances\navec simplicité et élégance.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                AnimatedVisibility(
                    visible = startAnimation,
                    enter = fadeIn(tween(1000, delayMillis = 300)) + slideInVertically(
                        animationSpec = tween(800, easing = FastOutSlowInEasing),
                        initialOffsetY = { it / 4 }
                    )
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        WelcomeFeatureCard(
                            icon = Icons.Default.BarChart,
                            title = "Suivi Intuitif",
                            description = "Visualisez vos dépenses mensuelles en un clin d'œil.",
                            color = MaterialTheme.colorScheme.primary
                        )
                        WelcomeFeatureCard(
                            icon = Icons.Default.PieChart,
                            title = "Analyses Précises",
                            description = "Comprenez où va votre argent par catégorie.",
                            color = MaterialTheme.colorScheme.secondary
                        )
                        WelcomeFeatureCard(
                            icon = Icons.Default.NotificationsActive,
                            title = "Budgets Intelligents",
                            description = "Fixez des limites et soyez alerté en cas d'excès.",
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(0.7f))

                AnimatedVisibility(
                    visible = startAnimation,
                    enter = fadeIn(tween(800, delayMillis = 600)) + slideInVertically(
                        animationSpec = tween(600, easing = FastOutSlowInEasing),
                        initialOffsetY = { 80 }
                    )
                ) {
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.onSurface
                        ),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 1.5.dp,
                            pressedElevation = 0.dp
                        )
                    ) {
                        Text(
                            text = "Commencer l'expérience",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.surface
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.surface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedVisibility(
                    visible = startAnimation,
                    enter = fadeIn(tween(1000, delayMillis = 800))
                ) {
                    Text(
                        text = "100% Privé • Vos données restent locales",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun WelcomeFeatureCard(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(52.dp),
                shape = RoundedCornerShape(16.dp),
                color = color.copy(alpha = 0.12f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
