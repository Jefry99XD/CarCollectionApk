# 🧪 GUÍA DE TESTING - LOGROS EXCLUSIVOS Y TIME_BASED

## 📋 CASOS DE PRUEBA

### 1. CREAR LOGRO EXCLUSIVO (PRIVADO)

**Pasos:**
1. Ir a "Administrar Logros" (Admin)
2. Click en "Agregar logro"
3. Categoría: **COLLECTION**
4. Título: `Copa de Ganador`
5. Meta: `1` (puede ser cualquier número)
6. Agregar una condición (ej: Concepto = "", cualquier carro)
7. **Scroll hasta abajo → Activar "Logro Exclusivo"**
8. Click en "Seleccionar usuarios"
9. Buscar y seleccionar 1 o más usuarios
10. Click en "Guardar logro"

**Resultado esperado:**
- El logro se crea con `isExclusive = true`
- Solo los usuarios seleccionados ven/reciben este logro
- El logro se desbloquea inmediatamente para ellos

---

### 2. CREAR LOGRO TIME_BASED (TIEMPO)

**Pasos:**
1. Ir a "Administrar Logros"
2. Click en "Agregar logro"
3. Categoría: **TIME_BASED**
4. Título: `Coleccionista del Mes`
5. Meta: `5` (ejemplo: 5 carros en 30 días)
6. **Automáticamente aparece:** "⏰ Rango de Tiempo"
7. Seleccionar: "📆 Últimos 30 días (1 mes)"
8. Click en "Guardar logro"

**Resultado esperado:**
- El logro se evalúa automáticamente
- Sistema cuenta carros agregados en los últimos 30 días
- Si usuario agregó 5+ carros en ese período → **Desbloqueado**
- Si agregó menos → Progreso parcial

**Importante:** Los carros deben tener `createdAt` con timestamp válido

---

### 3. CREAR LOGRO COMBINADO (EXCLUSIVO + TIME_BASED)

**Pasos:**
1. Categoría: **TIME_BASED**
2. Activar "Logro Exclusivo"
3. Seleccionar usuarios ganadores del torneo
4. Rango de tiempo: "Últimas 24 horas"
5. Meta: `3`
6. Guardar

**Caso de uso:**
- "Ganador de Torneo Flash" - solo para ganadores, 3 carros en 24h

---

### 4. VALIDACIONES QUE DEBEN FUNCIONAR

#### ✅ Error: No hay usuarios en logro exclusivo
```
Activar "Logro Exclusivo" sin seleccionar usuarios
→ Error: "Agrega al menos un usuario para un logro exclusivo"
```

#### ✅ Error: No hay rango de tiempo en TIME_BASED
```
Categoría TIME_BASED sin seleccionar rango
→ Error: "Selecciona un rango de tiempo (24h, 30 días o 365 días)"
```

#### ✅ Error: No hay condiciones en COLLECTION
```
Categoría COLLECTION sin agregar condiciones
→ Error: "Agrega al menos una condición"
```

---

## 🔧 PRUEBAS DEL DROPDOWN DE USUARIOS

### Test 1: Búsqueda en vivo
1. Click "Seleccionar usuarios"
2. Escribir nombre o ID en el campo de búsqueda
3. Los resultados se filtran en tiempo real

**Esperado:** No debe crashear la app

### Test 2: Selección múltiple
1. Click en un usuario → se marca con ✓
2. Click en otro usuario → se marca con ✓
3. Ambos aparecen en la lista superior

**Esperado:** Múltiples usuarios seleccionables

### Test 3: Deseleccionar
1. Click en usuario seleccionado → ✓ desaparece
2. Click en X del usuario en la lista superior → se elimina

**Esperado:** Dos formas de deseleccionar

### Test 4: Scroll en lista
1. Agregar 10+ usuarios a la selección
2. Hacer scroll en la lista
3. Buscar usuarios desde el dropdown

**Esperado:** Todo funciona sin crashes

---

## 🎯 VERIFICACIÓN FIREBASE

### Estructura esperada para Logro Exclusivo:
```json
{
  "id": "copa_de_ganador",
  "title": "Copa de Ganador",
  "category": "COLLECTION",
  "isExclusive": true,
  "exclusiveUserIds": ["uid1", "uid2", "uid3"],
  "conditions": [...],
  "goal": 1,
  "active": true,
  "hidden": false
}
```

### Estructura esperada para TIME_BASED:
```json
{
  "id": "coleccionista_del_mes",
  "title": "Coleccionista del Mes",
  "category": "TIME_BASED",
  "goal": 5,
  "rules": {
    "timeWindow": "MONTH",
    "conditionLogic": "AND"
  },
  "conditions": [],
  "isExclusive": false,
  "exclusiveUserIds": [],
  "active": true
}
```

---

## ⚠️ PROBLEMAS CONOCIDOS Y SOLUCIONES

### Problema: Dropdown crashea
**Antes:** Usaba `DropdownMenu` + `LazyColumn` (causes intrinsic measurements error)
**Ahora:** Usa `Card` personalizado + `Column` + `verticalScroll` ✅

### Problema: TIME_BASED no mostraba opciones
**Antes:** Estaba dentro de `else if (conditions.isEmpty())`
**Ahora:** Siempre se muestra si `category == TIME_BASED` ✅

---

## 📱 FLUJO DE USUARIO COMPLETO

```
Usuario Admin
    ↓
Agregar Logro
    ↓
Seleccionar Categoría (COLLECTION/TIME_BASED/USER)
    ↓
Llenar datos básicos (título, meta, rareza)
    ↓
Si COLLECTION → Agregar condiciones
Si TIME_BASED → Seleccionar rango de tiempo
Si USER → Especificar nivel
    ↓
¿Logro exclusivo? 
   Sí → Seleccionar usuarios (multiselect dropdown)
   No → Continuar
    ↓
Guardar
    ↓
Firebase ← Logro creado
    ↓
Evaluación automática en próxima sincronización
    ↓
Usuarios desbloqueados automáticamente (si exclusivo)
```


