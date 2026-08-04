package com.example.carcollection.featurecar.data;


import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featurecar.domain.CarStatsData
import com.example.carcollection.featurecar.domain.CarValidator
import com.example.carcollection.featurecar.domain.BatchAddResult
import com.example.carcollection.featurestats.ComparisonStats
import com.example.carcollection.featureuser.data.UserMethods
import com.example.carcollection.featureuser.domain.XPSource
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.delay
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Source
import com.google.firebase.firestore.Query
import com.example.carcollection.featuremenu.HighlightedCar.UserOfTheMonth


class CarMethods {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val userMethods = UserMethods()

    // Sistema de caché en memoria para getUserCars (5 minutos TTL)
    private var carsCache: List<Car>? = null
    private var cacheTimestamp: Long = 0
    private val CACHE_DURATION = 5 * 60 * 1000L // 5 minutos

    // Caché ligero para estadísticas (solo campos necesarios, 10 minutos TTL)
    private var statsDataCache: List<CarStatsData>? = null
    private var statsDataCacheTimestamp: Long = 0
    private val STATS_CACHE_DURATION = 10 * 60 * 1000L // 10 minutos

    /**
     * Invalida la caché de carros (completa y para stats)
     * Debe llamarse después de agregar, editar o eliminar carros
     */
    fun invalidateCache() {
        carsCache = null
        cacheTimestamp = 0
        statsDataCache = null
        statsDataCacheTimestamp = 0
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

                        // 📊 Actualizar contador de carros en perfil de usuario
                        updateUserCarsCount(userId, +1)

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

                // 📊 Actualizar contador de carros en perfil de usuario
                updateUserCarsCount(userId, -1)

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

                // 📊 Actualizar contador de carros en perfil de usuario
                updateUserCarsCount(userId, successCount.toLong())

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

    /**
     * Obtener carros específicos por sus IDs.
     * ✅ Optimizado: usa whereIn en lotes de 30 (límite de Firestore)
     * en lugar de N reads individuales — de O(n) fetches a O(n/30) fetches.
     *
     * Usado para mostrar carros favoritos del usuario actual.
     */
    suspend fun getCarsByIds(carIds: List<String>): List<Car> {
        if (carIds.isEmpty()) return emptyList()

        val firebaseUser = auth.currentUser ?: return emptyList()
        val userId = firebaseUser.uid

        return try {
            // Firestore whereIn tiene límite de 30 valores — chunked para colecciones grandes
            carIds.chunked(30).flatMap { chunk ->
                try {
                    db.collection("users")
                        .document(userId)
                        .collection("carsCollection")
                        .whereIn(com.google.firebase.firestore.FieldPath.documentId(), chunk)
                        .get()
                        .await()
                        .documents
                        .mapNotNull { doc ->
                            doc.toObject(Car::class.java)?.copy(id = doc.id)
                        }
                } catch (e: Exception) {
                    emptyList()
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Obtener carros específicos por IDs para un usuario en concreto (perfil público).
     * ✅ Optimizado con whereIn en lotes de 30.
     */
    suspend fun getCarsByIdsForUser(userId: String, carIds: List<String>): List<Car> {
        if (carIds.isEmpty()) return emptyList()

        return try {
            carIds.chunked(30).flatMap { chunk ->
                try {
                    db.collection("users")
                        .document(userId)
                        .collection("carsCollection")
                        .whereIn(com.google.firebase.firestore.FieldPath.documentId(), chunk)
                        .get()
                        .await()
                        .documents
                        .mapNotNull { doc ->
                            doc.toObject(Car::class.java)?.copy(id = doc.id)
                        }
                } catch (e: Exception) {
                    emptyList()
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MEJORA 1: Endpoint ligero para estadísticas (solo campos necesarios)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Obtiene solo los campos necesarios para calcular estadísticas.
     * Evita cargar photoUrl, backgroundUrl y otros campos pesados.
     * Tiene su propia caché de 10 minutos independiente de getUserCars().
     */
    suspend fun getUserCarsForStats(forceRefresh: Boolean = false): Result<List<CarStatsData>> {
        val firebaseUser = auth.currentUser
            ?: return Result.failure(Exception("No user currently logged in."))

        // Retornar caché si es válida
        if (!forceRefresh && statsDataCache != null) {
            val age = System.currentTimeMillis() - statsDataCacheTimestamp
            if (age < STATS_CACHE_DURATION) {
                return Result.success(statsDataCache!!)
            }
        }

        val userId = firebaseUser.uid
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("carsCollection")
                .get()
                .await()

            val statsData = snapshot.documents.mapNotNull { doc ->
                // Mapear solo los campos relevantes para stats
                CarStatsData(
                    brand      = doc.getString("brand"),
                    year       = doc.getString("year"),
                    color      = doc.getString("color"),
                    quality    = doc.getString("quality"),
                    type       = doc.getString("type"),
                    tags       = (doc.get("tags") as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                    createdAt  = doc.getLong("createdAt")
                )
            }

            statsDataCache = statsData
            statsDataCacheTimestamp = System.currentTimeMillis()

            Result.success(statsData)
        } catch (e: Exception) {
            Result.failure(Exception("Error fetching stats data: ${e.message}"))
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MEJORA 5: Mantenimiento de carsCount para comparativas entre usuarios
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Actualiza el contador carsCount en el documento del usuario.
     * Se usa internamente tras agregar o eliminar carros.
     * @param delta +1 al agregar, -1 al eliminar, N al batch import
     */
    private suspend fun updateUserCarsCount(userId: String, delta: Long) {
        try {
            db.collection("users")
                .document(userId)
                .update("carsCount", FieldValue.increment(delta))
                .await()
        } catch (e: Exception) {
            // No es crítico – si falla, la comparativa puede ser inexacta
            println("⚠️ Failed to update carsCount: ${e.message}")
        }
    }

    /**
     * Obtiene las estadísticas de comparación con otros usuarios.
     * Lee el campo carsCount de todos los documentos de usuario para
     * calcular el percentil del usuario actual.
     *
     * @return ComparisonStats con el percentil calculado
     */
    suspend fun getUserComparisonStats(): Result<ComparisonStats> {
        val firebaseUser = auth.currentUser
            ?: return Result.failure(Exception("No user currently logged in."))
        val myUserId = firebaseUser.uid

        return try {
            val usersSnapshot = db.collection("users").get().await()

            // Contar carros REALES de cada usuario iterando por documento
            val allCounts = mutableListOf<Int>()
            
            for (userDoc in usersSnapshot.documents) {
                val userId = userDoc.id
                val carCount = db.collection("users")
                    .document(userId)
                    .collection("carsCollection")
                    .get()
                    .await()
                    .size()
                
                allCounts.add(carCount)
            }

            // Contar carros del usuario actual
            val myCount = db.collection("users")
                .document(myUserId)
                .collection("carsCollection")
                .get()
                .await()
                .size()

            val totalUsers = allCounts.size
            val avgCars = if (totalUsers > 0) allCounts.average().toInt() else 0
            val usersWithFewer = allCounts.count { it < myCount }
            val percentile = if (totalUsers > 1) {
                ((usersWithFewer.toFloat() / (totalUsers - 1)) * 100).toInt().coerceIn(0, 100)
            } else 100

            Result.success(
                ComparisonStats(
                    myCarCount        = myCount,
                    totalUsers        = totalUsers,
                    percentile        = percentile,
                    averageCarsPerUser = avgCars
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Error fetching comparison stats: ${e.message}"))
        }
    }

    /**
     * Obtiene el usuario que agregó más carros en el mes especificado.
     * Se usa para mostrar "Tuba del mes" en el menú.
     * 
     * @param monthOffset 0 = current month, -1 = previous month (production), -2 = 2 months ago, etc.
     */
    suspend fun getUserOfTheMonth(monthOffset: Int = -1): Result<Map<String, Any>?> {
        return try {
            val calendar = java.util.Calendar.getInstance()
            
            // Retroceder los meses especificados
            calendar.add(java.util.Calendar.MONTH, monthOffset)
            
            // Obtener el primer día del mes
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val monthStartMs = calendar.timeInMillis
            
            // Obtener el último día del mes
            calendar.add(java.util.Calendar.MONTH, 1)
            calendar.add(java.util.Calendar.DAY_OF_MONTH, -1)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calendar.set(java.util.Calendar.MINUTE, 59)
            calendar.set(java.util.Calendar.SECOND, 59)
            calendar.set(java.util.Calendar.MILLISECOND, 999)
            val monthEndMs = calendar.timeInMillis
            
            android.util.Log.d("getUserOfTheMonth", "Searching for cars between $monthStartMs and $monthEndMs (monthOffset=$monthOffset)")
            
            // Obtener todos los usuarios
            val usersSnapshot = db.collection("users")
                .get(Source.SERVER)
                .await()
            
            android.util.Log.d("getUserOfTheMonth", "Found ${usersSnapshot.size()} users")
            
            val userCarCounts = mutableMapOf<String, Pair<Int, com.example.carcollection.featureuser.domain.User>>()
            
            // Para cada usuario, contar sus carros añadidos en el mes especificado
            for (userDoc in usersSnapshot.documents) {
                val userId = userDoc.id
                val user = userDoc.toObject(com.example.carcollection.featureuser.domain.User::class.java)
                
                if (user != null) {
                    try {
                        val carsSnapshot = db.collection("users").document(userId)
                            .collection("carsCollection")
                            .whereGreaterThanOrEqualTo("createdAt", monthStartMs)
                            .whereLessThanOrEqualTo("createdAt", monthEndMs)
                            .get(Source.SERVER)
                            .await()
                        
                        val carCount = carsSnapshot.size()
                        android.util.Log.d("getUserOfTheMonth", "User $userId (${user.username}) has $carCount cars added in month offset $monthOffset")
                        
                        if (carCount > 0) {
                            userCarCounts[userId] = Pair(carCount, user)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("getUserOfTheMonth", "Error fetching cars for user $userId: ${e.message}")
                    }
                }
            }
            
            android.util.Log.d("getUserOfTheMonth", "Total users with cars: ${userCarCounts.size}")
            
            // Encontrar el usuario con más carros
            if (userCarCounts.isEmpty()) {
                android.util.Log.d("getUserOfTheMonth", "No users found with cars in month offset $monthOffset")
                Result.success(null)
            } else {
                val (userId, countAndUser) = userCarCounts.maxByOrNull { it.value.first }
                    ?: return Result.success(null)
                
                val (count, user) = countAndUser
                
                android.util.Log.d("getUserOfTheMonth", "Winner: ${user.username} with $count cars")
                
                Result.success(mapOf(
                    "userId" to userId,
                    "username" to (user.username ?: "Usuario"),
                    "photoUrl" to (user.photoUrl ?: ""),
                    "carCount" to count
                ))
            }
        } catch (e: Exception) {
            android.util.Log.e("getUserOfTheMonth", "Error fetching user of the month", e)
            Result.failure(Exception("Error fetching user of the month: ${e.message}"))
        }
    }

    /**
     * Obtiene el ranking de usuarios por cantidad de carros agregados en un mes.
     * 
     * @param monthOffset 0 = current month, -1 = previous month
     * @param limit número máximo de usuarios a retornar
     */
    suspend fun getMonthlyLeaderboard(monthOffset: Int = 0, limit: Int = 5): Result<List<UserOfTheMonth>> {
        return try {
            val calendar = java.util.Calendar.getInstance()
            
            calendar.add(java.util.Calendar.MONTH, monthOffset)
            calendar.set(java.util.Calendar.DAY_OF_MONTH, 1)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
            calendar.set(java.util.Calendar.MINUTE, 0)
            calendar.set(java.util.Calendar.SECOND, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            val monthStartMs = calendar.timeInMillis
            
            calendar.add(java.util.Calendar.MONTH, 1)
            calendar.add(java.util.Calendar.DAY_OF_MONTH, -1)
            calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
            calendar.set(java.util.Calendar.MINUTE, 59)
            calendar.set(java.util.Calendar.SECOND, 59)
            calendar.set(java.util.Calendar.MILLISECOND, 999)
            val monthEndMs = calendar.timeInMillis
            
            val usersSnapshot = db.collection("users")
                .get(Source.SERVER)
                .await()
            
            val userCarCounts = mutableMapOf<String, Pair<Int, com.example.carcollection.featureuser.domain.User>>()
            
            for (userDoc in usersSnapshot.documents) {
                val userId = userDoc.id
                val user = userDoc.toObject(com.example.carcollection.featureuser.domain.User::class.java)
                
                if (user != null) {
                    try {
                        val carsSnapshot = db.collection("users").document(userId)
                            .collection("carsCollection")
                            .whereGreaterThanOrEqualTo("createdAt", monthStartMs)
                            .whereLessThanOrEqualTo("createdAt", monthEndMs)
                            .get(Source.SERVER)
                            .await()
                        
                        val carCount = carsSnapshot.size()
                        if (carCount > 0) {
                            userCarCounts[userId] = Pair(carCount, user)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("getMonthlyLeaderboard", "Error fetching cars for user $userId: ${e.message}")
                    }
                }
            }
            
            // Sort by car count descending and limit
            val leaderboard = userCarCounts
                .toList()
                .sortedByDescending { it.second.first }
                .take(limit)
                .map { (userId, pair) ->
                    val (count, user) = pair
                    UserOfTheMonth(
                        userId = userId,
                        username = user.username ?: "Usuario",
                        photoUrl = user.photoUrl ?: "",
                        carCount = count
                    )
                }
            
            Result.success(leaderboard)
        } catch (e: Exception) {
            android.util.Log.e("getMonthlyLeaderboard", "Error fetching leaderboard", e)
            Result.failure(Exception("Error fetching leaderboard: ${e.message}"))
        }
    }
}
