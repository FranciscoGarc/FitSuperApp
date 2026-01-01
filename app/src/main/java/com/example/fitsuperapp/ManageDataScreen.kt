package com.example.fitsuperapp

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitsuperapp.data.*

// Colores consistentes con el tema de la app
private val DarkBg = Color(0xFF111827)
private val CardBg = Color(0xFF1F2937)
private val AccentGreen = Color(0xFF10B981)
private val AccentCyan = Color(0xFF22d3ee)
private val TextGray = Color(0xFF9CA3AF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageDataScreen(
    viewModel: ManageDataViewModel,
    onExit: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Estado para diálogos
    var showAddRoutineDialog by remember { mutableStateOf(false) }
    var showAddHiitRoutineDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var exportType by remember { mutableStateOf("gym") }
    
    // Launchers para selección de archivos
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let { viewModel.exportToCsv(context, it, exportType) }
    }
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importFromCsv(context, it, exportType) }
    }
    
    // Mostrar mensajes
    LaunchedEffect(uiState.successMessage, uiState.errorMessage) {
        // Los mensajes se muestran como Snackbar
    }
    
    Scaffold(
        containerColor = DarkBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            // Header - Row simple sin padding extra
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    when {
                        uiState.selectedDay != null -> viewModel.clearSelectedDay()
                        uiState.selectedRoutine != null -> viewModel.clearSelectedRoutine()
                        uiState.selectedHiitRoutine != null -> viewModel.clearSelectedHiitRoutine()
                        else -> onExit()
                    }
                }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = Color.White)
                }
                Text(
                    text = when {
                        uiState.selectedDay != null -> "Editar Día"
                        uiState.selectedRoutine != null -> "Editar Rutina"
                        uiState.selectedHiitRoutine != null -> "Editar Rutina HIIT"
                        else -> "Gestionar Datos"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = AccentGreen
                    )
                }
                uiState.selectedDay != null -> {
                    DayEditContent(
                        dayWithExercises = uiState.selectedDay!!,
                        onAddExercise = { name, sets, reps, rest ->
                            viewModel.addExercise(
                                uiState.selectedDay!!.day.id,
                                name, sets, reps, rest,
                                uiState.selectedDay!!.exercises.size
                            )
                        },
                        onUpdateExercise = { viewModel.updateExercise(it) },
                        onDeleteExercise = { viewModel.deleteExercise(it) },
                        onUpdateDay = { viewModel.updateDay(it) }
                    )
                }
                uiState.selectedRoutine != null -> {
                    RoutineEditContent(
                        routineWithDays = uiState.selectedRoutine!!,
                        onDayClick = { viewModel.selectDay(it.id) },
                        onAddDay = { title ->
                            viewModel.addDay(
                                uiState.selectedRoutine!!.routine.id,
                                title,
                                uiState.selectedRoutine!!.days.size
                            )
                        },
                        onUpdateRoutine = { viewModel.updateRoutine(it) },
                        onDeleteRoutine = { viewModel.deleteRoutine(it) },
                        onDeleteDay = { viewModel.deleteDay(it) }
                    )
                }
                uiState.selectedHiitRoutine != null -> {
                    HiitRoutineEditContent(
                        routineWithSteps = uiState.selectedHiitRoutine!!,
                        onAddStep = { name, duration, type ->
                            viewModel.addHiitStep(
                                uiState.selectedHiitRoutine!!.routine.id,
                                name, duration, type,
                                uiState.selectedHiitRoutine!!.steps.size
                            )
                        },
                        onUpdateStep = { viewModel.updateHiitStep(it) },
                        onDeleteStep = { viewModel.deleteHiitStep(it) },
                        onUpdateRoutine = { viewModel.updateHiitRoutine(it) },
                        onDeleteRoutine = { viewModel.deleteHiitRoutine(it) }
                    )
                }
                else -> {
                    MainManageContent(
                        routines = uiState.routines,
                        hiitRoutines = uiState.hiitRoutines,
                        onRoutineClick = { viewModel.selectRoutine(it.id) },
                        onHiitRoutineClick = { viewModel.selectHiitRoutine(it.id) },
                        onAddRoutineClick = { showAddRoutineDialog = true },
                        onAddHiitRoutineClick = { showAddHiitRoutineDialog = true },
                        onExportClick = { showExportDialog = true },
                        onImportClick = { showImportDialog = true }
                    )
                }
            }
            
            // Mostrar mensajes de error/éxito
            uiState.errorMessage?.let { message ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearMessages() }) {
                            Text("OK", color = Color.White)
                        }
                    },
                    containerColor = Color(0xFFDC2626)
                ) {
                    Text(message, color = Color.White)
                }
            }
            
            uiState.successMessage?.let { message ->
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearMessages() }) {
                            Text("OK", color = Color.White)
                        }
                    },
                    containerColor = AccentGreen
                ) {
                    Text(message, color = Color.White)
                }
        }
    }
    
    // Diálogo para agregar rutina
    if (showAddRoutineDialog) {
        AddRoutineDialog(
            onDismiss = { showAddRoutineDialog = false },
            onConfirm = { name, id ->
                viewModel.addRoutine(name, id)
                showAddRoutineDialog = false
            }
        )
    }
    
    // Diálogo de exportación
    if (showExportDialog) {
        ExportImportDialog(
            title = "Exportar Datos",
            isExport = true,
            onDismiss = { showExportDialog = false },
            onConfirm = { type ->
                exportType = type
                val fileName = if (type == "gym") "gym_routines.csv" else "hiit_routine.csv"
                exportLauncher.launch(fileName)
                showExportDialog = false
            }
        )
    }
    
    // Diálogo de importación
    if (showImportDialog) {
        ExportImportDialog(
            title = "Importar Datos",
            isExport = false,
            onDismiss = { showImportDialog = false },
            onConfirm = { type ->
                exportType = type
                importLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "*/*"))
                showImportDialog = false
            }
        )
    }
    
    // Diálogo para agregar rutina HIIT
    if (showAddHiitRoutineDialog) {
        AddHiitRoutineDialog(
            onDismiss = { showAddHiitRoutineDialog = false },
            onConfirm = { name ->
                viewModel.addHiitRoutine(name)
                showAddHiitRoutineDialog = false
            }
        )
    }
}
}
}

