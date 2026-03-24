# Refactorización de Sistema de Fondos - Documentación

## Cambios Realizados

### 1. **Modelo de Datos Actualizado** 
   - **Archivo**: `BackgroundCategory.kt`
   - **Cambios**: 
     - `BackgroundItem` ahora usa `id`, `name`, `url`, y `thumbnailUrl` en lugar de `resource`
     - Se agregó `BackgroundsResponse` para facilitar el parsing del JSON

### 2. **Carga desde JSON**
   - **Archivo Nuevo**: `BackgroundLoader.kt`
   - **Función**: `loadBackgroundCategoriesFromJson(context: Context)`
   - **Características**:
     - Lee desde `assets/backgrounds.json`
     - Ejecuta en thread IO usando Coroutines
     - Manejo de excepciones

### 3. **Componente de Selección con URLs**
   - **Archivo Nuevo**: `BackgroundSelectorFromUrl.kt`
   - **Componentes**:
     - `BackgroundSelectorFromUrl`: Renderiza todas las categorías
     - `BackgroundCategoryRowFromUrl`: Renderiza una categoría
     - `BackgroundThumbnailFromUrl`: Renderiza una miniatura con soporte para URLs via Coil
   - **Características**:
     - Carga imágenes desde URLs
     - No usa recursos locales
     - Eficiente con AsyncImage de Coil

### 4. **BackgroundSelect.kt Simplificado**
   - Ahora actúa como proxy que llama a `BackgroundSelectorFromUrl`
   - Elimina toda la lógica de renderización local

### 5. **ViewModel Actualizado**
   - **Archivo**: `CarFormViewModel.kt`
   - **Cambios**:
     - Constructor ahora recibe `context: Context`
     - Importa y usa `loadBackgroundCategoriesFromJson`
     - Default background cambió de `"fondo"` a `"fondo_1"`
   - **Factory**:
     - `CarFormViewModelFactory` actualizada para recibir contexto

### 6. **NavGraph Actualizado**
   - Se pasa `LocalContext.current` a `CarFormViewModelFactory`
   - Dos rutas de AddEditCar ahora usan el contexto

### 7. **JSON de Configuración**
   - **Archivo**: `assets/backgrounds.json`
   - **Estructura**:
     ```json
     {
       "categories": [
         {
           "category": "Nombre categoría",
           "backgrounds": [
             {
               "id": "fondo_1",
               "name": "Nombre del fondo",
               "url": "https://...",
               "thumbnailUrl": "https://..."
             }
           ]
         }
       ]
     }
     ```

## Ventajas del Sistema Nuevo

✅ **Sin renderización local**: No consume recursos compilando drawables  
✅ **Escalable**: Fácil agregar nuevos fondos desde URLs  
✅ **Eficiente**: Coil cachea automáticamente las imágenes  
✅ **Flexible**: Los fondos pueden cambiar sin recompilar la app  
✅ **Mantenible**: Configuración centralizada en JSON  

## Cómo Agregar Nuevos Fondos

1. Subir las imágenes (completa y thumbnail) a un servidor
2. Editar `assets/backgrounds.json`:
   ```json
   {
     "id": "nombre_unico",
     "name": "Nombre visible",
     "url": "https://...",
     "thumbnailUrl": "https://..."
   }
   ```
3. ¡Listo! No necesita recompilación

## Nota sobre Archivos Antiguos

El archivo `backgroundResourceMap.kt` ahora es **obsoleto** y puede eliminarse una vez se verifique que todo funciona correctamente.

