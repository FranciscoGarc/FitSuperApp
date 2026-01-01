package com.example.fitsuperapp

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitsuperapp.data.AppDatabase
import com.example.fitsuperapp.data.GymExerciseEntity
import com.example.fitsuperapp.data.GymRepository
import com.example.fitsuperapp.data.GymRoutineWithDays
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import com.example.fitsuperapp.data.GymRoutineEntity

class GymViewModel(private val repository: GymRepository) : ViewModel() {

    // --- ESTADO ---
    private val _currentRoutineId = MutableStateFlow("torso-pierna")
    private val _currentDayIndex = MutableStateFlow(0)

    // El día actual que se mostrará en pantalla
    private val _currentDay = MutableStateFlow<GymDay?>(null)
    
    // Lista de rutinas disponibles
    private val _availableRoutines = MutableStateFlow<List<GymRoutineEntity>>(emptyList())

    // Timer de descanso (FitTrack tiene uno integrado en el header)
    private val _restTimerSeconds = MutableStateFlow(0)
    private val _isResting = MutableStateFlow(false)
    
    // Estado de carga
    private val _isLoading = MutableStateFlow(true)

    val currentRoutineId = _currentRoutineId.asStateFlow()
    val currentDay = _currentDay.asStateFlow()
    val restTimerSeconds = _restTimerSeconds.asStateFlow()
    val isResting = _isResting.asStateFlow()
    val isLoading = _isLoading.asStateFlow()
    val availableRoutines = _availableRoutines.asStateFlow()

    private var timerJob: Job? = null
    
    // Cache de rutinas cargadas
    private var cachedRoutines: MutableMap<String, GymRoutineWithDays> = mutableMapOf()
    
    // Cache de estados de completion por rutina y día: "routineId_dayIndex" -> Map<exerciseId, List<Boolean>>
    private val completionStateCache = mutableMapOf<String, Map<String, List<Boolean>>>()

    init {
        loadRoutines()
    }
    
