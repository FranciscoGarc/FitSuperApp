package com.example.fitsuperapp

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.Locale

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel,
    onBack: () -> Unit
) {
    val streak by viewModel.streak.collectAsState()
    val totalTime by viewModel.totalTimeFormatted.collectAsState()
    val quote by viewModel.quote.collectAsState()
    val workoutDays by viewModel.workoutDays.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(horizontal = 16.dp)
                .padding(bottom = padding.calculateBottomPadding()), // Respect bottom navigation
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header manual - Row simple sin padding extra
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
                Text(
                    text = "Mi Progreso",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Content
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tarjeta de Racha
                streakCard(streak, totalTime)

                // Calendario (Mes Actual)
                CalendarView(workoutDays)

                // Frase Motivacional
                MotivationalQuoteCard(quote)
            }
        }
    }
}

@Composable
fun streakCard(streak: Int, totalTime: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFD700), // Oro
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$streak",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("Días Racha", style = MaterialTheme.typography.bodyMedium)
            }
            
            Divider(
                modifier = Modifier
                    .height(80.dp)
                    .width(1.dp),
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.DateRange, // Icono de reloj o similar
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = totalTime, // Ej: "12h 30m"
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text("Tiempo Total", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun CalendarView(workoutDays: Map<String, Boolean>) {
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault())?.capitalize(Locale.getDefault()) ?: ""
    val year = calendar.get(Calendar.YEAR)

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$monthName $year",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp),
            fontWeight = FontWeight.Bold
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Días de la semana
            val weekDays = listOf("D", "L", "M", "M", "J", "V", "S")
            items(weekDays) { day ->
                Text(
                    text = day,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Offset del primer día
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 1=Sunday -> 0 offset
            
            items(firstDayOfWeek) {
                Spacer(modifier = Modifier)
            }

            // Días del mes
            items(maxDays) { index ->
                val day = index + 1
                val dayString = String.format("%02d/%02d/%d", day, currentMonth + 1, year)
                val isWorkoutDay = workoutDays.containsKey(dayString)
                val isToday = day == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(
                            if (isWorkoutDay) MaterialTheme.colorScheme.primary 
                            else if (isToday) MaterialTheme.colorScheme.surfaceVariant 
                            else Color.Transparent
                        )
                        .border(
                            width = 1.dp,
                            color = if (isToday && !isWorkoutDay) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape
                        )
                ) {
                    Text(
                        text = "$day",
                        color = if (isWorkoutDay) MaterialTheme.colorScheme.onPrimary 
                                else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun MotivationalQuoteCard(quote: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "💡 Motivación",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\"$quote\"",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }
    }
}
