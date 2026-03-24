# Guía de Integración: Fondos desde URLs Remotas

## 🌐 Opciones de Implementación

### Opción 1: URLs estáticas en JSON (Recomendado para comenzar)

**Ventaja**: Simple, no requiere servidor dinámico
**Desventaja**: Requiere actualizar app para cambiar URLs

```json
{
  "categories": [
    {
      "category": "Fondos",
      "backgrounds": [
        {
          "id": "fondo_1",
          "name": "Fondo 1",
          "url": "https://cdn.example.com/backgrounds/fondo1.jpg",
          "thumbnailUrl": "https://cdn.example.com/backgrounds/fondo1_thumb.jpg"
        }
      ]
    }
  ]
}
```

### Opción 2: URLs desde Firebase Cloud Storage (Recomendado para producción)

**Ventaja**: Dinámico, actualizable sin recompilar
**Desventaja**: Requiere configuración en Firebase

```json
{
  "categories": [
    {
      "category": "Fondos",
      "backgrounds": [
        {
          "id": "fondo_1",
          "name": "Fondo 1",
          "url": "https://firebasestorage.googleapis.com/v0/b/[PROJECT].appspot.com/o/backgrounds%2Ffondo1.jpg?alt=media",
          "thumbnailUrl": "https://firebasestorage.googleapis.com/v0/b/[PROJECT].appspot.com/o/backgrounds%2Ffondo1_thumb.jpg?alt=media"
        }
      ]
    }
  ]
}
```

### Opción 3: URLs desde servidor REST API (Máxima flexibilidad)

Reemplazo para `loadBackgroundCategoriesFromJson()`:

```kotlin
suspend fun loadBackgroundCategoriesFromApi(): List<BackgroundCategory> {
    return withContext(Dispatchers.IO) {
        try {
            // Usando Retrofit, OkHttp, o Ktor
            val response = ApiClient.backgrounds.getCategories()
            response.categories
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
```

---

## 📦 Cómo subir imágenes a Firebase Storage

1. **En Firebase Console**:
   - Ve a `Storage`
   - Crea carpeta: `backgrounds/`
   - Sube tus imágenes

2. **Obtener URL pública**:
   - Click en imagen
   - "Copiar ruta de acceso público"
   - Pega en JSON

3. **Reglas de seguridad** (Firebase):
```
match /backgrounds/{allPaths=**} {
  allow read: if true;  // Público
}
```

---

## 🖼️ Recomendaciones de Imágenes

| Tipo | Resolución | Tamaño | Formato |
|------|-----------|--------|---------|
| **Thumbnail** | 80x80 px | < 15 KB | JPG/WebP |
| **Completa** | 1920x1080 px | < 500 KB | JPG/WebP |

**Compresión**:
- Usa ImageMagick o TinyPNG
- Calidad JPEG: 75-85%
- WebP: 20% más pequeño que JPG

---

## 🔄 Actualizar URLs en tiempo real (Avanzado)

Si quieres cambiar URLs sin recompilar:

```kotlin
// En ViewModel
private val _backgroundsUrl = MutableStateFlow("https://tu-api.com/backgrounds.json")

fun updateBackgroundsUrl(newUrl: String) {
    _backgroundsUrl.value = newUrl
    // Recargar fondos
}

// En loadBackgroundCategoriesFromJson()
suspend fun loadBackgroundCategoriesFromJson(
    context: Context,
    customUrl: String? = null
): List<BackgroundCategory> {
    return withContext(Dispatchers.IO) {
        try {
            val source = if (customUrl != null) {
                // Desde URL remota
                URL(customUrl).readText()
            } else {
                // Desde assets local
                context.assets.open("backgrounds.json").bufferedReader().use { it.readText() }
            }
            val response = Gson().fromJson(source, BackgroundsResponse::class.java)
            response.categories
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
```

---

## ⚡ Caché con Coil

Coil automáticamente cachea imágenes:

```kotlin
// Las imágenes se cachean en:
// - Memoria (LRU Cache)
// - Disco (~50 MB por defecto)

// Forzar recarga:
AsyncImage(
    model = ImageRequest.Builder(context)
        .data(url)
        .memoryCachePolicy(CachePolicy.DISABLED)
        .diskCachePolicy(CachePolicy.DISABLED)
        .build(),
    contentDescription = null
)
```

---

## 🚀 Checklist de Implementación

- [ ] URLs en JSON funcionan correctamente
- [ ] Imágenes se cargan sin errores
- [ ] Thumbnails cargan rápido
- [ ] Imágenes completas se ven bien
- [ ] Sin timeout en conexiones lentas
- [ ] Manejo de imágenes rotas (placeholder)
- [ ] Caché funciona correctamente

---

## 🔒 Consideraciones de Seguridad

1. **Validar URLs**:
   ```kotlin
   fun isValidUrl(url: String): Boolean {
       return url.startsWith("https://")  // Solo HTTPS
   }
   ```

2. **Límite de URLs**:
   ```kotlin
   if (backgrounds.size > 100) {
       log("Too many backgrounds")
   }
   ```

3. **Timeout**:
   ```kotlin
   AsyncImage(
       model = ImageRequest.Builder(context)
           .data(url)
           .crossfade(300)
           .build(),
       contentDescription = null
   )
   ```

---

## 📞 Soporte

Si las imágenes no cargan:
1. Verifica que la URL sea válida (abre en navegador)
2. Comprueba que sea HTTPS
3. Revisa los logs de Logcat
4. Verifica tamaño/peso de imagen

