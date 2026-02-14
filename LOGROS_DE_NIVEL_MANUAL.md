# 📝 GUÍA: AGREGAR LOGROS DE NIVEL MANUALMENTE EN FIREBASE

## 🎯 LOGROS A CREAR

Debes crear **10 logros de nivel** en la colección `/achievements/` de Firestore.

---

## 📋 PLANTILLA PARA CADA LOGRO

### Campos comunes para TODOS los logros:
```
conditions: [] (array vacío)
rules: {
  conditionLogic: "AND"
  timeWindow: null
}
category: "COLLECTION"
active: true
createdAt: [Timestamp actual]
```

---

## 🎮 LOGROS A CREAR (COPIAR Y PEGAR)

### 1. Nivel 5 - Coleccionista Principiante

```
ID del documento: level_5

Campos:
- id: "level_5"
- title: "Coleccionista Principiante"
- description: "Alcanza el nivel 5"
- goal: 5
- iconUrl: "https://firebasestorage.googleapis.com/v0/b/hotwheels-47418.appspot.com/o/achievement_logos%2Flevel_5.png?alt=media"
- conditions: []
- rules: { conditionLogic: "AND", timeWindow: null }
- category: "COLLECTION"
- active: true
- createdAt: [Timestamp actual]
```

---

### 2. Nivel 10 - Coleccionista Experimentado

```
ID del documento: level_10

Campos:
- id: "level_10"
- title: "Coleccionista Experimentado"
- description: "Alcanza el nivel 10"
- goal: 10
- iconUrl: "https://firebasestorage.googleapis.com/v0/b/hotwheels-47418.appspot.com/o/achievement_logos%2Flevel_10.png?alt=media"
- conditions: []
- rules: { conditionLogic: "AND", timeWindow: null }
- category: "COLLECTION"
- active: true
- createdAt: [Timestamp actual]
```

---

### 3. Nivel 15 - Coleccionista Dedicado

```
ID del documento: level_15

Campos:
- id: "level_15"
- title: "Coleccionista Dedicado"
- description: "Alcanza el nivel 15"
- goal: 15
- iconUrl: "https://firebasestorage.googleapis.com/v0/b/hotwheels-47418.appspot.com/o/achievement_logos%2Flevel_15.png?alt=media"
- conditions: []
- rules: { conditionLogic: "AND", timeWindow: null }
- category: "COLLECTION"
- active: true
- createdAt: [Timestamp actual]
```

---

### 4. Nivel 20 - Coleccionista Avanzado

```
ID del documento: level_20

Campos:
- id: "level_20"
- title: "Coleccionista Avanzado"
- description: "Alcanza el nivel 20"
- goal: 20
- iconUrl: "https://firebasestorage.googleapis.com/v0/b/hotwheels-47418.appspot.com/o/achievement_logos%2Flevel_20.png?alt=media"
- conditions: []
- rules: { conditionLogic: "AND", timeWindow: null }
- category: "COLLECTION"
- active: true
- createdAt: [Timestamp actual]
```

---

### 5. Nivel 25 - Coleccionista Elite

```
ID del documento: level_25

Campos:
- id: "level_25"
- title: "Coleccionista Elite"
- description: "Alcanza el nivel 25"
- goal: 25
- iconUrl: "https://firebasestorage.googleapis.com/v0/b/hotwheels-47418.appspot.com/o/achievement_logos%2Flevel_25.png?alt=media"
- conditions: []
- rules: { conditionLogic: "AND", timeWindow: null }
- category: "COLLECTION"
- active: true
- createdAt: [Timestamp actual]
```

---

### 6. Nivel 30 - Coleccionista Experto

```
ID del documento: level_30

Campos:
- id: "level_30"
- title: "Coleccionista Experto"
- description: "Alcanza el nivel 30"
- goal: 30
- iconUrl: "https://firebasestorage.googleapis.com/v0/b/hotwheels-47418.appspot.com/o/achievement_logos%2Flevel_30.png?alt=media"
- conditions: []
- rules: { conditionLogic: "AND", timeWindow: null }
- category: "COLLECTION"
- active: true
- createdAt: [Timestamp actual]
```

---

### 7. Nivel 40 - Coleccionista Maestro

```
ID del documento: level_40

Campos:
- id: "level_40"
- title: "Coleccionista Maestro"
- description: "Alcanza el nivel 40"
- goal: 40
- iconUrl: "https://firebasestorage.googleapis.com/v0/b/hotwheels-47418.appspot.com/o/achievement_logos%2Flevel_40.png?alt=media"
- conditions: []
- rules: { conditionLogic: "AND", timeWindow: null }
- category: "COLLECTION"
- active: true
- createdAt: [Timestamp actual]
```

---

### 8. Nivel 50 - Maestro Coleccionista

```
ID del documento: level_50

Campos:
- id: "level_50"
- title: "Maestro Coleccionista"
- description: "Alcanza el nivel 50"
- goal: 50
- iconUrl: "https://firebasestorage.googleapis.com/v0/b/hotwheels-47418.appspot.com/o/achievement_logos%2Flevel_50.png?alt=media"
- conditions: []
- rules: { conditionLogic: "AND", timeWindow: null }
- category: "COLLECTION"
- active: true
- createdAt: [Timestamp actual]
```

---

### 9. Nivel 75 - Leyenda Viviente

```
ID del documento: level_75

Campos:
- id: "level_75"
- title: "Leyenda Viviente"
- description: "Alcanza el nivel 75"
- goal: 75
- iconUrl: "https://firebasestorage.googleapis.com/v0/b/hotwheels-47418.appspot.com/o/achievement_logos%2Flevel_75.png?alt=media"
- conditions: []
- rules: { conditionLogic: "AND", timeWindow: null }
- category: "COLLECTION"
- active: true
- createdAt: [Timestamp actual]
```

