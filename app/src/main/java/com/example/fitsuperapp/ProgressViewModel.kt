package com.example.fitsuperapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitsuperapp.data.AppDatabase
import com.example.fitsuperapp.data.WorkoutSessionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ProgressViewModel(private val database: AppDatabase) : ViewModel() {

    private val sessionDao = database.workoutSessionDao()

    private val _streak = MutableStateFlow(0)
    val streak = _streak.asStateFlow()

    private val _totalTimeFormatted = MutableStateFlow("0h 0m")
    val totalTimeFormatted = _totalTimeFormatted.asStateFlow()
    
    // Mapa de Fecha (String dd/MM/yyyy) -> Boolean (hizo ejercicio)
    private val _workoutDays = MutableStateFlow<Map<String, Boolean>>(emptyMap())
    val workoutDays = _workoutDays.asStateFlow()
    
    private val _quote = MutableStateFlow("")
    val quote = _quote.asStateFlow()

    private val quotes = listOf(
        "El único mal entrenamiento es el que no sucedió.",
        "Tu cuerpo puede con todo, es tu mente a la que tienes que convencer.",
        "No pares cuando duela, para cuando hayas terminado.",
        "Cada gota de sudor es un paso más cerca de tu meta.",
        "La disciplina es hacer lo que tienes que hacer, incluso cuando no quieres.",
        "El dolor de hoy es la fuerza de mañana.",
        "No busques tiempo, créalo.",
        "La motivación es lo que te pone en marcha, el hábito es lo que hace que sigas.",
        "Entrena como una bestia para lucir como una belleza."
    )

    init {
        loadData()
        _quote.value = quotes.random()
    }
    
    fun refresh() {
        loadData()
        _quote.value = quotes.random()
    }

    private fun loadData() {
        viewModelScope.launch {
            sessionDao.getAllSessions().collect { sessions ->
                calculateStats(sessions)
            }
        }
    }

    private fun calculateStats(sessions: List<WorkoutSessionEntity>) {
        // 1. Total Time
        val totalSeconds = sessions.sumOf { it.durationSeconds }
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        _totalTimeFormatted.value = "${hours}h ${minutes}m"

        // 2. Workout Days Map
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val daysMap = sessions.associate { 
            dateFormat.format(Date(it.startTime)) to true 
        }
        _workoutDays.value = daysMap

        // 3. Streak Calculation
        // Ordenar fechas únicas descendente
        val uniqueDates = sessions
            .map { dateFormat.parse(dateFormat.format(Date(it.startTime)))!! }
            .distinct()
            .sortedDescending()

        var currentStreak = 0
        val cal = Calendar.getInstance()
        // Setear a medianoche de hoy para comparar
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        
        // Verificar si entrenó hoy
        var lastDate = if (uniqueDates.isNotEmpty() && isSameDay(uniqueDates[0], today)) {
             currentStreak++
             uniqueDates[0]
        } else {
            // Si no entrenó hoy, checkeamos ayer
             val yesterday = Calendar.getInstance().apply {
                 time = today
                 add(Calendar.DAY_OF_YEAR, -1)
             }.time
             if (uniqueDates.isNotEmpty() && isSameDay(uniqueDates[0], yesterday)) {
                 currentStreak++
                 uniqueDates[0]
             } else {
                 null // Racha rota o 0
             }
        }

        if (lastDate != null) {
            // Iterar hacia atrás
            for (i in 1 until uniqueDates.size) {
                val expectedPrevDay = Calendar.getInstance().apply {
                    time = lastDate
                    add(Calendar.DAY_OF_YEAR, -1)
                }.time
                
                if (isSameDay(uniqueDates[i], expectedPrevDay)) {
                    currentStreak++
                    lastDate = uniqueDates[i]
                } else {
                    break
                }
            }
        }
        
        _streak.value = currentStreak
    }
    
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return fmt.format(date1) == fmt.format(date2)
    }

    class Factory(private val database: AppDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProgressViewModel::class.java)) {
                return ProgressViewModel(database) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
