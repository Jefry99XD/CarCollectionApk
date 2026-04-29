package com.example.carcollection.featureAchievements.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.carcollection.featureAchievements.domain.*
import kotlinx.coroutines.launch

/* ───── Componente helper ───── */
@Composable
fun DropdownSelector(
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(selected)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAchievementForm(
    viewModel: AchievementViewModel,
    onBackClick: () -> Unit,
    achievementId: String? = null // null = crear nuevo, valor = editar
) {
    val scope = rememberCoroutineScope()
    val isLoading by viewModel.isLoading.collectAsState()

    val isEditMode = achievementId != null

    /* ───── Estado básico ───── */
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var iconUrl by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }

    var category by remember { mutableStateOf(AchievementCategory.COLLECTION) }
    var rarity by remember { mutableStateOf(AchievementRarity.COMUN) }
    var hidden by remember { mutableStateOf(false) }
    var active by remember { mutableStateOf(true) }
    var conditionLogic by remember { mutableStateOf(ConditionLogic.AND) }
    var rules by remember { mutableStateOf(AchievementRules()) }

    /* ───── TIME_BASED: Rango personalizado ───── */
    var timeValue by remember { mutableStateOf("") }
    var timeUnit by remember { mutableStateOf("DAYS") } // DAYS, MONTHS, YEARS

    /* ───── STREAK_BASED: Racha personalizada ───── */
    var streakValue by remember { mutableStateOf("") }
    var streakUnit by remember { mutableStateOf("DAYS") } // DAYS, MONTHS
    var streakDays by remember { mutableStateOf("") } // Cantidad de días consecutivos

    /* ───── Condición actual ───── */
    var concept by remember { mutableStateOf("") }
    var aliases by remember { mutableStateOf("") }
    var matchType by remember { mutableStateOf(MatchType.EXACT) }
    var selectedFields by remember { mutableStateOf(setOf<CarMatchField>()) }
    var allowMultiplePerConcept by remember { mutableStateOf(false) }

    val conditions = remember { mutableStateListOf<AchievementCondition>() }

    // Para editar condiciones
    var editingConditionIndex by remember { mutableStateOf<Int?>(null) }

    /* ───── Logros exclusivos ───── */
    var isExclusive by remember { mutableStateOf(false) }
    var selectedUserIds by remember { mutableStateOf(listOf<String>()) }
    var userSearchQuery by remember { mutableStateOf("") }
    var availableUsers by remember { mutableStateOf(listOf<Pair<String, String>>()) }
    var showUserDropdown by remember { mutableStateOf(false) }
    var isLoadingUsers by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var isLoadingData by remember { mutableStateOf(false) }

    // Cargar datos si está en modo edición
    LaunchedEffect(achievementId) {
        if (achievementId != null) {
            isLoadingData = true
            scope.launch {
                try {
                    val achievement = viewModel.getAchievementById(achievementId)
                    if (achievement != null) {
                        title = achievement.title
                        description = achievement.description
                        iconUrl = achievement.iconUrl
                        goal = achievement.goal.toString()
                        category = achievement.category
                        rarity = achievement.rarity
                        hidden = achievement.hidden
                        active = achievement.active
                        conditionLogic = achievement.rules.conditionLogic
                        rules = achievement.rules
                        conditions.clear()
                        conditions.addAll(achievement.conditions)

                        // Cargar datos exclusivos
                        isExclusive = achievement.isExclusive
                        selectedUserIds = achievement.exclusiveUserIds
                    }
                } catch (e: Exception) {
                    errorMessage = "Error al cargar el logro: ${e.message}"
                }
                isLoadingData = false
            }
        }
    }

    // Cargar usuarios cuando se activa logro exclusivo
    LaunchedEffect(isExclusive) {
        if (isExclusive && availableUsers.isEmpty()) {
            isLoadingUsers = true
            scope.launch {
                try {
                    val result = viewModel.getAllUsers()
                    availableUsers = result
                } catch (e: Exception) {
                    errorMessage = "Error al cargar usuarios: ${e.message}"
                }
                isLoadingUsers = false
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) "Editar logro" else "Agregar logro") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->

        if (isLoadingData) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                /* ───── Guía de ayuda ───── */
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "💡 Ejemplos rápidos",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "COLLECTION:\n" +
                                    "• Total: Concepto vacío, campos NAME, meta 500\n" +
                                    "• Marca: Concepto 'ferrari', campos BRAND, meta 10\n" +
                                    "• Calidad: Concepto 'th', campos QUALITY (EXACT), meta 15\n" +
                                    "• Lista OR: Condiciones sin allowMultiple, meta total\n" +
                                    "• Complejos AND: Varias condiciones, todas deben cumplirse\n" +
                                    "\nTIME_BASED:\n" +
                                    "• Mes: Carros agregados en mes específico\n" +
                                    "\nUSER:\n" +
                                    "• Nivel: Sin condiciones, meta = nivel requerido",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                /* ───── Categoría (mover arriba) ───── */
                Text("Categoría", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "COLLECTION = Carros (con condiciones) | TIME_BASED = Tiempo | USER = Nivel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                DropdownSelector(
                    selected = category.name,
                    options = AchievementCategory.entries.map { it.name },
                    onSelected = { selectedCategory ->
                        category = AchievementCategory.valueOf(selectedCategory)
                    }
                )

                Spacer(Modifier.height(16.dp))

                /* ───── Rareza ───── */
                Text("Rareza (Determina XP otorgada)", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "COMUN = 200 XP | RARO = 400 XP | LEGENDARIO = 800 XP | SPECIAL = 1200 XP",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                DropdownSelector(
                    selected = rarity.name,
                    options = AchievementRarity.entries.map { it.name },
                    onSelected = { selectedRarity ->
                        rarity = AchievementRarity.valueOf(selectedRarity)
                    }
                )

                Spacer(Modifier.height(16.dp))

                /* ───── Campos base ───── */
                if (category != AchievementCategory.USER) {
                    Text(
                        text = "El ID se generará automáticamente desde el título",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text(
                        text = "El ID se generará automáticamente como 'level_X'",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    placeholder = { Text(if (category == AchievementCategory.USER) "Ej: Coleccionista Principiante" else "Ej: Registra 500 carros") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    placeholder = { Text(if (category == AchievementCategory.USER) "Ej: Alcanza el nivel 5" else "Ej: Agrega 500 carros a tu colección") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = iconUrl,
                    onValueChange = { iconUrl = it },
                    label = { Text("URL ícono (opcional)") },
                    placeholder = { Text("https://...") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = goal,
                    onValueChange = { goal = it },
                    label = { Text(if (category == AchievementCategory.USER) "Nivel requerido" else "Meta (cantidad)") },
                    placeholder = { Text(if (category == AchievementCategory.USER) "Ej: 5, 10, 50" else "Ej: 10, 50, 500") },
                    supportingText = {
                        if (category == AchievementCategory.USER) {
                            Text("El nivel que el usuario debe alcanzar")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                /* ───── SECCIÓN POR CATEGORÍA ───── */

                if (category == AchievementCategory.TIME_BASED) {
                    /* ───── TIME_BASED: Rango personalizado ───── */
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                                alpha = 0.3f
                            )
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "⏰ Rango de Tiempo Personalizado",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Especifica cuánto tiempo atrás deben haberse agregado los carros",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))

                            // Campo de entrada para el número
                            OutlinedTextField(
                                value = timeValue,
                                onValueChange = { timeValue = it },
                                label = { Text("Cantidad") },
                                placeholder = { Text("Ej: 1, 2, 6, 12") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                )
                            )

                            Spacer(Modifier.height(12.dp))

                            // Selector de unidad
                            Text("Unidad de tiempo", style = MaterialTheme.typography.labelMedium)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("DAYS" to "Días", "MONTHS" to "Meses", "YEARS" to "Años").forEach { (unit, label) ->
                                    FilterChip(
                                        selected = timeUnit == unit,
                                        onClick = { timeUnit = unit },
                                        label = { Text(label) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Ejemplo dinámico
                            if (timeValue.isNotEmpty()) {
                                Text(
                                    "Ejemplo: Necesitas agregar carros en los últimos $timeValue ${timeUnit.lowercase().dropLast(1)}s.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    "Ejemplo: 2 en Meses = últimos 2 meses | 6 en Días = últimos 6 días",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                } else if (category == AchievementCategory.STREAK_BASED) {
                    /* ───── STREAK_BASED: Racha Personalizada ───── */
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                                alpha = 0.3f
                            )
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                "🔥 Racha de Carros",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Agrega X carros en X días/meses consecutivos sin romper la racha",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))

                            // Campo: Cantidad de carros
                            OutlinedTextField(
                                value = streakValue,
                                onValueChange = { streakValue = it },
                                label = { Text("Carros a agregar") },
                                placeholder = { Text("Ej: 5, 10, 15") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                )
                            )

                            Spacer(Modifier.height(12.dp))

                            // Campo: Cantidad de días consecutivos
                            OutlinedTextField(
                                value = streakDays,
                                onValueChange = { streakDays = it },
                                label = { Text("Días consecutivos") },
                                placeholder = { Text("Ej: 7, 14, 30") },
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                                )
                            )

                            Spacer(Modifier.height(12.dp))

                            // Selector de unidad para el rango inicial
                            Text("Duración máxima de racha", style = MaterialTheme.typography.labelMedium)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("DAYS" to "Días", "MONTHS" to "Meses").forEach { (unit, label) ->
                                    FilterChip(
                                        selected = streakUnit == unit,
                                        onClick = { streakUnit = unit },
                                        label = { Text(label) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Ejemplo dinámico
                            if (streakValue.isNotEmpty() && streakDays.isNotEmpty()) {
                                Text(
                                    "Ejemplo: Agregar $streakValue carros en $streakDays días consecutivos sin faltar.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                Text(
                                    "Ejemplo: 5 carros en 7 días = agregar al menos 1 carro cada día durante una semana",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                } else if (category == AchievementCategory.USER) {
                    /* ───── USER: Logro de Nivel ───── */
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(
                                alpha = 0.3f
                            )
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "🎮 Logro de Nivel",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Los logros de nivel no requieren condiciones. Se desbloquean automáticamente cuando el usuario alcanza el nivel especificado.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                } else {
                    // COLLECTION: Condiciones
                    /* ───── COLLECTION: Condiciones ───── */
                    Text("Lógica de condiciones", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = if (conditionLogic == ConditionLogic.AND)
                            "AND: Un carro debe cumplir TODAS las condiciones (Ej: Ferrari roja)"
                        else
                            "OR: Un carro debe cumplir AL MENOS UNA condición (Ej: Lista de nombres)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = conditionLogic == ConditionLogic.AND,
                            onClick = { conditionLogic = ConditionLogic.AND },
                            label = { Text("AND (Todas)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = conditionLogic == ConditionLogic.OR,
                            onClick = { conditionLogic = ConditionLogic.OR },
                            label = { Text("OR (Al menos una)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    /* ───── Condición ───── */
                    Text("Nueva condición", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Para cantidad total: deja el concepto vacío. Para filtrar: escribe el concepto.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(Modifier.height(8.dp))

                    OutlinedTextField(
                        value = concept,
                        onValueChange = { concept = it },
                        label = { Text("Concepto") },
                        placeholder = { Text("Vacío = cualquier carro, o 'premium', 'pontiac'...") },
                        supportingText = { Text("Deja vacío para contar cualquier carro") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(6.dp))

                    OutlinedTextField(
                        value = aliases,
                        onValueChange = { aliases = it },
                        label = { Text("Aliases (separados por coma)") },
                        placeholder = { Text("Ej: sth, treasure hunt, th") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(6.dp))

                    Text("Campos a evaluar")

                    CarMatchField.entries.forEach { field ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = field in selectedFields,
                                onCheckedChange = {
                                    selectedFields =
                                        if (it) selectedFields + field
                                        else selectedFields - field
                                }
                            )
                            Text(field.name)
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    DropdownSelector(
                        selected = matchType.name,
                        options = MatchType.entries.map { it.name },
                        onSelected = { selectedMatchType ->
                            matchType = MatchType.valueOf(selectedMatchType)
                        }
                    )

                    Spacer(Modifier.height(8.dp))

                    // Checkbox para permitir múltiples conceptos
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = allowMultiplePerConcept,
                            onCheckedChange = { allowMultiplePerConcept = it }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Contar múltiples por concepto",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                if (allowMultiplePerConcept)
                                    "✓ Contará todos los carros (Ej: 10 Ferrari + 10 Lamborghini = 20)"
                                else
                                    "✗ Solo contará 1 por concepto (Ej: 1 Ferrari + 1 Lamborghini = 2)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Mostrar botón de cancelar si estamos editando
                    if (editingConditionIndex != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    editingConditionIndex = null
                                    concept = ""
                                    aliases = ""
                                    selectedFields = emptySet()
                                    matchType = MatchType.EXACT
                                    allowMultiplePerConcept = false
                                }
                            ) {
                                Text("Cancelar edición")
                            }
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    if (selectedFields.isEmpty()) {
                                        errorMessage = "Selecciona al menos un campo a evaluar"
                                        return@Button
                                    }

                                    val index = editingConditionIndex ?: return@Button
                                    conditions[index] = AchievementCondition(
                                        concept = concept.trim(),
                                        aliases = aliases.split(",").map { it.trim() }
                                            .filter { it.isNotEmpty() },
                                        matchFields = selectedFields.toList(),
                                        matchType = matchType,
                                        allowMultiplePerConcept = allowMultiplePerConcept
                                    )

                                    editingConditionIndex = null
                                    concept = ""
                                    aliases = ""
                                    selectedFields = emptySet()
                                    matchType = MatchType.EXACT
                                    allowMultiplePerConcept = false
                                    errorMessage = ""
                                }
                            ) {
                                Text("Actualizar condición")
                            }
                        }
                    } else {
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                // Validate - concept can be empty for "any car" achievements
                                if (selectedFields.isEmpty()) {
                                    errorMessage = "Selecciona al menos un campo a evaluar"
                                    return@Button
                                }

                                conditions += AchievementCondition(
                                    concept = concept.trim(),
                                    aliases = aliases.split(",").map { it.trim() }
                                        .filter { it.isNotEmpty() },
                                    matchFields = selectedFields.toList(),
                                    matchType = matchType,
                                    allowMultiplePerConcept = allowMultiplePerConcept
                                )

                                // Reset form
                                concept = ""
                                aliases = ""
                                selectedFields = emptySet()
                                matchType = MatchType.EXACT
                                allowMultiplePerConcept = false
                                errorMessage = ""
                            }
                        ) {
                            Text("Agregar condición")
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    /* ───── Lista de condiciones agregadas ───── */
                    if (conditions.isNotEmpty()) {
                        Text(
                            "Condiciones agregadas (${conditions.size})",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))

                        conditions.forEachIndexed { index, condition ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (condition.concept.isEmpty())
                                                "Concepto: (cualquier carro)"
                                            else
                                                "Concepto: ${condition.concept}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                        )
                                        if (condition.aliases.isNotEmpty()) {
                                            Text(
                                                text = "Aliases: ${condition.aliases.joinToString(", ")}",
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                        Text(
                                            text = "Campos: ${condition.matchFields.joinToString(", ") { it.name }}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        Text(
                                            text = "Tipo: ${condition.matchType.name}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                        if (condition.allowMultiplePerConcept) {
                                            Text(
                                                text = "✓ Múltiples por concepto",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Row {
                                        IconButton(
                                            onClick = {
                                                // Cargar condición en el formulario
                                                concept = condition.concept
                                                aliases = condition.aliases.joinToString(", ")
                                                selectedFields = condition.matchFields.toSet()
                                                matchType = condition.matchType
                                                allowMultiplePerConcept =
                                                    condition.allowMultiplePerConcept
                                                editingConditionIndex = index
                                                errorMessage = ""
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Editar condición",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        IconButton(onClick = { conditions.removeAt(index) }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Eliminar condición",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                    }

                    // Fin de sección COLLECTION
                    Spacer(Modifier.height(16.dp))
                }

                /* ───── Opciones adicionales ───── */
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Oculto")
                    Switch(checked = hidden, onCheckedChange = { hidden = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Activo")
                    Switch(checked = active, onCheckedChange = { active = it })
                }

                Spacer(Modifier.height(16.dp))

                /* ───── LOGROS EXCLUSIVOS ───── */
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "🏆 Logro Exclusivo",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                )
                                Text(
                                    "Asignar este logro solo a ciertos usuarios",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = isExclusive,
                                onCheckedChange = { isExclusive = it }
                            )
                        }

                        if (isExclusive) {
                            Spacer(Modifier.height(12.dp))

                            Text(
                                "Usuarios seleccionados: ${selectedUserIds.size}",
                                style = MaterialTheme.typography.bodySmall
                            )

                            Spacer(Modifier.height(8.dp))

                            // Mostrar usuarios seleccionados
                            if (selectedUserIds.isNotEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 150.dp)
                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant)
                                        .verticalScroll(rememberScrollState()),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    selectedUserIds.forEachIndexed { index, userId ->
                                        val userName =
                                            availableUsers.find { it.first == userId }?.second
                                                ?: userId

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    userName,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                                )
                                                Text(
                                                    userId,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            IconButton(
                                                onClick = {
                                                    selectedUserIds =
                                                        selectedUserIds.filterIndexed { i, _ -> i != index }
                                                },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Delete,
                                                    contentDescription = "Remover usuario",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.error
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(Modifier.height(8.dp))
                            }

                            // Dropdown para seleccionar usuarios - Simplificado con tamaño fijo
                            if (!isLoadingUsers) {
                                OutlinedButton(
                                    onClick = { showUserDropdown = !showUserDropdown },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Seleccionar usuarios")
                                        Icon(
                                            if (showUserDropdown) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Toggle dropdown"
                                        )
                                    }
                                }
                            } else {
                                Text("Cargando usuarios...", style = MaterialTheme.typography.bodySmall)
                            }

                            if (showUserDropdown && !isLoadingUsers) {
                                Spacer(Modifier.height(8.dp))

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(300.dp)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp)),
                                    elevation = CardDefaults.cardElevation(4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        OutlinedTextField(
                                            value = userSearchQuery,
                                            onValueChange = { userSearchQuery = it },
                                            label = { Text("Buscar") },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(8.dp),
                                            singleLine = true
                                        )

                                        HorizontalDivider()

                                        val filteredUsers = if (userSearchQuery.isBlank()) {
                                            availableUsers
                                        } else {
                                            availableUsers.filter { (uid, name) ->
                                                name.contains(userSearchQuery, ignoreCase = true) ||
                                                uid.contains(userSearchQuery, ignoreCase = true)
                                            }
                                        }

                                        if (filteredUsers.isEmpty()) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("No encontrado")
                                            }
                                        } else {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .weight(1f)
                                                    .verticalScroll(rememberScrollState())
                                            ) {
                                                filteredUsers.forEach { (uid, name) ->
                                                    val isSelected = uid in selectedUserIds
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                selectedUserIds = if (isSelected) {
                                                                    selectedUserIds.filter { it != uid }
                                                                } else {
                                                                    selectedUserIds + uid
                                                                }
                                                            }
                                                            .padding(8.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(name, style = MaterialTheme.typography.bodySmall)
                                                        }
                                                        if (isSelected) {
                                                            Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(4.dp))

                            Text(
                                "💡 Busca usuarios por nombre o ID. Puedes seleccionar múltiples usuarios.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                /* ───── Guardar ───── */
                Button(
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    enabled = !isLoading,
                    onClick = {
                        errorMessage = ""
                        successMessage = ""

                        // Validate
                        if (title.isBlank()) {
                            errorMessage = "El título es obligatorio"
                            return@Button
                        }

                        val goalInt = goal.toIntOrNull()
                        if (goalInt == null || goalInt <= 0) {
                            errorMessage = if (category == AchievementCategory.USER) {
                                "El nivel debe ser un número positivo"
                            } else {
                                "La meta debe ser un número positivo"
                            }
                            return@Button
                        }

                        // Para logros COLLECTION, necesitan condiciones
                        if (category == AchievementCategory.COLLECTION && conditions.isEmpty()) {
                            errorMessage = "Agrega al menos una condición"
                            return@Button
                        }

                        // Para logros TIME_BASED, necesitan timeValue
                        if (category == AchievementCategory.TIME_BASED && timeValue.isBlank()) {
                            errorMessage =
                                "Especifica una cantidad de tiempo válida (días, meses o años)"
                            return@Button
                        }

                        // Para logros STREAK_BASED, necesitan streakValue y streakDays
                        if (category == AchievementCategory.STREAK_BASED && (streakValue.isBlank() || streakDays.isBlank())) {
                            errorMessage =
                                "Especifica carros a agregar y días consecutivos para la racha"
                            return@Button
                        }

                        // Validar logros exclusivos
                        if (isExclusive && selectedUserIds.isEmpty()) {
                            errorMessage = "Agrega al menos un usuario para un logro exclusivo"
                            return@Button
                        }

                        scope.launch {
                            // Generar ID según la categoría
                            val generatedId = if (category == AchievementCategory.USER) {
                                // Para logros de nivel: level_X
                                "level_$goalInt"
                            } else {
                                // Para otros: desde el título
                                title.lowercase().trim()
                                    .replace(Regex("[^a-z0-9]+"), "_")
                                    .removePrefix("_")
                                    .removeSuffix("_")
                            }

                            val achievementData = AchievementGlobal(
                                id = if (isEditMode) achievementId else generatedId,
                                title = title,
                                description = description,
                                iconUrl = iconUrl,
                                category = category,
                                rarity = rarity,
                                conditions = if (category == AchievementCategory.USER || category == AchievementCategory.TIME_BASED || category == AchievementCategory.STREAK_BASED) emptyList() else conditions.toList(),
                                goal = goalInt,
                                rules = when (category) {
                                    AchievementCategory.COLLECTION -> AchievementRules(
                                        conditionLogic = conditionLogic
                                    )
                                    AchievementCategory.TIME_BASED -> rules // Usa el que seleccionó el usuario
                                    AchievementCategory.STREAK_BASED -> rules // Usa el que seleccionó el usuario
                                    AchievementCategory.USER -> AchievementRules(conditionLogic = ConditionLogic.AND)
                                },
                                hidden = hidden,
                                active = active,
                                isExclusive = isExclusive,
                                exclusiveUserIds = if (isExclusive) selectedUserIds else emptyList()
                            )

                            if (isEditMode) {
                                viewModel.updateGlobalAchievement(achievementData)
                                successMessage = "Logro actualizado correctamente"
                            } else {
                                viewModel.addGlobalAchievement(achievementData)
                                successMessage = "Logro guardado con ID: ${achievementData.id}"
                                // Reset form only when creating
                                title = ""
                                description = ""
                                iconUrl = ""
                                goal = ""
                                conditions.clear()
                                category = AchievementCategory.COLLECTION
                                conditionLogic = ConditionLogic.AND
                                rules = AchievementRules()
                                hidden = false
                                active = true
                                isExclusive = false
                                selectedUserIds = emptyList()
                                userSearchQuery = ""
                                showUserDropdown = false
                            }
                        }
                    }) {
                        if (isLoading) {
                            CircularProgressIndicator(strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text(if (isEditMode) "Actualizar logro" else "Guardar logro")
                        }
                    }

                    if (errorMessage.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(errorMessage, color = MaterialTheme.colorScheme.error)
                    }

                    if (successMessage.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(successMessage, color = MaterialTheme.colorScheme.primary)
                    }
                } // Column
            } // else
        } // Scaffold
    }

