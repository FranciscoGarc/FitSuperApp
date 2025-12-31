package com.example.fitsuperapp

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitsuperapp.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

// Estados de UI para gestión de datos
data class ManageDataUiState(
    val routines: List<GymRoutineEntity> = emptyList(),
    val selectedRoutine: GymRoutineWithDays? = null,
    val selectedDay: GymDayWithExercises? = null,
    val hiitRoutines: List<HiitRoutineEntity> = emptyList(),
    val selectedHiitRoutine: HiitRoutineWithSteps? = null,
    val hiitSteps: List<HiitStepEntity> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class ManageDataViewModel(private val database: AppDatabase) : ViewModel() {
    
    private val repository = GymRepository(database)
    private val hiitRoutineDao = database.hiitRoutineDao()
    private val hiitDao = database.hiitStepDao()
    
    private val _uiState = MutableStateFlow(ManageDataUiState())
    val uiState = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                repository.getAllRoutinesFlow().collect { routines ->
                    _uiState.value = _uiState.value.copy(
                        routines = routines,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error cargando datos: ${e.message}",
                    isLoading = false
                )
            }
        }
        
        // Cargar rutinas HIIT
        viewModelScope.launch {
            hiitRoutineDao.getAllRoutines().collect { routines ->
                _uiState.value = _uiState.value.copy(hiitRoutines = routines)
            }
        }
        
        // Cargar pasos HIIT (para compatibilidad)
        viewModelScope.launch {
            hiitDao.getAllSteps().collect { steps ->
                _uiState.value = _uiState.value.copy(hiitSteps = steps)
            }
        }
    }
    
    // --- RUTINAS ---
    
    fun selectRoutine(routineId: Long) {
        viewModelScope.launch {
            val routineWithDays = repository.getRoutineWithDays(routineId)
            _uiState.value = _uiState.value.copy(selectedRoutine = routineWithDays)
        }
    }
    
    fun clearSelectedRoutine() {
        _uiState.value = _uiState.value.copy(selectedRoutine = null, selectedDay = null)
    }
    
    fun addRoutine(name: String, routineId: String) {
        viewModelScope.launch {
            try {
                repository.insertRoutine(
                    GymRoutineEntity(routineId = routineId, name = name)
                )
                _uiState.value = _uiState.value.copy(successMessage = "Rutina creada")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error: ${e.message}")
            }
        }
    }
    
    fun updateRoutine(routine: GymRoutineEntity) {
        viewModelScope.launch {
            try {
                repository.updateRoutine(routine)
                selectRoutine(routine.id) // Refrescar
                _uiState.value = _uiState.value.copy(successMessage = "Rutina actualizada")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error: ${e.message}")
            }
        }
    }
    
    fun deleteRoutine(routine: GymRoutineEntity) {
        viewModelScope.launch {
            try {
                repository.deleteRoutine(routine)
                clearSelectedRoutine()
                _uiState.value = _uiState.value.copy(successMessage = "Rutina eliminada")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error: ${e.message}")
            }
        }
    }
    
    // --- DÍAS ---
    
    fun selectDay(dayId: Long) {
        viewModelScope.launch {
            val dayWithExercises = repository.getDayWithExercises(dayId)
            _uiState.value = _uiState.value.copy(selectedDay = dayWithExercises)
        }
    }
    
    fun clearSelectedDay() {
        _uiState.value = _uiState.value.copy(selectedDay = null)
    }
    
    fun addDay(routineId: Long, title: String, dayIndex: Int) {
        viewModelScope.launch {
            try {
                repository.insertDay(
                    GymDayEntity(routineId = routineId, dayIndex = dayIndex, title = title)
                )
                selectRoutine(routineId) // Refrescar rutina
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error: ${e.message}")
            }
        }
    }
    
    fun updateDay(day: GymDayEntity) {
        viewModelScope.launch {
            try {
                repository.updateDay(day)
                _uiState.value.selectedRoutine?.let { selectRoutine(it.routine.id) }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error: ${e.message}")
            }
        }
    }
    
    fun deleteDay(day: GymDayEntity) {
        viewModelScope.launch {
            try {
                repository.deleteDay(day)
                clearSelectedDay()
                _uiState.value.selectedRoutine?.let { selectRoutine(it.routine.id) }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error: ${e.message}")
            }
        }
    }
    
    // --- EJERCICIOS ---
    
    fun addExercise(dayId: Long, name: String, sets: Int, reps: Int, rest: Int, exerciseIndex: Int) {
        viewModelScope.launch {
            try {
                repository.insertExercise(
                    GymExerciseEntity(
                        dayId = dayId,
                        exerciseIndex = exerciseIndex,
                        name = name,
                        sets = sets,
                        baseReps = reps,
                        restSeconds = rest
                    )
                )
                selectDay(dayId) // Refrescar día
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error: ${e.message}")
            }
        }
    }
    
    fun updateExercise(exercise: GymExerciseEntity) {
        viewModelScope.launch {
            try {
                repository.updateExercise(exercise)
                selectDay(exercise.dayId) // Refrescar día
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error: ${e.message}")
            }
        }
    }
    
    fun deleteExercise(exercise: GymExerciseEntity) {
        viewModelScope.launch {
            try {
                repository.deleteExercise(exercise)
                selectDay(exercise.dayId) // Refrescar día
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error: ${e.message}")
            }
        }
    }
    
    // --- HIIT ROUTINES ---
    
    fun selectHiitRoutine(routineId: Long) {
        viewModelScope.launch {
            val routine = hiitRoutineDao.getRoutineById(routineId)
            val steps = hiitDao.getStepsByRoutineSync(routineId)
            if (routine != null) {
                _uiState.value = _uiState.value.copy(
                    selectedHiitRoutine = HiitRoutineWithSteps(routine, steps)
                )
            }
        }
    }
    
    fun clearSelectedHiitRoutine() {
        _uiState.value = _uiState.value.copy(selectedHiitRoutine = null)
    }
    
    fun addHiitRoutine(name: String) {
        viewModelScope.launch {
            try {
                hiitRoutineDao.insertRoutine(
                    HiitRoutineEntity(name = name)
                )
                _uiState.value = _uiState.value.copy(successMessage = "Rutina HIIT creada")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error: ${e.message}")
            }
        }
    }
    
    fun updateHiitRoutine(routine: HiitRoutineEntity) {
        viewModelScope.launch {
            try {
                hiitRoutineDao.updateRoutine(routine)
                selectHiitRoutine(routine.id)
                _uiState.value = _uiState.value.copy(successMessage = "Rutina HIIT actualizada")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error: ${e.message}")
            }
        }
    }
    
    fun deleteHiitRoutine(routine: HiitRoutineEntity) {
        viewModelScope.launch {
            try {
                hiitRoutineDao.deleteRoutine(routine)
                clearSelectedHiitRoutine()
                _uiState.value = _uiState.value.copy(successMessage = "Rutina HIIT eliminada")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error: ${e.message}")
            }
        }
    }
    
    // --- HIIT STEPS ---
    
    fun addHiitStep(routineId: Long, name: String, duration: Int, type: String, stepIndex: Int) {
        viewModelScope.launch {
            try {
                hiitDao.insertStep(
                    HiitStepEntity(
                        routineId = routineId,
                        stepIndex = stepIndex,
                        name = name,
                        durationSeconds = duration,
                        type = type
                    )
                )
                selectHiitRoutine(routineId) // Refresh
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error: ${e.message}")
            }
        }
    }
    
    fun updateHiitStep(step: HiitStepEntity) {
        viewModelScope.launch {
            try {
                hiitDao.updateStep(step)
                selectHiitRoutine(step.routineId) // Refresh
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error: ${e.message}")
            }
        }
    }
    
    fun deleteHiitStep(step: HiitStepEntity) {
        viewModelScope.launch {
            try {
                hiitDao.deleteStep(step)
                selectHiitRoutine(step.routineId) // Refresh
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error: ${e.message}")
            }
        }
    }
    
    // --- CSV EXPORT/IMPORT ---
    
    fun exportToCsv(context: Context, uri: Uri, exportType: String) {
        viewModelScope.launch {
            try {
                val outputStream = context.contentResolver.openOutputStream(uri)
                outputStream?.bufferedWriter()?.use { writer ->
                    when (exportType) {
                        "gym" -> {
                            // Header
                            writer.write("routine_id,routine_name,day_index,day_title,exercise_index,exercise_name,sets,reps,rest_seconds\n")
                            
                            _uiState.value.routines.forEach { routine ->
                                val routineWithDays = repository.getRoutineWithDays(routine.id)
                                routineWithDays?.days?.forEach { dayWithExercises ->
                                    if (dayWithExercises.exercises.isEmpty()) {
                                        // Día sin ejercicios
                                        writer.write("${routine.routineId},${escapeCSV(routine.name)},${dayWithExercises.day.dayIndex},${escapeCSV(dayWithExercises.day.title)},,,,\n")
                                    } else {
                                        dayWithExercises.exercises.forEach { exercise ->
                                            writer.write("${routine.routineId},${escapeCSV(routine.name)},${dayWithExercises.day.dayIndex},${escapeCSV(dayWithExercises.day.title)},${exercise.exerciseIndex},${escapeCSV(exercise.name)},${exercise.sets},${exercise.baseReps},${exercise.restSeconds}\n")
                                        }
                                    }
                                }
                            }
                        }
                        "hiit" -> {
                            // Header
                            writer.write("step_index,name,duration_seconds,type\n")
                            
                            _uiState.value.hiitSteps.forEach { step ->
                                writer.write("${step.stepIndex},${escapeCSV(step.name)},${step.durationSeconds},${step.type}\n")
                            }
                        }
                    }
                }
                _uiState.value = _uiState.value.copy(successMessage = "Exportación completada")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error exportando: ${e.message}")
            }
        }
    }
    
    fun importFromCsv(context: Context, uri: Uri, importType: String) {
        viewModelScope.launch {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val reader = BufferedReader(InputStreamReader(inputStream))
                val lines = reader.readLines()
                reader.close()
                
                if (lines.size <= 1) {
                    _uiState.value = _uiState.value.copy(errorMessage = "Archivo vacío o solo contiene cabecera")
                    return@launch
                }
                
                when (importType) {
                    "gym" -> importGymCsv(lines.drop(1)) // Skip header
                    "hiit" -> importHiitCsv(lines.drop(1)) // Skip header
                }
                
                _uiState.value = _uiState.value.copy(successMessage = "Importación completada")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error importando: ${e.message}")
            }
        }
    }
    
    private suspend fun importGymCsv(lines: List<String>) {
        // Agrupar por rutina
        val routineGroups = mutableMapOf<String, MutableList<List<String>>>()
        
        lines.forEach { line ->
            val parts = parseCSVLine(line)
            if (parts.size >= 4) {
                val routineId = parts[0]
                routineGroups.getOrPut(routineId) { mutableListOf() }.add(parts)
            }
        }
        
        routineGroups.forEach { (routineId, rows) ->
            // Verificar si la rutina ya existe
            var routine = repository.getRoutineByStringId(routineId)
            val routineName = rows.firstOrNull()?.getOrNull(1) ?: routineId
            
            if (routine == null) {
                val newId = repository.insertRoutine(
                    GymRoutineEntity(routineId = routineId, name = routineName)
                )
                routine = GymRoutineEntity(id = newId, routineId = routineId, name = routineName)
            }
            
            // Agrupar por día
            val dayGroups = rows.groupBy { it.getOrNull(2)?.toIntOrNull() ?: 0 }
            
            dayGroups.forEach { (dayIndex, dayRows) ->
                val dayTitle = dayRows.firstOrNull()?.getOrNull(3) ?: "Día ${dayIndex + 1}"
                val dayId = database.gymDayDao().insertDay(
                    GymDayEntity(routineId = routine.id, dayIndex = dayIndex, title = dayTitle)
                )
                
                dayRows.forEachIndexed { idx, row ->
                    val exerciseName = row.getOrNull(5)
                    if (!exerciseName.isNullOrBlank()) {
                        database.gymExerciseDao().insertExercise(
                            GymExerciseEntity(
                                dayId = dayId,
                                exerciseIndex = row.getOrNull(4)?.toIntOrNull() ?: idx,
                                name = exerciseName,
                                sets = row.getOrNull(6)?.toIntOrNull() ?: 3,
                                baseReps = row.getOrNull(7)?.toIntOrNull() ?: 10,
                                restSeconds = row.getOrNull(8)?.toIntOrNull() ?: 60
                            )
                        )
                    }
                }
            }
        }
    }
    
    private suspend fun importHiitCsv(lines: List<String>) {
        // Create a new HIIT routine for imported data
        val routineId = hiitRoutineDao.insertRoutine(
            HiitRoutineEntity(name = "Imported HIIT Routine")
        )
        
        lines.forEachIndexed { index, line ->
            val parts = parseCSVLine(line)
            if (parts.size >= 4) {
                hiitDao.insertStep(
                    HiitStepEntity(
                        routineId = routineId,
                        stepIndex = parts[0].toIntOrNull() ?: index,
                        name = parts[1],
                        durationSeconds = parts[2].toIntOrNull() ?: 30,
                        type = parts[3].uppercase()
                    )
                )
            }
        }
    }
    
    private fun escapeCSV(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
    
    private fun parseCSVLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        
        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString())
        
        return result
    }
    
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
    
    // Factory para crear el ViewModel
    class Factory(private val database: AppDatabase) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ManageDataViewModel::class.java)) {
                return ManageDataViewModel(database) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
