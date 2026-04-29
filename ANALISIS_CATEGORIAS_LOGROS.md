# 📊 ANÁLISIS EXHAUSTIVO: CATEGORÍAS DE LOGROS

> ⚠️ **NOTA**: Este documento fue creado antes de la simplificación.
> Las categorías BRAND, FANTASY, PREMIUM y MIXED **han sido eliminadas** por ser redundantes.
> Ver: `SIMPLIFICACION_CATEGORIAS_LOGROS.md`
> 
> Sistema actual: **Solo COLLECTION, TIME_BASED, USER**

## 🎯 Resumen Ejecutivo

Actualmente **solo 2 categorías funcionan correctamente**:
- ✅ **COLLECTION** - Funciona perfectamente
- ✅ **USER** - Funciona perfectamente

Las otras 5 categorías están **definidas pero no implementadas en la lógica de evaluación**:
- ❌ **BRAND** - No tiene lógica de evaluación propia
- ❌ **FANTASY** - No tiene lógica de evaluación propia
- ❌ **PREMIUM** - No tiene lógica de evaluación propia
- ❌ **TIME_BASED** - Parcialmente implementado (solo para carros, no específico)
- ❌ **MIXED** - No tiene lógica de evaluación propia

---

## 📋 CAMPOS DISPONIBLES EN CAR

Para evaluar logros, disponemos de estos campos:

```
Car {
  id: String              // ID del documento
  name: String           // Nombre del carro
  brand: String          // Marca (ej: Ferrari, Ford, Lamborghini)
  serie: String          // Serie/línea (ej: Testarossa, Mustang)
  year: String           // Año (ej: 1985, 2024)
  color: String          // Color (ej: Rojo, Negro, Azul)
  type: String           // Tipo (ej: Deportivo, SUV, Clásico)
  quality: String        // Calidad (ej: Básico, TH, STH, Super TH)
  tags: List<String>     // Etiquetas personalizadas del usuario
  createdAt: Long        // Timestamp de creación del carro en la app
  
  // Excluidas para logros:
  photoUrl              // URL de foto
  backgroundName/Url    // Fondo
}
```

---

## ✅ CATEGORÍA 1: COLLECTION (FUNCIONA)

### Descripción
Logros basados en **cantidad total de carros o carros específicos** de la colección.

### Implementación Actual
✅ **Completamente funcional en `AchievementMethods.kt`**

### Campos Soportados
- `NAME` - Buscar por nombre del carro
- `BRAND` - Buscar por marca
- `SERIE` - Buscar por serie
- `TYPE` - Buscar por tipo
- `QUALITY` - Buscar por calidad
- `COLOR` - Buscar por color
- `YEAR` - Buscar por año
- `TAGS` - Buscar en tags del usuario

### Tipos de Logros Posibles

#### 1.1 Cantidad Total (sin filtros)
```
ID: "total_500_cars"
Título: "Coleccionista Supremo"
Descripción: "Registra 500 carros"
Categoría: COLLECTION
Goal: 500
Condiciones: 1 condición con:
  - concept: "" (vacío)
  - matchFields: [NAME]
  - meta: 500
```
📊 **Evaluación**: Cuenta todos los carros

#### 1.2 Por Marca
```
ID: "ferrari_10"
Título: "Fanático de Ferrari"
Descripción: "Colecciona 10 Ferrari"
Categoría: COLLECTION
Goal: 10
Condiciones: 1 condición con:
  - concept: "ferrari"
  - matchFields: [BRAND]
  - matchType: CONTAINS
  - meta: 10
```
📊 **Evaluación**: Cuenta carros cuya marca contiene "ferrari"

#### 1.3 Por Calidad
```
ID: "treasure_hunt_15"
Título: "Cazador de Tesoros"
Descripción: "Colecciona 15 Treasure Hunt"
Categoría: COLLECTION
Goal: 15
Condiciones: 1 condición con:
  - concept: "th"
  - matchFields: [QUALITY]
  - matchType: EXACT
  - meta: 15
```
📊 **Evaluación**: Cuenta carros con quality = "TH" (EXACT)