@Composable
private fun MainManageContent(
    routines: List<GymRoutineEntity>,
    hiitRoutines: List<HiitRoutineEntity>,
    onRoutineClick: (GymRoutineEntity) -> Unit,
    onHiitRoutineClick: (HiitRoutineEntity) -> Unit,
    onAddRoutineClick: () -> Unit,
    onAddHiitRoutineClick: () -> Unit,
    onExportClick: () -> Unit,
    onImportClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sección de rutinas de gym
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Rutinas de Gimnasio",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onAddRoutineClick) {
                    Icon(Icons.Default.Add, "Agregar", tint = AccentGreen)
                }
            }
        }
        
        items(routines) { routine ->
            DataCard(
                title = routine.name,
                subtitle = "ID: ${routine.routineId}",
                gradientColors = listOf(Color(0xFF059669), AccentGreen),
                onClick = { onRoutineClick(routine) }
            )
        }
        
        // Sección HIIT
        item {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Rutinas HIIT",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onAddHiitRoutineClick) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Agregar Rutina HIIT",
                        tint = AccentCyan
                    )
                }
            }
        }
        
        if (hiitRoutines.isEmpty()) {
            item {
                Text(
                    "No hay rutinas HIIT. Crea una nueva.",
                    color = TextGray,
                    fontSize = 14.sp
                )
            }
        } else {
            items(hiitRoutines) { routine ->
                DataCard(
                    title = routine.name,
                    subtitle = "Toca para editar",
                    gradientColors = listOf(Color(0xFF0891b2), AccentCyan),
                    onClick = { onHiitRoutineClick(routine) }
                )
            }
        }
        
        // Sección de exportar/importar
        item {
            Spacer(Modifier.height(24.dp))
            Text(
                "Importar / Exportar",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onExportClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen)
                ) {
                    Icon(Icons.Default.Upload, "Exportar", modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Exportar CSV")
                }
                
                Button(
                    onClick = onImportClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Icon(Icons.Default.Download, "Importar", modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Importar CSV")
                }
            }
        }
    }
}

