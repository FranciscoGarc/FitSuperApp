package com.example.fitsuperapp.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Repositorio que abstrae el acceso a los datos de gimnasio.
 * Convierte entre entidades Room y modelos de UI.
 */
class GymRepository(private val database: AppDatabase) {
    
    private val routineDao = database.gymRoutineDao()
    private val dayDao = database.gymDayDao()
    private val exerciseDao = database.gymExerciseDao()
    
    // --- RUTINAS ---
    
    fun getAllRoutinesFlow(): Flow<List<GymRoutineEntity>> = routineDao.getAllRoutines()
    
    suspend fun getRoutineByStringId(routineId: String): GymRoutineEntity? {
        return routineDao.getRoutineByStringId(routineId)
    }
    
    suspend fun getRoutineWithDays(routineId: Long): GymRoutineWithDays? {
        val routine = routineDao.getRoutineById(routineId) ?: return null
        val days = dayDao.getDaysByRoutineSync(routineId)
        val daysWithExercises = days.map { day ->
            val exercises = exerciseDao.getExercisesByDaySync(day.id)
            GymDayWithExercises(day, exercises)
        }
        return GymRoutineWithDays(routine, daysWithExercises)
    }
    
    suspend fun getRoutineWithDaysByStringId(routineId: String): GymRoutineWithDays? {
        val routine = routineDao.getRoutineByStringId(routineId) ?: return null
        return getRoutineWithDays(routine.id)
    }
    
    suspend fun insertRoutine(routine: GymRoutineEntity): Long {
        return routineDao.insertRoutine(routine)
    }
    
    suspend fun updateRoutine(routine: GymRoutineEntity) {
        routineDao.updateRoutine(routine)
    }
    
    suspend fun deleteRoutine(routine: GymRoutineEntity) {
        routineDao.deleteRoutine(routine)
    }
    
    // --- DÍAS ---
    
    fun getDaysByRoutineFlow(routineId: Long): Flow<List<GymDayEntity>> {
        return dayDao.getDaysByRoutine(routineId)
    }
    
    suspend fun getDayWithExercises(dayId: Long): GymDayWithExercises? {
        val day = dayDao.getDayById(dayId) ?: return null
        val exercises = exerciseDao.getExercisesByDaySync(dayId)
        return GymDayWithExercises(day, exercises)
    }
    
    suspend fun insertDay(day: GymDayEntity): Long {
        return dayDao.insertDay(day)
    }
    
    suspend fun updateDay(day: GymDayEntity) {
        dayDao.updateDay(day)
    }
    
    suspend fun deleteDay(day: GymDayEntity) {
        dayDao.deleteDay(day)
    }
    
    // --- EJERCICIOS ---
    
    fun getExercisesByDayFlow(dayId: Long): Flow<List<GymExerciseEntity>> {
        return exerciseDao.getExercisesByDay(dayId)
    }
    
    suspend fun insertExercise(exercise: GymExerciseEntity): Long {
        return exerciseDao.insertExercise(exercise)
    }
    
    suspend fun insertExercises(exercises: List<GymExerciseEntity>) {
        exerciseDao.insertExercises(exercises)
    }
    
    suspend fun updateExercise(exercise: GymExerciseEntity) {
        exerciseDao.updateExercise(exercise)
    }
    
    suspend fun deleteExercise(exercise: GymExerciseEntity) {
        exerciseDao.deleteExercise(exercise)
    }
    
    // --- UTILIDADES ---
    
    /**
     * Borra todos los días y ejercicios de una rutina y los reemplaza con nuevos.
     */
    suspend fun replaceRoutineDays(routineId: Long, daysWithExercises: List<GymDayWithExercises>) {
        dayDao.deleteDaysByRoutine(routineId)
        
        daysWithExercises.forEachIndexed { dayIndex, dayWithExercises ->
            val newDay = dayWithExercises.day.copy(
                id = 0,
                routineId = routineId,
                dayIndex = dayIndex
            )
            val dayId = dayDao.insertDay(newDay)
            
            val newExercises = dayWithExercises.exercises.mapIndexed { exerciseIndex, exercise ->
                exercise.copy(id = 0, dayId = dayId, exerciseIndex = exerciseIndex)
            }
            exerciseDao.insertExercises(newExercises)
        }
    }
}
