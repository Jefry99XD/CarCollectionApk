# 🎨 MIGRACIÓN DE FONDOS - DOCUMENTACIÓN

## 📋 OBJETIVO

Migrar automáticamente todos los carros existentes del usuario de usar **nombres de fondos** (campo `backgroundName`) a usar **URLs de fondos** (campo `backgroundUrl`), sin que el usuario tenga que reseleccionar cada fondo nuevamente.

---

## 🔄 FLUJO DE MIGRACIÓN

### 1. **Cuándo se ejecuta**
La migración se ejecuta automáticamente una única vez cuando el usuario inicia sesión:

```
Login → UserViewModel.fetchUserProfile() 
      → checkAndMigrateXP() 
      → migrateCarBackgrounds() ✅ AQUÍ
```

### 2. **Proceso**
```
CarMethods.migrateBackgrounds():
├─ Obtiene todos los carros del usuario
├─ Por cada carro con backgroundName:
│  ├─ Busca el nombre en el mapa de correspondencia
│  ├─ Obtiene el ID del JSON
│  ├─ Busca el URL correspondiente en idToUrlMapping
│  └─ Actualiza el documento con el URL
└─ Invalida caché y retorna conteo
```

### 3. **Mapeo de Datos**

#### backgroundMapping (Nombres → IDs JSON):
```kotlin
"Fondo 1" → "fondo_1"
"Fondo 2" → "fondo_9"
"Fondo 3" → "fondo_14"
... etc
```

#### idToUrlMapping (IDs → URLs):
```kotlin
"fondo_1" → "https://raw.githubusercontent.com/polarismkr/.../fondos/01.png"
"fondo_2" → "https://raw.githubusercontent.com/polarismkr/.../fondos/02.png"
... etc
```

---

## 📂 ARCHIVOS MODIFICADOS

### 1. **CarMethods.kt**
- **Nueva función**: `migrateBackgrounds()`
- **Ubicación**: Línea ~545 (al final de la clase)
- **Responsabilidad**: Ejecutar la migración en Firestore

```kotlin
suspend fun migrateBackgrounds(): Result<String>
```

**Características:**
- ✅ Obtiene todos los carros del usuario
- ✅ Itera sobre cada carro con `backgroundName`
- ✅ Mapea nombre → ID → URL
- ✅ Usa batch update para eficiencia
- ✅ Limpia el campo `backgroundName` después
- ✅ Invalida caché
- ✅ Retorna conteo de carros migrados

### 2. **UserViewModel.kt**
- **Función modificada**: `checkAndMigrateXP()`
- **Nueva función**: `migrateCarBackgrounds()`
- **Ubicación**: Línea ~443
- **Responsabilidad**: Orquestar llamada a migración

```kotlin
// Se ejecuta automáticamente después de XP migration
private fun migrateCarBackgrounds()
```

**Características:**
- ✅ Crea instancia de CarMethods
- ✅ Llama a `migrateBackgrounds()`
- ✅ Log de éxito/error con SecureLogger
- ✅ Manejo de excepciones

---

## 🔍 MAPEO DE NOMBRES A URLs

| Nombre Original | ID JSON | URL |
|---|---|---|
| Fondo 1 | fondo_1 | `.../fondos/01.png` |
| Fondo 2 | fondo_9 | `.../fondos/fondo2.png` |
| Fondo 3 | fondo_14 | `.../fondos/fondo3.jpg` |
| Fondo 4 | fondo_15 | `.../fondos/fondo4.jpg` |
| Fondo 5 | fondo_16 | `.../fondos/fondo5.jpg` |
| Fondo 6 | fondo_17 | `.../fondos/fondo6.jpg` |
| Fondo 7 | fondo_18 | `.../fondos/fondo7.png` |
| Fondo 8 | fondo_19 | `.../fondos/fondo8.png` |
| Fondo 10 | fondo_7 | `.../fondos/fondo10.png` |
| Fondo 15 | fondo_8 | `.../fondos/fondo15.jpeg` |
| Fondo 20 | fondo_10 | `.../fondos/fondo20.jpeg` |
| Fondo 23 | fondo_11 | `.../fondos/fondo23.jpg` |
| Fondo 24 | fondo_12 | `.../fondos/fondo24.jpeg` |
| Fondo 26 | fondo_13 | `.../fondos/fondo26.jpg` |
| Fondo F2 | fondo_5 | `.../fondos/f2.jpg` |
| Fondo | fondo_6 | `.../fondos/fondo.jpg` |

