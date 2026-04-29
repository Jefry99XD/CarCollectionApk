# ⏰ IMPLEMENTACIÓN: LOGROS TIME_BASED

## Descripción

Logros basados en **la velocidad de agregación de carros**, evaluando cuántos carros se agregaron en un rango de tiempo específico.

## Tres Rangos de Tiempo Disponibles

### 📅 DAY (24 horas)
```
Descripción: "Agrega X carros en las últimas 24 horas"
Rango: 24 * 60 * 60 * 1000 ms (86,400,000 ms)
Evaluación: Cuenta carros cuya fecha de creación es ≤ 24h
```

**Ejemplos:**
- "Agrega 5 carros en 1 día"
- "Agrega 10 carros en 24 horas"
- "Speed collector: Agrega 20 carros en un día"

### 📆 MONTH (30 días)
```
Descripción: "Agrega X carros en los últimos 30 días"
Rango: 30 * 24 * 60 * 60 * 1000 ms (2,592,000,000 ms)
Evaluación: Cuenta carros cuya fecha de creación es ≤ 30 días
```

**Ejemplos:**
- "Agrega 50 carros en un mes"
- "Coleccionista mensual: 30 carros en 30 días"
- "Mes productivo: Agrega 100 carros"

### 📊 YEAR (365 días)
```
Descripción: "Agrega X carros en los últimos 365 días"
Rango: 365 * 24 * 60 * 60 * 1000 ms (31,536,000,000 ms)
Evaluación: Cuenta carros cuya fecha de creación es ≤ 365 días
```

**Ejemplos:**
- "Agrega 200 carros en un año"
- "Año productivo: 500 carros"
- "Aniversario: Agrega 100 carros en tu primer año"

---

## Cómo Crear un Logro TIME_BASED

### En el Formulario

1. **Categoría**: Selecciona `TIME_BASED`
2. **Título**: Ej: "Speed Collector"
3. **Descripción**: Ej: "Agrega 10 carros en 24 horas"
4. **Meta (Goal)**: 10
5. **Rango de Tiempo**: 
   - 📅 Últimas 24 horas (1 día)
   - 📆 Últimos 30 días (1 mes)
   - 📊 Últimos 365 días (1 año)
6. Rareza, exclusivo, etc.

### Detalles Importantes

- ✅ **NO necesita condiciones** - Se evalúa solo por fecha
- ✅ **Usa timestamps reales** - `Car.createdAt` en milisegundos
- ✅ **Evaluación en tiempo real** - Usa `System.currentTimeMillis()` para comparar
- ✅ **Progreso visible** - Muestra cuántos carros se agregaron en el rango

---

## Ejemplo JSON

```json
{
  "id": "speed_collector_day",
  "title": "Speed Collector",
  "description": "Agrega 10 carros en 24 horas",
  "iconUrl": "https://...",
  "category": "TIME_BASED",
  "rarity": "RARO",
  "conditions": [],
  "goal": 10,
  "rules": {
    "timeWindow": "DAY",
    "conditionLogic": "AND",
    "uniquePerCar": true
  },
  "hidden": false,
  "active": true,
  "isExclusive": false,
  "exclusiveUserIds": [],
  "createdAt": 1703000000000
}
```

---

## Cálculo de Evaluación

### Proceso

```kotlin
val timeRangeMs = when (timeWindow) {
    DAY -> 24 * 60 * 60 * 1000L        // 86,400,000 ms
    MONTH -> 30 * 24 * 60 * 60 * 1000L // 2,592,000,000 ms
    YEAR -> 365 * 24 * 60 * 60 * 1000L // 31,536,000,000 ms
}

val currentTime = System.currentTimeMillis()

// Contar carros agregados en el rango
val carsInRange = cars.count { car ->
    val age = currentTime - car.createdAt
    age <= timeRangeMs
}

unlocked = carsInRange >= goal
progress = carsInRange
```

### Ejemplo Real

```
Hoy: 25/04/2026 15:30:00 (timestamp: 1765514853243 ms)

Carros del usuario:
1. Carro A - createdAt: 1765514753243 (hace 100 segundos) ✅
2. Carro B - createdAt: 1765514653243 (hace 200 segundos) ✅
3. Carro C - createdAt: 1765504853243 (hace ~2.7 horas) ✅
4. Carro D - createdAt: 1765414853243 (hace ~27.7 horas) ⚠️ (casi 28h)
5. Carro E - createdAt: 1764914853243 (hace 6.9 días) ❌

Para logro DAY (24h):
  - Carros válidos: A, B, C (3 carros)
  - Carro D no cuenta (27.7h > 24h)

Para logro MONTH (30 días):
  - Carros válidos: A, B, C, D, E (5 carros)
  
Para logro YEAR (365 días):
  - Carros válidos: A, B, C, D, E (5 carros)
```

---

## Cambios en el Código

### 1. AchievementGlobal.kt
- ✅ `TimeWindow` ahora incluye `YEAR`
- ✅ Documentación mejorada

### 2. AddAchievementForm.kt
- ✅ Muestra selector de `TimeWindow` solo para TIME_BASED
- ✅ Oculta condiciones para TIME_BASED
- ✅ Variable `rules` para manejar `timeWindow`
- ✅ Validación: Requiere seleccionar timeWindow para TIME_BASED

### 3. AchievementMethods.kt
- ✅ `evaluateTimeBasedAchievement()` reescrita
- ✅ Calcula rango en milisegundos
- ✅ Compara con timestamp actual
- ✅ Soporta DAY, MONTH, YEAR

---

## Casos de Uso

### 1. Daily Challenge
```
Logro: "Daily Hunter"
Meta: 5 carros en 24 horas
Reutilizable: Se reinicia cada día
```

### 2. Monthly Goal
```
Logro: "Monthly Collector"
Meta: 50 carros en 30 días
Reutilizable: Se reinicia cada mes
```

### 3. Yearly Achievement
```
Logro: "Annual Champion"
Meta: 500 carros en 365 días
Se desbloquea: Una vez al año
```

### 4. Anniversary
```
Logro: "First Year"
Meta: 100 carros en tu primer año
Se desbloquea: Una vez
```

---

## Cosas Importantes

⚠️ **Timestamp en milisegundos**: El campo `createdAt` debe estar en formato Unix (ms)

⚠️ **Evaluación en tiempo real**: Usa la hora actual del dispositivo, no del servidor

⚠️ **Sin condiciones**: TIME_BASED no evalúa qué carros se agregaron, solo cuántos

✅ **Progreso visible**: El usuario ve cuántos carros agregó en el rango

✅ **Re-evaluación automática**: Se verifica cada vez que se cargan los carros

---

## Ejemplo de Uso en Firebase

Para crear un logro TIME_BASED en Firestore directamente:

```json
{
  "id": "speed_collector",
  "title": "Speed Collector",
  "description": "Agrega 10 carros en 24 horas",
  "category": "TIME_BASED",
  "goal": 10,
  "rules": {
    "timeWindow": "DAY",
    "conditionLogic": "AND",
    "uniquePerCar": true
  },
  "rarity": "RARO",
  "active": true,
  "hidden": false
}
```

---

## Monitoreo de Progreso

El usuario verá:
- **Meta**: 10 carros
- **Progreso**: 7 carros (si agregó 7 en los últimos 24h)
- **Estado**: ❌ No desbloqueado (necesita 3 más)

Cuando alcance 10:
- **Estado**: ✅ Desbloqueado
- **Tiempo desbloqueado**: Timestamp guardado
- **XP otorgada**: Según rareza

---

## ¡Listo! 

Los logros TIME_BASED están completamente implementados y listos para usar.

