# 📊 GUÍA DE LOGS PARA RENDIMIENTO - ANDROID

## 🎯 FILTROS IMPORTANTES EN LOGCAT

### 1. **Frames Skipped (Prioridad Alta)**
```
Filtro: tag:Choreographer
Buscar: "Skipped.*frames"
```

**Ejemplo:**
```
I/Choreographer: Skipped 135 frames! The application may be doing too much work on its main thread.
```

**Acción:**
- ✅ Si ves esto, identifica qué operación está bloqueando el hilo principal
- ✅ Mueve operaciones pesadas a `Dispatchers.IO` o `Dispatchers.Default`
- ✅ Implementa debouncing si es necesario

---

### 2. **Garbage Collector Frecuente**
```
Filtro: tag:dalvikvm OR tag:art
Buscar: "GC_FOR_ALLOC"
```

**Ejemplo:**
```
D/dalvikvm: GC_FOR_ALLOC freed 2048K, 45% free 15360K/28160K
```

**Señales de alarma:**
- ❌ GC se ejecuta cada pocos segundos
- ❌ "freed" menos de 10% del heap
- ❌ Heap usage > 80%

**Acción:**
- ✅ Revisa fugas de memoria
- ✅ Reduce creación de objetos temporales
- ✅ Usa caché cuando sea posible

---

### 3. **ANR (Application Not Responding)**
```
Filtro: tag:ActivityManager
Buscar: "ANR"
```

**Ejemplo:**
```
E/ActivityManager: ANR in com.example.carcollection (com.example.carcollection/.MainActivity)
PID: 12345
Reason: Input dispatching timed out (Waiting to send key event because the focused window has not finished processing all of the input events that were previously delivered to it)
```

**Acción:**
- 🔴 Crítico: la app está bloqueada > 5 segundos
- ✅ Revisa operaciones síncronas en UI thread
- ✅ Usa `viewModelScope.launch` para operaciones largas

---

### 4. **StrictMode Violations**
```
Filtro: tag:StrictMode
Buscar: "violation"
```

**Ejemplo:**
```
D/StrictMode: StrictMode policy violation; ~duration=152 ms: android.os.strictmode.DiskReadViolation
```

**Tipos comunes:**
- `DiskReadViolation` - Lectura de disco en UI thread
- `DiskWriteViolation` - Escritura de disco en UI thread
- `NetworkViolation` - Red en UI thread

**Acción:**
- ✅ Mueve operaciones a `Dispatchers.IO`
- ✅ Usa `withContext(Dispatchers.IO) { ... }`

---

### 5. **Database Operations**
```
Filtro: tag:SQLiteDatabase OR tag:Firestore
Buscar: "query took"
```

**Ejemplo:**
```
D/SQLiteDatabase: Slow query: took 1523ms
```

**Acción:**
- ✅ Optimiza queries (índices, límites)
- ✅ Usa paginación
- ✅ Implementa caché local

---

### 6. **Memory Warnings**
```
Filtro: level:warn OR level:error
Buscar: "memory" OR "OutOfMemory"
```

**Ejemplo:**
```
W/System.err: java.lang.OutOfMemoryError: Failed to allocate a 4096 byte allocation
```

**Acción:**
- 🔴 Crítico: la app se quedó sin memoria
- ✅ Libera recursos (Bitmaps, Cursors)
- ✅ Reduce tamaño de imágenes
- ✅ Implementa caché con límites

---

### 7. **Network Issues**
```
Filtro: tag:OkHttp OR tag:Retrofit OR tag:Volley
Buscar: "timeout" OR "failed"
```

**Ejemplo:**
```
E/OkHttp: java.net.SocketTimeoutException: timeout
```

**Acción:**
- ✅ Aumenta timeouts si la red es lenta
- ✅ Implementa reintentos con backoff
- ✅ Muestra estado de carga al usuario

---

### 8. **Coroutine Errors**
```
Filtro: tag:AndroidRuntime
Buscar: "kotlinx.coroutines"
```

**Ejemplo:**
```
E/AndroidRuntime: kotlinx.coroutines.JobCancellationException
```

**Acción:**
- ✅ Maneja cancelación de coroutines
- ✅ Usa `try-catch` en coroutines críticas
- ✅ Limpia recursos en `finally`

---

### 9. **Firebase Performance**
```
Filtro: tag:FirebasePerformance
Buscar: "trace"
```

**Ejemplo:**
```
I/FirebasePerformance: Trace name: app_start, duration: 2345ms
```

**Acción:**
- ✅ Identifica operaciones lentas de Firebase
- ✅ Optimiza llamadas a Firestore
- ✅ Usa batch operations cuando sea posible

---

### 10. **App Startup Time**
```
Filtro: tag:ActivityManager
Buscar: "Displayed"
```

**Ejemplo:**
```
I/ActivityManager: Displayed com.example.carcollection/.MainActivity: +2s345ms
```

**Señales:**
- ✅ < 1 segundo = Excelente
- ⚠️ 1-3 segundos = Aceptable
- ❌ > 3 segundos = Lento

