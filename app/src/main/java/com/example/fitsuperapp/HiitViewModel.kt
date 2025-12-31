package com.example.fitsuperapp

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitsuperapp.data.AppDatabase
import com.example.fitsuperapp.data.HiitRoutineEntity
import com.example.fitsuperapp.data.HiitStepEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HiitViewModel(private val database: AppDatabase) : ViewModel() {

    private val _currentStepIndex = MutableStateFlow(0)
    private val _timeLeft = MutableStateFlow(0)
    private val _totalTime = MutableStateFlow(1)
    private val _isPlaying = MutableStateFlow(false)
    private val _currentStep = MutableStateFlow<HiitStep?>(null)
    private val _nextStepName = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(true)

    // Estado para saber si la rutina ha terminado
    private val _isWorkoutFinished = MutableStateFlow(false)
    val isWorkoutFinished = _isWorkoutFinished.asStateFlow()
    
    // Estado para rutinas disponibles (para la pantalla de selección)
    private val _availableRoutines = MutableStateFlow<List<HiitRoutineEntity>>(emptyList())
    val availableRoutines = _availableRoutines.asStateFlow()
    
    // Conteo de pasos por rutina
    private val _stepsCountByRoutine = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val stepsCountByRoutine = _stepsCountByRoutine.asStateFlow()

    private var timerJob: Job? = null
    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    
    // Rutina cargada desde la base de datos
    private var routine: List<HiitStep> = emptyList()
    private var currentRoutineId: Long? = null

    val timeLeft = _timeLeft.asStateFlow()
    val totalTime = _totalTime.asStateFlow()
    val isPlaying = _isPlaying.asStateFlow()
    val currentStep = _currentStep.asStateFlow()
    val nextStepName = _nextStepName.asStateFlow()
    val isLoading = _isLoading.asStateFlow()

    init {
        refreshRoutines()
    }
    
    // Cargar lista de rutinas disponibles
    fun refreshRoutines() {
        viewModelScope.launch {
            _isLoading.value = true
            
            val routineDao = database.hiitRoutineDao()
            val stepDao = database.hiitStepDao()
            
            val routines = routineDao.getAllRoutinesSync()
            _availableRoutines.value = routines
            
            // Contar pasos para cada rutina
            val stepsCount = mutableMapOf<Long, Int>()
            for (r in routines) {
                val steps = stepDao.getStepsByRoutineSync(r.id)
                stepsCount[r.id] = steps.size
            }
            _stepsCountByRoutine.value = stepsCount
            
            _isLoading.value = false
        }
    }
    
    // Cargar una rutina específica por ID
    fun loadRoutine(routineId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _isWorkoutFinished.value = false
            currentRoutineId = routineId
            
            val hiitDao = database.hiitStepDao()
            val stepsFromDb = hiitDao.getStepsByRoutineSync(routineId)
            
            routine = stepsFromDb.map { entity ->
                HiitStep(
                    name = entity.name,
                    durationSeconds = entity.durationSeconds,
                    type = HiitStepType.valueOf(entity.type)
                )
            }
            
            if (routine.isNotEmpty()) {
                loadStep(0)
            }
            _isLoading.value = false
        }
    }

    private fun loadStep(index: Int) {
        if (index >= routine.size) {
            finishWorkout() // Si se acaban los pasos, terminamos
            return
        }
        
        _currentStepIndex.value = index
        val step = routine[index]
        _currentStep.value = step
        _timeLeft.value = step.durationSeconds
        _totalTime.value = step.durationSeconds

        if (index + 1 < routine.size) {
            _nextStepName.value = "Siguiente: ${routine[index + 1].name}"
        } else {
            _nextStepName.value = "¡Último ejercicio!"
        }

        // Sonido C5 (similar al script original)
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
    }

    fun togglePlayPause() {
        if (_isPlaying.value) pauseTimer() else startTimer()
    }

    private fun startTimer() {
        if (_isPlaying.value) return // Prevenir múltiples timers
        _isPlaying.value = true
        
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isPlaying.value && !_isWorkoutFinished.value) {
                if (_timeLeft.value > 0) {
                    delay(1000L)
                    _timeLeft.value -= 1
                    if (_timeLeft.value in 1..3) {
                        toneGenerator.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 100)
                    }
                } else {
                    // El tiempo se acabó, pasar al siguiente paso
                    val nextIndex = _currentStepIndex.value + 1
                    if (nextIndex < routine.size) {
                         loadStep(nextIndex)
                         // No necesitamos reiniciar el timer, el bucle while continúa
                    } else {
                         finishWorkout()
                         break // Salir del bucle
                    }
                }
            }
        }
    }

    private fun pauseTimer() {
        _isPlaying.value = false
        timerJob?.cancel()
    }

    fun add10Seconds() {
        _timeLeft.value += 10
        if (_timeLeft.value > _totalTime.value) _totalTime.value = _timeLeft.value
    }

    fun skipWarmup() {
        val nextIndex = routine.indexOfFirst { it.type != HiitStepType.WARMUP }
        if (nextIndex != -1) {
            loadStep(nextIndex)
            pauseTimer()
        }
    }

    // Lógica para terminar manualmente o al final
    fun finishWorkout() {
        pauseTimer()
        _isWorkoutFinished.value = true
        // Sonido de victoria (C6)
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400)
    }

    // Reiniciar la rutina desde cero
    fun restartWorkout() {
        _isWorkoutFinished.value = false
        if (routine.isNotEmpty()) {
            loadStep(0)
        }
    }

    override fun onCleared() {
        super.onCleared()
        toneGenerator.release()
    }
    
    // Factory para crear el ViewModel con dependencias
    class Factory(private val database: AppDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HiitViewModel::class.java)) {
                return HiitViewModel(database) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
