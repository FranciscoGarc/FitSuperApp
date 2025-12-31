package com.example.fitsuperapp

// --- MODELOS PARA FITTRACK (Gimnasio) ---

data class GymExercise(
    val name: String,
    val sets: Int,
    val baseReps: Int,
    var currentReps: Int = baseReps, // Para la sobrecarga progresiva que tenías en JS
    val restSeconds: Int,
    var isCompleted: androidx.compose.runtime.MutableState<List<Boolean>> = androidx.compose.runtime.mutableStateOf(List(sets) { false }) // Estado observable
)

data class GymDay(
    val title: String, // Ej: "Día 1: Torso - Fuerza"
    val exercises: List<GymExercise>
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