#### 1.4 Por Año (Clásicos)
```
ID: "vintage_1970s"
Título: "Amante de Clásicos"
Descripción: "Colecciona 5 carros de los 70s"
Categoría: COLLECTION
Goal: 5
Condiciones: 1 condición con:
  - concept: "197"
  - matchFields: [YEAR]
  - matchType: STARTS_WITH
  - meta: 5
```
📊 **Evaluación**: Cuenta carros cuyo año comienza con "197"

#### 1.5 Lógica OR (Lista de Nombres)
```
ID: "movie_cars_collection"
Título: "Cars de Películas"
Descripción: "Colecciona al menos 1 de cada auto famoso"
Categoría: COLLECTION
Goal: 3
ConditionLogic: OR
Condiciones:
  1. concept: "delorean" | matchFields: [NAME, BRAND] | allowMultiple: false
  2. concept: "batmobile" | matchFields: [NAME] | allowMultiple: false
  3. concept: "aston martin db5" | matchFields: [NAME, BRAND] | allowMultiple: false
Meta: 3 (alcanzadas si tienes al menos 1 de cada concepto)
```
📊 **Evaluación**: Cuenta cuántos conceptos diferentes tienes (máx 1 por concepto)

#### 1.6 Lógica AND (Condiciones Compuestas)
```
ID: "red_ferrari"
Título: "Ferrari Rojo Puro"
Descripción: "Colecciona un Ferrari rojo"
Categoría: COLLECTION
Goal: 1
ConditionLogic: AND
Condiciones:
  1. concept: "ferrari" | matchFields: [BRAND] | matchType: CONTAINS
  2. concept: "rojo" | matchFields: [COLOR] | matchType: CONTAINS
Meta: 1 (cuyo BRAND contiene "ferrari" Y COLOR contiene "rojo")
```
📊 **Evaluación**: Cuenta carros que cumplen TODAS las condiciones

### Casos de Uso Actuales
- ✅ Coleccionar X cantidad de carros totales
- ✅ Coleccionar X cantidad de marca específica
- ✅ Coleccionar X cantidad con calidad específica
- ✅ Coleccionar X cantidad con color específico
- ✅ Coleccionar X cantidad de año específico
- ✅ Coleccionar X cantidad de tipo específico
- ✅ Coleccionar una lista de carros específicos
- ✅ Coleccionar carros que cumplan múltiples criterios

---

## ✅ CATEGORÍA 2: USER (FUNCIONA)

### Descripción
Logros basados en **nivel del usuario actual**.

### Implementación Actual
✅ **Completamente funcional en `AchievementMethods.kt`**

El sistema reconoce automáticamente logros con ID que comienzan con `level_`:
```kotlin
if (global.id.startsWith("level_")) {
    return evaluateLevelAchievement(global, previous, currentUser)
}
```

### Campos Únicos
- `goal` - Nivel que debe alcanzar el usuario (ej: 5, 10, 50, 100)
- **NO requiere condiciones** - Se evalúan automáticamente

### Tipos de Logros

#### 2.1 Logro por Nivel
```
ID: "level_10"
Título: "Principiante Confirmado"
Descripción: "Alcanza el nivel 10"
Categoría: USER
Goal: 10
Condiciones: [] (VACÍO - sin condiciones)
```
📊 **Evaluación**: Compara `currentUser.level >= 10`

#### 2.2 Hitos de Nivel
```
Crear 10 logros:
- level_5    → Alcanza nivel 5
- level_10   → Alcanza nivel 10
- level_20   → Alcanza nivel 20
- level_50   → Alcanza nivel 50
- level_100  → Alcanza nivel 100
- etc.
```