    private fun loadRoutines() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Cargar todas las rutinas disponibles
            repository.getAllRoutinesFlow().collect { routines ->
                _availableRoutines.value = routines
                
                // Cargar detalles de cada rutina para el cache
                routines.forEach { routine ->
                    val fullRoutine = repository.getRoutineWithDays(routine.id)
                    if (fullRoutine != null) {
                        cachedRoutines[routine.routineId] = fullRoutine
                    }
                }
                
                // Si la rutina actual no existe en las cargadas, seleccionar la primera
                if (!cachedRoutines.containsKey(_currentRoutineId.value)) {
                    _availableRoutines.value.firstOrNull()?.let {
                        _currentRoutineId.value = it.routineId
                    }
                }
                
                loadDay()
                _isLoading.value = false
            }
        }
    }
    
    fun refreshRoutines() {
        loadRoutines()
    }

    // Cargar el día según la rutina seleccionada y el índice
    private fun loadDay() {
        val routine = cachedRoutines[_currentRoutineId.value] ?: cachedRoutines.values.firstOrNull()
        if (routine == null) {
            _currentDay.value = null
            return
        }
        
        // Aseguramos que el índice no se salga de rango
        if (_currentDayIndex.value >= routine.days.size) _currentDayIndex.value = 0
        if (_currentDayIndex.value < 0) _currentDayIndex.value = routine.days.size - 1

        val dayWithExercises = routine.days[_currentDayIndex.value]
        
        // Convertir entidades a modelos de UI
        val exercises = dayWithExercises.exercises.map { entity ->
            GymExercise(
                name = entity.name,
                sets = entity.sets,
                baseReps = entity.baseReps,
                restSeconds = entity.restSeconds
                // id se genera automáticamente a partir del nombre
            )
        }
        
        // Crear key única para este día específico de esta rutina
        val cacheKey = "${_currentRoutineId.value}_${_currentDayIndex.value}"
        
        // Restaurar estado desde el cache global, o inicializar con false
        val cachedCompletion = completionStateCache[cacheKey] ?: emptyMap()
        val newCompletionState = exercises.associate { exercise ->
            exercise.id to (cachedCompletion[exercise.id] ?: List(exercise.sets) { false })
        }
        
        _currentDay.value = GymDay(
            title = dayWithExercises.day.title,
            exercises = exercises,
            completionState = newCompletionState
        )
    }

    // Cambiar de día (Botones < >)
    fun changeDay(offset: Int) {
        _currentDayIndex.value += offset
        loadDay()
        cancelRestTimer() // Cancelar descanso si cambias de día
    }

    // Cambiar rutina
    fun selectRoutine(routineId: String) {
        if (_currentRoutineId.value != routineId) {
            _currentRoutineId.value = routineId
            _currentDayIndex.value = 0 // Reiniciar al día 1
            loadDay()
            cancelRestTimer()
        }
    }

    // Cambiar rutina (Switch Torso/PPL) - Mantenido por compatibilidad si es necesario, pero selectRoutine es preferido
    fun toggleRoutine(isPPL: Boolean) {
        selectRoutine(if (isPPL) "ppl" else "torso-pierna")
    }

    // Marcar un set como completado
    fun toggleSet(exerciseIndex: Int, setIndex: Int) {
        val currentDayData = _currentDay.value ?: return
        val exercise = currentDayData.exercises[exerciseIndex]
        val exerciseId = exercise.id
        
        val currentCompletion = currentDayData.completionState[exerciseId] ?: return
        
        // Verificar si el set anterior está completado (locking secuencial)
        if (setIndex > 0) {
            val previousCompleted = currentCompletion.getOrElse(setIndex - 1) { false }
            if (!previousCompleted) {
                return // No permitir marcar si el anterior no está completo
            }
        }
        
        // Crear nuevo estado inmutable
        val newSetCompletions = currentCompletion.toMutableList()
        newSetCompletions[setIndex] = !newSetCompletions[setIndex]
        
        // Actualizar el estado completo de manera inmutable
        val newCompletionState = currentDayData.completionState.toMutableMap()
        newCompletionState[exerciseId] = newSetCompletions
        
        _currentDay.value = currentDayData.copy(
            completionState = newCompletionState
        )
        
        // Guardar en el cache global para persistencia
        val cacheKey = "${_currentRoutineId.value}_${_currentDayIndex.value}"
        completionStateCache[cacheKey] = newCompletionState

        // Si se completó (se marcó true), iniciamos el descanso automáticamente
        if (newSetCompletions[setIndex]) {
            startRestTimer(exercise.restSeconds)
        }
    }

    // Reiniciar todos los checkboxes del día actual
    fun resetCurrentDay() {
        val currentDayData = _currentDay.value ?: return
        
        // Reiniciar todos los estados a false de manera inmutable
        val resetCompletionState = currentDayData.exercises.associate { exercise ->
            exercise.id to List(exercise.sets) { false }
        }
        
        _currentDay.value = currentDayData.copy(
            completionState = resetCompletionState
        )
        
        // También limpiar del cache global
        val cacheKey = "${_currentRoutineId.value}_${_currentDayIndex.value}"
        completionStateCache[cacheKey] = resetCompletionState
        
        cancelRestTimer() // También cancelamos el timer de descanso
    }

    // Lógica del Timer de Descanso
    fun startRestTimer(seconds: Int) {
        timerJob?.cancel()
        _isResting.value = true
        _restTimerSeconds.value = seconds

        timerJob = viewModelScope.launch {
            while (_restTimerSeconds.value > 0) {
                delay(1000)
                _restTimerSeconds.value -= 1
            }
            _isResting.value = false
            // Aquí podrías poner un sonido de "beep" igual que en HIIT
        }
    }

    fun cancelRestTimer() {
        timerJob?.cancel()
        _isResting.value = false
        _restTimerSeconds.value = 0
    }
    
    // Factory para crear el ViewModel con dependencias
    class Factory(private val database: AppDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(GymViewModel::class.java)) {
                return GymViewModel(GymRepository(database)) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}