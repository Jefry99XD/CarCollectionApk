# 🎮 SISTEMA DE NIVELES - EJEMPLOS DE USO

## 📖 PARA DESARROLLADORES

### 1. Usar el LevelBadge en cualquier pantalla

```kotlin
import com.example.carcollection.featureuser.components.LevelBadge
import com.example.carcollection.featureuser.components.BadgeSize

@Composable
fun MyScreen() {
    // Badge pequeño
    LevelBadge(
        level = 15,
        size = BadgeSize.SMALL
    )
    
    // Badge mediano (default)
    LevelBadge(level = 15)
    
    // Badge grande
    LevelBadge(
        level = 15,
        size = BadgeSize.LARGE,
        modifier = Modifier.padding(16.dp)
    )
}
```

### 2. Mostrar barra de progreso

```kotlin
import com.example.carcollection.featureuser.components.XPProgressBar

@Composable
fun MyScreen(user: User) {
    XPProgressBar(
        currentXP = user.currentLevelXP,
        neededXP = user.xpForNextLevel,
        level = user.level,
        showDetailedInfo = true  // Muestra nivel y XP restante
    )
}
```

### 3. Mostrar card completa de nivel

```kotlin
import com.example.carcollection.featureuser.components.LevelCard

@Composable
fun ProfileScreen(user: User) {
    LevelCard(
        level = user.level,
        totalXP = user.totalXP,
        currentLevelXP = user.currentLevelXP,
        xpForNextLevel = user.xpForNextLevel,
        xpFromCars = user.xpFromCars,
        xpFromAchievements = user.xpFromAchievements
    )
}
```

### 4. Otorgar XP manualmente (desde backend)

```kotlin
// En tu ViewModel o Repository
suspend fun grantBonusXP(amount: Int) {
    val userMethods = UserMethods()
    
    val result = userMethods.addXP(
        amount = amount,
        source = XPSource.CAR_ADDED,  // O el source que corresponda
        sourceId = null  // Opcional
    )
    
    if (result.isSuccess) {
        val updatedUser = result.getOrNull()
        println("User now at level ${updatedUser?.level}")
    }
}
```

### 5. Calcular nivel teórico sin guardar

```kotlin
import com.example.carcollection.featureuser.domain.User

// ¿Qué nivel tendría con X XP?
val theoreticalLevel = User.calculateLevelFromXP(5000)
println("Con 5000 XP estarías en nivel: $theoreticalLevel")

// ¿Cuánta XP necesito para nivel 20?
val xpNeeded = User.calculateTotalXPForLevel(20)
println("Para llegar a nivel 20 necesitas: $xpNeeded XP total")

// ¿Cuánta XP necesito para subir del nivel 15 al 16?
val xpForNextLevel = User.calculateXPForLevel(15)
println("Del nivel 15 al 16 necesitas: $xpForNextLevel XP")
```

### 6. Verificar si usuario necesita migración

```kotlin
// Al iniciar sesión o en Settings
val userMethods = UserMethods()

val needsMigration = userMethods.needsXPMigration().getOrNull() ?: false

if (needsMigration) {
    // Mostrar diálogo: "Migrar tu XP ahora?"
    val result = userMethods.migrateUserXP()
    if (result.isSuccess) {
        println("✅ XP migrada exitosamente")
    }
}
```

### 7. Obtener historial de XP

```kotlin
val userMethods = UserMethods()

val history = userMethods.getXPHistory(limit = 20).getOrNull()

history?.forEach { activity ->
    println("""
        ${activity.source}: +${activity.amount} XP
        De nivel ${activity.levelBefore} a ${activity.levelAfter}
        Timestamp: ${activity.timestamp}
    """.trimIndent())
}
```

---

## 🎨 EJEMPLOS DE UI PERSONALIZADOS

### Badge con efecto de brillo