### Cálculo de XP/Nivel
```
XP Total = xpFromCars + xpFromAchievements

XP por carro:
  - Básico: 10 XP
  - TH: 50 XP
  - STH: 100 XP
  - Super TH: 200 XP

XP por logro (por rareza):
  - COMUN: 200 XP
  - RARO: 400 XP
  - LEGENDARIO: 800 XP
  - SPECIAL: 1200 XP

Cálculo de Nivel:
  level = floor(sqrt(totalXP / 100))
```

### Casos de Uso
- ✅ Alcanzar nivel específico
- ✅ Sistema completo de progresión
- ✅ Acumulación de XP desde carros y logros

---

## ❌ CATEGORÍA 3: BRAND (NO IMPLEMENTADA)

### Descripción
Logros específicos por **marca de vehículos**.

### Por qué no funciona
- No tiene lógica de evaluación propia en `AchievementMethods.kt`
- Se evalúa como `COLLECTION` genérico
- No hay diferencia con COLLECTION

### Propuesta de Implementación

Podría diferenciarse si agregamos:

#### Opción A: Estadísticas por Marca
```
Nuevos campos para AchievementGlobal:
- targetBrand: String     // Marca específica
- brandMinCount: Int      // Cantidad mínima de esa marca
- otherBrandMaxCount: Int // Máximo de otras marcas permitidas

Evaluación:
1. Contar carros de targetBrand
2. Si >= brandMinCount:
   - Verificar que total - targetBrand <= otherBrandMaxCount
   - Si cumple: desbloquea
```

Ejemplo:
```
ID: "ferrari_specialist"
Título: "Especialista en Ferrari"
Descripción: "Tienes 20+ Ferrari y menos de 5 otras marcas"
Categoría: BRAND
Goal: 20
targetBrand: "Ferrari"
brandMinCount: 20
otherBrandMaxCount: 5
```

#### Opción B: Dominancia de Marca
```
Nuevos campos:
- targetBrand: String
- percentageThreshold: Float (ej: 0.50 = 50%)

Evaluación:
- (carros de marca / total carros) >= percentageThreshold
```

Ejemplo:
```
ID: "ford_majority"
Título: "Mayoría Ford"
Descripción: "Más del 50% de tu colección son Ford"
Categoría: BRAND
Goal: 50 (sin usar, solo visual)
targetBrand: "Ford"
percentageThreshold: 0.50
```

#### Opción C: Colecciones Completas de Marca
```
Nuevos campos:
- targetBrand: String
- requiredSeries: List<String>

Evaluación:
- Tienes al menos 1 carro de cada serie requerida
```

Ejemplo:
```
ID: "lamborghini_complete"
Título: "Coleccionista Lamborghini"
Descripción: "Tienes Lamborghini de todas sus series principales"
Categoría: BRAND
targetBrand: "Lamborghini"
requiredSeries: ["Miura", "Countach", "Diablo", "Murciélago"]
```

### Campos Únicos Propuestos
```
data class AchievementGlobal(
  ...existing...
  // Para BRAND
  targetBrand: String? = null
  brandMinCount: Int? = null
  otherBrandMaxCount: Int? = null
  percentageThreshold: Float? = null
  requiredSeries: List<String>? = null
)
```

---

## ❌ CATEGORÍA 4: FANTASY (NO IMPLEMENTADA)

### Descripción
Logros para vehículos de **fantasía o no realistas** (ej: Batman, Toy Story, etc.).

### Por qué no funciona
- No hay campo específico en `Car` que marque esto
- Se podría usar `tags` pero no está formalizado
- No tiene lógica de evaluación propia

### Cómo identificar carros de fantasía actualmente

#### Opción 1: Usar TAGS
```
Tag: "Fantasy" o "Marvel" o "Disney" o "Movie"

Entonces la condición sería:
- concept: "fantasy"
- matchFields: [TAGS]
- matchType: CONTAINS
```