@Composable
private fun RoutineEditContent(
    routineWithDays: GymRoutineWithDays,
    onDayClick: (GymDayEntity) -> Unit,
    onAddDay: (String) -> Unit,
    onUpdateRoutine: (GymRoutineEntity) -> Unit,
    onDeleteRoutine: (GymRoutineEntity) -> Unit,
    onDeleteDay: (GymDayEntity) -> Unit
) {
    var showEditNameDialog by remember { mutableStateOf(false) }
    var showAddDayDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var dayToDelete by remember { mutableStateOf<GymDayEntity?>(null) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Nombre de la rutina editable
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Nombre de la rutina", color = TextGray, fontSize = 12.sp)
                        Text(routineWithDays.routine.name, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    Row {
                        IconButton(onClick = { showEditNameDialog = true }) {
                            Icon(Icons.Default.Edit, "Editar", tint = AccentGreen)
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, "Eliminar", tint = Color(0xFFEF4444))
                        }
                    }
                }
            }
        }
        
        // Días de la rutina
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Días", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { showAddDayDialog = true }) {
                    Icon(Icons.Default.Add, "Agregar día", tint = AccentGreen)
                }
            }
        }
        
        itemsIndexed(routineWithDays.days) { index, dayWithExercises ->
            DayCard(
                day = dayWithExercises.day,
                exerciseCount = dayWithExercises.exercises.size,
                onClick = { onDayClick(dayWithExercises.day) },
                onDelete = { dayToDelete = dayWithExercises.day }
            )
        }
    }
    
    // Diálogos
    if (showEditNameDialog) {
        EditTextDialog(
            title = "Editar nombre",
            initialValue = routineWithDays.routine.name,
            onDismiss = { showEditNameDialog = false },
            onConfirm = { newName ->
                onUpdateRoutine(routineWithDays.routine.copy(name = newName))
                showEditNameDialog = false
            }
        )
    }
    
    if (showAddDayDialog) {
        EditTextDialog(
            title = "Agregar día",
            initialValue = "Día ${routineWithDays.days.size + 1}",
            onDismiss = { showAddDayDialog = false },
            onConfirm = { title ->
                onAddDay(title)
                showAddDayDialog = false
            }
        )
    }
    
    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Eliminar rutina",
            message = "¿Estás seguro de eliminar '${routineWithDays.routine.name}'? Se eliminarán todos los días y ejercicios.",
            onDismiss = { showDeleteConfirm = false },
            onConfirm = {
                onDeleteRoutine(routineWithDays.routine)
                showDeleteConfirm = false
            }
        )
    }
    
    dayToDelete?.let { day ->
        ConfirmDialog(
            title = "Eliminar día",
            message = "¿Estás seguro de eliminar '${day.title}'?",
            onDismiss = { dayToDelete = null },
            onConfirm = {
                onDeleteDay(day)
                dayToDelete = null
            }
        )
    }
}

