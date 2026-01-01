package com.example.fitsuperapp

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(
    onNavigateToGym: () -> Unit,
    onNavigateToHiit: () -> Unit,
    onNavigateToManageData: () -> Unit = {},
    onNavigateToProgress: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111827)) // Fondo oscuro global
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- TÍTULO ---
        Text(
            text = "FitSuperApp",
            color = Color.White,
            fontSize = 40.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Elige tu entrenamiento de hoy",
            color = Color.Gray,
            fontSize = 16.sp,
            modifier = Modifier.padding(bottom = 48.dp)
        )

        // --- TARJETA GYM (FitTrack) ---
        MenuCard(
            title = "FitTrack Gym",
            subtitle = "Rutinas de Fuerza (Torso/Pierna/PPL)",
            icon = Icons.Default.FitnessCenter,
            gradientColors = listOf(Color(0xFF059669), Color(0xFF10B981)), // Verdes Esmeralda
            onClick = onNavigateToGym
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- TARJETA HIIT (Cardio) ---
        MenuCard(
            title = "Cardio HIIT",
            subtitle = "Temporizador de Intervalos",
            icon = Icons.Default.Timer,
            gradientColors = listOf(Color(0xFF0891b2), Color(0xFF22d3ee)), // Azules Cyan
            onClick = onNavigateToHiit
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // --- TARJETA GESTIONAR DATOS ---
        MenuCard(
            title = "Gestionar Datos",
            subtitle = "Editar rutinas • Exportar/Importar CSV",
            icon = Icons.Default.Settings,
            gradientColors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)), // Purples
            onClick = onNavigateToManageData
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- TARJETA PROGRESO ---
        MenuCard(
            title = "Mi Progreso",
            subtitle = "Rachas • Calendario • Estadísticas",
            icon = Icons.Default.Star, // O DateRange
            gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706)), // Naranjas/Ámbar
            onClick = onNavigateToProgress
        )
    }
}

@Composable
fun MenuCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937)) // Gris oscuro de fondo
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Barra de color lateral con gradiente
            Box(
                modifier = Modifier
                    .width(16.dp)
                    .fillMaxHeight()
                    .background(Brush.verticalGradient(gradientColors))
            )

            // Contenido
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        color = Color.Gray,
                        fontSize = 14.sp,
                        lineHeight = 18.sp
                    )
                }

                // Icono Circular
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF374151)), // Círculo gris más claro
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = gradientColors.last(), // Usamos el color claro del gradiente
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}