**Acción:**
- ✅ Reduce inicialización en `onCreate()`
- ✅ Usa `LazyColumn` en vez de listas normales
- ✅ Carga datos en background

---

## 🔧 FILTROS PERSONALIZADOS ÚTILES

### Filtro 1: Solo errores y warnings
```
level:warn OR level:error
```

### Filtro 2: Rendimiento general
```
tag:Choreographer OR tag:dalvikvm OR tag:art OR tag:StrictMode
```

### Filtro 3: Tu app específicamente
```
package:mine level:verbose
```

### Filtro 4: Problemas críticos
```
Skipped.*frames|ANR|OutOfMemory|timeout
```
(Usar como regex)

---

## 📈 MÉTRICAS OBJETIVO

### Frames por segundo:
- ✅ **60 FPS** = Objetivo (16.67ms por frame)
- ⚠️ **30-60 FPS** = Aceptable
- ❌ **< 30 FPS** = Laggy

### Frames skipped:
- ✅ **0-10** = Normal
- ⚠️ **10-60** = Monitorear
- ❌ **> 60** = Problema grave

### Startup time:
- ✅ **< 1s** = Excelente
- ⚠️ **1-3s** = Aceptable
- ❌ **> 3s** = Muy lento

### Memory:
- ✅ **< 50 MB** = Excelente
- ⚠️ **50-100 MB** = Normal
- ❌ **> 200 MB** = Alto (revisar leaks)

### GC Frequency:
- ✅ **Cada 30+ segundos** = Normal
- ⚠️ **Cada 10-30 segundos** = Monitorear
- ❌ **Cada < 10 segundos** = Problema

---

## 🛠️ HERRAMIENTAS ADICIONALES

### 1. **Android Profiler** (Android Studio)
- CPU Profiler
- Memory Profiler
- Network Profiler
- Energy Profiler

### 2. **Layout Inspector**
- Detecta overdraw
- Analiza jerarquía de vistas
- Identifica vistas innecesarias

### 3. **LeakCanary**
```gradle
dependencies {
    debugImplementation 'com.squareup.leakcanary:leakcanary-android:2.12'
}
```
Detecta memory leaks automáticamente

### 4. **Firebase Performance Monitoring**
```gradle
dependencies {
    implementation 'com.google.firebase:firebase-perf:20.5.1'
}
```
Métricas de rendimiento en producción

---

## 🎯 CHECKLIST DIARIO DE RENDIMIENTO

### Al desarrollar:
- [ ] Revisar Logcat cada vez que agregues funcionalidad pesada
- [ ] Buscar "Skipped frames" después de cambios en UI
- [ ] Verificar que no haya operaciones en hilo principal
- [ ] Comprobar uso de memoria con Memory Profiler

### Antes de commit:
- [ ] No hay warnings de StrictMode
- [ ] No hay frames skipped > 60
- [ ] GC no se ejecuta muy frecuentemente
- [ ] App inicia en < 3 segundos

### Antes de release:
- [ ] Profiling completo con Android Profiler
- [ ] Testing en dispositivos de gama baja
- [ ] Verificar con Firebase Performance
- [ ] Revisar reportes de LeakCanary

---

## 🚨 SEÑALES DE ALERTA INMEDIATA

Si ves estos logs, **detén todo y arréglalo**:

1. ❌ `Skipped 100+ frames` - App congelada
2. ❌ `ANR` - App bloqueada > 5 segundos
3. ❌ `OutOfMemoryError` - Crash inminente
4. ❌ `StrictMode policy violation` en producción
5. ❌ GC cada < 5 segundos - Fuga de memoria

---

## 📱 LOGS ESPECÍFICOS DE TU APP

### Para tu app CarCollection:

```
Filtro recomendado:
tag:Choreographer OR tag:CarViewModel OR tag:AchievementMethods OR tag:UserViewModel
```

**Qué monitorear:**
- Evaluación de logros (puede ser pesada)
- Carga de carros desde Firestore
- Carga de imágenes (Coil/Glide)
- Operaciones de XP/nivel
- Queries de búsqueda

**Puntos críticos identificados:**
- ✅ Ya optimizado: `evaluateAchievements` con debouncing
- ⚠️ Monitorear: Carga inicial de carros
- ⚠️ Monitorear: Lazy loading de imágenes
- ⚠️ Monitorear: Búsqueda/filtrado en tiempo real

---

## 🎉 RESUMEN RÁPIDO

**Para debugging diario:**
```
1. Abrir Logcat
2. Filtro: tag:Choreographer
3. Buscar: "Skipped"
4. Si ves > 60 frames skipped → Investigar
```

**Para análisis profundo:**
```
1. Android Profiler → CPU
2. Grabar sesión de uso normal
3. Identificar picos de CPU
4. Optimizar funciones pesadas
```

**Para producción:**
```
1. Firebase Performance
2. Crashlytics
3. Revisar métricas semanalmente
```

---

**Mantén estos filtros a mano y revisa Logcat regularmente!** 🚀

