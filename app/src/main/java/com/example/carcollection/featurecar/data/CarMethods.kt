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

}
