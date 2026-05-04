# 📝 GUÍA: AGREGAR LOGROS EN BATCH A FIREBASE

## 🎯 Objetivo

Agregar múltiples logros a Firebase de forma automatizada sin necesidad de crearlos manualmente en la app.

---

## 📋 ARCHIVOS NECESARIOS

### 1. **add_batch_achievements.py** ✅
Script principal que:
- Lee un JSON con los logros a crear
- Valida cada logro antes de insertar
- Ejecuta DRY RUN para verificar antes de insertar
- Crea los logros en Firebase
- Genera reporte de éxito/errores

### 2. **achievements_to_create.json** (Crear este archivo)
Archivo JSON con los logros a agregar.

**Ubicación:** `C:\Users\jeffr\Documents\GitHub\CarCollectionApk\achievements_to_create.json`

**Formato:**
```json
{
  "achievements": [
    {
      "id": "id_logro",
      "title": "Título del Logro",
      "description": "Descripción",
      "category": "COLLECTION",
      "rarity": "COMUN",
      "goal": 3,
      "conditions": [...],
      "rules": {...},
      "iconUrl": "https://..."
    }
  ]
}
```

### 3. **achievements_example.json** ✅
Ejemplo con 10 logros (para referencia y testing)

---

## 🚀 CÓMO USAR

### Paso 1: Crear archivo JSON con logros

**Opción A: Usar el ejemplo como base**
```bash
# Copiar el ejemplo
copy achievements_example.json achievements_to_create.json
```

**Opción B: Crear desde cero**

Crea `achievements_to_create.json` con el contenido deseado.

### Paso 2: Ejecutar el script

```bash
# Ir al directorio del proyecto
cd C:\Users\jeffr\Documents\GitHub\CarCollectionApk

# Ejecutar el script
python add_batch_achievements.py
```

### Paso 3: Seguir las instrucciones

El script te preguntará:

1. **¿Ejecutar DRY RUN primero?** → Escribe `s` (recomendado)
   - Valida todos los logros sin insertar en Firebase
   - Te muestra qué se va a crear
   - Detecta errores antes de insertar

2. **¿Continuar con la inserción real?** → Escribe `s` para proceder
   - Inserta los logros en Firebase
   - Muestra progreso en tiempo real
   - Genera reporte final

---

## 📚 ESTRUCTURA DE UN LOGRO JSON

### Campos Requeridos

| Campo | Tipo | Descripción | Ejemplo |
|-------|------|-------------|---------|
| `id` | String | ID único del logro (sin espacios) | `"ferrarista"` |
| `title` | String | Nombre mostrado | `"Ferrarista"` |
| `description` | String | Descripción del logro | `"Obtener 5 carros Ferrari"` |
| `category` | String | Tipo de logro | `"COLLECTION"`, `"TIME_BASED"`, `"USER"` |
| `goal` | Integer | Meta a alcanzar | `5` |

### Campos Opcionales (con valores por defecto)

| Campo | Tipo | Default | Descripción |
|-------|------|---------|-------------|
| `conditions` | Array | `[]` | Condiciones a cumplir |
| `rarity` | String | `"COMUN"` | Rareza (COMUN, RARO, LEGENDARIO, SPECIAL) |
| `rules` | Object | `{...}` | Reglas de evaluación |
| `hidden` | Boolean | `false` | ¿Está oculto? |
| `active` | Boolean | `true` | ¿Está activo? |
| `iconUrl` | String | Default | URL del icono |
| `isExclusive` | Boolean | `false` | ¿Es exclusivo? |
| `exclusiveUserIds` | Array | `[]` | IDs de usuarios para logros exclusivos |

---

## 🎯 EJEMPLOS DE LOGROS

### Ejemplo 1: Logro Simple (Colección por Marca)

```json
{
  "id": "hondero",
  "title": "Hondero",
  "description": "Obtener 5 carros Honda",
  "category": "COLLECTION",
  "rarity": "RARO",
  "goal": 5,
  "conditions": [
    {
      "concept": "honda",
      "aliases": [],
      "matchFields": ["BRAND"],
      "matchType": "CONTAINS",
      "allowMultiplePerConcept": false
    }
  ]
}
```

### Ejemplo 2: Logro por Color

```json
{
  "id": "negro_elegante",
  "title": "Negro Elegante",
  "description": "Obtener 4 carros negros",
  "category": "COLLECTION",
  "rarity": "COMUN",
  "goal": 4,
  "conditions": [
    {
      "concept": "negro",
      "aliases": ["black"],
      "matchFields": ["COLOR"],
      "matchType": "CONTAINS",
      "allowMultiplePerConcept": true
    }
  ]
}
```

### Ejemplo 3: Logro Time-Based

```json
{
  "id": "midnight_racer",
  "title": "Midnight Racer",
  "description": "Agregar 3 carros en 24 horas",
  "category": "TIME_BASED",
  "rarity": "RARO",
  "goal": 3,
  "conditions": [],
  "rules": {
    "conditionLogic": "AND",
    "timeWindow": "DAY",
    "uniquePerCar": true
  }
}
```

