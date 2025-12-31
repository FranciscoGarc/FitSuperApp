package com.example.fitsuperapp

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HiitScreen(
    viewModel: HiitViewModel = viewModel(),
    onExit: () -> Unit
) {
    val timeLeft by viewModel.timeLeft.collectAsState()
    val totalTime by viewModel.totalTime.collectAsState()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentStep by viewModel.currentStep.collectAsState()
    val nextStepName by viewModel.nextStepName.collectAsState()
    val isFinished by viewModel.isWorkoutFinished.collectAsState() // Observamos si terminó

    // Si terminó, mostramos la pantalla de celebración
    if (isFinished) {
        FinishView(
            onRestart = { viewModel.restartWorkout() },
            onExit = onExit
        )
    } else {
        // Si no, mostramos el entrenamiento
        WorkoutView(
            currentStep = currentStep,
            nextStepName = nextStepName,
            timeLeft = timeLeft,
            totalTime = totalTime,
            isPlaying = isPlaying,
            onPlayPause = { viewModel.togglePlayPause() },
            onAdd10s = { viewModel.add10Seconds() },
            onSkipWarmup = { viewModel.skipWarmup() },
            onFinish = { viewModel.finishWorkout() } // Botón terminar manual
        )
    }
}

@Composable
fun WorkoutView(
    currentStep: HiitStep?,
    nextStepName: String,
    timeLeft: Int,
    totalTime: Int,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onAdd10s: () -> Unit,
    onSkipWarmup: () -> Unit,
    onFinish: () -> Unit
) {
    val stepColor = when (currentStep?.type) {
        HiitStepType.EXERCISE -> Color(0xFF22d3ee)
        HiitStepType.WARMUP -> Color(0xFFa78bfa)
        HiitStepType.BREAK, HiitStepType.REST -> Color(0xFFf59e0b)
        HiitStepType.COOLDOWN -> Color(0xFF60a5fa)
        else -> Color.Gray
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111827))
            .statusBarsPadding()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- CABECERA ---
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = nextStepName,
                color = Color.Gray,
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
            Text(
                text = currentStep?.name ?: "Listo",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 8.dp).height(80.dp) // Altura fija para evitar saltos
            )
        }

        // --- TEMPORIZADOR ---
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp)) {
            val progress = if (totalTime > 0) timeLeft.toFloat() / totalTime.toFloat() else 0f
            Canvas(modifier = Modifier.size(250.dp)) {
                drawArc(
                    color = Color(0xFF374151),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 20.dp.toPx())
                )
                drawArc(
                    color = stepColor,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    style = Stroke(width = 20.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(
                text = timeLeft.toString(),
                color = stepColor,
                fontSize = 80.sp,
                fontWeight = FontWeight.Black
            )
        }

        // --- CONTROLES ---
        // Aquí aplicamos navigationBarsPadding para que los botones suban si hay gestos
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding() // <--- MAGIA: Ajuste automático a gestos/botones
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onAdd10s,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF374151)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("+10 segundos", color = Color(0xFF67e8f9))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onPlayPause,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isPlaying) Color(0xFFeab308) else Color(0xFF22c55e)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text(if (isPlaying) "Pausar" else "Reanudar", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onFinish,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFdc2626)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f).height(50.dp)
                ) {
                    Text("Terminar", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            if (currentStep?.type == HiitStepType.WARMUP) {
                Button(
                    onClick = onSkipWarmup,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8b5cf6)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Saltar Calentamiento", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// Pantalla de Finalización (Replicando tu HTML)
@Composable
fun FinishView(onRestart: () -> Unit, onExit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111827))
            .systemBarsPadding() // Padding arriba y abajo automáticamente
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "🎉", fontSize = 80.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¡Rutina Completada!",
            color = Color(0xFF22d3ee), // Cyan
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "¡Excelente trabajo! Has completado el entrenamiento de hoy. ¡No olvides estirar bien!",
            color = Color.Gray,
            textAlign = TextAlign.Center,
            fontSize = 18.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onRestart,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF06b6d4)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(60.dp)
        ) {
            Text("Hacerlo de Nuevo", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onExit) {
            Text("Volver al Menú", color = Color.Gray)
        }
    }
}