```kotlin
@Composable
fun GlowingLevelBadge(level: Int) {
    Box(
        modifier = Modifier
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                ambientColor = getLevelColor(level),
                spotColor = getLevelColor(level)
            )
    ) {
        LevelBadge(
            level = level,
            size = BadgeSize.LARGE
        )
    }
}
```

### Barra de progreso vertical

```kotlin
@Composable
fun VerticalXPBar(user: User) {
    val progress = user.levelProgress
    
    Column(
        modifier = Modifier
            .width(40.dp)
            .height(200.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Spacer(modifier = Modifier.weight(1f - progress))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(progress)
                .background(getLevelColor(user.level))
        )
    }
}
```

### Card de nivel minimizada

```kotlin
@Composable
fun CompactLevelCard(user: User) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = getLevelColor(user.level).copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LevelBadge(level = user.level, size = BadgeSize.SMALL)
            
            Text(
                "Nivel ${user.level}",
                fontWeight = FontWeight.Bold
            )
            
            Text(
                "${user.currentLevelXP}/${user.xpForNextLevel} XP",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
```

---

## 📊 CONSULTAS DE FIRESTORE

### Obtener usuarios por nivel (requiere índice)

```kotlin
suspend fun getUsersByLevel(minLevel: Int): List<User> {
    val snapshot = FirebaseFirestore.getInstance()
        .collection("users")
        .whereGreaterThanOrEqualTo("level", minLevel)
        .orderBy("level", Query.Direction.DESCENDING)
        .limit(10)
        .get()
        .await()
    
    return snapshot.documents.mapNotNull { 
        it.toObject(User::class.java)?.copy(uid = it.id)
    }
}
```

### Obtener top 10 usuarios con más XP

```kotlin
suspend fun getTopXPUsers(): List<User> {
    val snapshot = FirebaseFirestore.getInstance()
        .collection("users")
        .orderBy("totalXP", Query.Direction.DESCENDING)
        .limit(10)
        .get()
        .await()
    
    return snapshot.documents.mapNotNull { 
        it.toObject(User::class.java)?.copy(uid = it.id)
    }
}
```

### Índices necesarios en Firestore

Para las consultas anteriores, crea estos índices compuestos:

```
Collection: users
Fields:
  - level (Descending)
  - totalXP (Descending)
```

---

## 🔔 NOTIFICACIONES (Futuro)

### Ejemplo de notificación al subir de nivel

```kotlin
@Composable
fun UserScreen(userViewModel: UserViewModel) {
    val levelUpEvent by userViewModel.levelUpEvent.collectAsState()
    
    LaunchedEffect(levelUpEvent) {
        levelUpEvent?.let { newLevel ->
            // Mostrar notificación/diálogo
            showLevelUpDialog(newLevel)
            
            // Limpiar el evento
            userViewModel.clearLevelUpEvent()
        }
    }
}

@Composable
fun showLevelUpDialog(level: Int) {
    AlertDialog(
        onDismissRequest = { /* */ },
        icon = { LevelBadge(level, size = BadgeSize.LARGE) },
        title = { Text("¡Subiste de nivel!") },
        text = { Text("Ahora eres nivel $level") },
        confirmButton = {
            Button(onClick = { /* */ }) {
                Text("¡Genial!")
            }
        }
    )
}
```

### Toast de ganancia de XP

```kotlin
@Composable
fun UserScreen(userViewModel: UserViewModel) {
    val xpGainEvent by userViewModel.xpGainEvent.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(xpGainEvent) {
        xpGainEvent?.let { (amount, source) ->
            snackbarHostState.showSnackbar(
                message = "+$amount XP",
                duration = SnackbarDuration.Short
            )
            userViewModel.clearXPGainEvent()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { /* ... */ }
}
```

---

## 🧪 TESTING MANUAL

### Escenario 1: Nuevo usuario
```
1. Registrar nuevo usuario
2. Verificar: level = 1, totalXP = 0
3. Agregar primer carro
4. Verificar: totalXP = 100, level = 1 (no sube aún)
5. Agregar segundo carro
6. Verificar: totalXP = 200, level = 2 ✅
```

