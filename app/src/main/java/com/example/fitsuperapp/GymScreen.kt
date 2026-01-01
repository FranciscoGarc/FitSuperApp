package com.example.fitsuperapp

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
        containerColor = Color(0xFF111827), // bg-gray-900
        topBar = {
            // HEADER con Timer y Selectores
            Surface(
                color = Color(0xFF1F2937), // bg-gray-800
                shadowElevation = 8.dp,
                modifier = Modifier.statusBarsPadding()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    // Barra Superior Centralizada
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        // Salir (Izquierda)
                        TextButton(
                            onClick = onExit,
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Text("Salir", color = Color.Gray, fontSize = 14.sp)
                        }

                        // Display del Timer de Descanso (Centro)
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = if (isResting) "Descanso" else "Listo",
                                color = if (isResting) Color(0xFF34D399) else Color.Gray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = formatTime(restTimer),
                                color = if (isResting) Color(0xFF34D399) else Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        // Selector de Rutina (Derecha)
                        var expanded by remember { mutableStateOf(false) }
                        val availableRoutines by viewModel.availableRoutines.collectAsState()
                        val currentRoutine = availableRoutines.find { it.routineId == currentRoutineId }

                        Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                            TextButton(onClick = { expanded = true }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentRoutine?.name ?: "Rutina",
                                        color = Color(0xFF10B981),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("▼", color = Color(0xFF10B981), fontSize = 10.sp)
                                }
                            }

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Color(0xFF1F2937))
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

                    // Navegación de Días y Botón Reset
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp, start = 8.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { viewModel.changeDay(-1) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev", tint = Color.Gray)
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = currentDay?.title ?: "...",
                                color = Color(0xFF34D399),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }

                        IconButton(onClick = { viewModel.changeDay(1) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next", tint = Color.Gray)
                        }

                        // Reset más elegante al final o integrado
                        IconButton(onClick = { viewModel.resetCurrentDay() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color(0xFFdc2626))
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        val exercises = currentDay?.exercises ?: emptyList()
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = if (exercises.isEmpty()) Arrangement.Center else Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(16.dp)
        ) {
            if (exercises.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillParentMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF1F2937)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🧘‍♂️", fontSize = 56.sp)
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "¡Día de Descanso!",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tómate el día para recuperarte y volver con más fuerza.",
                            color = Color.Gray,
                            fontSize = 15.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                itemsIndexed(
                    items = exercises,
                    key = { _, exercise -> exercise.id } 
                ) { index, exercise ->
                    val completionState = currentDay?.completionState?.get(exercise.id) ?: List(exercise.sets) { false }
                    ExerciseCard(
                        exercise = exercise,
                        completionState = completionState,
                        onSetCheck = { setIndex -> viewModel.toggleSet(index, setIndex) }
                    )
                }
            }
        }
    }
}

@Composable
fun ExerciseCard(
    exercise: GymExercise,
    completionState: List<Boolean>,
    onSetCheck: (Int) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
            .border(1.dp, Color(0xFF374151).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Título y detalles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = exercise.name,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "${exercise.sets} series x ${exercise.currentReps} reps",
                        color = Color.Gray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Surface(
                    color = Color(0xFF374151).copy(alpha = 0.8f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${exercise.restSeconds}s desc.",
                        color = Color(0xFF34D399),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Checkboxes de las series
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                repeat(exercise.sets) { setIndex ->
                    val isCompleted = completionState.getOrElse(setIndex) { false }
                    
                    val isLocked = if (setIndex > 0) {
                        !completionState.getOrElse(setIndex - 1) { false }
                    } else {
                        false
                    }

                    SetCheckbox(
                        index = setIndex + 1,
                        isCompleted = isCompleted,
                        isLocked = isLocked,
                        onClick = { onSetCheck(setIndex) }
                    )
                }
            }
        }
    }
}

@Composable
fun SetCheckbox(
    index: Int,
    isCompleted: Boolean,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isCompleted) 1.1f else 1.0f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "scale"
    )

    val bgColor by animateColorAsState(
        targetValue = when {
            isCompleted -> Color(0xFF10B981)
            isLocked -> Color(0xFF374151).copy(alpha = 0.4f)
            else -> Color(0xFF374151)
        },
        label = "bgColor"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .scale(scale)
                .clip(RoundedCornerShape(12.dp))
                .background(bgColor)
                .clickable(enabled = !isLocked && !isCompleted) { onClick() }
                .then(
                    if (!isLocked && !isCompleted) Modifier.border(
                        1.dp,
                        Color(0xFF10B981).copy(alpha = 0.3f),
                        RoundedCornerShape(12.dp)
                    ) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                isCompleted -> Text("✓", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                isLocked -> Text("🔒", fontSize = 14.sp)
                else -> Text("$index", color = Color.LightGray, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        
        Text(
            text = "SERIE $index",
            color = if (isCompleted) Color(0xFF10B981) else Color.Gray,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

// Utilidad para formatear segundos a MM:SS
fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return "%02d:%02d".format(m, s)
}
