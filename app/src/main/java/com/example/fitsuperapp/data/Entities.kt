package com.example.fitsuperapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

// --- ENTIDADES PARA GYM ---

@Entity(tableName = "gym_routines")
data class GymRoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routineId: String, // "torso-pierna" o "ppl"
    val name: String
)

@Entity(
    tableName = "gym_days",
    foreignKeys = [
        ForeignKey(
            entity = GymRoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("routineId")]
)
data class GymDayEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routineId: Long,
    val dayIndex: Int, // Orden del día en la rutina
    val title: String
)

@Entity(
    tableName = "gym_exercises",
    foreignKeys = [
        ForeignKey(
            entity = GymDayEntity::class,
            parentColumns = ["id"],
            childColumns = ["dayId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("dayId")]
)
data class GymExerciseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayId: Long,
    val exerciseIndex: Int, // Orden del ejercicio en el día
    val name: String,
    val sets: Int,
    val baseReps: Int,
    val restSeconds: Int
)

// --- ENTIDADES PARA HIIT ---

@Entity(tableName = "hiit_routines")
data class HiitRoutineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)

@Entity(
    tableName = "hiit_steps",
    foreignKeys = [
        ForeignKey(
            entity = HiitRoutineEntity::class,
            parentColumns = ["id"],
            childColumns = ["routineId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("routineId")]
)
data class HiitStepEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val routineId: Long,
    val stepIndex: Int, // Orden del paso
    val name: String,
    val durationSeconds: Int,
    val type: String // "WARMUP", "EXERCISE", "REST", "BREAK", "COOLDOWN"
)

// --- RELACIONES ---

data class GymRoutineWithDays(
    val routine: GymRoutineEntity,
    val days: List<GymDayWithExercises>
)

data class GymDayWithExercises(
    val day: GymDayEntity,
    val exercises: List<GymExerciseEntity>
)

data class HiitRoutineWithSteps(
    val routine: HiitRoutineEntity,
    val steps: List<HiitStepEntity>
)
