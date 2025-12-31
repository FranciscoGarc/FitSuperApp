package com.example.fitsuperapp

object DataRepository {

    // --- DATOS DE FITTRACK ---
    // He migrado las rutinas 'torso-pierna' y 'ppl' de tu script.js
    val gymRoutines = listOf(
        GymRoutine(
            id = "torso-pierna",
            name = "Torso / Pierna",
            schedule = listOf(
                GymDay("Día 1: Torso - Fuerza", listOf(
                    GymExercise("Press de Banca con Barra", 4, 6, restSeconds = 110),
                    GymExercise("Remo con Barra", 4, 6, restSeconds = 110),
                    GymExercise("Press Militar con Barra", 4, 6, restSeconds = 110),
                    GymExercise("Jalón al Pecho en Máquina", 4, 6, restSeconds = 80),
                    GymExercise("Press de Pecho en Máquina", 3, 8, restSeconds = 80),
                    GymExercise("Extensiones de Tríceps en Polea Alta", 3, 8, restSeconds = 50),
                    GymExercise("Curl de Bíceps con Barra Z", 3, 8, restSeconds = 50)
                )),
                GymDay("Día 2: Pierna - Fuerza", listOf(
                    GymExercise("Sentadilla con Barra", 4, 6, restSeconds = 110),
                    GymExercise("Peso Muerto Rumano con Barra", 4, 6, restSeconds = 110),
                    GymExercise("Zancadas con Mancuernas", 4, 12, restSeconds = 80),
                    GymExercise("Extensiones de Cuádriceps en Máquina", 4, 6, restSeconds = 80),
                    GymExercise("Curl de Isquiotibiales con Mancuerna", 4, 8, restSeconds = 80),
                    GymExercise("Elevación de Gemelos de Pie", 4, 10, restSeconds = 50)
                )),
                GymDay("Día 3: Descanso Activo", emptyList()),
                GymDay("Día 4: Torso - Hipertrofia", listOf(
                    GymExercise("Press Inclinado con Barra", 4, 6, restSeconds = 80),
                    GymExercise("Remo con Mancuernas a una Mano", 4, 6, restSeconds = 80),
                    GymExercise("Aperturas en Máquina", 4, 8, restSeconds = 50),
                    GymExercise("Elevaciones Laterales con Mancuernas", 4, 7, restSeconds = 50),
                    GymExercise("Jalón al Pecho en Máquina", 4, 6, restSeconds = 80),
                    GymExercise("Press Francés con Barra Z", 3, 8, restSeconds = 50),
                    GymExercise("Curl de Bíceps con Mancuernas", 3, 8, restSeconds = 50)
                )),
                GymDay("Día 5: Pierna - Hipertrofia", listOf(
                    GymExercise("Sentadilla Búlgara con Mancuernas", 4, 6, restSeconds = 80),
                    GymExercise("Peso Muerto Rumano con Mancuernas", 4, 6, restSeconds = 80),
                    GymExercise("Extensiones de Cuádriceps en Máquina", 4, 6, restSeconds = 50),
                    GymExercise("Curl de Isquiotibiales con Mancuerna", 4, 8, restSeconds = 50),
                    GymExercise("Elevación de Gemelos Sentado", 4, 8, restSeconds = 50)
                )),
                GymDay("Días 6 y 7: Descanso", emptyList())
            )
        ),
        GymRoutine(
            id = "ppl",
            name = "Push / Pull / Legs",
            schedule = listOf(
                GymDay("Día 1: Empuje (Pecho, Hombros, Tríceps)", listOf(
                    GymExercise("Press de Banca con Barra", 4, 6, restSeconds = 110),
                    GymExercise("Press Inclinado con Barra", 4, 6, restSeconds = 110),
                    GymExercise("Press Militar con Mancuernas", 4, 6, restSeconds = 80),
                    GymExercise("Aperturas en Máquina", 4, 8, restSeconds = 50),
                    GymExercise("Elevaciones Laterales con Mancuernas", 4, 7, restSeconds = 50),
                    GymExercise("Extensiones de Tríceps en Polea Alta", 3, 8, restSeconds = 50)
                )),
                GymDay("Día 2: Jalón (Espalda, Bíceps)", listOf(
                    GymExercise("Remo con Barra", 4, 6, restSeconds = 110),
                    GymExercise("Jalón al Pecho en Máquina", 4, 6, restSeconds = 80),
                    GymExercise("Remo con Mancuernas a una Mano", 4, 6, restSeconds = 80),
                    GymExercise("Face Pulls", 4, 8, restSeconds = 50),
                    GymExercise("Curl de Bíceps con Barra Z", 3, 8, restSeconds = 50),
                    GymExercise("Curl Martillo con Mancuernas", 3, 10, restSeconds = 50)
                )),
                GymDay("Día 3: Piernas", listOf(
                    GymExercise("Sentadilla con Barra", 4, 6, restSeconds = 110),
                    GymExercise("Peso Muerto Rumano con Barra", 4, 6, restSeconds = 110),
                    GymExercise("Zancadas con Mancuernas", 4, 12, restSeconds = 80),
                    GymExercise("Extensiones de Cuádriceps en Máquina", 4, 6, restSeconds = 80),
                    GymExercise("Curl de Isquiotibiales con Mancuerna", 4, 8, restSeconds = 80),
                    GymExercise("Elevación de Gemelos de Pie", 4, 10, restSeconds = 50)
                )),
                GymDay("Día 4: Empuje (Segunda Sesión)", listOf(
                    GymExercise("Press Plano con Barra", 4, 6, restSeconds = 110),
                    GymExercise("Press de Pecho en Máquina", 3, 8, restSeconds = 80),
                    GymExercise("Press de Hombros con Barra", 4, 6, restSeconds = 80),
                    GymExercise("Aperturas Inclinadas con Mancuernas", 4, 8, restSeconds = 50),
                    GymExercise("Elevaciones Frontales con Mancuernas", 3, 8, restSeconds = 50),
                    GymExercise("Press Francés con Barra Z", 4, 6, restSeconds = 50)
                )),
                GymDay("Día 5: Jalón (Segunda Sesión)", listOf(
                    GymExercise("Jalón al Pecho en Máquina", 4, 6, restSeconds = 80),
                    GymExercise("Remo Inclinado con Mancuernas", 4, 6, restSeconds = 80),
                    GymExercise("Pull-overs con Mancuerna", 3, 8, restSeconds = 50),
                    GymExercise("Encogimientos de Hombros con Mancuernas", 4, 8, restSeconds = 50),
                    GymExercise("Curl de Bíceps con Mancuernas", 3, 8, restSeconds = 50),
                    GymExercise("Curl de Concentración con Mancuerna", 3, 8, restSeconds = 50)
                )),
                GymDay("Días 6 y 7: Descanso", emptyList())
            )
        )
    )

