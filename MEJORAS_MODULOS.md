# Mejoras por Módulo

## Índice
1. [featurecar](#featurecar)
2. [featureAchievements](#featureachievements)
3. [featureuser](#featureuser)
4. [featurestats](#featurestats)
5. [featuretags](#featuretags)
6. [featureWishlist](#featurewishlist)
7. [featureNotification](#featurenotification)
8. [featuremenu](#featuremenu)
9. [featureconfig](#featureconfig)

---

## featurecar

### Rendimiento
1. **Borrado en batch**: actualmente el borrado es de uno en uno — agregar selección múltiple y borrado en lote.

### Nuevas características
2. **Campo `condition`**: agregar estado del carro al modelo (Mint, Used, Damaged) — útil para coleccionistas serios.
3. **Campo `purchasePrice` / `estimatedValue`**: precio de compra y valor estimado. Permite calcular el valor total de la colección en featurestats.
4. **Campo `location`**: donde está físicamente el carro (ej: "Cajón 3", "Vitrina 2") — útil para colecciones grandes.
5. **Ordenar por quality**: actualmente se puede filtrar por quality pero no ordenar por ella.
6. **Exportar colección**: exportar lista de carros a CSV o PDF desde la vista de colección.
7. **Modo comparar**: seleccionar 2 carros y ver sus datos lado a lado.
8. **Historial de ediciones**: guardar un log de cuándo se editó un carro y qué cambió.

---

## featureAchievements

### Nuevas características
1. **Logros de racha (`STREAK_BASED`)**: la categoría existe en el enum pero no tiene lógica implementada. Implementar: agrega carros X días consecutivos.
2. **Progreso visible por logro**: en la pantalla de logros mostrar barra de progreso individual (ej: 7/10 carros rojos).
3. **Notificación al desbloquear**: actualmente el popup existe pero no dispara una notificación persistente en featureNotification.
4. **Logros secretos revelados al 50%**: si un logro es `hidden`, mostrarlo con nombre genérico cuando el usuario llega al 50% del progreso.
5. **Categoría `SOCIAL`**: logros por interacción — ver X perfiles, tener X seguidores, recibir X visitas en perfil.
6. **Insignias visuales en perfil**: los logros desbloqueados de rareza LEGENDARIO o SPECIAL deberían mostrarse como insignias en el perfil público.

---

## featureuser

### Nuevas características
1. **Seguir usuarios**: sistema de follow/unfollow. La lista de usuarios ya existe (`UserList`) pero no hay relación entre ellos.
2. **Feed de actividad**: ver los carros recientes de usuarios que sigues.
3. **Contador de visitas al perfil**: cuántas veces han visto tu perfil público.
4. **Buscar usuario por nombre**: en `UserList` agregar buscador por username en tiempo real.
5. **Compartir perfil**: botón para compartir el link del perfil público (deep link).
6. **Niveles con recompensas**: al subir de nivel, mostrar qué desbloquea (ej: nuevo fondo de pantalla, insignia exclusiva).

---

## featurestats

### Rendimiento
1. **`loadCars()` descarga toda la colección** solo para generar estadísticas — crear endpoints específicos en `CarMethods` que devuelvan solo los campos necesarios (brand, year, color, quality, type) sin cargar photoUrl ni backgroundUrl.
2. **Estadísticas cacheadas**: las stats no cambian hasta que se agrega/elimina un carro — invalidar el cache solo en esos eventos en lugar de recalcular siempre.

### Nuevas características
3. **Estadística de valor**: si se agrega `purchasePrice` al modelo de carro, mostrar valor total de la colección y distribución por quality.
4. **Evolución en el tiempo**: gráfico de líneas mostrando cuántos carros se agregaron por mes/año.
5. **Stats comparadas con otros usuarios**: "Tienes más X que el 80% de usuarios" — motivacional.
6. **Top 5 series más repetidas**: qué serie tiene más carros en la colección.
7. **Exportar stats**: imagen o PDF con las estadísticas para compartir.

---

## featuretags

### Nuevas características
1. **Tags con ícono**: además de color, permitir asignar un emoji o icono a cada tag.
2. **Tags predefinidos**: al crear la cuenta, ofrecer un set de tags sugeridos (Nuevo, En caja, Abierto, Para vender, etc.).
3. **Filtrar por múltiples tags a la vez**: actualmente el filtro de tags acepta solo uno — permitir selección múltiple (AND/OR).
4. **Tags globales sugeridos**: admin puede crear tags sugeridos que aparecen para todos los usuarios.

---

## featureWishlist

### Rendimiento
1. **Sin paginación**: la wishlist carga todos los items de golpe — agregar paginación o scroll virtual si crece mucho.

### Nuevas características
2. **Marcar como conseguido**: botón "Lo conseguí" que mueve el item a la colección (prellenando el formulario de agregar carro).
3. **Precio objetivo**: campo para poner cuánto estás dispuesto a pagar.
4. **Compartir wishlist**: ya existe `PUBLIC_WISHLIST` en nav pero la vista pública podría mejorar mostrando si el usuario ya tiene ese carro en su colección.
5. **Orden por prioridad**: ordenar la lista por prioridad (Urgente primero) de forma visual con colores.
6. **Notificación de disponibilidad**: si otro usuario tiene en su colección un carro de tu wishlist, notificarte (requiere featureNotification).

---

## featureNotification

### Rendimiento
1. **Listener de unread count** está activo permanentemente — desconectarlo cuando el usuario entra a la pantalla de notificaciones y reconectarlo al salir.

### Nuevas características
2. **Notificaciones push (FCM)**: actualmente las notificaciones son solo in-app (Firestore). Integrar Firebase Cloud Messaging para push notifications reales.
3. **Tipos `SOCIAL` y `REMINDER`** están definidos en el enum pero no se generan desde ningún lado — implementar:
   - `SOCIAL`: alguien ve tu perfil, te sigue.
   - `REMINDER`: "Llevas X días sin agregar un carro".
4. **Notificación de logro desbloqueado persistente**: cuando se desbloquea un logro, crear un documento en la colección de notificaciones además del popup.
5. **Acciones desde notificación**: el campo `referenceId` existe pero no se usa para navegar — al tocar una notificación navegar al recurso referenciado (carro, logro, perfil).
6. **Limpiar notificaciones antiguas**: botón "Marcar todo como leído" y "Eliminar leídas".

---

## featuremenu

### Nuevas características
1. **Carro del día: notificación diaria**: si el usuario tiene el carro del día, mandar notificación push al abrir la app (o via FCM).
2. **Sección "Novedades"**: pequeño feed en el menú principal con las últimas actualizaciones de la app o carros recientes de usuarios que sigues.
3. **Acceso rápido a stats**: botón en el menú principal que lleva directo a featurestats sin pasar por el menú lateral.
4. **Pantalla de bienvenida personalizada**: si es el primer login del día, mostrar un saludo con el streak de días activos.

---

## featureconfig

### Nuevas características
1. **Tema claro/oscuro**: agregar toggle de tema en configuración. Actualmente sigue solo el tema del sistema.
2. **Configuración de notificaciones**: toggle para activar/desactivar cada tipo de notificación (logros, social, recordatorios).
3. **Idoma**: preparar strings para internacionalización (actualmente todo está hardcodeado en español).
4. **Pantalla "Acerca de"**: versión de la app, changelog, créditos — ya existe la ruta `ABOUT` en NavRoutes.