@Composable
private fun DayEditContent(
    dayWithExercises: GymDayWithExercises,
    onAddExercise: (String, Int, Int, Int) -> Unit,
    onUpdateExercise: (GymExerciseEntity) -> Unit,
    onDeleteExercise: (GymExerciseEntity) -> Unit,
    onUpdateDay: (GymDayEntity) -> Unit
) {
    var showEditTitleDialog by remember { mutableStateOf(false) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var exerciseToEdit by remember { mutableStateOf<GymExerciseEntity?>(null) }
    var exerciseToDelete by remember { mutableStateOf<GymExerciseEntity?>(null) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Título del día editable
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Título del día", color = TextGray, fontSize = 12.sp)
                        Text(dayWithExercises.day.title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(onClick = { showEditTitleDialog = true }) {
                        Icon(Icons.Default.Edit, "Editar", tint = AccentGreen)
                    }
                }
            }
        }
        
        // Ejercicios
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Ejercicios", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { showAddExerciseDialog = true }) {
                    Icon(Icons.Default.Add, "Agregar ejercicio", tint = AccentGreen)
                }
            }
        }
        
        if (dayWithExercises.exercises.isEmpty()) {
            item {
                Text(
                    "No hay ejercicios. Toca + para agregar.",
                    color = TextGray,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        
        items(dayWithExercises.exercises) { exercise ->
            ExerciseCard(
                exercise = exercise,
                onEdit = { exerciseToEdit = exercise },
                onDelete = { exerciseToDelete = exercise }
            )
        }
    }
    
    // Diálogos
    if (showEditTitleDialog) {
        EditTextDialog(
            title = "Editar título",
            initialValue = dayWithExercises.day.title,
            onDismiss = { showEditTitleDialog = false },
            onConfirm = { newTitle ->
                onUpdateDay(dayWithExercises.day.copy(title = newTitle))
                showEditTitleDialog = false
            }
        )
    }
    
    if (showAddExerciseDialog) {
        ExerciseEditDialog(
            title = "Agregar ejercicio",
            exercise = null,
            onDismiss = { showAddExerciseDialog = false },
            onConfirm = { name, sets, reps, rest ->
                onAddExercise(name, sets, reps, rest)
                showAddExerciseDialog = false
            }
        )
    }
    
    exerciseToEdit?.let { exercise ->
        ExerciseEditDialog(
            title = "Editar ejercicio",
            exercise = exercise,
            onDismiss = { exerciseToEdit = null },
            onConfirm = { name, sets, reps, rest ->
                onUpdateExercise(exercise.copy(name = name, sets = sets, baseReps = reps, restSeconds = rest))
                exerciseToEdit = null
            }
        )
    }
    
    exerciseToDelete?.let { exercise ->
        ConfirmDialog(
            title = "Eliminar ejercicio",
            message = "¿Estás seguro de eliminar '${exercise.name}'?",
            onDismiss = { exerciseToDelete = null },
            onConfirm = {
                onDeleteExercise(exercise)
                exerciseToDelete = null
            }
        )
    }
}

// --- COMPONENTES REUTILIZABLES ---

@Composable
private fun DataCard(
    title: String,
    subtitle: String,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(8.dp)
                    .height(64.dp)
                    .background(Brush.verticalGradient(gradientColors))
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold)
                Text(subtitle, color = TextGray, fontSize = 12.sp)
            }
            Icon(
                Icons.Default.ChevronRight,
                "Ver",
                tint = TextGray,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun DayCard(
    day: GymDayEntity,
    exerciseCount: Int,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(day.title, color = Color.White, fontWeight = FontWeight.Medium)
                Text(
                    if (exerciseCount > 0) "$exerciseCount ejercicios" else "Sin ejercicios (descanso)",
                    color = TextGray,
                    fontSize = 12.sp
                )
            }
            Row {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Eliminar", tint = Color(0xFFEF4444))
                }
                Icon(Icons.Default.ChevronRight, "Ver", tint = TextGray)
            }
        }
    }
}

@Composable
private fun ExerciseCard(
    exercise: GymExerciseEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(exercise.name, color = Color.White, fontWeight = FontWeight.Medium)
                Text(
                    "${exercise.sets} series × ${exercise.baseReps} reps • ${exercise.restSeconds}s descanso",
                    color = TextGray,
                    fontSize = 12.sp
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Editar", tint = AccentGreen)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Eliminar", tint = Color(0xFFEF4444))
                }
            }
        }
    }
}

// --- DIÁLOGOS ---

@Composable
private fun AddRoutineDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, id: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var id by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = { Text("Nueva Rutina", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = TextGray
                    )
                )
                OutlinedTextField(
                    value = id,
                    onValueChange = { id = it.lowercase().replace(" ", "-") },
                    label = { Text("ID (sin espacios)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = TextGray
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank() && id.isNotBlank()) onConfirm(name, id) },
                enabled = name.isNotBlank() && id.isNotBlank()
            ) {
                Text("Crear", color = AccentGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextGray)
            }
        }
    )
}

@Composable
private fun EditTextDialog(
    title: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var value by remember { mutableStateOf(initialValue) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = { Text(title, color = Color.White) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = AccentGreen,
                    unfocusedBorderColor = TextGray
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (value.isNotBlank()) onConfirm(value) },
                enabled = value.isNotBlank()
            ) {
                Text("Guardar", color = AccentGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextGray)
            }
        }
    )
}