---

### 10. Nivel 100 - Leyenda Inmortal

```
ID del documento: level_100

Campos:
- id: "level_100"
- title: "Leyenda Inmortal"
- description: "Alcanza el nivel 100 - El pináculo de la colección"
- goal: 100
- iconUrl: "https://firebasestorage.googleapis.com/v0/b/hotwheels-47418.appspot.com/o/achievement_logos%2Flevel_100.png?alt=media"
- conditions: []
- rules: { conditionLogic: "AND", timeWindow: null }
- category: "COLLECTION"
- active: true
- createdAt: [Timestamp actual]
```

---

## 🔥 PASOS PARA AGREGAR EN FIREBASE CONSOLE

### 1. Acceder a Firestore
1. Ir a Firebase Console: https://console.firebase.google.com/
2. Seleccionar tu proyecto: `hotwheels-47418`
3. Ir a **Firestore Database**

### 2. Navegar a la colección de logros
1. Buscar la colección llamada `achievements`
2. Si no existe, crearla

### 3. Agregar cada logro
Para cada uno de los 10 logros:

1. **Clic en "Add document"** (Agregar documento)
2. **Document ID**: Usar el ID especificado (ej: `level_5`)
3. **Agregar campos uno por uno:**

#### Campos tipo String:
- `id` → type: **string** → valor: (ej: "level_5")
- `title` → type: **string** → valor: (ej: "Coleccionista Principiante")
- `description` → type: **string** → valor: (ej: "Alcanza el nivel 5")
- `iconUrl` → type: **string** → valor: (la URL completa)
- `category` → type: **string** → valor: "COLLECTION"

#### Campo numérico:
- `goal` → type: **number** → valor: (ej: 5)

#### Campo boolean:
- `active` → type: **boolean** → valor: `true`

#### Campo timestamp:
- `createdAt` → type: **timestamp** → valor: (fecha/hora actual)

#### Campo array vacío:
- `conditions` → type: **array** → valor: `[]` (vacío)

#### Campo map (objeto):
- `rules` → type: **map** → valor:
  - Agregar campo: `conditionLogic` → type: **string** → valor: "AND"
  - Agregar campo: `timeWindow` → type: **null** → valor: null

4. **Guardar el documento**

5. **Repetir para los otros 9 logros**

---

## 📊 RESUMEN DE CAMPOS

| Campo | Tipo | Valor Ejemplo |
|-------|------|---------------|
| id | string | "level_5" |
| title | string | "Coleccionista Principiante" |
| description | string | "Alcanza el nivel 5" |
| goal | number | 5 |
| iconUrl | string | "https://..." |
| conditions | array | [] (vacío) |
| rules | map | { conditionLogic: "AND", timeWindow: null } |
| category | string | "COLLECTION" |
| active | boolean | true |
| createdAt | timestamp | (actual) |

---

## ⚠️ PUNTOS IMPORTANTES

### 1. ID del documento
- **DEBE coincidir** con el campo `id` interno
- Formato: `level_X` donde X es el número del nivel
- Ejemplos: `level_5`, `level_10`, `level_100`

### 2. Campo goal
- Es un **número**, no string
- Representa el nivel que el usuario debe alcanzar
- Ejemplos: 5, 10, 15, 20, etc.

### 3. Conditions siempre vacío
- Los logros de nivel **NO usan conditions**
- Siempre dejar como array vacío: `[]`

### 4. Rules siempre igual
- `conditionLogic`: "AND"
- `timeWindow`: null

### 5. Category
- Siempre "COLLECTION"

### 6. Active
- Siempre `true` para que el logro esté activo

---

## 🎨 ICONOS

Las URLs de los iconos están configuradas en Firebase Storage:
```
https://firebasestorage.googleapis.com/v0/b/hotwheels-47418.appspot.com/o/achievement_logos%2F
```

### Lista de iconos necesarios:
- `level_5.png`
- `level_10.png`
- `level_15.png`
- `level_20.png`
- `level_25.png`
- `level_30.png`
- `level_40.png`
- `level_50.png`
- `level_75.png`
- `level_100.png`

**Nota:** Asegúrate de que estos archivos existan en Firebase Storage o usa URLs alternativas.

---

## ✅ VERIFICACIÓN

Después de agregar todos los logros:

1. **Verificar en Firestore Console:**
   - Debes ver 10 documentos en `/achievements/`
   - IDs: `level_5`, `level_10`, `level_15`, `level_20`, `level_25`, `level_30`, `level_40`, `level_50`, `level_75`, `level_100`

2. **Probar en la app:**
   - Abrir la app
   - Ir a pantalla de Logros
   - Debes ver los 10 logros de nivel listados
   - Los que ya alcanzaste deben estar desbloqueados
   - Los demás deben mostrar progreso (ej: 8/10)

3. **Subir de nivel:**
   - Agregar carros para ganar XP
   - Al alcanzar un nivel de logro, debe desbloquearse automáticamente
   - Debe aparecer notificación
   - Debes recibir +200 XP adicionales

---

## 🎉 RESUMEN

**Total de logros a crear:** 10
**Tiempo estimado:** 15-20 minutos
**Colección:** `/achievements/`
**Formato ID:** `level_X`

Una vez creados, el sistema detectará automáticamente estos logros y los evaluará cada vez que el usuario suba de nivel.

**¡Éxito!** 🚀

