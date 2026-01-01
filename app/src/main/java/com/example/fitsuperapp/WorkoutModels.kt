package com.example.fitsuperapp

// --- MODELOS PARA FITTRACK (Gimnasio) ---

data class GymExercise(
    val name: String,
    val sets: Int,
    val baseReps: Int,
    var currentReps: Int = baseReps, // Para la sobrecarga progresiva
    val restSeconds: Int,
    val id: String = name.hashCode().toString() // ID único para keys en LazyColumn, auto-generado si no se proporciona
)

data class GymDay(
    val title: String, // Ej: "Día 1: Torso - Fuerza"
    val exercises: List<GymExercise>,
    val completionState: Map<String, List<Boolean>> = emptyMap() // ID del ejercicio -> estado de completado
)

data class GymRoutine(
    val id: String, // "torso-pierna" o "ppl"
    val name: String,
    val schedule: List<GymDay>
)

// --- MODELOS PARA CARDIOHIIT (Intervalos) ---

enum class HiitStepType {
    WARMUP, EXERCISE, REST, BREAK, COOLDOWN
}

data class HiitStep(
    val name: String,
    val durationSeconds: Int,
    val type: HiitStepType
)