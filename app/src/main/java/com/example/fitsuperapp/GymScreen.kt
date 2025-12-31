package com.example.fitsuperapp

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitsuperapp.data.GymRoutineEntity

@Composable
fun GymScreen(
    viewModel: GymViewModel,
    onExit: () -> Unit
) {
    val currentDay by viewModel.currentDay.collectAsState()
    val currentRoutineId by viewModel.currentRoutineId.collectAsState()
    val restTimer by viewModel.restTimerSeconds.collectAsState()
    val isResting by viewModel.isResting.collectAsState()

    Scaffold(
        containerColor = Color(0xFF111827), // Tu bg-gray-900
        topBar = {
            // HEADER con Timer y Título
            Column(modifier = Modifier.background(Color(0xFF1F2937))) { // bg-gray-800
                // Barra Superior
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onExit) {
                        Text("Salir", color = Color.Gray)
                    }

                    // Display del Timer de Descanso
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (isResting) "Descanso" else "Listo",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        Text(
                            text = formatTime(restTimer),
                            color = if (isResting) Color(0xFF34D399) else Color.White, // Verde si cuenta
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Selector de Rutina (Dropdown)
                    var expanded by remember { mutableStateOf(false) }
                    val availableRoutines by viewModel.availableRoutines.collectAsState()
                    val currentRoutine = availableRoutines.find { it.routineId == currentRoutineId }

                    Box {
                        TextButton(onClick = { expanded = true }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentRoutine?.name ?: "Seleccionar Rutina",
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("▼", color = Color(0xFF10B981), fontSize = 10.sp)
                            }
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(Color(0xFF1F2937)) // bg-gray-800
                        ) {
                            availableRoutines.forEach { routine ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            routine.name,
                                            color = if (routine.routineId == currentRoutineId) Color(0xFF10B981) else Color.White
                                        )
                                    },
                                    onClick = {
                                        viewModel.selectRoutine(routine.routineId)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Botón de Reset
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = { viewModel.resetCurrentDay() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFdc2626)),
                        modifier = Modifier.fillMaxWidth(0.5f)
                    ) {
                        Text("⟳ Reiniciar Día", color = Color.White, fontSize = 14.sp)
                    }
                }

                // Navegación de Días (< Día X >)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.changeDay(-1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev", tint = Color.Gray)
                    }

                    Text(
                        text = currentDay?.title ?: "Cargando...",
                        color = Color(0xFF34D399),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = { viewModel.changeDay(1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", tint = Color.Gray)
                    }
                }
            }
        }
    ) { paddingValues ->
        // LISTA DE EJERCICIOS
        val exercises = currentDay?.exercises ?: emptyList()
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = if (exercises.isEmpty()) 
                Arrangement.Center 
            else 
                Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            if (exercises.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillParentMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🧘‍♂️",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "¡Día de Descanso!",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tómate el día para recuperarte",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            itemsIndexed(exercises) { index, exercise ->
                ExerciseCard(
                    exercise = exercise,
                    onSetCheck = { setIndex -> viewModel.toggleSet(index, setIndex) }
                )
            }
        }
    }
}

@Composable
fun ExerciseCard(
    exercise: GymExercise,
    onSetCheck: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Título y detalles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = exercise.name,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.weight(1f)
                )

                // Badge de Series/Reps
                Surface(
                    color = Color(0xFF374151),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "${exercise.sets} x ${exercise.currentReps}",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Checkboxes de las series
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(exercise.sets) { setIndex ->
                    val isCompleted = exercise.isCompleted.value.getOrElse(setIndex) { false }
                    
                    // Verificar si el checkbox está bloqueado (locked)
                    val isLocked = if (setIndex > 0) {
                        !exercise.isCompleted.value.getOrElse(setIndex - 1) { false }
                    } else {
                        false // El primer set siempre está desbloqueado
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when {
                                        isCompleted -> Color(0xFF10B981) // Verde si completado
                                        isLocked -> Color(0xFF374151).copy(alpha = 0.5f) // Gris oscuro con opacidad si bloqueado
                                        else -> Color(0xFF374151) // Gris normal si disponible
                                    }
                                )
                                .clickable(enabled = !isLocked) { onSetCheck(setIndex) },
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                isCompleted -> Text("✔", color = Color.White, fontSize = 14.sp)
                                isLocked -> Text("🔒", fontSize = 12.sp) // Candado para bloqueado
                            }
                        }
                        Text(
                            text = "${setIndex + 1}",
                            color = Color.Gray,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

// Utilidad para formatear segundos a MM:SS
fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}