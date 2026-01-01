package com.example.fitsuperapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        GymRoutineEntity::class,
        GymDayEntity::class,
        GymExerciseEntity::class,
        HiitRoutineEntity::class,
        HiitStepEntity::class,
        WorkoutSessionEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun gymRoutineDao(): GymRoutineDao
    abstract fun gymDayDao(): GymDayDao
    abstract fun gymExerciseDao(): GymExerciseDao
    abstract fun hiitRoutineDao(): HiitRoutineDao
    abstract fun hiitStepDao(): HiitStepDao
    abstract fun workoutSessionDao(): WorkoutSessionDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        // Migration from version 1 to 2: Add HIIT routines support
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // ... (existing migration code)
            }
        }

        // Migration from version 2 to 3: Add Workout Sessions
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS workout_sessions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        type TEXT NOT NULL,
                        routineName TEXT NOT NULL,
                        startTime INTEGER NOT NULL,
                        endTime INTEGER NOT NULL,
                        durationSeconds INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fitsuperapp_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
    
    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database)
                }
            }
        }
        
        suspend fun populateDatabase(database: AppDatabase) {
            val routineDao = database.gymRoutineDao()
            val dayDao = database.gymDayDao()
            val exerciseDao = database.gymExerciseDao()
            val hiitRoutineDao = database.hiitRoutineDao()
            val hiitDao = database.hiitStepDao()
            
            // --- Insertar rutina Torso/Pierna ---
            val torsoRoutineId = routineDao.insertRoutine(
                GymRoutineEntity(routineId = "torso-pierna", name = "Torso / Pierna")
            )
            
            // Día 1: Torso - Fuerza
            val day1Id = dayDao.insertDay(
                GymDayEntity(routineId = torsoRoutineId, dayIndex = 0, title = "Día 1: Torso - Fuerza")
            )
            exerciseDao.insertExercises(listOf(
                GymExerciseEntity(dayId = day1Id, exerciseIndex = 0, name = "Press de Banca con Barra", sets = 4, baseReps = 6, restSeconds = 110),
                GymExerciseEntity(dayId = day1Id, exerciseIndex = 1, name = "Remo con Barra", sets = 4, baseReps = 6, restSeconds = 110),
                GymExerciseEntity(dayId = day1Id, exerciseIndex = 2, name = "Press Militar con Barra", sets = 4, baseReps = 6, restSeconds = 110),
                GymExerciseEntity(dayId = day1Id, exerciseIndex = 3, name = "Jalón al Pecho en Máquina", sets = 4, baseReps = 6, restSeconds = 80),
                GymExerciseEntity(dayId = day1Id, exerciseIndex = 4, name = "Press de Pecho en Máquina", sets = 3, baseReps = 8, restSeconds = 80),
                GymExerciseEntity(dayId = day1Id, exerciseIndex = 5, name = "Extensiones de Tríceps en Polea Alta", sets = 3, baseReps = 8, restSeconds = 50),
                GymExerciseEntity(dayId = day1Id, exerciseIndex = 6, name = "Curl de Bíceps con Barra Z", sets = 3, baseReps = 8, restSeconds = 50)
            ))
            
            // Día 2: Pierna - Fuerza
            val day2Id = dayDao.insertDay(
                GymDayEntity(routineId = torsoRoutineId, dayIndex = 1, title = "Día 2: Pierna - Fuerza")
            )
            exerciseDao.insertExercises(listOf(
                GymExerciseEntity(dayId = day2Id, exerciseIndex = 0, name = "Sentadilla con Barra", sets = 4, baseReps = 6, restSeconds = 110),
                GymExerciseEntity(dayId = day2Id, exerciseIndex = 1, name = "Peso Muerto Rumano con Barra", sets = 4, baseReps = 6, restSeconds = 110),
                GymExerciseEntity(dayId = day2Id, exerciseIndex = 2, name = "Zancadas con Mancuernas", sets = 4, baseReps = 12, restSeconds = 80),
                GymExerciseEntity(dayId = day2Id, exerciseIndex = 3, name = "Extensiones de Cuádriceps en Máquina", sets = 4, baseReps = 6, restSeconds = 80),
                GymExerciseEntity(dayId = day2Id, exerciseIndex = 4, name = "Curl de Isquiotibiales con Mancuerna", sets = 4, baseReps = 8, restSeconds = 80),
                GymExerciseEntity(dayId = day2Id, exerciseIndex = 5, name = "Elevación de Gemelos de Pie", sets = 4, baseReps = 10, restSeconds = 50)
            ))
            
            // Día 3: Descanso Activo
            dayDao.insertDay(
                GymDayEntity(routineId = torsoRoutineId, dayIndex = 2, title = "Día 3: Descanso Activo")
            )
            
            // Día 4: Torso - Hipertrofia
            val day4Id = dayDao.insertDay(
                GymDayEntity(routineId = torsoRoutineId, dayIndex = 3, title = "Día 4: Torso - Hipertrofia")
            )
            exerciseDao.insertExercises(listOf(
                GymExerciseEntity(dayId = day4Id, exerciseIndex = 0, name = "Press Inclinado con Barra", sets = 4, baseReps = 6, restSeconds = 80),
                GymExerciseEntity(dayId = day4Id, exerciseIndex = 1, name = "Remo con Mancuernas a una Mano", sets = 4, baseReps = 6, restSeconds = 80),
                GymExerciseEntity(dayId = day4Id, exerciseIndex = 2, name = "Aperturas en Máquina", sets = 4, baseReps = 8, restSeconds = 50),
                GymExerciseEntity(dayId = day4Id, exerciseIndex = 3, name = "Elevaciones Laterales con Mancuernas", sets = 4, baseReps = 7, restSeconds = 50),
                GymExerciseEntity(dayId = day4Id, exerciseIndex = 4, name = "Jalón al Pecho en Máquina", sets = 4, baseReps = 6, restSeconds = 80),
                GymExerciseEntity(dayId = day4Id, exerciseIndex = 5, name = "Press Francés con Barra Z", sets = 3, baseReps = 8, restSeconds = 50),
                GymExerciseEntity(dayId = day4Id, exerciseIndex = 6, name = "Curl de Bíceps con Mancuernas", sets = 3, baseReps = 8, restSeconds = 50)
            ))
            
            // Día 5: Pierna - Hipertrofia
            val day5Id = dayDao.insertDay(
                GymDayEntity(routineId = torsoRoutineId, dayIndex = 4, title = "Día 5: Pierna - Hipertrofia")
            )
            exerciseDao.insertExercises(listOf(
                GymExerciseEntity(dayId = day5Id, exerciseIndex = 0, name = "Sentadilla Búlgara con Mancuernas", sets = 4, baseReps = 6, restSeconds = 80),
                GymExerciseEntity(dayId = day5Id, exerciseIndex = 1, name = "Peso Muerto Rumano con Mancuernas", sets = 4, baseReps = 6, restSeconds = 80),
                GymExerciseEntity(dayId = day5Id, exerciseIndex = 2, name = "Extensiones de Cuádriceps en Máquina", sets = 4, baseReps = 6, restSeconds = 50),
                GymExerciseEntity(dayId = day5Id, exerciseIndex = 3, name = "Curl de Isquiotibiales con Mancuerna", sets = 4, baseReps = 8, restSeconds = 50),
                GymExerciseEntity(dayId = day5Id, exerciseIndex = 4, name = "Elevación de Gemelos Sentado", sets = 4, baseReps = 8, restSeconds = 50)
            ))
            
            // Días 6 y 7: Descanso
            dayDao.insertDay(
                GymDayEntity(routineId = torsoRoutineId, dayIndex = 5, title = "Días 6 y 7: Descanso")
            )
            
            // --- Insertar rutina PPL ---
            val pplRoutineId = routineDao.insertRoutine(
                GymRoutineEntity(routineId = "ppl", name = "Push / Pull / Legs")
            )
            
            // Día 1: Empuje (Pecho, Hombros, Tríceps)
            val pplDay1Id = dayDao.insertDay(
                GymDayEntity(routineId = pplRoutineId, dayIndex = 0, title = "Día 1: Empuje (Pecho, Hombros, Tríceps)")
            )
            exerciseDao.insertExercises(listOf(
                GymExerciseEntity(dayId = pplDay1Id, exerciseIndex = 0, name = "Press de Banca con Barra", sets = 4, baseReps = 6, restSeconds = 110),
                GymExerciseEntity(dayId = pplDay1Id, exerciseIndex = 1, name = "Press Inclinado con Barra", sets = 4, baseReps = 6, restSeconds = 110),
                GymExerciseEntity(dayId = pplDay1Id, exerciseIndex = 2, name = "Press Militar con Mancuernas", sets = 4, baseReps = 6, restSeconds = 80),
                GymExerciseEntity(dayId = pplDay1Id, exerciseIndex = 3, name = "Aperturas en Máquina", sets = 4, baseReps = 8, restSeconds = 50),
                GymExerciseEntity(dayId = pplDay1Id, exerciseIndex = 4, name = "Elevaciones Laterales con Mancuernas", sets = 4, baseReps = 7, restSeconds = 50),
                GymExerciseEntity(dayId = pplDay1Id, exerciseIndex = 5, name = "Extensiones de Tríceps en Polea Alta", sets = 3, baseReps = 8, restSeconds = 50)
            ))
            
            // Día 2: Jalón (Espalda, Bíceps)
            val pplDay2Id = dayDao.insertDay(
                GymDayEntity(routineId = pplRoutineId, dayIndex = 1, title = "Día 2: Jalón (Espalda, Bíceps)")
            )
            exerciseDao.insertExercises(listOf(
                GymExerciseEntity(dayId = pplDay2Id, exerciseIndex = 0, name = "Remo con Barra", sets = 4, baseReps = 6, restSeconds = 110),
                GymExerciseEntity(dayId = pplDay2Id, exerciseIndex = 1, name = "Jalón al Pecho en Máquina", sets = 4, baseReps = 6, restSeconds = 80),
                GymExerciseEntity(dayId = pplDay2Id, exerciseIndex = 2, name = "Remo con Mancuernas a una Mano", sets = 4, baseReps = 6, restSeconds = 80),
                GymExerciseEntity(dayId = pplDay2Id, exerciseIndex = 3, name = "Face Pulls", sets = 4, baseReps = 8, restSeconds = 50),
                GymExerciseEntity(dayId = pplDay2Id, exerciseIndex = 4, name = "Curl de Bíceps con Barra Z", sets = 3, baseReps = 8, restSeconds = 50),
                GymExerciseEntity(dayId = pplDay2Id, exerciseIndex = 5, name = "Curl Martillo con Mancuernas", sets = 3, baseReps = 10, restSeconds = 50)
            ))
            
            // Día 3: Piernas
            val pplDay3Id = dayDao.insertDay(
                GymDayEntity(routineId = pplRoutineId, dayIndex = 2, title = "Día 3: Piernas")
            )
            exerciseDao.insertExercises(listOf(
                GymExerciseEntity(dayId = pplDay3Id, exerciseIndex = 0, name = "Sentadilla con Barra", sets = 4, baseReps = 6, restSeconds = 110),
                GymExerciseEntity(dayId = pplDay3Id, exerciseIndex = 1, name = "Peso Muerto Rumano con Barra", sets = 4, baseReps = 6, restSeconds = 110),
                GymExerciseEntity(dayId = pplDay3Id, exerciseIndex = 2, name = "Zancadas con Mancuernas", sets = 4, baseReps = 12, restSeconds = 80),
                GymExerciseEntity(dayId = pplDay3Id, exerciseIndex = 3, name = "Extensiones de Cuádriceps en Máquina", sets = 4, baseReps = 6, restSeconds = 80),
                GymExerciseEntity(dayId = pplDay3Id, exerciseIndex = 4, name = "Curl de Isquiotibiales con Mancuerna", sets = 4, baseReps = 8, restSeconds = 80),
                GymExerciseEntity(dayId = pplDay3Id, exerciseIndex = 5, name = "Elevación de Gemelos de Pie", sets = 4, baseReps = 10, restSeconds = 50)
            ))
            
            // Día 4: Empuje (Segunda Sesión)
            val pplDay4Id = dayDao.insertDay(
                GymDayEntity(routineId = pplRoutineId, dayIndex = 3, title = "Día 4: Empuje (Segunda Sesión)")
            )
            exerciseDao.insertExercises(listOf(
                GymExerciseEntity(dayId = pplDay4Id, exerciseIndex = 0, name = "Press Plano con Barra", sets = 4, baseReps = 6, restSeconds = 110),
                GymExerciseEntity(dayId = pplDay4Id, exerciseIndex = 1, name = "Press de Pecho en Máquina", sets = 3, baseReps = 8, restSeconds = 80),
                GymExerciseEntity(dayId = pplDay4Id, exerciseIndex = 2, name = "Press de Hombros con Barra", sets = 4, baseReps = 6, restSeconds = 80),
                GymExerciseEntity(dayId = pplDay4Id, exerciseIndex = 3, name = "Aperturas Inclinadas con Mancuernas", sets = 4, baseReps = 8, restSeconds = 50),
                GymExerciseEntity(dayId = pplDay4Id, exerciseIndex = 4, name = "Elevaciones Frontales con Mancuernas", sets = 3, baseReps = 8, restSeconds = 50),
                GymExerciseEntity(dayId = pplDay4Id, exerciseIndex = 5, name = "Press Francés con Barra Z", sets = 4, baseReps = 6, restSeconds = 50)
            ))
            
            // Día 5: Jalón (Segunda Sesión)
            val pplDay5Id = dayDao.insertDay(
                GymDayEntity(routineId = pplRoutineId, dayIndex = 4, title = "Día 5: Jalón (Segunda Sesión)")
            )
            exerciseDao.insertExercises(listOf(
                GymExerciseEntity(dayId = pplDay5Id, exerciseIndex = 0, name = "Jalón al Pecho en Máquina", sets = 4, baseReps = 6, restSeconds = 80),
                GymExerciseEntity(dayId = pplDay5Id, exerciseIndex = 1, name = "Remo Inclinado con Mancuernas", sets = 4, baseReps = 6, restSeconds = 80),
                GymExerciseEntity(dayId = pplDay5Id, exerciseIndex = 2, name = "Pull-overs con Mancuerna", sets = 3, baseReps = 8, restSeconds = 50),
                GymExerciseEntity(dayId = pplDay5Id, exerciseIndex = 3, name = "Encogimientos de Hombros con Mancuernas", sets = 4, baseReps = 8, restSeconds = 50),
                GymExerciseEntity(dayId = pplDay5Id, exerciseIndex = 4, name = "Curl de Bíceps con Mancuernas", sets = 3, baseReps = 8, restSeconds = 50),
                GymExerciseEntity(dayId = pplDay5Id, exerciseIndex = 5, name = "Curl de Concentración con Mancuerna", sets = 3, baseReps = 8, restSeconds = 50)
            ))
            
            // Días 6 y 7: Descanso
            dayDao.insertDay(
                GymDayEntity(routineId = pplRoutineId, dayIndex = 5, title = "Días 6 y 7: Descanso")
            )
            
            // --- Insertar rutina HIIT ---
            val hiitRoutineId = hiitRoutineDao.insertRoutine(
                HiitRoutineEntity(name = "Cardio HIIT Completo")
            )
            
            hiitDao.insertSteps(listOf(
                // Calentamiento
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 0, name = "Marcha en el lugar", durationSeconds = 30, type = "WARMUP"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 1, name = "Círculos de brazos", durationSeconds = 30, type = "WARMUP"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 2, name = "Trote suave", durationSeconds = 30, type = "WARMUP"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 3, name = "Balanceos de piernas", durationSeconds = 30, type = "WARMUP"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 4, name = "Sentadillas con peso corporal", durationSeconds = 30, type = "WARMUP"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 5, name = "Zancadas suaves", durationSeconds = 30, type = "WARMUP"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 6, name = "Jumping jacks (calentamiento)", durationSeconds = 30, type = "WARMUP"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 7, name = "Movilidad articular dinámica", durationSeconds = 30, type = "WARMUP"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 8, name = "Rotación de tobillos", durationSeconds = 30, type = "WARMUP"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 9, name = "Círculos de cadera (suaves)", durationSeconds = 30, type = "WARMUP"),
                // Circuito 1
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 10, name = "Sentadilla con salto", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 11, name = "Burpees", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 12, name = "Zancadas con salto (alterna)", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 13, name = "Rodillas al pecho", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 14, name = "Saltos de tijera", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 15, name = "Escaladores", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 16, name = "Descanso Completo", durationSeconds = 60, type = "BREAK"),
                // Circuito 2
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 17, name = "Plancha con rodilla al codo (alterna)", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 18, name = "Giros rusos con peso (opcional)", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 19, name = "Plancha con movimiento de cadera (side plank dips)", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 20, name = "Mountain climbers lentos (controlados)", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 21, name = "Abdominales de bicicleta", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 22, name = "Bear crawls (gateos de oso)", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 23, name = "Descanso Completo", durationSeconds = 60, type = "BREAK"),
                // Circuito 3
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 24, name = "Sentadilla con peso corporal", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 25, name = "Zancadas hacia atrás (alterna)", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 26, name = "Sentadilla sumo", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 27, name = "Zancada lateral (alterna)", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 28, name = "Elevación de talones (gemelos)", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 29, name = "Sentadilla sostenida (isométrica)", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 30, name = "Descanso Completo", durationSeconds = 60, type = "BREAK"),
                // Circuito 4
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 31, name = "Butt kicks (talones a los glúteos)", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 32, name = "High knees (rodillas altas) a máxima velocidad", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 33, name = "Saltos con cuerda (imaginaria)", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 34, name = "Saltos laterales con toque de pie", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 35, name = "Jumping jacks con sentadilla", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 36, name = "Skater jumps (saltos de patinador)", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 37, name = "Descanso Completo", durationSeconds = 60, type = "BREAK"),
                // Circuito 5
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 38, name = "Flexiones de brazos (modificada si es necesario)", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 39, name = "Remo inclinado con peso corporal (mesa/silla)", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 40, name = "Plancha a flexión (plank to push-up)", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 41, name = "Renegade rows (con pesas ligeras)", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 42, name = "Flexión de araña (spiderman push-ups)", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 43, name = "Plancha con elevación de brazo alterno", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 44, name = "Descanso Completo", durationSeconds = 60, type = "BREAK"),
                // Circuito 6
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 45, name = "Burpee con salto encogido", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 46, name = "Mountain climbers a máxima velocidad", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 47, name = "Pop squats (sentadillas con salto explosivo)", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 48, name = "Plank jacks (saltos de plancha)", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 49, name = "Rodillas al pecho (sprinting)", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 50, name = "Jumping jacks", durationSeconds = 40, type = "EXERCISE"),
                HiitStepEntity(routineId = hiitRoutineId, stepIndex = 51, name = "Descanso Completo", durationSeconds = 60, type = "BREAK")
            ))
        }
    }
}

