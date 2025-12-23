package com.example.carcollection.featurecar.data;


import com.example.carcollection.featurecar.domain.Car
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.Source


class CarMethods {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Sistema de caché en memoria para getUserCars
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

    suspend fun addCarToCollection(car:Car): Result<String> { // Returns the Firestore document ID of the new car
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            val userId = firebaseUser.uid
            try {
                // Get a reference to the specific user's carsCollection subcollection
                val carsCollectionRef = db.collection("users")
                        .document(userId)
                        .collection("carsCollection") // Make sure this matches your subcollection name!

                // Set the createdAt timestamp if not already set
                val carWithTimestamp = if (car.createdAt == null) {
                    car.copy(createdAt = System.currentTimeMillis())
                } else {
                    car
                }

                // Add the Car object directly. Firestore will auto-generate a document ID.
                val documentReference = carsCollectionRef.add(carWithTimestamp).await()

                // Invalidar caché después de agregar
                invalidateCache()

                println("Car '${car.name}' added to user $userId with ID: ${documentReference.id}")
                Result.success(documentReference.id) // Return the ID of the newly added car

            } catch (e: Exception) {
                Result.failure(Exception("Failed to add car: ${e.message}"))
            }
        } else {
            // No user is currently logged in
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

}
