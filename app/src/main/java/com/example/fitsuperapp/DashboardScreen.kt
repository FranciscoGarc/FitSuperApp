package com.example.fitsuperapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(
    progressViewModel: ProgressViewModel,
    onNavigateToGym: () -> Unit,
    onNavigateToHiit: () -> Unit
) {
    val streak by progressViewModel.streak.collectAsState()
    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale("es", "ES")))

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111827)) // bg-gray-900
            // Removed statusBarsPadding as it is handled by Main Scaffold
            .padding(start = 24.dp, end = 24.dp, bottom = 24.dp, top = 16.dp)
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Bienvenido de vuelta",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Text(
                    text = "Vamos a entrenar",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            // User Avatar Placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF374151)),
                contentAlignment = Alignment.Center
            ) {
                Text("👤", fontSize = 24.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- STREAK CARD ---
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF10B981), Color(0xFF059669))
                        )
                    )
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Racha Actual",
                            color = Color(0xFFD1FAE5),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "$streak",
                                color = Color.White,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = " días",
                                color = Color(0xFFD1FAE5),
                                fontSize = 18.sp,
                                modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                            )
                        }
                        Text(
                            text = resultMessage(streak),
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text("🔥", fontSize = 48.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- SECTION TITLE ---
        Text(
            text = "Tu entrenamiento de hoy",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = today.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            color = Color.Gray,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- ACTION CARDS ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // GYM CARD
            ActionCard(
                title = "Gimnasio",
                subtitle = "Pesas y Fuerza",
                icon = { Icon(Icons.Default.FitnessCenter, null, tint = Color.White) },
                colorStart = Color(0xFF3B82F6),
                colorEnd = Color(0xFF2563EB),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToGym
            )

            // HIIT CARD
            ActionCard(
                title = "Cardio HIIT",
                subtitle = "Intervalos",
                icon = { Icon(Icons.Default.Timer, null, tint = Color.White) },
                colorStart = Color(0xFFF59E0B),
                colorEnd = Color(0xFFD97706),
                modifier = Modifier.weight(1f),
                onClick = onNavigateToHiit
            )
        }
    }
}

@Composable
fun ActionCard(
    title: String,
    subtitle: String,
    icon: @Composable () -> Unit,
    colorStart: Color,
    colorEnd: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .height(160.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(colorStart, colorEnd)
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }

                Column {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

fun resultMessage(streak: Int): String {
    return when {
        streak == 0 -> "¡Empieza hoy!"
        streak < 3 -> "¡Buen comienzo!"
        streak < 7 -> "¡Mantén el ritmo!"
        else -> "¡Eres imparable!"
    }
}