@Composable
private fun ExerciseEditDialog(
    title: String,
    exercise: GymExerciseEntity?,
    onDismiss: () -> Unit,
    onConfirm: (name: String, sets: Int, reps: Int, rest: Int) -> Unit
) {
    var name by remember { mutableStateOf(exercise?.name ?: "") }
    var sets by remember { mutableStateOf(exercise?.sets?.toString() ?: "4") }
    var reps by remember { mutableStateOf(exercise?.baseReps?.toString() ?: "10") }
    var rest by remember { mutableStateOf(exercise?.restSeconds?.toString() ?: "60") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = { Text(title, color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del ejercicio") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentGreen,
                        unfocusedBorderColor = TextGray
                    )
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = sets,
                        onValueChange = { sets = it.filter { c -> c.isDigit() } },
                        label = { Text("Series") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentGreen,
                            unfocusedBorderColor = TextGray
                        )
                    )
                    OutlinedTextField(
                        value = reps,
                        onValueChange = { reps = it.filter { c -> c.isDigit() } },
                        label = { Text("Reps") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentGreen,
                            unfocusedBorderColor = TextGray
                        )
                    )
                    OutlinedTextField(
                        value = rest,
                        onValueChange = { rest = it.filter { c -> c.isDigit() } },
                        label = { Text("Desc(s)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentGreen,
                            unfocusedBorderColor = TextGray
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(
                            name,
                            sets.toIntOrNull() ?: 4,
                            reps.toIntOrNull() ?: 10,
                            rest.toIntOrNull() ?: 60
                        )
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Guardar", color = AccentGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextGray)
            }
        }
    )
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = { Text(title, color = Color.White) },
        text = { Text(message, color = TextGray) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Eliminar", color = Color(0xFFEF4444))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextGray)
            }
        }
    )
}

@Composable
private fun ExportImportDialog(
    title: String,
    isExport: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (type: String) -> Unit
) {
    var selectedType by remember { mutableStateOf("gym") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = { Text(title, color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    if (isExport) "Selecciona qué datos exportar:" else "Selecciona qué tipo de datos importar:",
                    color = TextGray
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedType == "gym",
                        onClick = { selectedType = "gym" },
                        label = { Text("Rutinas Gym") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedType == "hiit",
                        onClick = { selectedType = "hiit" },
                        label = { Text("Rutina HIIT") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentCyan,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedType) }) {
                Text(if (isExport) "Exportar" else "Importar", color = AccentGreen)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextGray)
            }
        }
    )
}

// --- HIIT EDITING COMPOSABLES ---

@Composable
private fun AddHiitRoutineDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = { Text("Nueva Rutina HIIT", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre de la rutina") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = TextGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onConfirm(name.trim()) },
                enabled = name.isNotBlank()
            ) {
                Text("Crear", color = if (name.isNotBlank()) AccentCyan else TextGray)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextGray)
            }
        }
    )
}

