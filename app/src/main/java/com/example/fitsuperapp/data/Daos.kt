package com.example.fitsuperapp.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

// --- DAO PARA RUTINAS DE GYM ---

@Dao
interface GymRoutineDao {
    @Query("SELECT * FROM gym_routines ORDER BY id")
    fun getAllRoutines(): Flow<List<GymRoutineEntity>>
    
    @Query("SELECT * FROM gym_routines WHERE routineId = :routineId")
    suspend fun getRoutineByStringId(routineId: String): GymRoutineEntity?
    
    @Query("SELECT * FROM gym_routines WHERE id = :id")
    suspend fun getRoutineById(id: Long): GymRoutineEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: GymRoutineEntity): Long
    
    @Update
    suspend fun updateRoutine(routine: GymRoutineEntity)
    
    @Delete
    suspend fun deleteRoutine(routine: GymRoutineEntity)
}

// --- DAO PARA DÍAS DE GYM ---

@Dao
interface GymDayDao {
    @Query("SELECT * FROM gym_days WHERE routineId = :routineId ORDER BY dayIndex")
    fun getDaysByRoutine(routineId: Long): Flow<List<GymDayEntity>>
    
    @Query("SELECT * FROM gym_days WHERE routineId = :routineId ORDER BY dayIndex")
    suspend fun getDaysByRoutineSync(routineId: Long): List<GymDayEntity>
    
    @Query("SELECT * FROM gym_days WHERE id = :id")
    suspend fun getDayById(id: Long): GymDayEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: GymDayEntity): Long
    
    @Update
    suspend fun updateDay(day: GymDayEntity)
    
    @Delete
    suspend fun deleteDay(day: GymDayEntity)
    
    @Query("DELETE FROM gym_days WHERE routineId = :routineId")
    suspend fun deleteDaysByRoutine(routineId: Long)
}

// --- DAO PARA EJERCICIOS DE GYM ---

@Dao
interface GymExerciseDao {
    @Query("SELECT * FROM gym_exercises WHERE dayId = :dayId ORDER BY exerciseIndex")
    fun getExercisesByDay(dayId: Long): Flow<List<GymExerciseEntity>>
    
    @Query("SELECT * FROM gym_exercises WHERE dayId = :dayId ORDER BY exerciseIndex")
    suspend fun getExercisesByDaySync(dayId: Long): List<GymExerciseEntity>
    
    @Query("SELECT * FROM gym_exercises WHERE id = :id")
    suspend fun getExerciseById(id: Long): GymExerciseEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercise(exercise: GymExerciseEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<GymExerciseEntity>)
    
    @Update
    suspend fun updateExercise(exercise: GymExerciseEntity)
    
    @Delete
    suspend fun deleteExercise(exercise: GymExerciseEntity)
    
    @Query("DELETE FROM gym_exercises WHERE dayId = :dayId")
    suspend fun deleteExercisesByDay(dayId: Long)
}

// --- DAO PARA RUTINAS HIIT ---

@Dao
interface HiitRoutineDao {
    @Query("SELECT * FROM hiit_routines ORDER BY id")
    fun getAllRoutines(): Flow<List<HiitRoutineEntity>>
    
    @Query("SELECT * FROM hiit_routines ORDER BY id")
    suspend fun getAllRoutinesSync(): List<HiitRoutineEntity>
    
    @Query("SELECT * FROM hiit_routines WHERE id = :id")
    suspend fun getRoutineById(id: Long): HiitRoutineEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: HiitRoutineEntity): Long
    
    @Update
    suspend fun updateRoutine(routine: HiitRoutineEntity)
    
    @Delete
    suspend fun deleteRoutine(routine: HiitRoutineEntity)
}

// --- DAO PARA PASOS HIIT ---

@Dao
interface HiitStepDao {
    @Query("SELECT * FROM hiit_steps WHERE routineId = :routineId ORDER BY stepIndex")
    fun getStepsByRoutine(routineId: Long): Flow<List<HiitStepEntity>>
    
    @Query("SELECT * FROM hiit_steps WHERE routineId = :routineId ORDER BY stepIndex")
    suspend fun getStepsByRoutineSync(routineId: Long): List<HiitStepEntity>
    
    @Query("SELECT * FROM hiit_steps ORDER BY stepIndex")
    fun getAllSteps(): Flow<List<HiitStepEntity>>
    
    @Query("SELECT * FROM hiit_steps ORDER BY stepIndex")
    suspend fun getAllStepsSync(): List<HiitStepEntity>
    
    @Query("SELECT * FROM hiit_steps WHERE id = :id")
    suspend fun getStepById(id: Long): HiitStepEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStep(step: HiitStepEntity): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<HiitStepEntity>)
    
    @Update
    suspend fun updateStep(step: HiitStepEntity)
    
    @Delete
    suspend fun deleteStep(step: HiitStepEntity)
    
    @Query("DELETE FROM hiit_steps WHERE routineId = :routineId")
    suspend fun deleteStepsByRoutine(routineId: Long)
    
    @Query("DELETE FROM hiit_steps")
    suspend fun deleteAllSteps()
}

