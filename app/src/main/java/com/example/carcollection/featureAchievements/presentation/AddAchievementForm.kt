package com.example.carcollection.featureAchievements.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
    var hidden by remember { mutableStateOf(false) }
    var active by remember { mutableStateOf(true) }
    var conditionLogic by remember { mutableStateOf(ConditionLogic.AND) }

    /* ───── Condición actual ───── */
    var concept by remember { mutableStateOf("") }
    var aliases by remember { mutableStateOf("") }
    var matchType by remember { mutableStateOf(MatchType.EXACT) }
    var selectedFields by remember { mutableStateOf(setOf<CarMatchField>()) }

    val conditions = remember { mutableStateListOf<AchievementCondition>() }

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
                        hidden = achievement.hidden
                        active = achievement.active
                        conditionLogic = achievement.rules.conditionLogic
                        conditions.clear()
                        conditions.addAll(achievement.conditions)
                    }
                } catch (e: Exception) {
                    errorMessage = "Error al cargar el logro: ${e.message}"
                }
                isLoadingData = false
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
                        "• Cantidad total: Concepto vacío, campos NAME, meta 500\n" +
                        "• Premium: Concepto 'premium', campos QUALITY, meta 10\n" +
                        "• Lista: Lógica OR, una condición por cada nombre\n" +
                        "• Nivel: Categoría USER, título + nivel deseado",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            /* ───── Categoría (mover arriba) ───── */
            Text("Categoría", style = MaterialTheme.typography.titleMedium)
            Text(
                text = "USER = Logros de nivel (simplificado)",
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

            /* ───── Condiciones solo si NO es USER ───── */
            if (category != AchievementCategory.USER) {
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

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = {
                    // Validate - concept can be empty for "any car" achievements
                    if (selectedFields.isEmpty()) {
                        errorMessage = "Selecciona al menos un campo a evaluar"
                        return@Button
                    }

                    conditions += AchievementCondition(
                        concept = concept.trim(),
                        aliases = aliases.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                        matchFields = selectedFields.toList(),
                        matchType = matchType
                    )

                    // Reset form
                    concept = ""
                    aliases = ""
                    selectedFields = emptySet()
                    errorMessage = ""
                }
            ) {
                Text("Agregar condición")
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

                Spacer(Modifier.height(16.dp))
            } else {
                /* ───── Mensaje para logros USER ───── */
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
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
            }

            } // Cierre de if (category != AchievementCategory.USER)

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

                    // Para logros USER, no se necesitan condiciones
                    if (category != AchievementCategory.USER && conditions.isEmpty()) {
                        errorMessage = "Agrega al menos una condición"
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
                            conditions = if (category == AchievementCategory.USER) emptyList() else conditions.toList(),
                            goal = goalInt,
                            rules = AchievementRules(
                                conditionLogic = if (category == AchievementCategory.USER) ConditionLogic.AND else conditionLogic
                            ),
                            hidden = hidden,
                            active = active
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
                            hidden = false
                            active = true
                        }
                    }
                }
            ) {
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

