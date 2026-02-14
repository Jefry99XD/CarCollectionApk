package com.example.carcollection.featureWishlist.data

import com.example.carcollection.featurecar.domain.Car
import com.example.carcollection.featureWishlist.domain.WishlistItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class WishlistMethods {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Sistema de caché en memoria para wishlist
    private var wishlistCache: List<Car>? = null
    private var cacheTimestamp: Long = 0
    private val CACHE_DURATION = 5 * 60 * 1000L // 5 minutos

    /**
     * Invalida la caché de wishlist
     * Debe llamarse después de agregar o eliminar items
     */
    fun invalidateCache() {
        wishlistCache = null
        cacheTimestamp = 0
    }

    /**
     * Verifica si la caché es válida
     */
    private fun isCacheValid(): Boolean {
        return wishlistCache != null &&
               (System.currentTimeMillis() - cacheTimestamp) < CACHE_DURATION
    }

    // Adds a Car to the user's wishlist. Returns the Firestore document ID of the new wishlist item.
    suspend fun addToWishlist(car: Car): Result<String> {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            val userId = firebaseUser.uid
            try {
                val wishlistRef = db.collection("users")
                    .document(userId)
                    .collection("wishlist")

                // ✅ Optimización: Verificar en caché primero si está disponible
                if (isCacheValid()) {
                    val duplicate = wishlistCache?.firstOrNull { existingCar ->
                        areSameCar(existingCar, car)
                    }
                    if (duplicate != null) {
                        return Result.failure(Exception("Item already in wishlist"))
                    }
                } else {
                    // ✅ Optimización: Query específico usando índices en lugar de leer todos los documentos
                    val querySnapshot = wishlistRef
                        .whereEqualTo("brand", car.brand)
                        .whereEqualTo("name", car.name)
                        .whereEqualTo("year", car.year)
                        .limit(5)
                        .get()
                        .await()

                    val duplicate = querySnapshot.documents.firstOrNull { doc ->
                        val existingCar = doc.toObject(Car::class.java)
                        existingCar != null && areSameCar(existingCar, car)
                    }

                    if (duplicate != null) {
                        return Result.failure(Exception("Item already in wishlist"))
                    }
                }

                val carWithTimestamp = if (car.createdAt == null) {
                    car.copy(createdAt = System.currentTimeMillis())
                } else {
                    car
                }

                val docRef = wishlistRef.add(carWithTimestamp).await()

                // ✅ Invalidar caché después de agregar
                invalidateCache()

                Result.success(docRef.id)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to add to wishlist: ${e.message}"))
            }
        } else {
            Result.failure(Exception("No user logged in to add to wishlist."))
        }
    }

    // Helper to compare two Car objects ignoring the id field.
    private fun areSameCar(a: Car, b: Car): Boolean {
        fun eq(s1: String?, s2: String?): Boolean = (s1 ?: "").trim().equals((s2 ?: "").trim(), ignoreCase = true)

        if (!eq(a.name, b.name)) return false
        if (!eq(a.brand, b.brand)) return false
        if (!eq(a.year, b.year)) return false
        if (!eq(a.serie, b.serie)) return false
        if (!eq(a.color, b.color)) return false
        if (!eq(a.photoUrl, b.photoUrl)) return false
        if (!eq(a.type, b.type)) return false

        val tagsA = a.tags
        val tagsB = b.tags
        val normA = tagsA.map { it.trim().lowercase() }.toSet()
        val normB = tagsB.map { it.trim().lowercase() }.toSet()
        if (normA != normB) return false

        return true
    }

    // Removes a wishlist item by its wishlist document ID.
    suspend fun removeFromWishlist(wishlistItemId: String): Result<Unit> {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            val userId = firebaseUser.uid
            try {
                val docRef = db.collection("users")
                    .document(userId)
                    .collection("wishlist")
                    .document(wishlistItemId)
                docRef.delete().await()

                // ✅ Invalidar caché después de eliminar
                invalidateCache()

                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to remove from wishlist: ${e.message}"))
            }
        } else {
            Result.failure(Exception("No user logged in to remove from wishlist."))
        }
    }

    // Retrieves the user's wishlist as a list of Car objects (each Car's id is set to the wishlist doc id).
    suspend fun retrieveWishlist(forceRefresh: Boolean = false): Result<List<Car>> {
        val firebaseUser = auth.currentUser
        return if (firebaseUser != null) {
            // ✅ Verificar caché primero (si no se fuerza refresh)
            if (!forceRefresh && isCacheValid()) {
                return Result.success(wishlistCache!!)
            }

            val userId = firebaseUser.uid
            try {
                val snapshot = db.collection("users")
                    .document(userId)
                    .collection("wishlist")
                    .get()
                    .await()

                val list = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(Car::class.java)?.copy(id = doc.id)
                }

                // ✅ Actualizar caché
                wishlistCache = list
                cacheTimestamp = System.currentTimeMillis()

                Result.success(list)
            } catch (e: Exception) {
                Result.failure(Exception("Failed to retrieve wishlist: ${e.message}"))
            }
        } else {
            Result.failure(Exception("No user logged in to retrieve wishlist."))
        }
    }

    /**
     * Obtener la wishlist pública de otro usuario
     */
    suspend fun getPublicWishlist(userId: String): Result<List<WishlistItem>> {
        return try {
            val snapshot = db.collection("users")
                .document(userId)
                .collection("wishlist")
                .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()

            val wishlist = snapshot.documents.mapNotNull { doc ->
                val car = doc.toObject(Car::class.java)
                car?.let {
                    WishlistItem(
                        id = doc.id,
                        userId = userId,
                        carName = it.name ?: "Sin nombre",
                        brand = it.brand ?: "",
                        serie = it.serie ?: "",
                        imageUrl = it.photoUrl ?: "",
                        priority = "Media", // Por ahora todos tienen prioridad media
                        notes = "", // Por ahora sin notas en la vista pública
                        addedAt = it.createdAt ?: System.currentTimeMillis()
                    )
                }
            }

            Result.success(wishlist)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to fetch public wishlist: ${e.message}"))
        }
    }
}