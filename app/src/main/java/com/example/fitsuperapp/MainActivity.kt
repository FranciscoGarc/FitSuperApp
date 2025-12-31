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

// Definimos las pantallas posibles
enum class Screen {
    HOME, GYM, HIIT_SELECTION, HIIT, MANAGE_DATA
}

class MainActivity : ComponentActivity() {
    
    // Inicializamos la base de datos
    private val database by lazy { AppDatabase.getDatabase(this) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializamos los ViewModels con sus factories
        val hiitViewModel: HiitViewModel by viewModels { HiitViewModel.Factory(database) }
        val gymViewModel: GymViewModel by viewModels { GymViewModel.Factory(database) }
        val manageDataViewModel: ManageDataViewModel by viewModels { ManageDataViewModel.Factory(database) }

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Color(0xFF111827),
                    primary = Color(0xFF10B981)
                )
            ) {
                // Estado para saber en qué pantalla estamos (Empieza en HOME)
                var currentScreen by remember { mutableStateOf(Screen.HOME) }
                
                // Estado para la rutina HIIT seleccionada
                var selectedHiitRoutineId by remember { mutableStateOf<Long?>(null) }
                
                // Cargar rutinas HIIT para la pantalla de selección
                val hiitRoutines by hiitViewModel.availableRoutines.collectAsState()
                val hiitStepsCountByRoutine by hiitViewModel.stepsCountByRoutine.collectAsState()

                // Interceptamos el botón "Atrás"
                BackHandler(enabled = currentScreen != Screen.HOME) {
                    when (currentScreen) {
                        Screen.HIIT -> currentScreen = Screen.HIIT_SELECTION
                        Screen.HIIT_SELECTION -> currentScreen = Screen.HOME
                        else -> currentScreen = Screen.HOME
                    }
                }

                // Crossfade hace una animación suave al cambiar de pantalla
                Crossfade(targetState = currentScreen, label = "screen_transition") { screen ->
                    when (screen) {
                        Screen.HOME -> HomeScreen(
                            onNavigateToGym = { currentScreen = Screen.GYM },
                            onNavigateToHiit = { 
                                hiitViewModel.refreshRoutines()
                                currentScreen = Screen.HIIT_SELECTION 
                            },
                            onNavigateToManageData = { currentScreen = Screen.MANAGE_DATA }
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
                            onExit = { currentScreen = Screen.HOME }
                        )
                        Screen.MANAGE_DATA -> ManageDataScreen(
                            viewModel = manageDataViewModel,
                            onExit = { 
                                // Refrescar datos cuando volvemos de la pantalla de gestión
                                gymViewModel.refreshRoutines()
                                hiitViewModel.refreshRoutines()
                                currentScreen = Screen.HOME 
                            }
                        )
                    }
                }
            }
        }
    }
}