### Ejemplo 4: Logro con Múltiples Condiciones (OR)

```json
{
  "id": "movie_legend",
  "title": "Movie Legend",
  "description": "Poseer autos de películas famosas",
  "category": "COLLECTION",
  "rarity": "RARO",
  "goal": 3,
  "conditions": [
    {
      "concept": "fast and furious",
      "aliases": ["rápido y furioso"],
      "matchFields": ["NAME", "TAGS"],
      "matchType": "CONTAINS",
      "allowMultiplePerConcept": true
    },
    {
      "concept": "james bond",
      "aliases": ["bond"],
      "matchFields": ["NAME", "TAGS"],
      "matchType": "CONTAINS",
      "allowMultiplePerConcept": true
    }
  ],
  "rules": {
    "conditionLogic": "OR"
  }
}
```

---

## 🎲 CAMPOS DE CONDICIÓN

### Parámetros de `conditions`

```json
{
  "concept": "ferrari",          // Concepto principal a buscar
  "aliases": ["prancing horse"], // Sinónimos aceptados
  "matchFields": ["BRAND"],      // Dónde buscar: NAME, BRAND, SERIE, COLOR, TYPE, QUALITY, YEAR, TAGS
  "matchType": "CONTAINS",       // CONTAINS, STARTS_WITH, EXACT
  "allowMultiplePerConcept": false  // ¿Permite múltiples carros por concepto?
}
```

### Tipos de Match

- **CONTAINS**: "ferrari" matchea "Ferrari Testarossa"
- **STARTS_WITH**: "fer" matchea "ferrari" pero no "superferry"
- **EXACT**: "ferrari" solo matchea exactamente "ferrari"

### allowMultiplePerConcept

- **true**: Permite contar varios carros del mismo concepto (ej: 3 Ferraris cuentan como 3)
- **false**: Solo cuenta 1 carro por concepto (ej: 3 Ferraris cuentan como 1)

---

## 📊 CATEGORÍAS DISPONIBLES

| Categoría | Descripción | Uso |
|-----------|-------------|-----|
| **COLLECTION** | Basado en condiciones de carros | Coleccionar marcas, colores, tipos |
| **TIME_BASED** | Basado en fecha de adición | Agregar X carros en 24h/30d/365d |
| **USER** | Basado en nivel del usuario | Requiere evaluación especial |
| **SPECIAL** | Car of the Day | Requiere lógica especial |

---

## 💎 RAREZAS Y XP

| Rareza | XP | Rareza Relativa |
|--------|----|----|
| COMUN | 200 | ⭐ |
| RARO | 400 | ⭐⭐ |
| LEGENDARIO | 800 | ⭐⭐⭐ |
| SPECIAL | 1200 | ⭐⭐⭐⭐ |

---

## ✅ CHECKLIST ANTES DE EJECUTAR

- [ ] `keys.json` existe en la raíz del proyecto
- [ ] `achievements_to_create.json` está creado y válido
- [ ] Todos los `id` son únicos (sin duplicados)
- [ ] Los `id` solo contienen letras y guiones bajos (no espacios)
- [ ] `goal` es un número entero > 0
- [ ] Las `conditions` están bien formadas
- [ ] Se ejecutó DRY RUN primero
- [ ] Se revisó el reporte de DRY RUN

---

## 🐛 SOLUCIÓN DE PROBLEMAS

### Error: "No se encontró achievements_to_create.json"

**Solución:** Crea el archivo JSON en la raíz del proyecto

```bash
copy achievements_example.json achievements_to_create.json
```

### Error: "IDs duplicados encontrados"

**Solución:** Revisa que no hay dos logros con el mismo `id`

### Error: "Falta campo requerido 'goal'"

**Solución:** Asegúrate que todos los logros tengan los campos: `id`, `title`, `description`, `category`, `goal`

### Firebase no inserta nada

**Solución:** 
1. Verifica que `keys.json` esté en la raíz
2. Verifica que tu cuenta de Firebase tiene permisos de escritura
3. Ejecuta primero el DRY RUN para detectar problemas

---

## 📈 REPORTE POST-INSERCIÓN

El script genera un reporte como este:

```
============================================================
📊 REPORTE FINAL
============================================================
✅ Creados exitosamente: 10
❌ Errores: 0
📈 Tasa de éxito: 100.0%

============================================================
🎉 ¡Operación completada!
============================================================
```

---

## 🎯 SIGUIENTES PASOS

1. **Crear `achievements_to_create.json`** con los logros que deseas agregar
2. **Ejecutar el script** en DRY RUN
3. **Revisar el reporte** de validación
4. **Ejecutar la inserción real** si todo está bien
5. **Verificar en la app** que los logros aparecen correctamente

---

## 📞 SOPORTE

Si hay problemas:
1. Revisa los logs del script
2. Verifica que el JSON es válido (usa un validador JSON online)
3. Ejecuta el DRY RUN para detectar problemas
4. Revisa la estructura con `achievements_example.json`