#### Opción 2: Agregar campo `isFantasy`
```
Nuevo campo en Car:
val isFantasy: Boolean = false

AchievementGlobal para FANTASY:
- evaluatFantasyAchievement()
- Cuenta carros con isFantasy = true
```

### Propuesta de Implementación

#### Opción A: Basado en Tags (Recomendado)
```
ID: "fantasy_collector"
Título: "Coleccionista de Fantasía"
Descripción: "Colecciona 10 carros de fantasía"
Categoría: FANTASY
Goal: 10
Condiciones: 1 condición con:
  - concept: "fantasy"
  - matchFields: [TAGS]
  - matchType: CONTAINS
```

**Requiere**: Que los usuarios agreguen tag "fantasy" a carros de fantasía

#### Opción B: Campo dedicado `isFantasy`
```
Agregar a Car:
val isFantasy: Boolean = false

AchievementGlobal:
val targetFantasyType: String? = null // "Marvel", "Disney", "Star Wars", etc.

Evaluación:
- Si targetFantasyType null: contar carros con isFantasy = true
- Si targetFantasyType != null: contar carros con isFantasy = true Y tags contiene targetFantasyType
```

Ejemplo:
```
ID: "marvel_heroes"
Título: "Héroes Marvel"
Descripción: "Colecciona 5 vehículos Marvel"
Categoría: FANTASY
Goal: 5
targetFantasyType: "Marvel"
```

### Campos Únicos Propuestos
```
// Para Car (si elegimos Opción B):
val isFantasy: Boolean = false

// Para AchievementGlobal:
val targetFantasyType: String? = null // "Marvel", "Disney", "Star Wars"
val fantasySourceField: CarMatchField? = TAGS // Dónde buscar
```

### Ejemplos de Logros
```
- fantasy_10: Colecciona 10 carros de fantasía
- marvel_5: Colecciona 5 carros Marvel
- disney_8: Colecciona 8 carros Disney
- star_wars_complete: Colecciona toda la colección Star Wars
- batman_vehicles: Colecciona todos los Batmóviles
```

---

## ❌ CATEGORÍA 5: PREMIUM (NO IMPLEMENTADA)

### Descripción
Logros basados en **calidad premium** de carros.

### Por qué no funciona
- Se puede usar con COLLECTION + quality field
- Pero podría tener lógica más específica

### Calidades Posibles en Sistema
```
Basado en Hot Wheels estándar:
- "Básico"  / "Basic"
- "TH"      / "Treasure Hunt"
- "STH"     / "Super Treasure Hunt"
- "Super TH"
- "Ultimate TH"
```

### Propuesta de Implementación

#### Opción A: Niveles de Rareza
```
Nuevos campos:
- minQualityLevel: Int (1=Basic, 2=TH, 3=STH, 4=Super TH, 5=Ultimate TH)

Evaluación:
- Contar carros con qualityLevel >= minQualityLevel
```

Ejemplo:
```
ID: "treasure_hunter_elite"
Título: "Cazador de Tesoros Élite"
Descripción: "Colecciona 20 Treasure Hunt o superior"
Categoría: PREMIUM
Goal: 20
minQualityLevel: 2 // TH o superior
```

#### Opción B: Distribución de Calidades
```
Nuevos campos:
- qualityDistribution: Map<String, Int>
  // "Basic": 10, "TH": 5, "STH": 2, "Super TH": 1

Evaluación:
- Verificar que tienes al menos los mínimos especificados de cada calidad
```

Ejemplo:
```
ID: "balanced_collector"
Título: "Coleccionista Equilibrado"
Descripción: "Tienes al menos 10 Básicos, 5 TH, 2 STH"
Categoría: PREMIUM
Goal: 17 (total)
qualityDistribution: {
  "Basic": 10,
  "TH": 5,
  "STH": 2
}
```

#### Opción C: Porcentaje de Premium
```
Nuevos campos:
- premiumQualities: List<String> // "TH", "STH", "Super TH"
- premiumPercentageMin: Float (ej: 0.25 = 25%)

Evaluación:
- (carros con calidad premium / total) >= premiumPercentageMin
```

