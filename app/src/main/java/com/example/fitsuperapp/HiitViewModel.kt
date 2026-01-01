package com.example.fitsuperapp

import android.app.Application
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitsuperapp.data.AppDatabase
import com.example.fitsuperapp.data.HiitRoutineEntity
import com.example.fitsuperapp.data.HiitStepEntity
import com.example.fitsuperapp.data.WorkoutSessionEntity
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HiitViewModel(application: Application, private val database: AppDatabase) : AndroidViewModel(application) {

    private val _currentStepIndex = MutableStateFlow(0)
    // Usamos el estado del Service si está en play, sino local?
    // Para simplificar, usamos TimerService como fuente de verdad del tiempo
    val timeLeft = TimerService.timeLeft
    
    private val _totalTime = MutableStateFlow(1)
    
    // isPlaying lo derivamos del Service o lo mantenemos?
    // TimerService.isRunning es global. Si usas ambos (gym y hiit), podría haber conflictos.
    // Asumimos uso exclusivo.
    val isPlaying = TimerService.isRunning
    
    private val _currentStep = MutableStateFlow<HiitStep?>(null)
    private val _nextStepName = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(true)

    // Estado para saber si la rutina ha terminado
    private val _isWorkoutFinished = MutableStateFlow(false)
    val isWorkoutFinished = _isWorkoutFinished.asStateFlow()
    
    // Estado para rutinas disponibles
    private val _availableRoutines = MutableStateFlow<List<HiitRoutineEntity>>(emptyList())
    val availableRoutines = _availableRoutines.asStateFlow()
    
    // Conteo de pasos por rutina
    private val _stepsCountByRoutine = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val stepsCountByRoutine = _stepsCountByRoutine.asStateFlow()

    private val toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 100)
    
    // Rutina cargada
    private var routine: List<HiitStep> = emptyList()
    private var currentRoutineId: Long? = null
    
    // Single job for timer completion to avoid duplicates
    private var timerCompletionJob: Job? = null
    
    // Flag para indicar si se debe auto-iniciar el timer al cargar
    private var shouldAutoStart = false
    
    // Tracking sesión
    private var workoutStartTime: Long? = null
    private var sessionRoutineName: String = "Entrenamiento HIIT"

    val currentStep = _currentStep.asStateFlow()
    val nextStepName = _nextStepName.asStateFlow()
    val isLoading = _isLoading.asStateFlow()
    val totalTime = _totalTime.asStateFlow()

    // Wrapper para exponer el tiempo correcto a la UI
    // Si el servicio corre, usamos su tiempo. Si no, usamos el tiempo total del paso actual.
    // Esto evita que muestre 0 o tiempos residuales cuando no está activo.
    private val _uiTimeLeft = MutableStateFlow(0L)
    val uiTimeLeft = _uiTimeLeft.asStateFlow()
    
    init {
        refreshRoutines()
        
        viewModelScope.launch {
            // Combinar estados para UI
            kotlinx.coroutines.flow.combine(TimerService.timeLeft, TimerService.isRunning, _currentStep) { time, running, step ->
                if (running) time else (step?.durationSeconds?.times(1000L) ?: 0L)
            }.collect {
                _uiTimeLeft.value = it
            }
        }

        // Observar eventos reales del Timer
        viewModelScope.launch {
            TimerService.timeLeft.collect { millis ->
                val seconds = (millis / 1000).toInt()
                
                // Sonido de cuenta regresiva (3..2..1)
                if (TimerService.isRunning.value && seconds in 1..3) {
                     // Evitar sonar múltiples veces en el mismo segundo (simple check)
                     // O confiar en que el collect viene cada segundo aprox
                     // toneGenerator.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 100)
                }
                
                // Si el tiempo llega a 0 y estaba corriendo...
                if (millis == 0L && _currentStep.value != null && !isWorkoutFinished.value) {
                    // El servicio se detiene solo en 0. Nosotros detectamos el fin.
                    // Pero necesitamos dispararlo UNA vez.
                    // La lógica de avance automático es complicada si el VM muere.
                    // Aquí asumimos que el VM sigue vivo.
                    if (isPlaying.value) { 
                       // No podemos confiar en isPlaying.value porque el servicio lo pone a false al terminar
                    }
                }
            }
        }
    }
    
    // Cargar lista de rutinas disponibles
    fun refreshRoutines() {
        viewModelScope.launch {
            _isLoading.value = true
            
            val routineDao = database.hiitRoutineDao()
            val stepDao = database.hiitStepDao()
            
            val routines = routineDao.getAllRoutinesSync()
            _availableRoutines.value = routines
            
            val stepsCount = mutableMapOf<Long, Int>()
            for (r in routines) {
                val steps = stepDao.getStepsByRoutineSync(r.id)
                stepsCount[r.id] = steps.size
            }
            _stepsCountByRoutine.value = stepsCount
            
            _isLoading.value = false
        }
    }
    
    fun loadRoutine(routineId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _isWorkoutFinished.value = false
            currentRoutineId = routineId
            
            val routineDao = database.hiitRoutineDao()
            val rEntity = routineDao.getRoutineById(routineId)
            sessionRoutineName = rEntity?.name ?: "Entrenamiento HIIT"
            
            val hiitDao = database.hiitStepDao()
            val stepsFromDb = hiitDao.getStepsByRoutineSync(routineId)
            
            // Limpiar cualquier timer previo (ej. de Gym)
            pauseTimer()
            timerCompletionJob?.cancel()
            timerCompletionJob = null
            
            routine = stepsFromDb.map { entity ->
                HiitStep(
                    name = entity.name,
                    durationSeconds = entity.durationSeconds,
                    type = HiitStepType.valueOf(entity.type)
                )
            }
            
            if (routine.isNotEmpty()) {
                shouldAutoStart = true // Auto-iniciar al cargar la rutina
                loadStep(0)
            }
            _isLoading.value = false
            workoutStartTime = System.currentTimeMillis() // Iniciar tracking desde ahora
        }
    }

    private fun loadStep(index: Int) {
        if (index >= routine.size) {
            finishWorkout()
            return
        }
        
        _currentStepIndex.value = index
        val step = routine[index]
        _currentStep.value = step
        _totalTime.value = step.durationSeconds

        if (index + 1 < routine.size) {
            _nextStepName.value = "Siguiente: ${routine[index + 1].name}"
        } else {
            _nextStepName.value = "¡Último ejercicio!"
        }

        // Sonido C5
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_PIP, 150)
        
        // Auto-start si se configuró
        if (shouldAutoStart) {
            if (workoutStartTime == null) {
                workoutStartTime = System.currentTimeMillis()
            }
            startTimerService(step.durationSeconds * 1000L)
        }
    }

    fun togglePlayPause() {
        if (isPlaying.value) {
            pauseTimer()
        } else {
            // Empezar tracking de sesión total
            if (workoutStartTime == null) {
                workoutStartTime = System.currentTimeMillis()
            }
            
            // Usar el tiempo guardado en pausa, o el tiempo del paso si es inicio fresco
            val stepMillis = _currentStep.value?.durationSeconds?.times(1000L) ?: 30000L
            
            // Prioridad: savedTimeOnPause > uiTimeLeft > stepMillis
            val durationToRun = when {
                savedTimeOnPause > 500 -> {
                    val time = savedTimeOnPause
                    savedTimeOnPause = 0L // Reset para el próximo uso
                    time
                }
                _uiTimeLeft.value > 500 -> _uiTimeLeft.value
                else -> stepMillis
            }
            
            startTimerService(durationToRun) // Iniciar servicio
        }
    }

    private fun startTimerService(durationMillis: Long) {
        val context = getApplication<Application>()
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_START_TIMER
            putExtra(TimerService.EXTRA_DURATION, durationMillis)
            putExtra(TimerService.EXTRA_TITLE, _currentStep.value?.name ?: "HIIT")
        }
        context.startService(intent)
        
        // Cancelar job anterior para evitar listeners múltiples
        timerCompletionJob?.cancel()
        
        // MANEJO MANUAL DEL BUCLE:
        // El VM debe observar el fin del timer.
        timerCompletionJob = viewModelScope.launch {
            var finished = false
            TimerService.timeLeft.collect { millis ->
                 // Solo reaccionar si NO hemos terminado manualmente, NO es un salto manual Y NO estamos pausando
                 if (!_isWorkoutFinished.value && !isManualSkip && !isPausing) {
                     if (millis == 0L && !finished) {
                         // Pequeño delay para asegurar que ui actualice
                         delay(500)
                         // Verificar de nuevo todas las condiciones
                         if (TimerService.isRunning.value == false && !isManualSkip && !isPausing) {
                             finished = true
                             onStepFinished()
                         }
                     }
                 }
            }
        }
    }
    
    private fun onStepFinished() {
        if (_isWorkoutFinished.value) return // Doble check
        
        // Pasar al siguiente paso automágicamente
        val nextIndex = _currentStepIndex.value + 1
        if (nextIndex < routine.size) {
             loadStep(nextIndex)
             val nextStep = routine[nextIndex]
             startTimerService(nextStep.durationSeconds * 1000L)
        } else {
             finishWorkout()
        }
    }

    // Flag para indicar que estamos pausando (evitar detección de fin)
    private var isPausing = false
    
    // Tiempo guardado cuando se pausa (para reanudar correctamente)
    private var savedTimeOnPause: Long = 0L
    
    private fun pauseTimer() {
        isPausing = true
        // Guardar el tiempo restante ANTES de detener el servicio
        savedTimeOnPause = TimerService.timeLeft.value
        
        // Cancelar el job de detección para evitar falsos positivos
        timerCompletionJob?.cancel()
        timerCompletionJob = null
        
        val context = getApplication<Application>()
        val intent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_STOP_TIMER
        }
        context.startService(intent)
        
        // Actualizar uiTimeLeft para que muestre el tiempo guardado
        _uiTimeLeft.value = savedTimeOnPause
        
        // Reset del flag después de un pequeño delay
        viewModelScope.launch {
            delay(500)
            isPausing = false
        }
    }

    fun add10Seconds() {
        val additionalTime = 10000L
        
        if (isPlaying.value) {
            val newTime = timeLeft.value + additionalTime
            // Actualizar totalTime para que el círculo refleje el nuevo total
            _totalTime.value = (newTime / 1000).toInt()
            startTimerService(newTime)
        } else {
            // Si está pausado, actualizar el tiempo guardado
            val currentTimeMs = if (savedTimeOnPause > 0) savedTimeOnPause else _uiTimeLeft.value
            val newTime = currentTimeMs + additionalTime
            savedTimeOnPause = newTime
            _totalTime.value = (newTime / 1000).toInt()
            _uiTimeLeft.value = newTime
        }
    }

    // Flag to ignore timer finish event when manually skipping
    private var isManualSkip = false

    fun nextStep() {
        if (routine.isNotEmpty() && _currentStepIndex.value < routine.size - 1) {
            isManualSkip = true // Set flag
            pauseTimer()
            loadStep(_currentStepIndex.value + 1)
            // Reset flag after a delay or just let the timer event pass
            viewModelScope.launch {
                delay(1000)
                isManualSkip = false
            }
        } else {
            finishWorkout()
        }
    }

    fun skipWarmup() {
        // Encontrar el primer paso que NO sea WARMUP
        val nextIndex = routine.indexOfFirst { it.type != HiitStepType.WARMUP }
        if (nextIndex != -1) {
            isManualSkip = true // Set flag
            pauseTimer()
            loadStep(nextIndex)
            viewModelScope.launch {
                delay(1000)
                isManualSkip = false
            }
        }
    }

    fun finishWorkout() {
        pauseTimer()
        _isWorkoutFinished.value = true
        toneGenerator.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 400)
        
        // Guardar sesión
        val start = workoutStartTime
        if (start != null) {
            val durationSeconds = (System.currentTimeMillis() - start) / 1000
            val sessionDao = database.workoutSessionDao()
            viewModelScope.launch {
                sessionDao.insertSession(
                    WorkoutSessionEntity(
                        type = "HIIT",
                        routineName = sessionRoutineName,
                        startTime = start,
                        endTime = System.currentTimeMillis(),
                        durationSeconds = durationSeconds
                    )
                )
            }
            workoutStartTime = null
        }
    }

    fun restartWorkout() {
        _isWorkoutFinished.value = false
        timerCompletionJob?.cancel()
        timerCompletionJob = null
        shouldAutoStart = true // Auto-iniciar al reiniciar
        if (routine.isNotEmpty()) {
            loadStep(0)
        }
        workoutStartTime = System.currentTimeMillis()
    }

    override fun onCleared() {
        super.onCleared()
        toneGenerator.release()
    }
    
    class Factory(private val application: Application, private val database: AppDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HiitViewModel::class.java)) {
                return HiitViewModel(application, database) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
