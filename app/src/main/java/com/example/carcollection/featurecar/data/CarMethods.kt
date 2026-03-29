package com.example.carcollection.featurecar.data;


import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featurecar.domain.CarValidator
import com.example.carcollection.featurecar.domain.BatchAddResult
import com.example.carcollection.featureuser.data.UserMethods
import com.example.carcollection.featureuser.domain.XPSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.Query


class CarMethods {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val userMethods = UserMethods()

    // Sistema de caché en memoria para getUserCars (5 minutos TTL)
    private var carsCache: List<Car>? = null
    private var cacheTimestamp: Long = 0
    private val CACHE_DURATION = 5 * 60 * 1000L // 5 minutos

    /**
     * Invalida la caché de carros
     * Debe llamarse después de agregar, editar o eliminar carros
     */
    fun invalidateCache() {
        carsCache = null
        cacheTimestamp = 0
    }

    /**
     * Verifica si la caché es válida
     */
    private fun isCacheValid(): Boolean {
        return carsCache != null &&
               (System.currentTimeMillis() - cacheTimestamp) < CACHE_DURATION
    }

    suspend fun addCarToCollection(car: Car): Result<String> {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            val userId = firebaseUser.uid
            try {
                // ✅ VALIDAR antes de operación DB
                val validationErrors = CarValidator.validateCar(car)
                if (validationErrors.isNotEmpty()) {
                    return Result.failure(Exception(validationErrors.first().toUserMessage()))
                }

                val carsCollectionRef = db.collection("users")
                    .document(userId)
                    .collection("carsCollection")

                val carWithTimestamp = if (car.createdAt == null) {
                    car.copy(createdAt = System.currentTimeMillis())
                } else {
                    car
                }

                // ✅ RETRY logic para network errors (3 intentos con exponential backoff)
                var lastException: Exception? = null
                repeat(3) { attempt ->
                    try {
                        val documentReference = carsCollectionRef.add(carWithTimestamp).await()

                        invalidateCache()

                        // 🎮 Otorgar XP por agregar carro
                        try {
                            val xpAmount = userMethods.calculateXPByCarQuality(car.quality)
                            userMethods.addXP(
                                amount = xpAmount,
                                source = XPSource.CAR_ADDED,
                                sourceId = documentReference.id
                            )
                            println("✅ Granted $xpAmount XP for adding car (quality: ${car.quality})")
                        } catch (xpError: Exception) {
                            println("⚠️ Failed to grant XP: ${xpError.message}")
                        }

                        println("Car '${car.name}' added to user $userId with ID: ${documentReference.id}")
                        return Result.success(documentReference.id)
                    } catch (e: Exception) {
                        lastException = e
                        if (attempt < 2) {
                            delay(1000L * (attempt + 1)) // Exponential backoff: 1s, 2s
                        }
                    }
                }

                Result.failure(lastException ?: Exception("Unknown error adding car"))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to add car: ${e.message}"))
            }
        } else {
            Result.failure(Exception("No user logged in to add car to collection."))
        }
    }

    suspend fun syncLocalCarsToFirebase(localCars: List<Car>): Result<Pair<Int, Int>> {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            val userId = firebaseUser.uid
            try {
                val carsCollectionRef = db.collection("users")
                    .document(userId)
                    .collection("carsCollection")

                // Firebase permite máximo 500 operaciones por batch
                // Dividir en chunks de 500 para manejar colecciones grandes
                val batches = localCars.chunked(500)
                var successCount = 0

                for (batchChunk in batches) {
                    val writeBatch = db.batch()

                    for (car in batchChunk) {
                        // Set the createdAt timestamp if not already set
                        val carWithTimestamp = if (car.createdAt == null) {
                            car.copy(createdAt = System.currentTimeMillis())
                        } else {
                            car
                        }

                        // Create new document reference
                        val docRef = carsCollectionRef.document()
                        writeBatch.set(docRef, carWithTimestamp)
                    }

                    // Commit the batch (1 write operation for all documents in chunk)
                    writeBatch.commit().await()
                    successCount += batchChunk.size

                    println("Batch sync: ${batchChunk.size} cars added to user $userId")
                }

                // Invalidar caché después de sincronizar
                invalidateCache()

                Result.success(Pair(successCount, 0))
            } catch (e: Exception) {
                Result.failure(Exception("Failed to sync cars: ${e.message}"))
            }
        } else {
            Result.failure(Exception("No user logged in to sync cars."))
        }
    }

    suspend fun deleteCarFromCollection(carId: String): Result<Unit> {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            val userId = firebaseUser.uid
            try {
                val carDocRef = db.collection("users")
                    .document(userId)
                    .collection("carsCollection")
                    .document(carId)
                carDocRef.delete().await()

                // Invalidar caché después de eliminar
                invalidateCache()

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to delete car: ${e.message}"))
            }
        } else {
            Result.failure(Exception("No user logged in to delete car from collection."))
        }
    }

    suspend fun updateCarInCollection(carId: String, updatedCar: Car): Result<Unit> {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            val userId = firebaseUser.uid
            try {
                val carDocRef = db.collection("users")
                    .document(userId)
                    .collection("carsCollection")
                    .document(carId)
                carDocRef.set(updatedCar).await()

                // Invalidar caché después de actualizar
                invalidateCache()

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to update car: ${e.message}"))
            }
        } else {
            Result.failure(Exception("No user logged in to update car in collection."))
        }
    }


    suspend fun getUserCars(forceRefresh: Boolean = false): Result<List<Car>> {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            // Verificar caché primero (si no se fuerza refresh)
            if (!forceRefresh && isCacheValid()) {
                return Result.success(carsCache!!)
            }

            val userId = firebaseUser.uid
            try {
                val carsCollectionRef = db.collection("users")
                    .document(userId)
                    .collection("carsCollection")

                val querySnapshot = carsCollectionRef.get().await()

                val cars = querySnapshot.documents.mapNotNull { doc ->
                    doc.toObject(Car::class.java)?.copy(id = doc.id)
                }

                // Actualizar caché
                carsCache = cars
                cacheTimestamp = System.currentTimeMillis()

                Result.success(cars)
            } catch (e: Exception) {
                Result.failure(Exception("Error fetching cars: ${e.message}"))
            }
        } else {
            Result.failure(Exception("No user currently logged in."))
        }
    }

    /**
     * Obtiene carros paginados - soporta lazy loading
     * @param pageNumber Número de página (0-indexed)
     * @param pageSize Cantidad de items por página (default: 50)
     * @param forceRefresh Forzar refresh ignorando caché
     * @return Lista de carros de la página solicitada
     */
    suspend fun getUserCarsPaginated(
        pageNumber: Int = 0,
        pageSize: Int = 50,
        forceRefresh: Boolean = false
    ): Result<List<Car>> {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            val userId = firebaseUser.uid
            try {
                // ✅ Verificar caché primero (si no se fuerza refresh)
                // Si está en caché y es la página 0, retornar
                if (!forceRefresh && pageNumber == 0 && isCacheValid()) {
                    val cached = carsCache ?: emptyList()
                    val end = minOf(pageSize, cached.size)
                    return Result.success(cached.subList(0, end))
                }

                val carsCollectionRef = db.collection("users")
                    .document(userId)
                    .collection("carsCollection")

                // Firestore NO soporta OFFSET nativamente
                // Alternativa: Cargar todos (para pequeñas colecciones) o usar cursor
                val snapshot = carsCollectionRef
                    .orderBy("createdAt", Query.Direction.DESCENDING)
                    .get()
                    .await()

                val allCars = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Car::class.java)?.copy(id = doc.id)
                }

                // Actualizar caché con todos
                if (!forceRefresh && pageNumber == 0) {
                    carsCache = allCars
                    cacheTimestamp = System.currentTimeMillis()
                }

                // Calcular índices para la página solicitada
                val startIndex = pageNumber * pageSize
                val endIndex = minOf(startIndex + pageSize, allCars.size)

                if (startIndex >= allCars.size) {
                    return Result.success(emptyList())
                }

                Result.success(allCars.subList(startIndex, endIndex))
            } catch (e: Exception) {
                Result.failure(Exception("Error fetching paginated cars: ${e.message}"))
            }
        } else {
            Result.failure(Exception("No user logged in to check car existence."))
        }
    }

    suspend fun getCarById(carId: String): Result<Car> {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            val userId = firebaseUser.uid
            try {
                val carDocRef = db.collection("users")
                    .document(userId)
                    .collection("carsCollection")
                    .document(carId)
                val documentSnapshot = carDocRef.get().await()
                if (documentSnapshot.exists()) {
                    val car = documentSnapshot.toObject(Car::class.java)
                    if (car != null) {
                        // Set the ID from the document
                        val carWithId = car.copy(id = documentSnapshot.id)
                        Result.success(carWithId)
                    } else {
                        Result.failure(Exception("Failed to parse car data from Firestore."))
                    }
                } else {
                    Result.failure(Exception("Car with ID $carId not found in Firestore."))
                }
            } catch (e: Exception) {
                Result.failure(Exception("Error fetching car by ID: ${e.message}"))
            }
        } else {
            Result.failure(Exception("No user currently logged in."))
        }
    }
    suspend fun updateTagNameInAllCars(oldTagName: String, newTagName: String): Result<Unit> {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            val userId = firebaseUser.uid
            try {
                val carsCollectionRef = db.collection("users")
                    .document(userId)
                    .collection("carsCollection")
                val querySnapshot = carsCollectionRef
                    .get(Source.SERVER)
                    .await()

                val batch = db.batch()

                for (document in querySnapshot.documents) {
                    val car = document.toObject(Car::class.java)
                    if (car != null && oldTagName in car.tags) {
                        val updatedTags = car.tags.map { if (it == oldTagName) newTagName else it }
                        val updatedCar = car.copy(tags = updatedTags)
                        val carDocRef = carsCollectionRef.document(document.id)
                        batch.set(carDocRef, updatedCar)
                    }
                }

                batch.commit().await()

                // Invalidar caché después de actualizar tags
                invalidateCache()

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to update tag names in cars: ${e.message}"))
            }
        } else {
            Result.failure(Exception("No user logged in to update tags in cars."))
        }
    }

    suspend fun getRecentCars(limit: Long = 3): List<Car> {
        val firebaseUser = auth.currentUser ?: return emptyList()
        val userId = firebaseUser.uid

        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("carsCollection")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit)
                .get()
                .await()

            snapshot.toObjects(Car::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addMissingCreatedAtToAllCars(): Result<Int> {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            val userId = firebaseUser.uid
            try {
                val carsCollectionRef = db.collection("users")
                    .document(userId)
                    .collection("carsCollection")

                val querySnapshot = carsCollectionRef.get().await()
                val batch = db.batch()
                var updatedCount = 0

                for (document in querySnapshot.documents) {
                    val data = document.data ?: continue
                    // Solo actualizar si no tiene createdAt
                    if (!data.containsKey("createdAt")) {
                        val docRef = carsCollectionRef.document(document.id)
                        batch.update(docRef, "createdAt", System.currentTimeMillis())
                        updatedCount++
                    }
                }

                // Ejecutar el batch si hay documentos por actualizar
                if (updatedCount > 0) {
                    batch.commit().await()
                }

                Result.success(updatedCount)
            } catch (e: Exception) {
                Result.failure(Exception("Error al agregar createdAt: ${e.message}"))
            }
        } else {
            Result.failure(Exception("No hay usuario logueado."))
        }
    }

    suspend fun carExistsInCollection(car: Car): Result<Boolean> {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            val userId = firebaseUser.uid
            try {
                val carsCollectionRef = db.collection("users")
                    .document(userId)
                    .collection("carsCollection")

                // Optimización: Query específico usando índices en lugar de leer todos los documentos
                // Firestore permite hasta 10 whereEqualTo en una query
                // Usamos los campos más discriminatorios primero
                val querySnapshot = carsCollectionRef
                    .whereEqualTo("brand", car.brand)
                    .whereEqualTo("name", car.name)
                    .whereEqualTo("year", car.year)
                    .limit(5) // Límite pequeño, probablemente solo habrá 0-1 resultados
                    .get()
                    .await()

                // Si encontramos documentos, verificar campos adicionales en memoria
                // (photoUrl, tags, backgroundName pueden variar más)
                val exists = querySnapshot.documents.any { doc ->
                    val existingCar = doc.toObject(Car::class.java)
                    existingCar != null &&
                        existingCar.serie == car.serie &&
                        existingCar.color == car.color &&
                        existingCar.type == car.type &&
                        existingCar.quality == car.quality &&
                        existingCar.photoUrl == car.photoUrl &&
                        existingCar.tags == car.tags &&
                        existingCar.backgroundName == car.backgroundName
                }

                Result.success(exists)
            } catch (e: Exception) {
                Result.failure(Exception("Error checking if car exists: ${e.message}"))
            }
        } else {
            Result.failure(Exception("No user logged in to check car existence."))
        }
    }

    /**
     * Agregar múltiples carros en una sola operación
     * @param cars Lista de carros a agregar
     * @return BatchAddResult con estadísticas
     */
    suspend fun batchAddCars(cars: List<Car>): Result<BatchAddResult> {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            val userId = firebaseUser.uid
            try {
                if (cars.isEmpty()) {
                    return Result.success(BatchAddResult(0, 0, 0))
                }

                var successCount = 0
                var failureCount = 0
                val errors = mutableListOf<String>()

                // Dividir en batches de 500 (límite de Firestore)
                val batches = cars.chunked(500)

                for (batch in batches) {
                    try {
                        val carsCollectionRef = db.collection("users")
                            .document(userId)
                            .collection("carsCollection")

                        val writeBatch = db.batch()

                        batch.forEach { car ->
                            // ✅ Validar cada carro
                            val validationErrors = CarValidator.validateCar(car)
                            if (validationErrors.isNotEmpty()) {
                                failureCount++
                                errors.add("${car.brand} ${car.name}: ${validationErrors.first().toUserMessage()}")
                                return@forEach
                            }

                            val carWithTimestamp = car.copy(
                                createdAt = car.createdAt ?: System.currentTimeMillis()
                            )

                            val docRef = carsCollectionRef.document()
                            writeBatch.set(docRef, carWithTimestamp)
                            successCount++
                        }

                        writeBatch.commit().await()
                        println("Batch: $successCount cars added successfully")

                    } catch (e: Exception) {
                        failureCount += batch.size
                        errors.add("Batch error: ${e.message}")
                    }
                }

                invalidateCache()

                // ✅ Otorgar XP total por batch
                try {
                    val validCars = cars.filter { CarValidator.validateCar(it).isEmpty() }
                    val totalXP = validCars.sumOf { car ->
                        userMethods.calculateXPByCarQuality(car.quality)
                    }
                    if (totalXP > 0) {
                        userMethods.addXP(totalXP, XPSource.CAR_ADDED, "batch_$userId")
                        println("✅ Granted $totalXP total XP for batch import")
                    }
                } catch (xpError: Exception) {
                    println("⚠️ Failed to grant batch XP: ${xpError.message}")
                }

                Result.success(BatchAddResult(successCount, failureCount, 0, errors))
            } catch (e: Exception) {
                Result.failure(Exception("Batch operation failed: ${e.message}"))
            }
        } else {
            Result.failure(Exception("No user logged in"))
        }
    }

    /**
     * FUNCIÓN DE MIGRACIÓN: Migra carros con backgroundName antiguo a backgroundUrl
     * Se ejecuta una sola vez por usuario (verificada por backgroundsMigrated flag en User)
     *
     * Mapa de correspondencia (nombres antiguos -> IDs en JSON):
     * Fondo 1 -> fondo_1, Fondo 2 -> fondo_9, Fondo 3 -> fondo_14, etc.
     */
    suspend fun migrateBackgrounds(): Result<String> {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            try {
                val userId = firebaseUser.uid
                val userDocRef = db.collection("users").document(userId)

                // ✅ PASO 1: Verificar si ya se hizo la migración
                val userSnapshot = userDocRef.get().await()
                val backgroundsMigrated = userSnapshot.getBoolean("backgroundsMigrated") ?: false

                if (backgroundsMigrated) {
                    println("ℹ️ Backgrounds already migrated for user $userId (skipping)")
                    return Result.success("Already migrated")
                }

                // ✅ PASO 2: Proceder con migración
                val carsCollectionRef = db.collection("users")
                    .document(userId)
                    .collection("carsCollection")

                // Mapa de nombres antiguos a IDs JSON
                val backgroundMapping = mapOf(
                    "Fondo 1" to "fondo_1",
                    "Fondo 01" to "fondo_1",
                    "Fondo 2" to "fondo_9",
                    "Fondo 02" to "fondo_2",
                    "Fondo 3" to "fondo_14",
                    "Fondo 03" to "fondo_3",
                    "Fondo 4" to "fondo_15",
                    "Fondo 04" to "fondo_4",
                    "Fondo 5" to "fondo_16",
                    "Fondo 6" to "fondo_17",
                    "Fondo 7" to "fondo_18",
                    "Fondo 8" to "fondo_19",
                    "Fondo 10" to "fondo_7",
                    "Fondo 15" to "fondo_8",
                    "Fondo 20" to "fondo_10",
                    "Fondo 23" to "fondo_11",
                    "Fondo 24" to "fondo_12",
                    "Fondo 26" to "fondo_13",
                    "Fondo F2" to "fondo_5",
                    "Fondo" to "fondo_6"
                )

                // Mapa de IDs JSON a URLs (basado en backgrounds.json)
                val idToUrlMapping = mapOf(
                    "fondo_1" to "https://raw.githubusercontent.com/polarismkr/diecastimghoster/main/fondos/01.png",
                    "fondo_2" to "https://raw.githubusercontent.com/polarismkr/diecastimghoster/main/fondos/02.png",
                    "fondo_3" to "https://raw.githubusercontent.com/polarismkr/diecastimghoster/main/fondos/03.png",
                    "fondo_4" to "https://raw.githubusercontent.com/polarismkr/diecastimghoster/main/fondos/04.png",
                    "fondo_5" to "https://raw.githubusercontent.com/polarismkr/diecastimghoster/main/fondos/f2.jpg",
                    "fondo_6" to "https://raw.githubusercontent.com/polarismkr/diecastimghoster/main/fondos/fondo.jpg",
                    "fondo_7" to "https://raw.githubusercontent.com/polarismkr/diecastimghoster/main/fondos/fondo10.png",
                    "fondo_8" to "https://raw.githubusercontent.com/polarismkr/diecastimghoster/main/fondos/fondo15.jpeg",
                    "fondo_9" to "https://raw.githubusercontent.com/polarismkr/diecastimghoster/main/fondos/fondo2.png",
                    "fondo_10" to "https://raw.githubusercontent.com/polarismkr/diecastimghoster/main/fondos/fondo20.jpeg",
                    "fondo_11" to "https://raw.githubusercontent.com/polarismkr/diecastimghoster/main/fondos/fondo23.jpg",
                    "fondo_12" to "https://raw.githubusercontent.com/polarismkr/diecastimghoster/main/fondos/fondo24.jpeg",
                    "fondo_13" to "https://raw.githubusercontent.com/polarismkr/diecastimghoster/main/fondos/fondo26.jpg",
                    "fondo_14" to "https://raw.githubusercontent.com/polarismkr/diecastimghoster/main/fondos/fondo3.jpg",
                    "fondo_15" to "https://raw.githubusercontent.com/polarismkr/diecastimghoster/main/fondos/fondo4.jpg",
                    "fondo_16" to "https://raw.githubusercontent.com/polarismkr/diecastimghoster/main/fondos/fondo5.jpg",
                    "fondo_17" to "https://raw.githubusercontent.com/polarismkr/diecastimghoster/main/fondos/fondo6.jpg",
                    "fondo_18" to "https://raw.githubusercontent.com/polarismkr/diecastimghoster/main/fondos/fondo7.png",
                    "fondo_19" to "https://raw.githubusercontent.com/polarismkr/diecastimghoster/main/fondos/fondo8.png"
                )

                // 🔹 URL default (fondo_1 - el primero del JSON)
                val defaultBackgroundUrl = "https://raw.githubusercontent.com/polarismkr/diecastimghoster/main/fondos/01.png"

                // Obtener todos los carros del usuario
                val snapshot = carsCollectionRef.get().await()
                var migratedCount = 0
                var unmappedCount = 0
                var emptyBackgroundCount = 0

                // Batch update para migraciones
                val writeBatch = db.batch()

                for (doc in snapshot.documents) {
                    val backgroundName = doc.getString("backgroundName")
                    val backgroundUrl = doc.getString("backgroundUrl")
                    var shouldUpdate = false
                    var newBackgroundUrl = backgroundUrl

                    // ✅ CASO 1: Si tiene backgroundName antiguo, migrar a URL
                    if (!backgroundName.isNullOrBlank()) {
                        val mappedId = backgroundMapping[backgroundName]
                        newBackgroundUrl = if (mappedId != null) {
                            idToUrlMapping[mappedId] ?: defaultBackgroundUrl  // Fallback a default si falta en map
                        } else {
                            // Si no se encuentra en el mapa, usar fondo default
                            println("⚠️ Unknown background name: '$backgroundName' for car ${doc.id}, using default")
                            unmappedCount++
                            defaultBackgroundUrl
                        }
                        shouldUpdate = true
                        migratedCount++
                    }
                    // ✅ CASO 2: Si NO tiene backgroundUrl o está vacío/null, asignar default
                    else if (backgroundUrl.isNullOrBlank()) {
                        println("⚠️ Car ${doc.id} has no background, assigning default")
                        newBackgroundUrl = defaultBackgroundUrl
                        shouldUpdate = true
                        emptyBackgroundCount++
                    }

                    // Actualizar documento si es necesario
                    if (shouldUpdate) {
                        writeBatch.update(
                            doc.reference,
                            mapOf(
                                "backgroundUrl" to newBackgroundUrl,
                                "backgroundName" to null  // Limpiar nombre antiguo si existe
                            )
                        )
                    }
                }

                // ✅ PASO 3: Ejecutar batch update
                val totalUpdated = migratedCount + emptyBackgroundCount
                if (totalUpdated > 0) {
                    writeBatch.commit().await()
                    invalidateCache()
                    println("✅ Updated $totalUpdated cars - $migratedCount migrated, $emptyBackgroundCount assigned default")
                    if (unmappedCount > 0) {
                        println("⚠️ $unmappedCount cars had unknown background names and were assigned default")
                    }
                } else {
                    println("ℹ️ All cars have valid backgrounds, no migration needed")
                }

                // ✅ PASO 4: Marcar en usuario que migración fue completada
                userDocRef.update(
                    mapOf(
                        "backgroundsMigrated" to true,
                        "backgroundsMigratedAt" to System.currentTimeMillis()
                    )
                ).await()

                println("✅ Background migration flag updated for user $userId")
                Result.success("Processed $totalUpdated cars (migrated: $migratedCount, assigned default: $emptyBackgroundCount, unmapped: $unmappedCount)")

            } catch (e: Exception) {
                Result.failure(Exception("Background migration failed: ${e.message}"))
            }
        } else {
            Result.failure(Exception("No user logged in"))
        }
    }

}