    // --- DATOS DE CARDIOHIIT ---
    // Migrado de tu routine array en script.js
    val hiitRoutine = listOf(
        // Calentamiento
        HiitStep("Marcha en el lugar", 30, HiitStepType.WARMUP),
        HiitStep("Círculos de brazos", 30, HiitStepType.WARMUP),
        HiitStep("Trote suave", 30, HiitStepType.WARMUP),
        HiitStep("Balanceos de piernas", 30, HiitStepType.WARMUP),
        HiitStep("Sentadillas con peso corporal", 30, HiitStepType.WARMUP),
        HiitStep("Zancadas suaves", 30, HiitStepType.WARMUP),
        HiitStep("Jumping jacks (calentamiento)", 30, HiitStepType.WARMUP),
        HiitStep("Movilidad articular dinámica", 30, HiitStepType.WARMUP),
        HiitStep("Rotación de tobillos", 30, HiitStepType.WARMUP),
        HiitStep("Círculos de cadera (suaves)", 30, HiitStepType.WARMUP),
        // Circuito 1
        HiitStep("Sentadilla con salto", 40, HiitStepType.EXERCISE),
        HiitStep("Burpees", 40, HiitStepType.EXERCISE),
        HiitStep("Zancadas con salto (alterna)", 40, HiitStepType.EXERCISE),
        HiitStep("Rodillas al pecho", 40, HiitStepType.EXERCISE),
        HiitStep("Saltos de tijera", 40, HiitStepType.EXERCISE),
        HiitStep("Escaladores", 40, HiitStepType.EXERCISE),
        HiitStep("Descanso Completo", 60, HiitStepType.BREAK),
        // Circuito 2
        HiitStep("Plancha con rodilla al codo (alterna)", 40, HiitStepType.EXERCISE),
        HiitStep("Giros rusos con peso (opcional)", 40, HiitStepType.EXERCISE),
        HiitStep("Plancha con movimiento de cadera (side plank dips)", 40, HiitStepType.EXERCISE),
        HiitStep("Mountain climbers lentos (controlados)", 40, HiitStepType.EXERCISE),
        HiitStep("Abdominales de bicicleta", 40, HiitStepType.EXERCISE),
        HiitStep("Bear crawls (gateos de oso)", 40, HiitStepType.EXERCISE),
        HiitStep("Descanso Completo", 60, HiitStepType.BREAK),
        // Circuito 3
        HiitStep("Sentadilla con peso corporal", 40, HiitStepType.EXERCISE),
        HiitStep("Zancadas hacia atrás (alterna)", 40, HiitStepType.EXERCISE),
        HiitStep("Sentadilla sumo", 40, HiitStepType.EXERCISE),
        HiitStep("Zancada lateral (alterna)", 40, HiitStepType.EXERCISE),
        HiitStep("Elevación de talones (gemelos)", 40, HiitStepType.EXERCISE),
        HiitStep("Sentadilla sostenida (isométrica)", 40, HiitStepType.EXERCISE),
        HiitStep("Descanso Completo", 60, HiitStepType.BREAK),
        // Circuito 4
        HiitStep("Butt kicks (talones a los glúteos)", 40, HiitStepType.EXERCISE),
        HiitStep("High knees (rodillas altas) a máxima velocidad", 40, HiitStepType.EXERCISE),
        HiitStep("Saltos con cuerda (imaginaria)", 40, HiitStepType.EXERCISE),
        HiitStep("Saltos laterales con toque de pie", 40, HiitStepType.EXERCISE),
        HiitStep("Jumping jacks con sentadilla", 40, HiitStepType.EXERCISE),
        HiitStep("Skater jumps (saltos de patinador)", 40, HiitStepType.EXERCISE),
        HiitStep("Descanso Completo", 60, HiitStepType.BREAK),
        // Circuito 5
        HiitStep("Flexiones de brazos (modificada si es necesario)", 40, HiitStepType.EXERCISE),
        HiitStep("Remo inclinado con peso corporal (mesa/silla)", 40, HiitStepType.EXERCISE),
        HiitStep("Plancha a flexión (plank to push-up)", 40, HiitStepType.EXERCISE),
        HiitStep("Renegade rows (con pesas ligeras)", 40, HiitStepType.EXERCISE),
        HiitStep("Flexión de araña (spiderman push-ups)", 40, HiitStepType.EXERCISE),
        HiitStep("Plancha con elevación de brazo alterno", 40, HiitStepType.EXERCISE),
        HiitStep("Descanso Completo", 60, HiitStepType.BREAK),
        // Circuito 6
        HiitStep("Burpee con salto encogido", 40, HiitStepType.EXERCISE),
        HiitStep("Mountain climbers a máxima velocidad", 40, HiitStepType.EXERCISE),
        HiitStep("Pop squats (sentadillas con salto explosivo)", 40, HiitStepType.EXERCISE),
        HiitStep("Plank jacks (saltos de plancha)", 40, HiitStepType.EXERCISE),
        HiitStep("Rodillas al pecho (sprinting)", 40, HiitStepType.EXERCISE),
        HiitStep("Jumping jacks", 40, HiitStepType.EXERCISE),
        HiitStep("Descanso Completo", 60, HiitStepType.BREAK)
    )
}