---

## 📊 ESTRUCTURA EN FIRESTORE

### Antes (backgroundName):
```json
{
  "id": "car_123",
  "name": "Ferrari F40",
  "backgroundName": "Fondo 3",
  "photoUrl": "..."
}
```

### Después (backgroundUrl):
```json
{
  "id": "car_123",
  "name": "Ferrari F40",
  "backgroundUrl": "https://raw.githubusercontent.com/.../fondo3.jpg",
  "backgroundName": null,
  "photoUrl": "..."
}
```

---

## 🔐 CARACTERÍSTICAS DE SEGURIDAD

1. **Una sola ejecución**: Se ejecuta solo en `fetchUserProfile()` (primera vez al login)
2. **Batch Update**: Todos los cambios se aplican atómicamente
3. **Error Handling**: Si falla, se loguea pero no rompe la app
4. **Caché Invalidation**: Se limpia caché después de actualización
5. **Logs**: SecureLogger registra éxito/fallo sin datos sensibles

---

## 🐛 CONSIDERACIONES ESPECIALES

### ¿Qué pasa si un usuario no tiene fondos?
- Se retorna `"No cars needed migration"`
- No hay impacto, todo funciona normalmente

### ¿Qué pasa si un nombre no está en el mapa?
- Se asigna URL vacío (`""`)
- El fondo simplemente no se muestra (fallback a blanco)

### ¿Se puede ejecutar manualmente?
- Sí, directamente: `carMethods.migrateBackgrounds()`
- Se hará en futuro si es necesario un "re-migrar"

### ¿Afecta performance?
- No significativamente
- Usa batch update (1 operación por lote)
- Se ejecuta en background thread (viewModelScope)

---

## 📝 LOGS ESPERADOS

### Éxito con carros migrados:
```
✅ Migrated 15 cars from backgroundName to backgroundUrl
Background migration completed: Migrated 15 cars successfully
```

### Éxito sin migración necesaria:
```
ℹ️ No cars needed migration (backgroundName field is empty)
Background migration completed: No cars needed migration
```

### Error:
```
⚠️ Failed to migrate backgrounds: Network error
Background migration: Background migration failed: [error details]
```

---

## 🔧 CÓMO PROBAR

### 1. En emulador con usuario nuevo:
```
1. Crear usuario nuevo
2. Agregar carros con fondos antiguos
3. Cerrar sesión
4. Iniciar sesión nuevamente
5. Revisar Firestore → los carros ahora tienen backgroundUrl
```

### 2. En código:
```kotlin
// Desde ConsoleScreen o admin panel
val carMethods = CarMethods()
val result = carMethods.migrateBackgrounds()
println(result.getOrNull())  // "Migrated X cars successfully"
```

---

## ✅ CHECKLIST POST-IMPLEMENTACIÓN

- [x] Función `migrateBackgrounds()` agregada a CarMethods.kt
- [x] Llamada a migración en UserViewModel.kt en `migrateCarBackgrounds()`
- [x] Mapa de correspondencia nombres → IDs correcto
- [x] Mapa de correspondencia IDs → URLs correcto
- [x] Error handling implementado
- [x] Logs de SecureLogger configurados
- [x] Batch update implementado
- [x] Caché invalidation incluido
- [x] Documentación completada

---

## 🚀 PRÓXIMAS ACCIONES

1. Compilar el proyecto para validar
2. Testear en emulador con usuario con carros antiguos
3. Verificar Firestore para confirmar migración
4. Hacer commit de cambios

---

**Creado**: Marzo 28, 2026  
**Versión**: 1.0  
**Estado**: ✅ Implementado