### Escenario 2: Usuario existente
```
1. Usuario con 10 carros, 3 logros (antes del sistema)
2. Hacer login
3. Verificar migración automática:
   - xpFromCars = 1000 (10 × 100)
   - xpFromAchievements = 600 (3 × 200)
   - totalXP = 1600
   - level = 6 ✅
```

### Escenario 3: Subir muchos niveles
```
1. Usuario en nivel 1
2. Desbloquear 10 logros (2000 XP)
3. Verificar que sube varios niveles de golpe
4. UI muestra nivel correcto
```

---

## 🎯 BUENAS PRÁCTICAS

### ✅ DO:
- Usar `User.calculateLevelFromXP()` para recalcular nivel si es necesario
- Siempre usar `userMethods.addXP()` para otorgar XP (nunca modificar directo)
- Verificar `result.isSuccess` al otorgar XP
- Usar propiedades computadas (`currentLevelXP`, `xpForNextLevel`) en lugar de calcular manualmente

### ❌ DON'T:
- Nunca modificar `level` o `totalXP` directamente en el objeto User
- No hacer cálculos de nivel en UI (usa propiedades computadas)
- No asumir que otorgar XP siempre funciona (manejar errores)
- No crear múltiples instancias de UserMethods (usar la del ViewModel)

---

## 📈 ESTADÍSTICAS ÚTILES

### Calcular XP promedio por nivel

```kotlin
fun getAverageXPPerLevel(fromLevel: Int, toLevel: Int): Long {
    val totalXP = User.calculateTotalXPForLevel(toLevel) - 
                  User.calculateTotalXPForLevel(fromLevel)
    val levelDiff = toLevel - fromLevel
    return totalXP / levelDiff
}

// Ejemplo: Promedio entre nivel 1 y 10
val avg = getAverageXPPerLevel(1, 10)
println("Promedio XP por nivel (1-10): $avg")
```

### Estimar tiempo para alcanzar nivel

```kotlin
fun estimateDaysToLevel(
    currentLevel: Int,
    targetLevel: Int,
    xpPerDay: Int
): Int {
    val currentTotalXP = User.calculateTotalXPForLevel(currentLevel)
    val targetTotalXP = User.calculateTotalXPForLevel(targetLevel)
    val xpNeeded = targetTotalXP - currentTotalXP
    return (xpNeeded / xpPerDay).toInt()
}

// Ejemplo: Usuario nivel 5 quiere llegar a 20
// ganando 500 XP por día
val days = estimateDaysToLevel(5, 20, 500)
println("Te tomará aproximadamente $days días")
```

---

## 🎁 IDEAS PARA EL FUTURO

### Recompensas por nivel
```kotlin
data class LevelReward(
    val level: Int,
    val title: String,
    val description: String,
    val badge: String,
    val unlockFeature: String?
)

val LEVEL_REWARDS = listOf(
    LevelReward(5, "Coleccionista Novato", "Has comenzado tu viaje", "🌟", null),
    LevelReward(10, "Coleccionista Experimentado", "Dominas lo básico", "⭐", "custom_profile_url"),
    LevelReward(25, "Maestro Coleccionista", "Eres un experto", "💎", "featured_cars_x5"),
    LevelReward(50, "Leyenda", "Pocos llegan aquí", "👑", "exclusive_theme"),
)
```

### Sistema de racha diaria
```kotlin
// Bonus XP por días consecutivos
val dailyStreak = getUserStreak(userId)
val bonusMultiplier = when {
    dailyStreak >= 30 -> 2.0f
    dailyStreak >= 7 -> 1.5f
    dailyStreak >= 3 -> 1.25f
    else -> 1.0f
}
```

---

**Sistema completamente funcional y listo para usar! 🚀**

