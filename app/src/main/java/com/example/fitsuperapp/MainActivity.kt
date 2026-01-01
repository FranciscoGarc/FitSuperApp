package com.example.fitsuperapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import androidx.compose.ui.graphics.Color
import com.example.fitsuperapp.data.AppDatabase
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier

// Definimos las pantallas posibles
enum class Screen {
    HOME, GYM, HIIT_SELECTION, HIIT, MANAGE_DATA, PROGRESS
}

class MainActivity : ComponentActivity() {
    
    // Inicializamos la base de datos
    private val database by lazy { AppDatabase.getDatabase(this) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializamos los ViewModels con sus factories
        val hiitViewModel: HiitViewModel by viewModels { HiitViewModel.Factory(application, database) }
        val gymViewModel: GymViewModel by viewModels { GymViewModel.Factory(application, database) }
        val manageDataViewModel: ManageDataViewModel by viewModels { ManageDataViewModel.Factory(database) }
        val progressViewModel: ProgressViewModel by viewModels { ProgressViewModel.Factory(database) }

        // Solicitar permisos de notificación en Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permissionRequest = registerForActivityResult(
                androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
            ) { }
            permissionRequest.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF111827),
                    primary = Color(0xFF10B981)
                )
            ) {
                // Estado para saber en qué pantalla estamos
                var currentScreen by remember { mutableStateOf(Screen.HOME) } // HOME = DASHBOARD
                
                // Estado para la rutina HIIT seleccionada
                var selectedHiitRoutineId by remember { mutableStateOf<Long?>(null) }
                
                // Cargar datos
                val hiitRoutines by hiitViewModel.availableRoutines.collectAsState()
                val hiitStepsCountByRoutine by hiitViewModel.stepsCountByRoutine.collectAsState()

                // Interceptamos el botón "Atrás"
                BackHandler(enabled = currentScreen != Screen.HOME) {
                    when (currentScreen) {
                        Screen.HIIT -> currentScreen = Screen.HIIT_SELECTION
                        Screen.HIIT_SELECTION -> currentScreen = Screen.HOME
                        Screen.PROGRESS -> currentScreen = Screen.HOME
                        Screen.MANAGE_DATA -> currentScreen = Screen.HOME
                        Screen.GYM -> {
                            gymViewModel.cancelRestTimer()
                            currentScreen = Screen.HOME
                        }
                        else -> currentScreen = Screen.HOME
                    }
                }

                Scaffold(
                    bottomBar = {
                        // Ocultar barra en pantallas de entrenamiento inmersivas
                        if (currentScreen != Screen.GYM && currentScreen != Screen.HIIT && currentScreen != Screen.HIIT_SELECTION) {
                            NavigationBar(
                                containerColor = Color(0xFF1F2937), // gray-800
                                contentColor = Color.White
                            ) {
                                NavigationBarItem(
                                    icon = { Icon(androidx.compose.material.icons.Icons.Default.Home, contentDescription = "Inicio") },
                                    label = { Text("Inicio") },
                                    selected = currentScreen == Screen.HOME,
                                    onClick = { currentScreen = Screen.HOME },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF10B981),
                                        selectedTextColor = Color(0xFF10B981),
                                        indicatorColor = Color(0xFF374151)
                                    )
                                )
                                NavigationBarItem(
                                    icon = { Icon(androidx.compose.material.icons.Icons.Default.DateRange, contentDescription = "Progreso") },
                                    label = { Text("Progreso") },
                                    selected = currentScreen == Screen.PROGRESS,
                                    onClick = { 
                                        progressViewModel.refresh()
                                        currentScreen = Screen.PROGRESS 
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF10B981),
                                        selectedTextColor = Color(0xFF10B981),
                                        indicatorColor = Color(0xFF374151)
                                    )
                                )
                                NavigationBarItem(
                                    icon = { Icon(androidx.compose.material.icons.Icons.Default.Settings, contentDescription = "Gestión") },
                                    label = { Text("Gestión") },
                                    selected = currentScreen == Screen.MANAGE_DATA,
                                    onClick = { currentScreen = Screen.MANAGE_DATA },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFF10B981),
                                        selectedTextColor = Color(0xFF10B981),
                                        indicatorColor = Color(0xFF374151)
                                    )
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = androidx.compose.ui.Modifier.padding(innerPadding)) {
                        Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                            when (screen) {
                                Screen.HOME -> DashboardScreen(
                                    progressViewModel = progressViewModel,
                                    onNavigateToGym = { currentScreen = Screen.GYM },
                                    onNavigateToHiit = { 
                                        hiitViewModel.refreshRoutines()
                                        currentScreen = Screen.HIIT_SELECTION 
                                    }
                                )
                                Screen.HIIT_SELECTION -> HiitSelectionScreen(
                                    routines = hiitRoutines,
                                    stepsCountByRoutine = hiitStepsCountByRoutine,
                                    onRoutineSelected = { routineId ->
                                        selectedHiitRoutineId = routineId
                                        hiitViewModel.loadRoutine(routineId)
                                        currentScreen = Screen.HIIT
                                    },
                                    onBack = { currentScreen = Screen.HOME }
                                )
                                Screen.HIIT -> HiitScreen(
                                    viewModel = hiitViewModel,
                                    onExit = { currentScreen = Screen.HIIT_SELECTION }
                                )
                                Screen.GYM -> GymScreen(
                                    viewModel = gymViewModel,
                                    onExit = { 
                                        gymViewModel.cancelRestTimer()
                                        currentScreen = Screen.HOME // Volver al Dashboard
                                    }
                                )
                                Screen.MANAGE_DATA -> ManageDataScreen(
                                    viewModel = manageDataViewModel,
                                    onExit = { 
                                        gymViewModel.refreshRoutines()
                                        hiitViewModel.refreshRoutines()
                                        currentScreen = Screen.HOME 
                                    }
                                )
                                Screen.PROGRESS -> ProgressScreen(
                                    viewModel = progressViewModel,
                                    onBack = { currentScreen = Screen.HOME }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}