Ejemplo:
```
ID: "premium_collector"
Título: "Coleccionista Premium"
Descripción: "Al menos 25% de tu colección es Premium o Superior"
Categoría: PREMIUM
Goal: 0 (sin usar)
premiumQualities: ["TH", "STH", "Super TH", "Ultimate TH"]
premiumPercentageMin: 0.25
```

### Campos Únicos Propuestos
```
data class AchievementGlobal(
  ...existing...
  // Para PREMIUM
  minQualityLevel: Int? = null  // 1-5
  qualityDistribution: Map<String, Int>? = null
  premiumQualities: List<String>? = null
  premiumPercentageMin: Float? = null
)
```

### Mapeo de Calidades
```
1 = "Basic" / "Basico"
2 = "TH" / "Treasure Hunt"
3 = "STH" / "Super Treasure Hunt"
4 = "Super TH" / "Ultimate TH"
5 = "Custom" / "Edición Especial"
```

---

## ❌ CATEGORÍA 6: TIME_BASED (PARCIALMENTE IMPLEMENTADA)

### Descripción
Logros basados en **tiempo**: día, mes, año, etc.

### Implementación Actual
⚠️ **Parcialmente implementado** - Existe código pero es genérico:

```kotlin
if (global.rules.timeWindow != null) {
    return evaluateTimeBasedAchievement(global, previous, cars)
}
```

Solo soporta:
- `TimeWindow.DAY` - Carros agregados en un día específico
- `TimeWindow.MONTH` - Carros agregados en un mes específico

### Por qué es limitado
- Solo evalúa carros agregados en cierta fecha
- No hay lógica para:
  - Racha de días
  - Actividad semanal
  - Meses específicos
  - Estaciones
  - Años fiscales

### Propuesta de Implementación Mejorada

#### Opción A: Actividad Diaria/Mensual Actual
```
ID: "daily_collector_nov_2026"
Título: "Coleccionista de Noviembre"
Descripción: "Agrega 5 carros en noviembre"
Categoría: TIME_BASED
Goal: 5
rules.timeWindow: MONTH
rules.timeKey: "2026-11" (Se genera automáticamente)

Evaluación:
- Contar carros con createdAt en el mes "2026-11"
```

**Estado**: ✅ Ya implementado

#### Opción B: Racha de Días Consecutivos
```
Nuevos campos:
- streakDays: Int           // Cantidad de días consecutivos
- minAdditionsPerDay: Int   // Mínimo de carros por día

Evaluación:
- Verificar últimos streakDays días
- Si en cada uno se agregó >= minAdditionsPerDay carros
```

Ejemplo:
```
ID: "daily_addict"
Título: "Adicto Diario"
Descripción: "Agrega al menos 1 carro cada día durante 7 días"
Categoría: TIME_BASED
Goal: 7
streakDays: 7
minAdditionsPerDay: 1
```

#### Opción C: Actividad por Día de la Semana
```
Nuevos campos:
- dayOfWeek: DayOfWeek[] // MONDAY, TUESDAY, etc.
- minAdditionsPerWeek: Int

Evaluación:
- Contar carros agregados en esos días específicos esta semana
```

Ejemplo:
```
ID: "weekend_collector"
Título: "Coleccionista de Fin de Semana"
Descripción: "Agrega 5 carros los sábados y domingos"
Categoría: TIME_BASED
Goal: 5
dayOfWeek: [SATURDAY, SUNDAY]
```

#### Opción D: Período Específico del Año
```
Nuevos campos:
- monthStart: Int    // 1-12
- monthEnd: Int      // 1-12
- minAdditions: Int

Evaluación:
- Contar carros agregados entre monthStart-monthEnd en cualquier año
```