@Composable
private fun HiitRoutineEditContent(
    routineWithSteps: HiitRoutineWithSteps,
    onAddStep: (String, Int, String) -> Unit,
    onUpdateStep: (HiitStepEntity) -> Unit,
    onDeleteStep: (HiitStepEntity) -> Unit,
    onUpdateRoutine: (HiitRoutineEntity) -> Unit,
    onDeleteRoutine: (HiitRoutineEntity) -> Unit
) {
    var showAddStepDialog by remember { mutableStateOf(false) }
    var editingStep by remember { mutableStateOf<HiitStepEntity?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var editingName by remember { mutableStateOf(routineWithSteps.routine.name) }
    var showEditNameDialog by remember { mutableStateOf(false) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header con acciones
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        routineWithSteps.routine.name,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${routineWithSteps.steps.size} pasos",
                        color = TextGray,
                        fontSize = 14.sp
                    )
                }
                Row {
                    IconButton(onClick = { showEditNameDialog = true }) {
                        Icon(Icons.Default.Edit, "Editar nombre", tint = AccentCyan)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, "Eliminar rutina", tint = Color(0xFFEF4444))
                    }
                }
            }
        }
        
        item { Divider(color = TextGray.copy(alpha = 0.3f)) }
        
        // Título de pasos
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Pasos de la rutina",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showAddStepDialog = true }) {
                    Icon(Icons.Default.Add, "Agregar paso", tint = AccentCyan)
                }
            }
        }
        
        // Lista de pasos
        if (routineWithSteps.steps.isEmpty()) {
            item {
                Text(
                    "No hay pasos. Agrega uno para comenzar.",
                    color = TextGray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        } else {
            items(routineWithSteps.steps) { step ->
                HiitStepCard(
                    step = step,
                    onEdit = { editingStep = step },
                    onDelete = { onDeleteStep(step) }
                )
            }
        }
    }
    
    // Diálogo para agregar paso
    if (showAddStepDialog) {
        AddEditHiitStepDialog(
            step = null,
            onDismiss = { showAddStepDialog = false },
            onConfirm = { name, duration, type ->
                onAddStep(name, duration, type)
                showAddStepDialog = false
            }
        )
    }
    
    // Diálogo para editar paso
    editingStep?.let { step ->
        AddEditHiitStepDialog(
            step = step,
            onDismiss = { editingStep = null },
            onConfirm = { name, duration, type ->
                onUpdateStep(step.copy(name = name, durationSeconds = duration, type = type))
                editingStep = null
            }
        )
    }
    
    // Diálogo para editar nombre de rutina
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            containerColor = CardBg,
            title = { Text("Editar Nombre", color = Color.White) },
            text = {
                OutlinedTextField(
                    value = editingName,
                    onValueChange = { editingName = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = TextGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateRoutine(routineWithSteps.routine.copy(name = editingName.trim()))
                    showEditNameDialog = false
                }) {
                    Text("Guardar", color = AccentCyan)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancelar", color = TextGray)
                }
            }
        )
    }
    
    // Diálogo de confirmación de eliminación
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = CardBg,
            title = { Text("¿Eliminar rutina?", color = Color.White) },
            text = { Text("Esta acción eliminará la rutina y todos sus pasos.", color = TextGray) },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteRoutine(routineWithSteps.routine)
                    showDeleteConfirm = false
                }) {
                    Text("Eliminar", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar", color = TextGray)
                }
            }
        )
    }
}

@Composable
private fun HiitStepCard(
    step: HiitStepEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val typeColor = when (step.type) {
        "WARMUP" -> Color(0xFFFBBF24)
        "EXERCISE" -> AccentGreen
        "REST" -> Color(0xFF60A5FA)
        "BREAK" -> Color(0xFFA78BFA)
        "COOLDOWN" -> Color(0xFF2DD4BF)
        else -> TextGray
    }
    
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Indicador de tipo
                Surface(
                    color = typeColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = step.type.first().toString(),
                            color = typeColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    Text(
                        step.name,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        "${step.durationSeconds}s · ${step.type}",
                        color = TextGray,
                        fontSize = 12.sp
                    )
                }
            }
            
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Editar", tint = TextGray, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Eliminar", tint = Color(0xFFEF4444).copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditHiitStepDialog(
    step: HiitStepEntity?,
    onDismiss: () -> Unit,
    onConfirm: (String, Int, String) -> Unit
) {
    var name by remember { mutableStateOf(step?.name ?: "") }
    var duration by remember { mutableStateOf((step?.durationSeconds ?: 30).toString()) }
    var selectedType by remember { mutableStateOf(step?.type ?: "EXERCISE") }
    var typeExpanded by remember { mutableStateOf(false) }
    
    val stepTypes = listOf("WARMUP", "EXERCISE", "REST", "BREAK", "COOLDOWN")
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardBg,
        title = { Text(if (step == null) "Agregar Paso" else "Editar Paso", color = Color.White) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre del paso") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = TextGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Duración
                OutlinedTextField(
                    value = duration,
                    onValueChange = { if (it.all { c -> c.isDigit() }) duration = it },
                    label = { Text("Duración (segundos)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = TextGray
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Tipo (Dropdown)
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de paso") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = AccentCyan,
                            unfocusedBorderColor = TextGray
                        ),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        stepTypes.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type) },
                                onClick = {
                                    selectedType = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val durationInt = duration.toIntOrNull() ?: 30
                    if (name.isNotBlank() && durationInt > 0) {
                        onConfirm(name.trim(), durationInt, selectedType)
                    }
                },
                enabled = name.isNotBlank() && (duration.toIntOrNull() ?: 0) > 0
            ) {
                Text(if (step == null) "Agregar" else "Guardar", color = AccentCyan)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextGray)
            }
        }
    )
}
