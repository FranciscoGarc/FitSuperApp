package com.example.fitsuperapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitsuperapp.data.HiitRoutineEntity
import kotlinx.coroutines.flow.Flow

// Colores consistentes con el tema
private val DarkBg = Color(0xFF111827)
private val CardBg = Color(0xFF1F2937)
private val AccentCyan = Color(0xFF22d3ee)
private val TextGray = Color(0xFF9CA3AF)

@Composable
fun HiitSelectionScreen(
    routines: List<HiitRoutineEntity>,
    stepsCountByRoutine: Map<Long, Int>,
    onRoutineSelected: (Long) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = DarkBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            // Header - Row simple sin padding extra
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                }
                Text(
                    text = "Seleccionar Rutina HIIT",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
            // Header
            Text(
                text = "¿Qué rutina quieres hacer hoy?",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Selecciona una rutina para comenzar tu entrenamiento HIIT",
                color = TextGray,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            if (routines.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🏃",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay rutinas HIIT",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Crea una en Gestionar Datos",
                            color = TextGray,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                // Routines list
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(routines) { routine ->
                        HiitRoutineCard(
                            routine = routine,
                            stepsCount = stepsCountByRoutine[routine.id] ?: 0,
                            onClick = { onRoutineSelected(routine.id) }
                        )
                    }
                }
            }
        }
    }
}
}

@Composable
private fun HiitRoutineCard(
    routine: HiitRoutineEntity,
    stepsCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Accent bar
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(80.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF0891b2), AccentCyan)
                        )
                    )
            )
            
            // Content
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routine.name,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            tint = TextGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$stepsCount pasos",
                            color = TextGray,
                            fontSize = 12.sp
                        )
                    }
                }
                
                // Play button
                Surface(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    color = AccentCyan.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Iniciar",
                            tint = AccentCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}