Ejemplo:
```
ID: "summer_collector"
Título: "Coleccionista de Verano"
Descripción: "Agrega 10 carros en junio, julio o agosto"
Categoría: TIME_BASED
Goal: 10
monthStart: 6
monthEnd: 8
```

### Campos Únicos Propuestos
```
data class AchievementGlobal(
  ...existing...
  // Para TIME_BASED
  streakDays: Int? = null
  minAdditionsPerDay: Int? = null
  dayOfWeek: List<DayOfWeek>? = null
  minAdditionsPerWeek: Int? = null
  monthStart: Int? = null    // 1-12
  monthEnd: Int? = null      // 1-12
)

enum class DayOfWeek {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}
```

---

## ❌ CATEGORÍA 7: MIXED (NO IMPLEMENTADA)

### Descripción
Logros con **condiciones muy complejas** combinando múltiples criterios.

### Por qué no funciona
- La lógica AND/OR existente podría soportarlo
- Pero falta interfaz UI clara para definirlos
- Complejidad de evaluación

### Propuesta de Implementación

#### Ejemplo 1: Combinación de Marca + Calidad + Color
```
ID: "red_th_ferrari"
Título: "Ferrari Rojo Treasure Hunt Raro"
Descripción: "Tienes 1+ Ferrari rojo que es Treasure Hunt"
Categoría: MIXED
Goal: 1
ConditionLogic: AND
Condiciones:
  1. concept: "ferrari" | matchFields: [BRAND] | matchType: CONTAINS
  2. concept: "rojo" | matchFields: [COLOR] | matchType: CONTAINS
  3. concept: "th" | matchFields: [QUALITY] | matchType: EXACT
```

#### Ejemplo 2: Película + Marca + Año Específico
```
ID: "back_to_future_delorean"
Título: "Máquina del Tiempo DeLorean"
Descripción: "Tienes el DeLorean de Regreso al Futuro año 1981"
Categoría: MIXED
Goal: 1
ConditionLogic: AND
Condiciones:
  1. concept: "delorean" | matchFields: [BRAND, NAME] | matchType: CONTAINS
  2. concept: "back to future" | matchFields: [TAGS] | matchType: CONTAINS
  3. concept: "1981" | matchFields: [YEAR] | matchType: EXACT
```

#### Ejemplo 3: Colección Multicondición (OR complejo)
```
ID: "superhero_vehicles"
Título: "Vehículos de Superhéroes"
Descripción: "Tienes Batmóvil O Batavia O Wonder Woman jet"
Categoría: MIXED
Goal: 1 (obtenerlo si tienes al menos 1 de estos)
ConditionLogic: OR
Condiciones:
  1. concept: "batman" | matchFields: [TAGS] | allowMultiple: false
  2. concept: "wonder woman" | matchFields: [TAGS] | allowMultiple: false
  3. concept: "aquaman" | matchFields: [TAGS] | allowMultiple: false
```

#### Ejemplo 4: Cantidad Relativa (Avanzado)
```
ID: "quality_diversity"
Título: "Diversidad de Calidades"
Descripción: "Tienes al menos 1 Basic, 1 TH, 1 STH"
Categoría: MIXED
Goal: 1
ConditionLogic: AND
Condiciones:
  1. concept: "basic" | matchFields: [QUALITY] | matchType: EXACT | allowMultiple: false
  2. concept: "th" | matchFields: [QUALITY] | matchType: EXACT | allowMultiple: false
  3. concept: "sth" | matchFields: [QUALITY] | matchType: EXACT | allowMultiple: false
```

### Por qué ya debería funcionar
El código actual ya soporta:
- ✅ Lógica AND (todas las condiciones deben cumplirse)
- ✅ Lógica OR (al menos una condición)
- ✅ Múltiples campos de búsqueda por condición
- ✅ Diferentes tipos de match (EXACT, CONTAINS, STARTS_WITH)

**Conclusión**: MIXED ya funciona con la infraestructura actual, solo falta:
1. Mejorar la UI para expresar complejidad
2. Ejemplos claros en la documentación
3. Validaciones adicionales

---

## 📊 TABLA COMPARATIVA

| Categoría | Estado | Funcionamiento | Campos únicos | Complejidad | Recomendación |
|-----------|--------|-----------------|--------------|-------------|---------------|
| COLLECTION | ✅ | Total | Conditions | Baja | Usar ahora |
| USER | ✅ | Total | goal, level | Baja | Usar ahora |
| BRAND | ❌ | No tiene lógica | targetBrand, % | Media | Implementar Opción A o B |
| FANTASY | ❌ | Usa COLLECTION | isFantasy, type | Media | Implementar Opción B |
| PREMIUM | ❌ | Usa COLLECTION | qualityLevel, % | Media | Implementar Opción A o C |
| TIME_BASED | ⚠️ | Parcial | timeWindow, streak | Alta | Expandir Opción A |
| MIXED | ✅ | Total pero sin UI | Conditions complejas | Alta | Mejorar documentación |

---

## 🎯 PLAN DE IMPLEMENTACIÓN RECOMENDADO

### Fase 1: Consolidar lo Existente (1-2 días)
- [ ] Documentar COLLECTION y USER mejor
- [ ] Crear ejemplos de MIXED más claros
- [ ] Mejorar UI para evitar confusión

### Fase 2: Implementar BRAND (1-2 días)
- [ ] Agregar campos a `AchievementGlobal`
- [ ] Implementar evaluación en `AchievementMethods.kt`
- [ ] UI en `AddAchievementForm.kt`
- Recomendación: **Opción C (Colecciones completas por marca)**

### Fase 3: Implementar FANTASY (1 día)
- [ ] Opción B: Agregar campo `isFantasy` a `Car`
- [ ] Actualizar `CarForm` para flag isFantasy
- [ ] Implementar evaluación
- Recomendación: **Opción B (campo dedicado)**

### Fase 4: Implementar PREMIUM (1-2 días)
- [ ] Agregar mapeo de calidades
- [ ] Implementar evaluación
- [ ] UI en formulario
- Recomendación: **Opción C (Porcentaje de Premium)**

### Fase 5: Expandir TIME_BASED (1-2 días)
- [ ] Implementar racha de días
- [ ] Implementar actividad semanal
- Recomendación: **Opción B (Racha de días)**

---

## 💡 RECOMENDACIONES FINALES

1. **Mantén COLLECTION y USER como están** - Funcionan perfectamente

2. **BRAND**: Implementar "Colecciones Completas por Marca"
   - Permite logros como "Tienes 1+ de cada serie de Lamborghini"
   - Más interesante que solo cantidad

3. **FANTASY**: Agregar campo `isFantasy` al Car
   - Simple pero efectivo
   - Permite filtrar y crear logros específicos

4. **PREMIUM**: Usar "Porcentaje de Premium"
   - Más interesante que cantidad absoluta
   - Logro tipo: "25%+ de tu colección es Premium"

5. **TIME_BASED**: Implementar "Racha de Días"
   - Gamification fuerte
   - Logro tipo: "Agrega al menos 1 carro 7 días consecutivos"

6. **MIXED**: Mejorar documentación
   - Ya funciona, solo necesita ejemplos claros

---

## 📝 PRÓXIMOS PASOS

1. Crear `CategoryEvaluation.kt` con:
   ```kotlin
   sealed class CategoryEvaluator {
       object Collection : CategoryEvaluator
       object User : CategoryEvaluator
       object Brand : CategoryEvaluator
       object Fantasy : CategoryEvaluator
       object Premium : CategoryEvaluator
       object TimeBased : CategoryEvaluator
       object Mixed : CategoryEvaluator
   }
   ```

2. Refactorizar `evaluateSingleAchievement()` para delegar por categoría

3. Actualizar modelo `AchievementGlobal` progresivamente

4. Expandir `AddAchievementForm.kt` con campos específicos por categoría

